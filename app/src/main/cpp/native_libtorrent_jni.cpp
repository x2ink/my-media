#include <jni.h>

#include <algorithm>
#include <chrono>
#include <iomanip>
#include <memory>
#include <mutex>
#include <set>
#include <sstream>
#include <string>
#include <unordered_map>
#include <vector>

#if MYMEDIA_HAS_LIBTORRENT
#include <libtorrent/add_torrent_params.hpp>
#include <libtorrent/alert_types.hpp>
#include <libtorrent/announce_entry.hpp>
#include <libtorrent/bencode.hpp>
#include <libtorrent/error_code.hpp>
#include <libtorrent/extensions/smart_ban.hpp>
#include <libtorrent/extensions/ut_metadata.hpp>
#include <libtorrent/extensions/ut_pex.hpp>
#include <libtorrent/file_storage.hpp>
#include <libtorrent/magnet_uri.hpp>
#include <libtorrent/session.hpp>
#include <libtorrent/session_params.hpp>
#include <libtorrent/settings_pack.hpp>
#include <libtorrent/torrent_handle.hpp>
#include <libtorrent/torrent_info.hpp>
#include <libtorrent/torrent_status.hpp>
#include <libtorrent/version.hpp>
#include <libtorrent/write_resume_data.hpp>
#endif

