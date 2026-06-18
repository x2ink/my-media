# AGENTS.md

本文件是本项目的 Agent 强制规范。任何 AI Coding Agent、协作开发者或自动化改代码工具在修改本项目代码前，必须先阅读并遵守本文件。

本项目是一个 Android 本地音视频播放器 Demo，目标是学习 Android 媒体开发、现代存储模型、Jetpack Media3、MVVM 与 Clean Architecture。项目当前采用单模块清晰分包的架构。

---

## 1. 总原则

### 1.1 必须遵守
1. 必须使用 Kotlin 作为主要开发语言。
2. 必须使用 MVVM + Clean Architecture 组织业务。
3. 必须保持单向依赖，不允许跨层乱调。
4. 必须通过 UseCase 组织业务动作。
5. 必须通过 Repository 接口隔离数据来源。
6. 必须通过 Room 保存 App 私有媒体库、播放进度、播放历史、播放列表。
7. 必须在导入媒体目录创建 `.nomedia`。
8. 必须使用 Media3 `PlaybackService` (MediaSessionService) + `ExoPlayer` 统一音频和视频播放。
9. UI 层不允许直接创建、持有或释放 `ExoPlayer` 实例。
10. Domain 层必须保持纯 Kotlin，不允许出现任何 Android Framework 类型 (如 Context, Uri, File, DocumentFile 等)。
11. 绝不允许使用高风险的 `MANAGE_EXTERNAL_STORAGE` 权限作为主扫描/操作流程。

### 1.2 严禁事项
1. 严禁把 `File` 递归扫描 `/storage/emulated/0` 作为主扫描方案。
2. 严禁直接扫描第三方 App 私有目录（如网易云音乐、QQ音乐等私有存储路径）。
3. 严禁在 `domain` 层引入以下类型：
   * Context, Uri, File, DocumentFile, ContentResolver
   * ExoPlayer, MediaItem
   * Dao, Room Entity, DataStore, SharedPreferences
4. 严禁 `feature` 层直接访问：
   * Room Dao, ContentResolver, MediaStore
   * DocumentFile, File 直接拷贝动作
   * ExoPlayer 实例
5. 严禁 `data` 层和 `playback` 层依赖 `feature` 层。
6. 严禁 `domain` 层依赖 `data`、`playback`、`app` 层。
7. 严禁音频和视频各自创建一套独立的播放器实例，必须共享 `PlaybackService` 内部封装的单例播放核心。
8. 严禁在 Activity 或 Fragment 中写长耗时的文件复制或元数据解析逻辑，必须使用 Coroutines 放到非主线程执行。
9. 严禁为了省事把所有扫描、拷贝和播放管理逻辑堆进一个 ViewModel。
10. 严禁新增无关依赖、实验性框架或未经讨论的重型第三方库。
11. 严禁无理由重命名大量文件、移动大量目录或随意改变既有包名。

---

## 2. 当前技术栈约束

### 2.1 依赖体系
* Kotlin + Coroutines
* Android SDK 36 (targetSdk) / minSdk 30
* ViewBinding + DataBinding
* Room Database (v2.8.4)
* Dagger Hilt (v2.55)
* Jetpack Media3 (v1.10.1) - exoplayer, ui, common, session
* PermissionX (v1.8.1)
* Glide (v4.11.0)
* Fastjson2 (v2.0.61)
* Orhanobut Logger (v2.2.0)

### 2.2 UI 技术约束
1. 页面必须使用 **Activity / Fragment + XML layout**。
2. 页面视图绑定必须使用 **ViewBinding**，部分页面在需要时可结合 **DataBinding**，但严禁在 XML 中编写复杂的逻辑表达式。
3. **严禁主动引入或转换页面为 Jetpack Compose**，除非用户在 Request 中有明确要求。
4. Fragment 基类需使用 `BaseFragment`，且其 `layoutId` 只用于声明加载的布局，严禁在基类中塞入具体业务代码。
5. RecyclerView Adapter 必须优先使用 **ListAdapter + DiffUtil** 进行数据绑定，点击事件必须通过回调委托给 Fragment/ViewModel。

---

## 3. 包结构强制规范

项目采用单模块清晰分包：
```text
app/src/main/java/ink/x2/mymedia/
├── MyMediaApp.kt
├── MainActivity.kt
│
├── di/                # 仅放依赖注入配置模块，按数据库、调度器、仓储独立拆分
├── core/              # 跨业务通用工具和通用 UI 装饰，不允许放具体媒体库扫描或播放逻辑
├── domain/            # 纯 Kotlin 业务模型、Repository 接口定义和用例 (UseCase)
├── data/              # 各种 Repository 接口的实现、Room 读写、存储复制和 MediaStore 扫描
├── playback/          # ExoPlayer Service 管理、统一控制类 PlaybackController 及 UI 绑定器
└── feature/           # 页面 UI 层 (Activity, Fragment, ViewModel, Adapter, UiState)
```

---

## 4. 依赖方向强制规范

