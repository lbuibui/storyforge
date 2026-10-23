# StoryForge Android 原生重写计划

> 层级：L4 规划 · 2026-10-23
> 这是产品能力研发的施工地图，不是一部作品的生产流程。它不进入
> `S1 世界封存`、`S2 产品定向`、`S3 产品执行`，也不改写 Web 端的
> `CONTEXT_SOURCES`、`FIELD_REGISTRY` / `adopt()`、`PROJECT_TABLES`。
> 已发布的 Capacitor 客户端仍以 [`../guides/CLIENT-DISTRIBUTION.md`](../guides/CLIENT-DISTRIBUTION.md) 为准。
> 本文件不表示原生客户端已经可以创作。

## 1. 边界

| 项 | 决定 |
|---|---|
| 产品 | 共享客户端底座。长篇仍是独立创作产品；世界、跑团、聊天、小镇、文字游戏的 owner 不变 |
| 代码位置 | 新目录 `android-native/`。现有 `android/` Capacitor 工程保持不动 |
| 应用 ID | `com.storyforge.nativerewrite`。`native` 是 Java 关键字，不能做 namespace，所以应用 ID 与包名用同一个可编译的名字。Capacitor 正式包是 `com.storyforge.client`，两边可以同时安装，签名和更新通道也不共用 |
| 数据 owner | Web 的 `Project` 仍只是 LocalWorkspace 壳，`Work` 仍拥有标题、简介、流派、状态、字数和文风。原生 Room 是另一台设备上的本地容器，不是第二套权威，也不回写共享世界 |
| 交接物 | 与 Web 备份 JSON v14 / v15 相同的项目包。API Key、主题和语言留在本机设置里，不进作品备份 |
| 世界与媒资 | MVP 不读取 `WorldRelease`，不把图片、音频、视频或排版资产写进世界表 |

作者在手机上要完成的第一件事，是打开从电脑导出的作品、改一章、让模型续写一截、再把同一份 JSON 带回去。原生界面是这条路径的新壳，不是一套平行的故事格式。

## 2. 现有 Capacitor 客户端已经解决的问题

`android/` 是单 Activity WebView，加载 `dist-android`，固定源站 `https://localhost/storyforge/`。创作、三注册表、Dexie 和 Harness 都还在网页里。原生层只补网页做不到的系统能力：

| 插件 | 已经解决 |
|---|---|
| `StoryForgeHttpPlugin` | 只允许 HTTPS、拒绝带 userinfo 的 URL、不跟随重定向、可取消的流式读取、最多 16 路、连接 30 秒、读取 10 分钟 |
| `StoryForgeFilesPlugin` | 系统保存对话框。调用方必须等到系统写入结果，取消保存会挡住依赖这次备份的操作 |
| `ClientUpdatePlugin` | 只检查固定 GitHub 仓库的稳定 Release，核对包名、递增 `versionCode`、资产 SHA-256 和当前签名后再交给系统安装器 |
| `ClientDiagnosticsPlugin` | 本应用的退出信息摘要。不读 trace，不上传手稿、Prompt、密钥或完整路径 |

同时已经定下来的行为：禁止系统云备份，避免把密钥交给系统；Activity 重建后 IndexedDB 和 localStorage 还在；Debug 包应用 ID 带 `.dev`，不参与正式更新；本机 Codex CLI 在手机上不可用；长时间生成时进程可能被系统杀掉，结果未知不能自动重发。

这些约束在原生重写里继续有效。Capacitor 客户端解决的是“把现有 Web 应用装进手机”。它没有解决 WebView 里 TipTap、百余张表和 Three.js 在中低端机器上的内存与触控成本，也没有把创作链从浏览器存储里搬出来。原生计划补的是后面这件事，并且只按里程碑搬。

## 3. 功能盘点与原生映射

Web 端是 React + TypeScript + Vite，状态用 Zustand，正文用 TipTap，三维场景用 Three.js，本地库是 Dexie / IndexedDB。正式模型调用走登记过的 Skill、Run Contract 和 durable Harness。下面每行都按这个事实映射，不按页面名称另起一套产品。