namespace {

void throw_runtime(JNIEnv* env, std::string const& message) {
    jclass clazz = env->FindClass("java/lang/RuntimeException");
    if (clazz != nullptr) {
        env->ThrowNew(clazz, message.c_str());
    }
}

std::string jstring_to_string(JNIEnv* env, jstring value) {
    if (value == nullptr) {
        return {};
    }
    char const* chars = env->GetStringUTFChars(value, nullptr);
    std::string result(chars == nullptr ? "" : chars);
    if (chars != nullptr) {
        env->ReleaseStringUTFChars(value, chars);
    }
    return result;
}

std::vector<int> jint_array_to_vector(JNIEnv* env, jintArray array) {
    std::vector<int> result;
    if (array == nullptr) {
        return result;
    }
    jsize size = env->GetArrayLength(array);
    result.resize(static_cast<std::size_t>(size));
    if (size > 0) {
        env->GetIntArrayRegion(array, 0, size, result.data());
    }
    return result;
}

jbyteArray vector_to_jbyte_array(JNIEnv* env, std::vector<char> const& data) {
    auto array = env->NewByteArray(static_cast<jsize>(data.size()));
    if (array != nullptr && !data.empty()) {
        env->SetByteArrayRegion(
                array,
                0,
                static_cast<jsize>(data.size()),
                reinterpret_cast<jbyte const*>(data.data())
        );
    }
    return array;
}

std::vector<char> jbyte_array_to_vector(JNIEnv* env, jbyteArray array) {
    std::vector<char> result;
    if (array == nullptr) {
        return result;
    }
    jsize size = env->GetArrayLength(array);
    result.resize(static_cast<std::size_t>(size));
    if (size > 0) {
        env->GetByteArrayRegion(
                array,
                0,
                size,
                reinterpret_cast<jbyte*>(result.data())
        );
    }
    return result;
}

std::string json_escape(std::string const& value) {
    std::ostringstream out;
    for (unsigned char c : value) {
        switch (c) {
            case '"': out << "\\\""; break;
            case '\\': out << "\\\\"; break;
            case '\b': out << "\\b"; break;
            case '\f': out << "\\f"; break;
            case '\n': out << "\\n"; break;
            case '\r': out << "\\r"; break;
            case '\t': out << "\\t"; break;
            default:
                if (c < 0x20) {
                    out << "\\u" << std::hex << std::setw(4) << std::setfill('0') << int(c);
                } else {
                    out << char(c);
                }
        }
    }
    return out.str();
}

std::string extension_of(std::string const& name) {
    auto dot = name.find_last_of('.');
    if (dot == std::string::npos || dot + 1 >= name.size()) {
        return {};
    }
    std::string ext = name.substr(dot + 1);
    std::transform(ext.begin(), ext.end(), ext.begin(), [](unsigned char c) {
        return static_cast<char>(std::tolower(c));
    });
    return ext;
}

bool starts_with(std::string const& value, std::string const& prefix) {
    return value.size() >= prefix.size()
           && std::equal(prefix.begin(), prefix.end(), value.begin());
}

#if MYMEDIA_HAS_LIBTORRENT

std::vector<std::string> default_public_trackers() {
    return {
            "udp://tracker.opentrackr.org:1337/announce",
            "udp://open.stealth.si:80/announce",
            "udp://tracker.torrent.eu.org:451/announce",
            "udp://tracker.bittor.pw:1337/announce",
            "udp://exodus.desync.com:6969/announce",
            "udp://open.demonii.com:1337/announce",
            "udp://tracker.moeking.me:6969/announce",
            "udp://explodie.org:6969/announce",
            "https://tracker.tamersunion.org:443/announce",
            "https://tracker.gbitt.info:443/announce"
    };
}

std::vector<std::string> g_public_trackers = default_public_trackers();

void add_public_trackers(lt::add_torrent_params& params) {
    for (auto const& tracker : g_public_trackers) {
        if (std::find(params.trackers.begin(), params.trackers.end(), tracker) == params.trackers.end()) {
            params.trackers.push_back(tracker);
        }
    }
}

std::mutex g_mutex;
std::unique_ptr<lt::session> g_session;
std::unordered_map<std::string, lt::torrent_handle> g_handles;

std::string sha1_to_hex(lt::sha1_hash const& hash) {
    std::ostringstream out;
    for (auto byte : hash) {
        out << std::hex << std::setw(2) << std::setfill('0') << int(byte);
    }
    return out.str();
}

std::string info_hash_to_string(lt::info_hash_t const& hashes) {
    if (hashes.has_v1()) {
        return sha1_to_hex(hashes.v1);
    }
    if (hashes.has_v2()) {
        return sha1_to_hex(lt::sha1_hash(hashes.v2.data()));
    }
    return {};
}

lt::session& session() {
    if (!g_session) {
        lt::settings_pack settings;
        settings.set_str(lt::settings_pack::user_agent, "MyMedia/libtorrent-" LIBTORRENT_VERSION);
        settings.set_str(lt::settings_pack::listen_interfaces, "0.0.0.0:6881-6891,[::]:6881-6891");
        settings.set_bool(lt::settings_pack::enable_dht, true);
        settings.set_bool(lt::settings_pack::enable_lsd, true);
        settings.set_bool(lt::settings_pack::enable_natpmp, true);
        settings.set_bool(lt::settings_pack::enable_upnp, true);
        settings.set_bool(lt::settings_pack::use_dht_as_fallback, false);
        settings.set_bool(lt::settings_pack::announce_to_all_trackers, true);
        settings.set_bool(lt::settings_pack::announce_to_all_tiers, true);
        settings.set_bool(lt::settings_pack::announce_crypto_support, true);
        settings.set_bool(lt::settings_pack::prefer_udp_trackers, true);
        settings.set_int(lt::settings_pack::out_enc_policy, lt::settings_pack::pe_enabled);
        settings.set_int(lt::settings_pack::in_enc_policy, lt::settings_pack::pe_enabled);
        settings.set_int(lt::settings_pack::allowed_enc_level, lt::settings_pack::pe_both);
        settings.set_int(lt::settings_pack::active_downloads, 8);
        settings.set_int(lt::settings_pack::active_dht_limit, 200);
        settings.set_int(lt::settings_pack::active_tracker_limit, 200);
        settings.set_int(lt::settings_pack::active_limit, 200);
        settings.set_int(lt::settings_pack::connections_limit, 500);
        settings.set_int(lt::settings_pack::unchoke_slots_limit, -1);
        settings.set_int(lt::settings_pack::download_rate_limit, 0);
        settings.set_int(lt::settings_pack::upload_rate_limit, 0);
        settings.set_int(
                lt::settings_pack::alert_mask,
                lt::alert_category::error |
                lt::alert_category::status |
                lt::alert_category::storage
        );
        lt::session_params params(settings);
        g_session = std::make_unique<lt::session>(std::move(params));
        g_session->add_extension(&lt::create_ut_pex_plugin);
        g_session->add_extension(&lt::create_ut_metadata_plugin);
        g_session->add_extension(&lt::create_smart_ban_plugin);
    }
    return *g_session;
}

std::string metadata_json(lt::torrent_info const& info) {
    static std::set<std::string> const video_extensions = {
            "mp4", "mkv", "avi", "mov", "webm", "m4v",
            "ts", "mpeg", "mpg", "3gp", "flv", "wmv"
    };

    lt::file_storage const& files = info.files();
    std::ostringstream out;
    out << "{";
    out << "\"infoHash\":\"" << json_escape(info_hash_to_string(info.info_hashes())) << "\",";
    out << "\"name\":\"" << json_escape(info.name()) << "\",";
    out << "\"totalSizeBytes\":" << info.total_size() << ",";
    out << "\"videoFiles\":[";

    bool first = true;
    for (int i = 0; i < files.num_files(); ++i) {
        lt::file_index_t file_index(i);
        std::string name(files.file_name(file_index).data(), files.file_name(file_index).size());
        std::string ext = extension_of(name);
        if (video_extensions.find(ext) == video_extensions.end()) {
            continue;
        }
        if (!first) {
            out << ",";
        }
        first = false;
        out << "{";
        out << "\"index\":" << i << ",";
        out << "\"path\":\"" << json_escape(files.file_path(file_index)) << "\",";
        out << "\"name\":\"" << json_escape(name) << "\",";
        out << "\"sizeBytes\":" << files.file_size(file_index) << ",";
        out << "\"extension\":\"" << json_escape(ext) << "\"";
        out << "}";
    }

    out << "]}";
    return out.str();
}

std::vector<char> torrent_bytes_from_handle(lt::torrent_handle const& handle, int timeout_seconds) {
    auto& ses = session();
    auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(timeout_seconds);
    handle.save_resume_data(lt::torrent_handle::save_info_dict);

    while (std::chrono::steady_clock::now() < deadline) {
        std::vector<lt::alert*> alerts;
        ses.pop_alerts(&alerts);
        for (lt::alert* alert : alerts) {
            if (auto* data = lt::alert_cast<lt::save_resume_data_alert>(alert)) {
                if (!(data->handle == handle)) {
                    continue;
                }
                data->params.merkle_trees.clear();
                return lt::write_torrent_file_buf(
                        data->params,
                        lt::write_flags::allow_missing_piece_layer
                );
            }
            if (auto* failed = lt::alert_cast<lt::save_resume_data_failed_alert>(alert)) {
                if (!(failed->handle == handle)) {
                    continue;
                }
                throw std::runtime_error(failed->message());
            }
        }
        ses.wait_for_alert(std::chrono::milliseconds(200));
    }

    throw std::runtime_error("生成 torrent 元数据超时");
}

#endif

} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeVersion(
        JNIEnv* env,
        jobject /* thiz */) {
#if MYMEDIA_HAS_LIBTORRENT
    std::string version = LIBTORRENT_VERSION;
#else
    std::string version = "libtorrent disabled: Boost headers missing";
#endif
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeFetchMetadata(
        JNIEnv* env,
        jobject /* thiz */,
        jstring magnet_uri,
        jint timeout_seconds,
        jint max_metadata_bytes) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto& ses = session();
        lt::add_torrent_params params = lt::parse_magnet_uri(jstring_to_string(env, magnet_uri));
        params.save_path = ".";
        params.flags &= ~(lt::torrent_flags::auto_managed | lt::torrent_flags::paused);
        params.file_priorities.assign(100, lt::dont_download);
        add_public_trackers(params);

        lt::torrent_handle handle = ses.add_torrent(std::move(params));
        handle.force_reannounce();
        handle.force_dht_announce();
        auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(timeout_seconds);

        while (std::chrono::steady_clock::now() < deadline) {
            std::vector<lt::alert*> alerts;
            ses.pop_alerts(&alerts);
            for (lt::alert* alert : alerts) {
                if (auto const* metadata = lt::alert_cast<lt::metadata_received_alert>(alert)) {
                    if (!(metadata->handle == handle)) {
                        continue;
                    }
                    auto info = metadata->handle.torrent_file();
                    if (!info) {
                        throw std::runtime_error("libtorrent 已收到元数据，但 torrent_info 为空");
                    }
                    std::string json = metadata_json(*info);
                    std::vector<char> torrent = torrent_bytes_from_handle(metadata->handle, timeout_seconds);
                    if (static_cast<int>(torrent.size()) > max_metadata_bytes) {
                        throw std::runtime_error("torrent 元数据超过大小限制");
                    }

                    std::string info_hash = info_hash_to_string(info->info_hashes());
                    ses.remove_torrent(metadata->handle);
                    g_handles.erase(info_hash);

                    jclass object_class = env->FindClass("java/lang/Object");
                    auto result = env->NewObjectArray(2, object_class, nullptr);
                    env->SetObjectArrayElement(result, 0, env->NewStringUTF(json.c_str()));
                    env->SetObjectArrayElement(result, 1, vector_to_jbyte_array(env, torrent));
                    return result;
                }
                if (auto const* error = lt::alert_cast<lt::torrent_error_alert>(alert)) {
                    throw std::runtime_error(error->message());
                }
            }
            ses.wait_for_alert(std::chrono::milliseconds(200));
        }

        ses.remove_torrent(handle);
        throw std::runtime_error("获取磁力元数据超时，请检查资源是否有可用 Peer");
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
        return nullptr;
    }
