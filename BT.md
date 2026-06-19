# BT Native Environment

本文说明新设备拉取本项目后，如何配置 BT 下载功能所需的 native 依赖。

## 依赖目录

当前项目的 CMake 固定读取以下两个目录：

```text
/Users/yangchenglin/Documents/bt/libtorrent-rasterbar-2.0.13
/Users/yangchenglin/Documents/bt/boost_1_85_0
```

新设备上需要保持同样的目录结构。最终应类似：

```text
/Users/yangchenglin/Documents/bt/
├── boost_1_85_0/
│   └── boost/version.hpp
└── libtorrent-rasterbar-2.0.13/
    ├── CMakeLists.txt
    ├── include/
    ├── src/
    └── deps/try_signal/
```

## 下载依赖

### libtorrent

使用官方源码仓库，当前项目按 `2.0.13` 版本配置：

```bash
mkdir -p /Users/yangchenglin/Documents/bt
cd /Users/yangchenglin/Documents/bt
git clone --branch v2.0.13 --depth 1 https://github.com/arvidn/libtorrent.git libtorrent-rasterbar-2.0.13
```

如果网络不稳定，也可以下载官方 release 源码包，解压后目录名改为：

```text
libtorrent-rasterbar-2.0.13
```

### Boost

当前项目使用 Boost `1.85.0` 头文件：

```bash
cd /Users/yangchenglin/Documents/bt
curl -L -o boost_1_85_0.tar.gz https://archives.boost.io/release/1.85.0/source/boost_1_85_0.tar.gz
tar -xzf boost_1_85_0.tar.gz
```

检查文件是否存在：

```bash
ls /Users/yangchenglin/Documents/bt/boost_1_85_0/boost/version.hpp
ls /Users/yangchenglin/Documents/bt/libtorrent-rasterbar-2.0.13/CMakeLists.txt
```

## 项目配置位置

native 路径配置在：

```text
app/src/main/cpp/CMakeLists.txt
```

关键配置：

```cmake
set(LIBTORRENT_ROOT "/Users/yangchenglin/Documents/bt/libtorrent-rasterbar-2.0.13" CACHE PATH "Official libtorrent source root" FORCE)
set(BOOST_ROOT "/Users/yangchenglin/Documents/bt/boost_1_85_0" CACHE PATH "Boost source root" FORCE)
set(Boost_INCLUDE_DIR "${BOOST_ROOT}" CACHE PATH "Boost headers" FORCE)
```

如果新设备用户名不是 `yangchenglin`，需要把这三个路径改成该设备上的实际路径。

## 构建验证

配置完成后，在项目根目录执行：

```bash
./gradlew :app:externalNativeBuildDebug
./gradlew :app:assembleDebug
```

两个命令都通过后，说明 libtorrent 和 Boost 已被正确识别并参与打包。

## 常见问题

### 仍然读取旧路径

Android Studio / Gradle 可能复用旧的 CMake 缓存。当前 `CMakeLists.txt` 已使用 `FORCE` 覆盖缓存；如果仍异常，可以删除 native 构建缓存后重试：

```bash
rm -rf app/.cxx
./gradlew :app:externalNativeBuildDebug
```

### 提示 Boost headers not found

说明 `BOOST_ROOT` 不正确，或者 Boost 没有解压到：

```text
/Users/yangchenglin/Documents/bt/boost_1_85_0
```

必须能找到：

```text
/Users/yangchenglin/Documents/bt/boost_1_85_0/boost/version.hpp
```

### native 编译出现大量 warning

libtorrent 源码在 C++17 下会出现一些 `deprecated-redundant-constexpr-static-def` 警告。只要最终 `BUILD SUCCESSFUL`，这些警告不影响使用。