| 能力 | Web 上的事实 | 原生映射 | 这一阶段 |
|---|---|---|---|
| 写作编辑器 | TipTap 富文本，正文在 `chapters.content`，字数是去掉空白后的字符数 | 单 Activity。MVP 用 Compose 文本字段编辑纯文本 / Markdown 源文，字数函数与 `countWords()` 对齐。富文本标记和选区改写后做 | 骨架有编辑页；MVP 做可保存的正文 |
| 项目与章节 | `Project` 是工作区壳；`Work` 拥有标题和字数；`chapters` 挂 `projectId` 和 `outlineNodeId` | Navigation 三条主路：作品列表、作品详情 / 章节、编辑器。Room 只镜像这几张核心表 | 骨架用占位数据把导航跑通 |
| 模型服务 | `AIProvider` 覆盖云端、兼容端点和 Ollama；请求体是 OpenAI 兼容 Chat Completions，可流式 | Ktor + OkHttp。只走 HTTPS，关闭自动重定向，取消和超时沿用现有插件的边界。密钥放加密设置，不进 Room 作品表 | 骨架只保留端点拼接；MVP 做续写流 |
| Agent Harness | Run Contract、事件、checkpoint、候选、`CreativeArtifact`、作者确认后才 `adopt()` | ViewModel 只持有候选。写入 Room 的正文必须经过明确的采纳动作。失败区分认证、配额、限流、协议、结构、预算、stale 和结果未知 | MVP 只做“续写候选 + 采纳 / 丢弃”；完整 Harness 在里程碑 6 |
| 剧本 / 漫画 / 漫剧 | 各自的 production、场次、页格、分镜和物料表，owner 仍是作品 | 导入备份时先整包保存，界面只读打开剧本场次和漫画页。不在手机上做分页排字或视频提示词包 | 里程碑 4 只读 |
| 插件 | `.sfplugin` 是受信任的 JS / 内容包，按作品启用 | JS 插件不能直接进 Kotlin。原生只做安装清单和启用开关；要跑原插件界面时放进隔离 WebView | 里程碑 5 |
| 世界引擎 | `World`、修订、`WorldRelease`。上层产品只读冻结版本 | Room 保留 `worlds` 壳，用来守住 Project / Work 的外键。封存、能力画像和 `describe/search/read` 不在 MVP | 里程碑 3 起做只读设定 |
| 跑团 / 冒险 / 文字游戏 | 独立产品运行时、存档和媒资。内置《雾港》等可以离线玩 | 游玩包用 WebView 打开现有页面，进度仍按该产品自己的存档模型。不把运行态写回世界表 | 里程碑 5 |
| 三维场景 | Three.js | 不移植渲染器。场景页嵌 WebView，加载现有页面 | 里程碑 5 |
| 数据模型 | Dexie schema v1，表清单以 `PROJECT_TABLES` 为准，目前备份版本 15 | Room 先建工作区、世界壳、作品、大纲节点、章节。其余表在导入时保留为备份 JSON 的未解释片段，避免静默丢列 | 骨架有五张表 |
| 导入导出 | `{ version, exportedAt, project, ...表名 }`，只接受 v14 和 v15 | 同一 JSON。导入在事务里重映射数字 id，失败整单回滚。导出由 `PROJECT_TABLES` 的可导出表驱动，不在 Kotlin 里再抄一份会过期的表名单 | MVP 做往返；骨架只有版本门闩 |
| 国际化 | 界面有 `zh-CN` 与 `en`，仓库 README 另有九种语言 | `values/` 中文、`values-en/` 英文，跟随系统语言。未覆盖的语言在原生端回退中文 | 骨架已放两套字符串 |
| 节点图 | 与长篇共用 Skill 和采纳链，自带图和运行记录 | 图编辑器后置。手机上先不提供第二套生成入口 | 不在前六个里程碑 |
| 本机 Codex | 桌面端通过 app-server 复用 ChatGPT 登录 | 手机没有这台机器上的 Codex CLI。设置里不出现这条通道 | 保持不做 |
| 客户端更新与协议 | Capacitor 已有 GitHub APK 更新和统一协议门 | 原生包用自己的应用 ID 和签名，另做更新。协议正文可以复用，接受记录仍不进作品表 | 里程碑 6 |