#else
    throw_runtime(env, "libtorrent disabled: Boost headers missing");
    return nullptr;
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeParseTorrentMetadata(
        JNIEnv* env,
        jobject /* thiz */,
        jbyteArray torrent_data) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        auto data = jbyte_array_to_vector(env, torrent_data);
        if (data.empty()) {
            throw std::runtime_error("种子文件为空");
        }

        lt::error_code error;
        lt::torrent_info info(data.data(), static_cast<int>(data.size()), error);
        if (error) {
            throw std::runtime_error(error.message());
        }

        std::string json = metadata_json(info);
        return env->NewStringUTF(json.c_str());
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
        return nullptr;
    }
#else
    throw_runtime(env, "libtorrent disabled: Boost headers missing");
    return nullptr;
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeStartDownload(
        JNIEnv* env,
        jobject /* thiz */,
        jstring magnet_uri,
        jbyteArray torrent_data,
        jintArray selected_file_indexes,
        jstring save_dir) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto data = jbyte_array_to_vector(env, torrent_data);
        if (data.empty()) {
            throw std::runtime_error("torrent 元数据为空");
        }

        lt::error_code error;
        auto info = std::make_shared<lt::torrent_info>(
                data.data(),
                static_cast<int>(data.size()),
                error
        );
        if (error) {
            throw std::runtime_error(error.message());
        }

        std::set<int> selected;
        for (int index : jint_array_to_vector(env, selected_file_indexes)) {
            selected.insert(index);
        }

        std::string source = jstring_to_string(env, magnet_uri);
        lt::add_torrent_params params = starts_with(source, "magnet:?")
                ? lt::parse_magnet_uri(source)
                : lt::add_torrent_params();
        params.ti = info;
        params.save_path = jstring_to_string(env, save_dir);
        params.flags &= ~(lt::torrent_flags::auto_managed | lt::torrent_flags::paused);
        add_public_trackers(params);
        params.file_priorities.clear();
        for (int i = 0; i < info->files().num_files(); ++i) {
            params.file_priorities.push_back(selected.count(i) > 0 ? lt::default_priority : lt::dont_download);
        }

        auto& ses = session();
        std::string info_hash = info_hash_to_string(info->info_hashes());
        auto existing = g_handles.find(info_hash);
        if (existing != g_handles.end() && existing->second.is_valid()) {
            existing->second.resume();
            return env->NewStringUTF(info_hash.c_str());
        }

        lt::torrent_handle handle = ses.add_torrent(std::move(params));
        handle.resume();
        handle.force_reannounce();
        handle.force_dht_announce();
        g_handles[info_hash] = handle;
        return env->NewStringUTF(info_hash.c_str());
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
        return nullptr;
    }
