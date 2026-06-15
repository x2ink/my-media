# AGENTS.md

本文件是本项目的 Agent 强制规范。任何 AI Coding Agent、协作开发者或自动化改代码工具在修改本项目代码前，必须先阅读并遵守本文件。

本项目是 Android 本地音视频播放器 Demo，目标是学习 Android 媒体开发、现代存储模型、Jetpack Media3、MVVM 与 Clean Architecture。项目当前优先采用单模块清晰分包，后期主链路稳定后再考虑多模块拆分。

---

## 1. 总原则

### 1.1 必须遵守

1. 必须使用 Kotlin 作为主要开发语言。
2. 必须使用 MVVM + Clean Architecture 组织业务。
3. 必须保持单向依赖，不允许跨层乱调。
4. 必须通过 UseCase 组织业务动作。
5. 必须通过 Repository 接口隔离数据来源。
6. 必须通过 Room 保存 App 私有媒体库、播放进度、播放历史、播放列表。
7. 必须通过 DataStore 保存轻量设置和当前播放会话。
8. 必须通过 MediaStore 扫描系统媒体库。
9. 必须通过 SAF 扫描用户授权目录和执行导出。
10. 必须将导入媒体默认保存到 `context.getExternalFilesDir(null)/media/`。
11. 必须在导入媒体目录创建 `.nomedia`。
12. 必须使用 Media3 `MediaSessionService + ExoPlayer` 统一音频和视频播放。
13. UI 不允许直接创建或持有 `ExoPlayer`。
14. Domain 层必须保持纯 Kotlin，不允许出现 Android Framework 类型。
15. 第一版不允许为了“全盘扫描”引入 `MANAGE_EXTERNAL_STORAGE` 作为主流程。
16. 第一版不允许过度设计，不允许提前拆多模块。

### 1.2 严禁事项

严禁任何 Agent 做以下修改：

1. 严禁把 `File` 递归扫描作为主扫描方案。
2. 严禁直接扫描第三方 App 私有目录，例如：

   ```text
   /storage/emulated/0/Android/data/com.netease.cloudmusic/
   ```

3. 严禁在 `domain` 层引入以下类型：

   ```text
   Context
   Uri
   File
   DocumentFile
   ContentResolver
   ExoPlayer
   MediaItem
   Dao
   Room Entity
   DataStore
   SharedPreferences
   ```

4. 严禁 `feature` 层直接访问：

   ```text
   Room Dao
   ContentResolver
   MediaStore
   DocumentFile
   File copy
   ExoPlayer
   DataStore
   ```

5. 严禁 `data` 层依赖 `feature`。
6. 严禁 `playback` 层依赖 `feature`。
7. 严禁 `domain` 层依赖 `data`、`playback`、`app`。
8. 严禁音频和视频各自创建一套播放器。
9. 严禁在 Activity 或 Fragment 中直接写长耗时文件复制逻辑。
10. 严禁为了省事把所有业务都写进 ViewModel。
11. 严禁把所有依赖注入都堆进一个巨大 Module。
12. 严禁新增无关依赖、实验性框架或重型三方库。
13. 严禁无理由重命名大量文件、移动大量目录、改变既有包名。
14. 严禁为了“美化 UI”破坏架构或加入不必要动画。
15. 严禁输出无法编译的示例代码后声称任务完成。

---

## 2. 当前技术栈约束

### 2.1 Android 基础

1. Kotlin。
2. AndroidX。
3. Coroutines。
4. Flow / StateFlow。
5. ViewModel。
6. Hilt。
7. Room。
8. DataStore。
9. Jetpack Media3。
10. Storage Access Framework。
11. MediaStore。
12. WorkManager 后置，不作为第一版核心依赖。

### 2.2 UI 技术约束

当前项目 UI 以 XML 为主。Agent 必须遵守：

