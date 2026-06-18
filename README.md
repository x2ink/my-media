# 《我的媒体》(MyMedia) 本地音视频播放器项目说明文档

## 一、 项目定位与核心目标

本项目是一个 Android 本地音视频播放器 Demo。项目的主要定位是作为学习与演示现代 Android 媒体开发、现代存储模型、MVVM 架构和 Clean Architecture（清洁架构）的实践项目。

### 1.1 核心学习要点
1. **现代存储模型 (Scoped Storage)**：学习在不申请高风险的 `MANAGE_EXTERNAL_STORAGE` 权限下，如何通过 `MediaStore` 扫描系统公共媒体库、通过 `Private Storage` 管理 App 私有媒体，以及如何创建 `.nomedia` 文件来规避其他第三方媒体 App 对其进行扫描。
2. **Jetpack Media3 统一播放架构**：利用 `MediaSessionService` + `ExoPlayer` + `MediaSession` 实现音频 and 视频的统一播放管理，提供后台播放、系统通知栏、锁屏、耳机和蓝牙控制支持。
3. **分层架构设计 (Clean Architecture)**：严格遵循 UI 层 (`feature`)、领域层 (`domain`)、数据层 (`data`) 以及播放层 (`playback`) 之间的单向依赖关系，保持 Domain 层的纯 Kotlin 化。
4. **异步状态流 (Flow / StateFlow)**：利用 Kotlin 协程和 StateFlow 实现扫描进度、导入进度与播放状态的响应式更新。

---

## 二、 核心技术栈与依赖清单

本项目目前基于单模块 (`app`) 进行清晰的分包管理，核心依赖配置在 `gradle/libs.versions.toml` 中，主要技术栈如下：

* **核心语言**：Kotlin + Coroutines (协程) + Flow / StateFlow
* **UI 视图框架**：Android Jetpack XML Layouts + ViewBinding / DataBinding 混合开发（未使用 Jetpack Compose，兼容 Material3 样式风格）
* **依赖注入**：Dagger Hilt (v2.55)
* **数据库**：Room Database (v2.8.4)
* **网络/多媒体库**：
  * **Jetpack Media3 (v1.10.1)**：统一采用 ExoPlayer 引擎、Session 管理和 UI 组件。
  * **Glide (v4.11.0)**：用于本地媒体封面及图片的异步加载。
* **工具库**：
  * **PermissionX (v1.8.1)**：用于简化 Android 运行时权限申请逻辑。
  * **Fastjson2 (v2.0.61)**：用于进行轻量 JSON 解析。
  * **Orhanobut Logger (v2.2.0)**：提供结构化、可读性高的日志输出。

---

## 三、 项目架构与目录结构

项目采用了 **Clean Architecture** 分层体系，依赖方向仅限单向流入（`feature -> domain`，`data -> domain`，`playback -> domain`），具体包结构及核心类如下：

