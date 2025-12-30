# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建和运行命令

### 构建项目
```bash
# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease

# 清理项目
./gradlew clean
```

### 运行应用
```bash
# 运行在 TV 设备/模拟器
./gradlew installDebug
```

### 代码检查和格式化
```bash
# 检查代码风格
./gradlew ktlintCheck

# 自动格式化代码
./gradlew ktlintFormat
```

### 打包发布版本
```bash
# 打包 APK
./gradlew assembleRelease

# 打包 AAB（推荐）
./gradlew bundleRelease
```

## 项目架构

### 架构模式
项目采用 **MVVM (Model-View-ViewModel)** 架构模式：

- **Model**: 位于 `data/entities` 和 `data/repositories`，负责数据管理和业务逻辑
- **View**: 位于 `ui/screens`，使用 Jetpack Compose 构建 UI
- **ViewModel**: 位于各个 Screen 包中，管理 UI 状态和业务逻辑

### 核心技术栈
- **UI 框架**: Jetpack Compose + Compose Compiler
- **状态管理**: StateFlow + Flow
- **依赖注入**: 手动管理（暂未使用 Hilt）
- **网络请求**: OkHttp + AsyncHttp
- **视频播放**: AndroidX Media3 (ExoPlayer)
- **序列化**: Kotlinx Serialization
- **TV 支持**: AndroidX TV Foundation & Material

### 多端支持
项目同一代码库支持多种设备：
- **LeanbackActivity**: Android TV 模式（横屏，遥控器操作）
- **MobileActivity**: 手机模式（竖屏，触摸操作）
- **PadActivity**: 平板模式（横屏，触摸操作）

通过 `MainActivity` 根据设备类型自动跳转到对应界面。

### 关键目录结构
```
app/src/main/java/top/yogiczy/mytv/
├── activities/         # Activities (TV/手机/平板入口)
├── data/              # 数据层
│   ├── entities/      # 数据实体 (Iptv, Epg, etc.)
│   └── repositories/  # 数据仓库模式
├── ui/                # UI 层
│   ├── screens/       # 屏幕 (Leanback, Mobile, Pad)
│   │   ├── leanback/
│   │   │   ├── main/      # 主界面（频道列表）
│   │   │   ├── panel/     # 面板（频道信息）
│   │   │   ├── settings/  # 设置界面
│   │   │   └── video/     # 播放界面
│   └── theme/        # 主题配置
├── utils/             # 工具类
└── BootReceiver.kt    # 开机自启接收器
```

### 数据流
1. **Repository** 从网络或缓存获取数据
2. **ViewModel** 通过 StateFlow 暴露数据
3. **Compose UI** 使用 `collectAsState()` 观察状态变化
4. **UI 事件** 通过回调触发 ViewModel 操作

## TV 特性

### Android TV 配置
- **最小 SDK**: 21 (Android 5.0)
- **Leanback 支持**: 支持 D-Pad 导航和 OK 键
- **画中画 (PiP)**: 支持后台播放
- **触摸屏**: 支持（非必需）
- **横屏模式**: 固定横屏（sensorLandscape）

### TV 交互设计
- **遥控器操作**:
  - 上下方向键切换频道
  - OK 键选择频道/确认
  - 菜单键/帮助键打开设置
  - 双击 OK 键退出应用
- **焦点管理**: TV 专用的焦点处理机制
- **动画效果**: 电视界面过渡动画

### 特色功能
1. **多订阅源**: 支持 M3U、TVBox 格式
2. **多线路**: 同一频道多个播放地址，自动切换
3. **节目单**: 支持 XML、XML.GZ 格式
4. **频道收藏**: 长按收藏/取消收藏
5. **数字选台**: 支持数字键直接选台
6. **HTTP 服务器**: 内置 Web 服务器（端口 10481）
   - 用于远程设置和配置
   - 支持自定义订阅源和节目单

## 开发注意事项

### 播放器相关
- 使用 **Media3** 框架，支持 HLS、RTSP 协议
- 支持线路自动切换和重试机制
- 播放失败时自动切换到备用线路
- 支持 HTTP/HTTPS 代理设置

### 网络请求
- 使用 **OkHttp** 进行网络请求
- 支持 **AsyncHttp** 异步处理
- 已实现 **重试机制** 和 **超时控制**
- 可配置自定义 User-Agent

### 缓存策略
- 使用 **FileCacheRepository** 管理本地缓存
- 支持订阅源和节目单的缓存
- 可配置缓存时间

### 应用设置
- 使用 **SharedPreferences** 存储配置
- 支持 **远程配置**（通过 HTTP 服务器）
- 所有设置项都可以通过网页配置

### 发布版本
- 使用 **ProGuard** 混淆和资源压缩
- 支持 **签名配置**（通过 key.properties 或环境变量）
- 支持 **NDK 架构**: armeabi-v7a, arm64-v8a, x86_64

### 调试功能
- 内置 **MonitorScreen** 用于调试
- 支持 **日志显示** 和 **状态监控**
- 可通过设置界面开启调试模式

## 常见开发任务

### 添加新的设置项
1. 在 `SettingsCategories.kt` 添加分类
2. 在 `ui/screens/leanback/settings/components/` 创建对应组件
3. 在 `SettingsViewModel.kt` 添加状态管理

### 添加新的数据源类型
1. 在 `data/repositories/` 创建新的 Repository
2. 实现 `Fetcher` 接口
3. 在对应的 Parser 中添加解析逻辑

### 修改 UI 主题
- TV 主题: `ui/theme/LeanbackTheme.kt`
- 手机主题: `ui/theme/MobileTheme.kt`
- 平板主题: `ui/theme/PadTheme.kt`

### 添加新的播放协议
1. 在 `Media3VideoPlayer.kt` 中添加新的协议支持
2. 配置对应的 ExoPlayer 组件
3. 添加协议检测和切换逻辑

## 版本信息

- **当前版本**: 1.4.4
- **编译 SDK**: 34 (Android 14)
- **目标 SDK**: 34
- **最低 SDK**: 21 (Android 5.0)
- **Gradle 版本**: 8.7
- **Kotlin 版本**: 根据版本目录确定

## 重要提醒

1. **网络要求**: 应用支持 IPv6，网络环境必须支持 IPv6（默认订阅源）
2. **设备测试**: 只在开发者自有设备上测试过，其他 TV 设备稳定性未知
3. **版本更新**: 支持自动更新功能，通过 GitHub/Gitee Release 检查更新
4. **签名配置**: 发布版本需要正确配置签名信息
5. **混淆规则**: 已配置基本混淆规则，如需修改请编辑 `proguard-rules.pro`