1. 页面使用 `Activity / Fragment + XML layout`。
2. 推荐使用 ViewBinding。
3. 如项目已开启 DataBinding，可以在既有页面沿用，但不要滥用复杂表达式。
4. 不要主动新增 Compose 页面。
5. 不要把原 XML 页面改成 Compose，除非用户明确要求。
6. Material3 仅作为组件、主题、颜色、样式体系使用时，必须和 XML 页面兼容。
7. Fragment 基类建议使用：

   ```kotlin
   abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId)
   ```

8. `layoutId` 只用于告诉 Fragment 加载哪个 XML 布局，不允许在 BaseFragment 中写具体业务逻辑。

---

## 3. 包结构强制规范

第一版采用单模块清晰分包。

```text
app/
└── src/main/java/ink/x2/mymedia/
    ├── MyMediaApp.kt
    ├── MainActivity.kt
    │
    ├── di/
    ├── core/
    ├── domain/
    ├── data/
    ├── playback/
    ├── worker/
    └── feature/
```

### 3.1 di

只放依赖注入配置。

推荐拆分：

```text
di/
├── DatabaseModule.kt
├── DataStoreModule.kt
├── RepositoryModule.kt
├── PlaybackModule.kt
├── StorageModule.kt
└── WorkerModule.kt
```

要求：

1. 一个 Module 只负责一类依赖。
2. Repository 绑定放 `RepositoryModule`。
3. Room 创建放 `DatabaseModule`。
4. DataStore 创建放 `DataStoreModule`。
5. Media3 / Playback 相关依赖放 `PlaybackModule`。
6. 文件存储相关依赖放 `StorageModule`。

### 3.2 core

只放跨业务通用能力。

```text
core/
├── common/
├── permission/
├── storage/
└── ui/
```

允许内容：

1. 通用 Result / Error。
2. DispatchersProvider。
3. 权限状态封装。
4. App 私有目录工具。
5. `.nomedia` 创建工具。
6. 文件命名工具。
7. UI 通用组件、BaseActivity、BaseFragment、UiEvent。

不允许内容：

1. 不允许放媒体扫描业务。
2. 不允许放播放业务。
3. 不允许放具体页面逻辑。
4. 不允许放 Repository 实现。

### 3.3 domain

只放纯业务模型、Repository 接口、UseCase。

```text
domain/
├── model/
├── repository/
└── usecase/
```

要求：

1. `domain/model` 是纯 Kotlin 数据模型。
2. `domain/repository` 只定义接口。
3. `domain/usecase` 只编排业务规则。
4. 所有 Android 类型必须转成 String、Long、Boolean、enum 等纯类型。
5. `Uri` 在 domain 中统一用 `sourceUriString: String` 表示。

### 3.4 data

只放数据实现。

```text
data/
├── local/
│   ├── db/
│   ├── datastore/
│   └── storage/
├── source/
│   ├── mediastore/
│   ├── saf/
│   └── metadata/
├── mapper/
└── repository/
```

职责：

1. 实现 domain 中的 Repository 接口。
2. 查询 MediaStore。
3. 扫描 SAF 授权目录。
4. 读写 Room。
5. 读写 DataStore。
6. 导入、导出媒体文件。
7. 创建 `.nomedia`。
8. 读取媒体元数据。
9. Entity 和 Domain Model 相互转换。

### 3.5 playback

只放播放系统。

```text
playback/
├── service/
├── session/
├── controller/
├── mapper/
├── notification/
└── gesture/
```

职责：

1. 创建并管理 ExoPlayer。
2. 创建并管理 MediaSession。
3. 提供 MediaController 连接。
4. 对接通知栏、锁屏、耳机、蓝牙控制。
5. 将 Domain 媒体模型转换为 Media3 `MediaItem`。
6. 提供播放状态观察。
7. 视频手势只处理控制逻辑，不访问数据库。

### 3.6 worker

只放后台任务。

```text
worker/
├── ImportMediaWorker.kt
└── ExportMediaWorker.kt
```