```text
app/src/main/java/ink/x2/mymedia/
├── MyMediaApp.kt                  # 自定义 Application，初始化日志及依赖注入
├── MainActivity.kt                # 主界面，包含 BottomNavigationView 底部导航切换
│
├── di/                            # 依赖注入模块 (Dagger Hilt)
│   ├── DatabaseModule.kt          # 提供 AppDatabase 及其 Room Dao 实例
│   ├── DispatcherModule.kt        # 提供 @IoDispatcher 协程调度器，保证耗时操作在非主线程运行
│   └── RepositoryModule.kt        # 绑定 Repository 接口与具体实现类
│
├── core/                          # 跨业务通用能力
│   ├── base/                      # 基础基类 (BaseActivity, BaseFragment)
│   ├── common/                    # 通用结果与错误封装 (AppResult, AppError)
│   ├── ext/                       # 扩展函数 (Long.toDurationString 格式化时间)
│   └── ui/                        # 通用 UI 组件与装饰器 (VerticalGapDecoration 分割线)
│
├── domain/                        # 领域层 (纯 Kotlin 逻辑，严禁依赖 Android 专有类)
│   ├── model/                     # 业务数据模型
│   │   ├── MediaType.kt           # 媒体类型枚举 (AUDIO / VIDEO)
│   │   ├── LocalMedia.kt          # App 私有库中媒体的模型表示
│   │   ├── LocalMediaItem.kt      # 待导入/扫描结果的模型表示
│   │   └── ImportProgress.kt      # 导入进度模型 (Loading, Success, Failure)
│   ├── repository/                # 仓储接口定义
│   │   ├── MediaRepository.kt     # 本地私有媒体库仓储接口
│   │   └── ScanRepository.kt      # 媒体扫描与导入仓储接口
│   └── usecase/                   # 业务用例 (UseCase)
│       ├── ScanMediaUseCase.kt    # 执行扫描与导入
│       ├── GetAudioLibraryUseCase.kt # 获取私有音频列表
│       └── GetVideoLibraryUseCase.kt # 获取私有视频列表
│
├── data/                          # 数据源实现层
│   ├── local/                     # 本地存储操作
│   │   ├── db/                    # Room 数据库定义 (AppDatabase, MediaEntity, MediaDao)
│   │   └── storage/               # 磁盘文件操作 (PrivateMediaStorage 复制文件并生成哈希)
│   ├── source/mediastore/         # 系统媒体库读取 (MediaStoreScanner 扫描系统音频与视频)
│   ├── mapper/                    # 数据映射器 (LocalMediaMapper 转换 Entity 和 Domain Model)
│   └── repository/                # 仓储接口的具体实现
│       ├── MediaRepositoryImpl.kt # 管理本地 Room 媒体数据的流
│       └── ScanRepositoryImpl.kt  # 核心扫描与导入业务流程实现
│
├── playback/                      # 统一播放器引擎
│   ├── service/                   # 播放服务 (PlaybackService，核心 MediaSessionService)
│   ├── controller/                # 播放控制器组件
│   │   ├── PlaybackController.kt  # 单例，封装与 PlaybackService 的 MediaController 异步连接及控制
│   │   └── PlaybackUiBinder.kt    # 将播放器状态自动绑定并驱动 UI 控件 (SeekBar, PlayPause 按钮等)
│   └── mapper/                    # 媒体项转换器 (Media3ItemMapper，将 LocalMedia 转为 Media3 MediaItem)
│
└── feature/                       # Feature UI 展现层 (ViewModel + XML 页面)
    ├── home/                      # 首页模块 (HomeFragment, 展示媒体库入口与底部播放控制卡片)
    ├── audio/                     # 我的音频库 (AudioFragment, RecyclerView 展示导入后的音频)
    ├── video/                     # 我的视频库 (VideoFragment, RecyclerView 并支持列表项内直接播放视频)
    ├── scan/                      # 扫描与导入页 (ScanActivity, 扫描系统库并支持多选重命名导入)
    ├── playing/                   # 播放控制页
    │   ├── AudioPlayingActivity   # 音频播放大图控制页 (进度条、播放/暂停、音视频信息)
    │   └── VideoPlayingFragment   # 视频播放容器
    └── setting/                   # 设置页模块 (SettingFragment)
```

---

## 四、 核心业务机制原理

### 4.1 系统媒体扫描与哈希去重导入机制

1. **MediaStore 扫描**：
   * 在 `ScanActivity` 中，点击扫描后，`ScanViewModel` 调用 `ScanMediaUseCase`。
   * 最终在 `MediaStoreScanner` 中使用 `ContentResolver` 查询系统公共音频库 (`MediaStore.Audio.Media`) 和视频库 (`MediaStore.Video.Media`)，返回 `LocalMediaItem` 列表。
2. **唯一性 SHA-256 去重**：
   * 在导入前，`ScanRepositoryImpl` 调用 `PrivateMediaStorage.calculateUriHash` 计算待导入媒体的 SHA-256 哈希值。
   * 查询 Room 数据库中是否存在该 Hash。如已存在，自动跳过导入，避免重复拷贝占用额外磁盘空间。
3. **私有存储物理拷贝**：
   * 调用 `PrivateMediaStorage.copyMediaToPrivateStorage`，将该媒体从公共 Content URI 拷贝至 App 私有存储目录：
     * 音频：`context.getExternalFilesDir(null)/media/audio/`
     * 视频：`context.getExternalFilesDir(null)/media/video/`
   * **安全命名规则**：导入的文件命名格式为 `System.currentTimeMillis()_safeName.ext`，规避文件名特殊字符冲突。
   * **安全防扫**：在 `media/` 根目录下自动创建 `.nomedia` 空文件，防止其他第三方音乐、相册 App 扫描到此私有目录。
4. **Room 数据库入库**：
   * 文件拷贝成功后，将绝对路径（`localRelativePath` 字段，已实现，后期优化为相对路径）、哈希值、原始标题、歌手、时长、大小及导入时间写入 `media` 数据库表中。

### 4.2 基于 Jetpack Media3 的统一播放控制机制

本项目只维护 **一套** 播放器引擎，即 `PlaybackService` (继承 `MediaSessionService`)，这极大地简化了音视频的前后台播放衔接：

1. **后台 Service 管理**：
   * `PlaybackService` 包含一个懒加载的 `ExoPlayer` 实例以及对应的 `MediaSession`。
   * 无论播放的是音频还是视频，都注入该播放器，且生命周期独立于 UI 页面。