架构落地时对应这些固定选择：

- 单 Activity，`ComponentActivity` + `enableEdgeToEdge()`。
- Navigation Compose，路由只表达 MVP 的四个界面。
- 单向数据流：Room / DataStore 为事实来源，ViewModel 暴露状态，界面发出意图。
- 依赖注入先用手写 `AppGraph`。Hilt 等到表、导入任务和模型客户端都稳定后再引入，避免骨架被注解处理器绑死。
- 作品用 Room，主题和动态取色用 DataStore。密钥在真正发出请求之前不落盘。
- 长导入、长导出和可恢复的模型任务用 WorkManager。进程被杀后由任务状态说明“结果未知”，不自动再打一次模型。

## 4. 必须和 Web 共用的格式

当前备份版本是 `CURRENT_BACKUP_VERSION = 15`，导入接受 v14 和 v15。根对象至少包含 `version`、`exportedAt` 和 `project`，其余键是 `PROJECT_TABLES` 里 `exportable` 的表名。数字主键在导出时会重排，导入再映射回去。

原生端要守住的字段：

| Web 类型 | 关键字段 | 原生 Room |
|---|---|---|
| `Project` | `workspaceUid`、`workspacePurpose`、`name`、`activeWorldId`、`activeWorkId`、时间戳 | `projects`。不放标题、流派、字数 |
| `World` | `identityKind`、`code`、`name`、`currentVersion` | `worlds`。MVP 只保证外键，不编辑世界 |
| `Work` | `code`、`kind`、`novelProfile`、`title`、`genres`、`status`、目标字数和当前字数 | `works`。`genres` 在列里存 JSON 数组，导出时还原成字符串数组 |
| `OutlineNode` | `type`、`parentId`、`title`、`summary`、`order` | `outline_nodes` |
| `Chapter` | `outlineNodeId`、`title`、`content`、`wordCount`、`status`、`order`、`notes`、`summary` | `chapters` |

字数口径与 `src/lib/utils/html.ts` 的 `countWords()` 相同：去掉空白后的字符长度。中文一个字算 1，英文单词也按去掉空白后的字符算，不再按空格分词。

作品 JSON 里不放 API Key、DataStore 偏好、更新协议接受记录或客户端诊断队列。这和 Web 端密钥留在本地设置、不进项目导出的做法一致。

未在 Room 建表的数据，导入时整段保留，导出时原样放回。不能因为手机端还没有漫画页或跑团存档的界面，就把这些键丢掉。

## 5. 设计与依赖策略（Design & dependency policy）

界面按 Material 3 Expressive 来设计：更大的圆角、更明确的字号层次、动态取色，以及带轻微回弹的进入动画。依赖锁死在已经发布的稳定版上。

| 依赖 | 锁定 |
|---|---|
| Compose BOM | `2026.09.00`（当前稳定 BOM，对应 Compose 1.12.1） |
| `androidx.compose.material3:material3` | **正好 `1.4.0`**，写在 `android-native/gradle/libs.versions.toml`。不使用 `1.5.0-alpha` 或 `1.5.0-beta` |
| compileSdk | 37，平台包 `platforms;android-37.0`。Compose 1.12 要求 compileSdk 37 和 AGP 9 |
| targetSdk | 36，与正在分发的 Capacitor 客户端相同。骨架没有调用 API 37 的新行为；提到 37 之前要单独核对返回手势、边到边和后台限制 |
| minSdk | 26 |
| AGP / Gradle / JDK | AGP `9.1.1`、Gradle `9.3.1`、JDK 21。Kotlin 使用 AGP 9 自带的 `2.2.10`，KSP `2.2.10-2.0.2` |