第一版可以先不实现 Worker。导入导出主链路跑通后再接入。

### 3.7 feature

只放页面和 ViewModel。

```text
feature/
├── home/
├── scan/
├── library/
├── player/
├── playlist/
├── export/
└── settings/
```

每个页面目录建议保持：

```text
FeatureNameFragment.kt
FeatureNameViewModel.kt
FeatureNameUiState.kt
FeatureNameAdapter.kt
```

XML 放到：

```text
res/layout/
```

命名示例：

```text
fragment_home.xml
fragment_scan.xml
fragment_library.xml
fragment_audio_player.xml
fragment_video_player.xml
```

---

## 4. 依赖方向强制规范

### 4.1 允许依赖

```text
feature -> domain
feature -> playback/controller
feature -> core/ui

playback -> domain
playback -> core

data -> domain
data -> core

worker -> domain
worker -> data

app -> feature
app -> data
app -> playback
app -> core
```

### 4.2 禁止依赖

```text
domain -> data
domain -> app
domain -> playback
domain -> feature

data -> feature
playback -> feature
core -> feature
core -> data
core -> playback
```

### 4.3 Agent 修改代码前检查

每次新增类时必须先判断：

1. 这个类属于哪一层？
2. 它是否引用了不该引用的类型？
3. 它是否让依赖方向反了？
4. 它是否把业务写到了 UI 层？
5. 它是否绕过了 UseCase 或 Repository？

只要有一项不符合，必须重新设计，不允许继续生成代码。

---

## 5. Domain 层规范

### 5.1 允许的模型

推荐核心模型：

```text
MediaType
ScannedMedia
LocalMedia
PlaybackSession
PlaybackProgress
ImportProgress
ExportProgress
Playlist
RepeatMode
SortMode
```

### 5.2 ScannedMedia 规范

外部扫描结果使用：

```kotlin
data class ScannedMedia(
    val id: String,
    val sourceUriString: String,
    val displayName: String,
    val title: String,
    val type: MediaType,
    val durationMs: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    val sourceName: String,
    val imported: Boolean
)
```

强制要求：

1. `sourceUriString` 不允许改成 `Uri`。
2. `id` 可以来自 MediaStore id 或 SAF 自定义 id。
3. `imported` 用于 UI 标识是否已导入。

### 5.3 LocalMedia 规范

App 私有媒体使用：

```kotlin
data class LocalMedia(
    val id: Long,
    val type: MediaType,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    val localRelativePath: String,
    val coverRelativePath: String?,
    val importedAt: Long,
    val lastPlayedAt: Long?,
    val lastPositionMs: Long,
    val playCount: Int,
    val isFavorite: Boolean
)
```

强制要求：

1. `localRelativePath` 只保存相对路径。
2. 不要在 domain 中保存绝对 File。
3. UI 展示标题，不依赖导入后的文件名。

### 5.4 UseCase 规范

UseCase 命名必须清晰：

```text
ScanAudioUseCase
ScanVideoUseCase
ScanFolderUseCase
ImportMediaUseCase
ExportMediaUseCase
GetAudioLibraryUseCase
GetVideoLibraryUseCase
DeleteMediaUseCase
SearchMediaUseCase
SavePlaybackProgressUseCase
GetPlaybackSessionUseCase
SavePlaybackSessionUseCase
GetResumeMediaUseCase
```

要求：

1. 一个 UseCase 做一件事。
2. UseCase 可以组合多个 Repository。
3. UseCase 不直接调用 Android API。
4. UseCase 不直接操作 Room Entity。
5. UseCase 返回 Domain Model 或 Flow。

---

## 6. Data 层规范

### 6.1 Room 规范

必须包含以下核心表：

```text
media
playlist
playlist_media_cross_ref
playback_history
```

`media` 表必须包含：