2. **UI 间接控制 (PlaybackController)**：
   * UI 层绝不直接持有 `ExoPlayer` 引用，而是通过 Hilt 注入的单例 `PlaybackController` 发送控制指令。
   * `PlaybackController` 在初始化时，会通过 `MediaController.Builder` 异步连接到后台的 `PlaybackService`。
   * 通过 `StateFlow` (`isPlaying`, `currentMediaItem`) 向外暴露当前播放状态。
3. **UI 状态快速绑定 (PlaybackUiBinder)**：
   * 引入了 `PlaybackUiBinder`，只需要在 Fragment / Activity 中传入页面上的 `SeekBar`、`MaterialButton`、`TextView`，Binder 会自动创建协程周期性更新进度（每 500ms），处理用户的 SeekBar 拖拽 Seek 事件，以及同步播放暂停图标。
4. **列表项内视频播放 (共享 PlayerView)**：
   * 在 `ScanActivity` 以及 `VideoFragment` 中，支持在 RecyclerView 列表中直接播放视频。
   * **实现方式**：在 Activity/Fragment 内维护一个唯一的 `PlayerView`。当用户点击某个列表项的播放按钮时，如果该列表项正处于绑定状态，则动态将该唯一的 `PlayerView` 移出其先前的父容器，并 add 到当前点击项的 `flVideoContainer` 容器中。同时，将 `PlayerView.player` 指向 `PlaybackController.getPlayer()`。这种机制完全规避了在 RecyclerView 中创建多个播放器实例导致的内存溢出及状态冲突问题。

---

## 五、 已实现功能 vs 待开发计划 (Gap 分析)

根据当前的开发进度，项目的完成度如下：

### 5.1 已实现的核心功能 (Milestones Completed)
* [x] **完全单模块 Clean Architecture 分包**：`di`、`core`、`domain`、`data`、`playback`、`feature` 包职责完全分离。
* [x] **运行时权限管理**：适配 Android 13+ 细分媒体权限（`READ_MEDIA_AUDIO`/`VIDEO`）及 Android 12 及以下读取外置存储权限。
* [x] **MediaStore 双媒体扫描**：支持音频及视频查询与显示，支持搜索过滤。
* [x] **哈希去重与安全复制**：支持计算 SHA-256 进行物理去重，文件存储至 App 私有外置存储，并配备 `.nomedia`。
* [x] **列表内重命名**：在扫描结果页，支持调起重命名对话框直接修改媒体 Title。
* [x] **Media3 后台播放**：音频大图播放页（`AudioPlayingActivity`）和首页底部迷你控制卡片与后台 Service 完美同步。
* [x] **列表内视频播放**：视频列表项支持直接切换播放，并且在 `VideoFragment.onPause` 时自动暂停播放。

### 5.2 待开发/可扩展功能 (Future Backlog)
1. **SAF 补充扫描 & SAF 写入导出**：
   * 原设计要求支持用户通过 `ACTION_OPEN_DOCUMENT_TREE` 授权特定文件夹，并能递归扫描 (`SafFolderScanner`)。
   * 原设计要求支持将私有目录下的媒体导出到用户选择的外部文件夹中。这两部分目前暂未在 data/domain 层实现。
2. **轻量状态 DataStore 会话保存**：
   * 目前暂未引入 `DataStore` 依赖，重开 App 时无法通过会话管理器恢复上次播放的媒体项和队列。
3. **本地媒体相对路径存储**：
   * 目前 `MediaEntity` 中的 `localRelativePath` 实质上存入了文件的 **绝对路径**。为了确保 App 卸载重装或数据备份迁移后的兼容性，后期应在 `PrivateMediaStorage` 导入时只返回并存储 **相对路径**（例如 `media/audio/xxx.mp3`），并在 `data` 层读取时结合 `context.getExternalFilesDir(null)` 进行动态拼接。
4. **历史记录与播放列表**：
   * 目前暂无多播放列表（Playlist）及历史记录表（PlaybackHistory）的具体实现。

---

## 六、 维护规范说明

后续参与本项目的开发者或 AI Coding Agent 在新增功能或修改代码时，必须严格遵守以下规则：
1. **优先遵循单模块分包**：不要擅自创建多 Gradle 模块，除非项目主链路功能（包含 SAF 导出和 DataStore 会话）已完全开发完毕。
2. **禁止依赖反转**：任何新增的 Domain 业务类（如 UseCase）严禁引用 `android.content.Context`、`android.net.Uri`、Room Entity 或 `ExoPlayer`。数据映射必须在 `data/mapper` 下进行。
3. **UI 限制**：界面开发以 XML 布局配合 ViewBinding 为核心。严禁为了贪图省事直接在 View 内部创建 `ExoPlayer` 或直接进行数据库/文件 IO 操作，必须通过 ViewModel 调用 UseCase。
4. **提交代码前自检**：在进行 Git 提交前，需通过 `./gradlew assembleDebug` 验证项目编译状况，确保没有遗留未解决的编译错误。