#else
    throw_runtime(env, "libtorrent disabled: Boost headers missing");
    return nullptr;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativePause(
        JNIEnv* env,
        jobject /* thiz */,
        jstring info_hash) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto it = g_handles.find(jstring_to_string(env, info_hash));
        if (it != g_handles.end() && it->second.is_valid()) {
            it->second.pause();
        }
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
    }
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeResume(
        JNIEnv* env,
        jobject /* thiz */,
        jstring info_hash) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto it = g_handles.find(jstring_to_string(env, info_hash));
        if (it != g_handles.end() && it->second.is_valid()) {
            it->second.resume();
        }
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
    }
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeRemove(
        JNIEnv* env,
        jobject /* thiz */,
        jstring info_hash) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        std::string key = jstring_to_string(env, info_hash);
        auto it = g_handles.find(key);
        if (it != g_handles.end() && it->second.is_valid()) {
            session().remove_torrent(it->second);
        }
        g_handles.erase(key);
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
    }
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeSnapshot(
        JNIEnv* env,
        jobject /* thiz */,
        jstring info_hash) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto it = g_handles.find(jstring_to_string(env, info_hash));
        if (it == g_handles.end() || !it->second.is_valid()) {
            return nullptr;
        }
        lt::torrent_status status = it->second.status(
                lt::torrent_handle::query_accurate_download_counters
        );
        int progress = status.total_wanted > 0
                ? static_cast<int>((status.total_wanted_done * 100) / status.total_wanted)
                : status.progress_ppm / 10000;
        progress = std::max(0, std::min(100, progress));

        std::ostringstream out;
        out << "{";
        out << "\"progress\":" << progress << ",";
        out << "\"downloadedBytes\":" << status.total_wanted_done << ",";
        out << "\"totalBytes\":" << status.total_wanted << ",";
        out << "\"downloadSpeedBytes\":" << status.download_payload_rate << ",";
        out << "\"peersCount\":" << status.num_peers << ",";
        out << "\"isFinished\":" << (status.is_finished || status.state == lt::torrent_status::seeding ? "true" : "false");
        out << "}";
        return env->NewStringUTF(out.str().c_str());
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
        return nullptr;
    }