```text
id
type
title
artist
album
duration_ms
size_bytes
mime_type
local_relative_path
source_uri
cover_relative_path
imported_at
last_played_at
last_position_ms
play_count
is_favorite
created_at
updated_at
```

要求：

1. Entity 只能放在 `data/local/db/entity`。
2. Dao 只能放在 `data/local/db/dao`。
3. Entity 不允许传到 feature 层。
4. Entity 不允许传到 domain 层。
5. 必须通过 Mapper 转换。
6. 修改表结构必须增加 Migration，不允许直接 destructive migration，除非用户明确说明是临时 demo 可清库。

### 6.2 DataStore 规范

DataStore 只保存轻量状态：

```text
lastPage
currentMediaId
currentQueueMediaIds
currentIndex
repeatMode
shuffleEnabled
autoResumeEnabled
sortMode
storageMode
```

禁止保存：

1. 全部媒体列表。
2. 大型 JSON。
3. 每个媒体的详细播放历史。
4. 文件二进制内容。
5. 封面图片。

### 6.3 MediaStore 扫描规范

音频扫描使用：

```text
MediaStore.Audio.Media
```

视频扫描使用：

```text
MediaStore.Video.Media
```

要求：

1. Android 13+ 使用 `READ_MEDIA_AUDIO`、`READ_MEDIA_VIDEO`。
2. Android 12 及以下使用 `READ_EXTERNAL_STORAGE`。
3. 不允许申请 `MANAGE_EXTERNAL_STORAGE` 作为主线。
4. 扫描结果统一转换为 `ScannedMedia`。
5. MediaStore 查询逻辑必须在 `data/source/mediastore`。
6. ViewModel 不允许直接写 ContentResolver 查询。

### 6.4 SAF 规范

SAF 用于：

1. 用户授权目录扫描。
2. 导出到用户选择目录。

要求：

1. 选择目录使用 `ACTION_OPEN_DOCUMENT_TREE`。
2. 必须调用 `takePersistableUriPermission` 保存授权。
3. SAF 递归扫描放在 `SafFolderScanner`。
4. SAF 写文件放在 `SafFileWriter`。
5. `DocumentFile` 不允许出现在 domain 或 feature 的业务逻辑中。
6. 对外只传递 `treeUriString` 或 `sourceUriString`。

### 6.5 私有存储规范

默认导入目录：

```text
context.getExternalFilesDir(null)/media/
├── .nomedia
├── audio/
└── video/
```

要求：

1. 导入前必须确保 `.nomedia` 存在。
2. 音频进入 `media/audio/`。
3. 视频进入 `media/video/`。
4. 文件名不直接使用原始文件名。
5. 数据库保存原始标题和导入后的相对路径。
6. 删除媒体时必须同时处理数据库记录和私有文件。

### 6.6 文件命名规范

导入文件命名：

```text
audio_yyyyMMdd_HHmmss_random.ext
video_yyyyMMdd_HHmmss_random.ext
```

示例：

```text
audio_20260615_143000_8F3A.mp3
video_20260615_143010_9B1C.mp4
```

要求：

1. 文件名必须规避特殊字符。
2. 扩展名优先从 MIME type 或原始 displayName 推导。
3. 不允许直接信任用户原始文件名。
4. 文件重名时必须生成新名称，不允许覆盖。

### 6.7 去重规范

第一版去重规则：

```text
title + size + duration + mimeType
```

第二版可扩展：

```text
sourceUri + size + duration
```

后期再考虑：

```text
SHA-256 hash
```

第一版不要为了 hash 牺牲导入速度和实现复杂度。

---

## 7. Playback 层规范

### 7.1 统一播放器

本项目只允许一套播放核心：

```text
PlaybackService
    ↓
ExoPlayer
    ↓
MediaSession
```

UI 控制路径：

```text
Fragment / ViewModel
    ↓
PlaybackController
    ↓
MediaController
    ↓
PlaybackService
    ↓
ExoPlayer
```