`1.4.0` 的公开 API 已经核对过源码包。可以直接用的有：`ShortNavigationBar`、`WideNavigationRail`、`SecureTextField` / `OutlinedSecureTextField`、`LargeTopAppBar`，以及带 `maxItemWidth` 的实验性 `HorizontalCenteredHeroCarousel`。`MotionScheme` 和 `MaterialExpressiveTheme` 在 1.4.0 里是 internal。五个公开形状档（extraSmall 到 extraLarge）可以自定义；`largeIncreased`、`extraLargeIncreased`、`extraExtraLarge` 还不能从应用代码读取。组件内部已经走标准运动方案，应用代码不能切换到 Expressive 运动方案。

因此骨架的做法是：

- 颜色：Android 12 及以上用动态取色，并保留青绿 / 纸色的固定方案，供关闭动态色或旧系统使用。
- 形状：把五个公开档调大（8 / 12 / 20 / 28 / 36 dp），让卡片、按钮和输入框先接近 Expressive 的圆角。
- 运动：页面进入使用 `spring(dampingRatio = 0.72)`。这是回弹的近似，不是 `MotionScheme.expressive()`。
- 导航：手机用 `ShortNavigationBar`。宽屏轨道以后直接用 1.4.0 已公开的 `WideNavigationRail`，不必等 1.5.0。
- 密钥框：设置页使用 `OutlinedSecureTextField`。
- 首页轮播：`HorizontalCenteredHeroCarousel` 仍标着 `ExperimentalMaterial3Api`，骨架不用它做主路径。

`1.5.0` 发布为稳定版之后，再改版本目录并替换下表。在那之前不引入 1.5 的任何预览版。

| 界面 | 现在的 1.4.0 写法 | `1.5.0` 稳定后更换 |
|---|---|---|
| 全局主题 | `MaterialTheme` + 自定义 `Shapes` + 自管 spring | 公开的 `MaterialExpressiveTheme` 与 `MotionScheme.expressive()`。形状改回标准五档，更大的圆角改挂 `largeIncreased`、`extraLargeIncreased`、`extraExtraLarge` |
| 作品列表 | `LargeTopAppBar`、圆角 `Card`、`ExtendedFloatingActionButton` | 公开的 `LargeFlexibleTopAppBar`（副标题放字数）、FAB Menu（新建 / 导入）、波纹 `LoadingIndicator` |
| 作品详情 | `ListItem` 与 `Card` | Expressive list item；章节筛选用 `ButtonGroup` |
| 编辑器 | 底部一排 `FilledTonalButton` / `OutlinedButton` | `FloatingToolbar` 或 `HorizontalFloatingToolbar`；续写用 `SplitButton`（续写、改写、扩写）；生成中用 `LoadingIndicator` |
| 设置 | `OutlinedSecureTextField`、`FilterChip`、`Switch` | 带形状形变的 `FilterChip`；主题切换用 Expressive `ToggleButton` |
| 流式等待 | `CircularProgressIndicator` | `LoadingIndicator` |
| 首页推荐位 | 普通卡片 | 若仍需要轮播，把 `maxItemWidth` 迁到 `preferredItemWidth`，并去掉实验注解 |

## 6. 推迟与 WebView

这些能力留在后面的里程碑，或者嵌现有页面，而不是在 Kotlin 里重写一遍：

- Three.js 场景、内置视觉小说 / 文字冒险页面、以及必须执行作者 JS 的插件界面，用独立 WebView。WebView 不持有作品主库，只接收已经封存的发布物或只读预览地址。
- TipTap 的标记、批注和协同光标。MVP 保存的是纯文本；从 Web 导入时若正文带 HTML，先原样放在 `content` 里，避免转换丢字。
- 节点图、漫剧分镜板、漫画排字、完整 Harness 时间线和本机 Codex。
- 插件代码执行。原生端不内置 JS 引擎去跑不受信任的包。

## 7. 风险