### 4.1 允许依赖
* `feature -> domain`
* `feature -> playback/controller` (如使用 PlaybackController 和 PlaybackUiBinder)
* `feature -> core/ui`
* `playback -> domain`
* `playback -> core`
* `data -> domain`
* `data -> core`
* `app -> feature`, `app -> data`, `app -> playback`, `app -> core`

### 4.2 禁止依赖
* `domain -> data`
* `domain -> playback`
* `domain -> feature`
* `domain -> app`
* `data -> feature`
* `playback -> feature`
* `core -> data` / `playback` / `feature`

---

## 5. Domain 层模型与用例规范

### 5.1 数据模型 (Domain Model)
* `MediaType`：媒体类型枚举（`AUDIO` / `VIDEO`）。
* `LocalMedia`：数据库入库媒体信息模型（不直接使用 Entity）。
* `LocalMediaItem`：扫描出来待导入的媒体基础模型。
* `ImportProgress`：导入状态和当前进度包装流。
* **规则**：所有模型中禁止出现 `Uri` 或 `File` 类型，资源定位一律用 `uriString: String` 或相对路径表示。

### 5.2 用例 (UseCase)
* 每一个 UseCase 必须只负责一项独立的业务活动（例如 `ScanMediaUseCase`、`GetAudioLibraryUseCase`）。
* UseCase 不允许注入 Context，也不允许包含任何具体数据库 Dao 操作，必须通过注入的 Repository 接口代理。

---

## 6. Data 层规范

### 6.1 Room 数据库与路径规约
1. 实体表定义存放在 `data/local/db/entity/`，Dao 存放在 `data/local/db/dao/`。
2. 实体表（Room Entity）严禁向 domain 层或 feature 层直接暴露，必须通过 `LocalMediaMapper` 转换为 Domain Model。
3. **重要（关于存储路径）**：虽然第一阶段出于快速验证目的，将绝对路径物理存入了 `localRelativePath` 字段中，但在后续开发中，必须逐步向真正的“相对路径”重构。新写的读取文件代码若需要解析路径，应在 Data 层动态调用 `context.getExternalFilesDir(null)` 与数据库存储的相对部分拼接出最终绝对路径。

### 6.2 导入机制与 Hash 去重
1. 导入物理拷贝文件必须在协程的 `Dispatchers.IO` 中执行。
2. 文件导入时，必须使用 `PrivateMediaStorage.calculateUriHash` 计算文件的 SHA-256 哈希值，写入 Room 数据库前需调用 `mediaDao.exitsByHash` 判断是否存在，确保**物理去重**。
3. 导入成功的文件名须经过安全清洗，防止非法字符，命名模板：`System.currentTimeMillis()_safeName.ext`。
4. 确保在物理存储根目录 `/media` 下创建 `.nomedia`。

### 6.3 SAF 目录扫描与导出机制 (后续开发建议)
1. **扫描授权目录**：如需实现指定文件夹扫描，必须使用 `ACTION_OPEN_DOCUMENT_TREE`，并调用 `takePersistableUriPermission` 持久化授权，将遍历代码放置于 `data/source/saf/SafFolderScanner.kt` 中。
2. **安全导出**：必须让用户选择导出位置。导出逻辑需放置于 `data/source/saf/SafFileWriter.kt` 中，不可直接存入公共 Music / Movies 根目录，必须规避覆盖已有文件并提供进度回传。

---

## 7. Playback 播放层规范

### 7.1 统一播放核心与控制门面
1. `PlaybackService` 负责维护一个 ExoPlayer，作为前后台播放的唯一引擎。
2. UI 层必须统一使用 `PlaybackController` 实现操作（`play`, `pause`, `seekTo`, `skipToNext` 等），**严禁在 UI Fragment 内直接获取或持有 ExoPlayer 的强引用**。
3. 使用 `PlaybackUiBinder` 进行视图控件状态与播放器进度的快速绑定，避免在各 Fragment 中重复编写拖拽更新与计时更新进度的协程。

### 7.2 视频列表播放
1. 视频播放器不能在列表中为每个 item 实例化，必须在 Activity/Fragment 内维持一个唯一的 `PlayerView`。
2. 当发生点击切换时，动态将 `PlayerView` 从旧的父容器中 remove，再 add 到新的 Item 容器（`flVideoContainer`）中，同时绑定对应的 Player 实例。
3. 当页面 `onPause` 时，必须调用 `PlaybackController.pause()` 暂停播放并妥善管理生命周期。

---

## 8. Agent 新增代码自检流程

在提交或声称任务完成前，Agent 必须通过以下流程自检：
1. **架构合规性**：新写的类分包是否正确？是否存在跨层依赖或反向依赖？
2. **语言与 UI 规范**：是否有 Compose 混入？是否使用了 ViewBinding/DataBinding 绑定 XML？
3. **线程安全性**：耗时 IO 操作（如文件复制、哈希计算、MediaStore 查询）是否已封装在 `ioDispatcher` 协程中？
4. **编译测试**：运行编译命令是否正常通过？