### 7.2 PlaybackService 规范

`PlaybackService` 必须放在：

```text
playback/service/PlaybackService.kt
```

职责：

1. 创建 ExoPlayer。
2. 创建 MediaSession。
3. 管理播放队列。
4. 响应 MediaController 命令。
5. 对接通知栏。
6. 对接锁屏控制。
7. 对接耳机和蓝牙按键。
8. 保存必要播放状态。
9. 正确释放播放器资源。

禁止：

1. 不允许依赖任何 Fragment。
2. 不允许依赖任何 ViewModel。
3. 不允许直接操作 UI。
4. 不允许把播放业务散落到 Activity 中。

### 7.3 MediaController 规范

UI 层只能通过 `PlaybackController` 操作播放。

允许操作：

```text
play(media)
playQueue(queue, startIndex)
pause()
resume()
seekTo(positionMs)
seekForward()
seekBack()
skipToNext()
skipToPrevious()
setRepeatMode()
setShuffleEnabled()
setPlaybackSpeed()
```

要求：

1. `PlaybackController` 是 UI 可用的播放门面。
2. `MediaControllerConnector` 负责连接 MediaSessionService。
3. `PlaybackStateObserver` 负责把 Player 状态转成 Flow / StateFlow。
4. ViewModel 不直接拿 `ExoPlayer`。

### 7.4 Media3 Mapper 规范

Domain `LocalMedia` 转 Media3 `MediaItem` 的逻辑必须放在：

```text
playback/mapper/Media3ItemMapper.kt
```

禁止散落在：

1. Fragment。
2. Activity。
3. ViewModel。
4. Repository。
5. UseCase。

### 7.5 视频播放页规范

视频页面只负责：

1. 承载 `PlayerView`。
2. 全屏 / 横屏。
3. 手势层。
4. 亮度调整。
5. 音量调整。
6. 快进快退手势。
7. 控制层显示隐藏。

禁止：

1. 不自己创建 ExoPlayer。
2. 不自己维护播放队列。
3. 不自己保存播放进度。
4. 不直接访问数据库。
5. 不直接访问 MediaStore。

---

## 8. Feature 层规范

### 8.1 ViewModel 规范

ViewModel 只允许：

1. 持有 UiState。
2. 调用 UseCase。
3. 调用 PlaybackController。
4. 处理用户意图。
5. 暴露 StateFlow。
6. 暴露一次性 UiEvent。

ViewModel 禁止：

1. 不直接访问 Dao。
2. 不直接访问 Room Database。
3. 不直接访问 ContentResolver。
4. 不直接访问 DocumentFile。
5. 不直接复制文件。
6. 不直接创建 ExoPlayer。
7. 不直接持有 View、Activity、Fragment。
8. 不持有可泄漏的 Context。确需 Context 时使用 ApplicationContext 并放在 data/core 层封装。

### 8.2 UiState 规范

每个页面必须有独立 UiState。

命名：

```text
HomeUiState
ScanUiState
LibraryUiState
AudioPlayerUiState
VideoPlayerUiState
ExportUiState
SettingsUiState
```

UiState 要求：

1. 使用不可变 data class。
2. 字段有默认值。
3. 不放 View。
4. 不放 Context。
5. 不放 Cursor。
6. 不放 Entity。
7. 不放 ExoPlayer。
8. 错误信息优先使用业务错误模型或字符串资源 id。

### 8.3 Fragment 规范

Fragment 只负责：

1. 初始化 ViewBinding。
2. 初始化 RecyclerView / Adapter。
3. 收集 ViewModel 状态。
4. 绑定点击事件。
5. 调起系统权限、SAF Picker。
6. 渲染 UI。

Fragment 禁止：

1. 不写业务判断。
2. 不直接操作数据库。
3. 不直接操作播放器。
4. 不直接复制文件。
5. 不直接写 MediaStore 查询。
6. 不在 Fragment 中启动长时间阻塞任务。