1. **格式漂移。** Web 的可导出表有一百余张，备份版本会继续涨。原生导入必须拒绝未知主版本，并保留未建表的键。手写一张“手机支持的表清单”会在下一次 Web 加表时丢数据。
2. **两套存储。** 浏览器、Capacitor WebView 和 `com.storyforge.nativerewrite` 的 Room 互不自动同步。迁移只走用户导出的 JSON。应用 ID 不能改成 `com.storyforge.client`，否则会覆盖现有安装。
3. **采纳被绕过。** 流式 token 如果直接写进 `chapters.content`，就打破“先候选、后采纳”。编辑器里的手动键入可以写正文；模型输出必须先停在候选上。
4. **结果未知。** 流已经发出、进程被杀、响应没读完时，不能用 WorkManager 的自动重试再请求一次付费模型。重试只留给用户点。
5. **密钥。** 设置页的密钥框在骨架里不落盘。正式存储要用加密 DataStore，并继续关闭 `allowBackup`。日志和备份夹具里不能出现密钥。
6. **WebView 岛的边界。** 场景和插件如果再打开一份 IndexedDB，就会出现两份手稿。嵌入页只能接收调用方传入的只读载荷。
7. **依赖政策。** 把 `material3` 升到 1.5 预览版可以少写一些近似布局，但会把每日构建放在未稳定 API 上。版本目录里的 `1.4.0` 是硬约束。

## 8. MVP

MVP 是作者能在手机上完成一次跨设备往返，而不是把网站搬完。

1. 作品列表：显示工作区名称、当前作品标题、章节数和字数。
2. 章节列表：打开一部作品，看到章节顺序和状态。
3. 编辑器：修改纯文本正文，字数按 Web 口径更新，手动编辑可以保存。
4. AI 续写：读取当前章和上一章摘要，通过 HTTPS 流式请求 OpenAI 兼容接口，把结果放在候选里。作者点采纳后才写入 `content`。取消、认证失败和读到一半的断线都留在界面上，不自动重发。
5. 备份：导入 Web 导出的 v14 / v15 JSON，再导出同一版本。往返测试至少覆盖 `project`、`works`、`chapters` 和一张尚未建 Room 表的未知数组（导入后再导出，数组保持原样）。

MVP 不做富文本、世界编辑、改编生产、插件执行、三维、跑团主持和自动更新。

## 9. MVP 之后的里程碑

1. **续写上下文。** 把大纲节点、视角角色和上一章 `summary` 送进续写请求。候选、拒绝和采纳分开记。旧候选在章节被手改后标记 stale。
2. **编辑与导出正文。** 选区改写、章节状态、作者笔记，以及 TXT / Markdown 导出。导出的是正文，不是完整备份。
3. **设定与大纲。** 从已导入备份里阅读并编辑角色、世界观字段、大纲和细纲。仍然遵守 Work 与 World 的 owner，不在作品行上复制世界字段。
4. **改编只读。** 剧本场次、漫画页格和漫剧分集可以打开和搜索。生产、验收和媒资仍留在各产品自己的流程里。
5. **WebView 岛。** 三维场景、内置互动作品和受信任插件界面。嵌入页与 Room 主库隔离，并沿用 HTTPS 限制。
6. **恢复与分发。** WorkManager 上的长导入导出、失败分类、加密密钥存储、统一协议门，以及 `com.storyforge.nativerewrite` 自己的签名更新通道。

## 10. 这个仓库里的骨架

`android-native/` 现在只证明工程能装起来：Gradle Kotlin DSL、版本目录、四个占位界面、五张 Room 表、中英字符串、动态色和边到边。首次进入会写入一条本地示例，用来看列表和编辑器，不代表已经导入了《雾港》。示例不上传，密钥框不保存。

构建（本机需 JDK 21、Android SDK 平台 37.0 和 Build Tools 36）：

```sh
cd android-native
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

CI 工作流是 `.github/workflows/android-native.yml`。它只构建这个目录，不跑 Capacitor 的 `clients.yml`。