#else
    return nullptr;
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeDownloadedFiles(
        JNIEnv* env,
        jobject /* thiz */,
        jstring info_hash,
        jintArray selected_file_indexes,
        jstring save_dir) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        auto it = g_handles.find(jstring_to_string(env, info_hash));
        if (it == g_handles.end() || !it->second.is_valid()) {
            return env->NewStringUTF("[]");
        }
        auto info = it->second.torrent_file();
        if (!info) {
            return env->NewStringUTF("[]");
        }
        std::string base = jstring_to_string(env, save_dir);
        lt::file_storage const& files = info->files();
        std::ostringstream out;
        out << "[";
        bool first = true;
        for (int index : jint_array_to_vector(env, selected_file_indexes)) {
            if (index < 0 || index >= files.num_files()) {
                continue;
            }
            if (!first) {
                out << ",";
            }
            first = false;
            out << "\"" << json_escape(files.file_path(lt::file_index_t(index), base)) << "\"";
        }
        out << "]";
        return env->NewStringUTF(out.str().c_str());
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
        return nullptr;
    }
#else
    return env->NewStringUTF("[]");
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_ink_x2_mymedia_data_source_torrent_NativeLibtorrentBridge_nativeUpdatePublicTrackers(
        JNIEnv* env,
        jobject /* thiz */,
        jobjectArray trackers) {
#if MYMEDIA_HAS_LIBTORRENT
    try {
        std::lock_guard<std::mutex> lock(g_mutex);
        std::vector<std::string> next;
        if (trackers != nullptr) {
            jsize size = env->GetArrayLength(trackers);
            next.reserve(static_cast<std::size_t>(size));
            for (jsize i = 0; i < size; ++i) {
                auto item = static_cast<jstring>(env->GetObjectArrayElement(trackers, i));
                std::string tracker = jstring_to_string(env, item);
                env->DeleteLocalRef(item);
                if (!tracker.empty() && std::find(next.begin(), next.end(), tracker) == next.end()) {
                    next.push_back(std::move(tracker));
                }
            }
        }
        if (!next.empty()) {
            g_public_trackers = std::move(next);
        }
    } catch (std::exception const& e) {
        throw_runtime(env, e.what());
    }
#endif
}