### 8.4 Adapter 规范

RecyclerView Adapter 要求：

1. 优先使用 ListAdapter + DiffUtil。
2. 不在 Adapter 中发起网络、数据库、文件操作。
3. 点击事件通过回调传给 Fragment / ViewModel。
4. Adapter 只做列表展示。
5. 不把业务状态藏在 Adapter 内部，选中状态应由 UiState 管理，简单临时 UI 状态除外。

---

## 9. 扫描流程强制规范

### 9.1 系统媒体扫描

主流程：

```text
Fragment
    ↓
ViewModel
    ↓
ScanAudioUseCase / ScanVideoUseCase
    ↓
ScanRepository
    ↓
MediaStoreScanner
    ↓
ScannedMedia
```

要求：

1. 页面可以写“全盘扫描”，代码含义必须是“扫描系统媒体库”。
2. 不允许递归扫 `/storage/emulated/0` 作为主流程。
3. 不允许直接扫第三方 App 私有目录。
4. 扫描结果只作为待导入列表，不等于 App 私有媒体库。

### 9.2 SAF 补充扫描

主流程：

```text
Fragment 调起 ACTION_OPEN_DOCUMENT_TREE
    ↓
拿到 treeUriString
    ↓
ViewModel
    ↓
ScanFolderUseCase
    ↓
ScanRepository
    ↓
SafFolderScanner
    ↓
ScannedMedia
```

要求：

1. 用户不授权就不能扫描该目录。
2. 授权 URI 必须持久化。
3. 扫描失败要给出明确错误。
4. 不允许静默跳过所有异常。

---

## 10. 导入流程强制规范

主流程：

```text
ScanFragment
    ↓
ScanViewModel
    ↓
ImportMediaUseCase
    ↓
MediaRepository
    ↓
PrivateMediaStorage
    ↓
Room
```

要求：

1. 导入必须复制到 App 私有媒体目录。
2. 导入必须创建 `.nomedia`。
3. 导入成功后必须写入 Room。
4. 导入失败必须记录失败项。
5. 导入过程必须暴露进度。
6. 大文件复制必须放到 IO Dispatcher。
7. 不能在主线程复制文件。
8. 不能把源 URI 当成本地文件路径直接使用。
9. 不能假设所有 URI 都能通过 `File(uri.path)` 读取。
10. 不能覆盖已有文件。

---

## 11. 导出流程强制规范

主流程：

```text
ExportFragment
    ↓
ACTION_OPEN_DOCUMENT_TREE
    ↓
ExportViewModel
    ↓
ExportMediaUseCase
    ↓
MediaRepository
    ↓
MediaExportStorage
    ↓
SafFileWriter
```

要求：

1. 第一版只做 SAF 导出。
2. 用户必须主动选择导出目录。
3. 不默认导出到公共 Music / Movies。
4. 导出失败必须记录失败项。
5. 导出过程必须暴露进度。
6. 导出不能破坏 App 私有媒体库。
7. 导出不改变原媒体路径。
8. 导出文件名可使用展示标题，但必须清洗非法字符。

---

## 12. 播放状态恢复规范

### 12.1 Room 保存

Room 保存每个媒体的：

```text
lastPositionMs
lastPlayedAt
playCount
completed
playback_history
```

### 12.2 DataStore 保存

DataStore 保存当前播放会话：

```text
lastPage
currentMediaId
currentQueueMediaIds
currentIndex
mediaType
repeatMode
shuffleEnabled
updatedAt
```

### 12.3 保存时机

必须至少覆盖：

1. 播放中定时保存，建议每 5 秒。
2. 暂停时保存。
3. 切换媒体时保存。
4. 页面退出时保存。
5. Service 销毁前保存。
6. App 进入后台时保存。

### 12.4 恢复规则

App 启动时：

```text
读取 DataStore PlaybackSession
    ↓
读取 Room 中 currentMediaId 对应的 LocalMedia
    ↓
检查私有文件是否存在
    ↓
恢复队列
    ↓
恢复页面
    ↓
seek 到 lastPositionMs
```

视频恢复：

```text
如果 lastPositionMs < durationMs - 10_000
    从上次位置继续
否则
    从 0 开始
```

---

## 13. 权限规范

### 13.1 必需权限

Android 13+：

```xml
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Android 12 及以下：

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

后台播放：

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

### 13.2 不推荐权限

不允许作为主线：

```xml
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

如需实验，只允许放在 debug flavor，并且必须标明不是正式方案。

---

## 14. Gradle 与依赖规范

### 14.1 Version Catalog

新增依赖必须写入：

```text
gradle/libs.versions.toml
```

格式示例：

```toml
[versions]
media3 = "x.y.z"

[libraries]
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
```

使用方式：

```kotlin
implementation(libs.androidx.media3.exoplayer)
implementation(libs.androidx.media3.session)
implementation(libs.androidx.media3.ui)
```

### 14.2 依赖新增原则

新增依赖前必须判断：

1. AndroidX 是否已有官方方案？
2. 是否会破坏项目学习目标？
3. 是否引入过多传递依赖？
4. 是否能用标准库或已有依赖解决？
5. 是否和 minSdk / targetSdk / compileSdk 兼容？

不能为了少写几行代码引入大型库。

---

## 15. 线程与协程规范

### 15.1 Dispatcher

必须通过 `DispatchersProvider` 注入：

```kotlin
interface DispatchersProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
```

要求：

1. 文件复制使用 IO。
2. MediaStore 查询使用 IO。
3. Room 大量读写使用 IO。
4. 元数据解析使用 IO 或 Default。
5. UI 状态更新回 Main。
6. 测试时可以替换 Dispatcher。

### 15.2 Flow 规范

1. ViewModel 对外暴露 `StateFlow<UiState>`。
2. 一次性事件使用 `SharedFlow<UiEvent>` 或 Channel。
3. Repository 可以返回 Flow。
4. 不允许在 UI 层收集无生命周期保护的 Flow。
5. Fragment 收集 Flow 必须结合 `repeatOnLifecycle`。

---

## 16. 错误处理规范

必须有统一错误模型，例如：

```text
AppError
AppResult
```

要求：

1. 不允许吞异常。
2. 不允许只打印日志不反馈 UI。
3. 文件导入失败要包含失败原因。
4. SAF 权限失效要明确提示。
5. 源文件不存在要明确提示。
6. 私有文件丢失要从数据库状态中处理。
7. 播放失败要展示可理解错误。
8. 批量任务必须支持部分成功、部分失败。

---

## 17. 日志规范

可以使用 Logger，但必须：

1. 只在 Application 初始化一次。
2. 不在 domain 层直接依赖 Logger。
3. 不打印用户完整私有路径。
4. 不打印敏感 URI 授权信息。
5. 不打印大量媒体列表。
6. Debug 日志必须可控。
7. Release 下避免无意义日志。

初始化位置：

```kotlin
class MyMediaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}
```

---

## 18. 测试与验收规范

### 18.1 Agent 修改后必须自检

每次修改后必须至少检查：

1. 是否能编译。
2. 是否破坏包结构。
3. 是否引入反向依赖。
4. 是否把 Android 类型放进 domain。
5. 是否把 ExoPlayer 放进 UI。
6. 是否主线程做 IO。
7. 是否权限分支覆盖 Android 13+ 和 Android 12-。
8. 是否导入目录创建 `.nomedia`。
9. 是否 Room / DataStore 分工正确。
10. 是否有失败路径处理。

### 18.2 第一版功能验收

第一版只验收以下主链路：

1. 首页。
2. 扫描音乐。
3. 扫描视频。
4. 一键导入到 `getExternalFilesDir()/media`。
5. `.nomedia` 创建成功。
6. Room 显示我的音乐 / 我的视频。
7. PlaybackService + ExoPlayer 播放音乐。
8. PlaybackService + ExoPlayer 播放视频。
9. 播放、暂停、seek、快进、快退。
10. 保存并恢复上次播放进度。
11. SAF 导出音乐。
12. SAF 导出视频。

暂不验收：

1. 歌词。
2. 网络播放。
3. 在线封面。
4. 复杂播放列表。
5. 高级手势。
6. WorkManager。
7. 多模块。
8. hash 去重。
9. Android Auto 深度适配。

---

## 19. Agent 工作流程

任何 Agent 接到任务后必须按以下流程执行：

### 19.1 修改前

1. 先判断需求属于哪个层。
2. 先列出将要修改的文件。
3. 先确认是否需要新增类。
4. 先确认是否需要新增依赖。
5. 先确认是否会影响数据库结构。
6. 先确认是否会影响权限。
7. 先确认是否会影响播放服务。

### 19.2 修改中

1. 小步修改。
2. 不做无关重构。
3. 不改无关格式。
4. 不删除已有功能。
5. 不改变用户未要求的 UI 风格。
6. 不用伪代码替代真实代码。
7. 不留下 TODO 当作完成结果。

### 19.3 修改后

必须输出：

1. 修改了哪些文件。
2. 每个文件改了什么。
3. 是否新增依赖。
4. 是否新增权限。
5. 是否新增数据库字段或迁移。
6. 是否需要用户手动操作。
7. 是否有未完成项。
8. 是否通过编译或无法编译的原因。

---

## 20. 代码风格规范

### 20.1 命名

1. 类名使用 PascalCase。
2. 函数名使用 camelCase。
3. 常量使用 UPPER_SNAKE_CASE。
4. XML 文件使用 snake_case。
5. layout id 使用 snake_case。
6. UseCase 以 `UseCase` 结尾。
7. Repository 接口不加 Impl。
8. Repository 实现必须以 `Impl` 结尾。
9. Mapper 以 `Mapper` 结尾。
10. UiState 以 `UiState` 结尾。

### 20.2 Kotlin

1. 优先使用 `val`。
2. 避免可变全局状态。
3. 避免 `!!`。
4. 避免超长函数。
5. 避免上帝类。
6. suspend 函数不直接在主线程做阻塞 IO。
7. Flow 命名表达数据流含义。
8. 错误路径必须明确处理。

### 20.3 XML

1. id 命名必须表达业务含义。
2. 不要在 XML 写复杂 DataBinding 表达式。
3. 公共尺寸抽到 `dimens.xml`。
4. 公共颜色抽到 `colors.xml`。
5. 文案必须放 `strings.xml`。
6. 不允许硬编码中文到 layout。
7. 不允许硬编码大段样式到单个页面。
8. Material 组件样式必须统一。

---

## 21. 当前阶段优先级

当前阶段优先级从高到低：

1. 架构分层正确。
2. 主链路可运行。
3. 权限和存储模型正确。
4. 播放架构正确。
5. 数据可恢复。
6. UI 简洁可用。
7. 再考虑体验优化。
8. 最后考虑多模块和 WorkManager。

Agent 不允许为了低优先级目标破坏高优先级目标。

---

## 22. 最终硬性结论

本项目的核心链路必须保持：

```text
MediaStore 扫描系统媒体
    ↓
SAF 补充扫描用户授权目录
    ↓
导入到 getExternalFilesDir()/media
    ↓
创建 .nomedia
    ↓
Room 建立 App 私有媒体库
    ↓
PlaybackService + ExoPlayer 播放
    ↓
MediaController 控制 UI
    ↓
Room 保存每个媒体进度
    ↓
DataStore 保存当前播放会话
    ↓
SAF 导出
```

任何代码修改只要破坏以上链路，都必须停止并重新设计。
