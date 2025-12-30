# 我的电视 - 项目结构与新手指南

## 项目概述

这是一个使用Android原生开发的视频播放软件，支持频道切换、数字选台、节目单查看、开机自启、自动更新等功能。支持通过网页端进行自定义订阅源、节目单设置，仅支持Android 5及以上系统，网络需支持IPV6。

## 项目结构

```
mytv-android/
├── app/                    # 应用主模块
│   ├── src/main/
│   │   ├── java/top/yogiczy/mytv/
│   │   │   ├── activities/     # 页面Activity
│   │   │   ├── data/           # 数据层
│   │   │   │   ├── entities/   # 数据实体类
│   │   │   │   ├── repositories/ # 数据仓库
│   │   │   │   └── utils/      # 数据相关工具
│   │   │   ├── ui/             # UI组件
│   │   │   │   ├── screens/    # 各个页面
│   │   │   │   │   └── leanback/ # Leanback界面组件
│   │   │   │   │       ├── components/ # 通用组件
│   │   │   │   │       ├── main/     # 主界面
│   │   │   │   │       ├── panel/    # 面板界面
│   │   │   │   │       ├── settings/ # 设置界面
│   │   │   │   │       ├── video/    # 视频播放界面
│   │   │   │   │       └── toast/    # 提示界面
│   │   │   │   ├── theme/        # 主题样式
│   │   │   │   └── utils/        # UI相关工具
│   │   │   ├── utils/            # 通用工具类
│   │   │   ├── AppGlobal.kt      # 全局变量
│   │   │   ├── BootReceiver.kt   # 开机自启接收器
│   │   │   ├── MyTVApplication.kt # 应用入口
│   │   │   └── UnsafeTrustManager.kt # 信任管理器
│   │   └── res/                  # 资源文件
├── build.gradle.kts        # 模块构建配置
├── gradle.properties       # Gradle配置
├── settings.gradle.kts     # 项目设置
├── README.md              # 项目说明
└── PROJECT_GUIDE.md       # 本指南
```

## 核心功能

- 换台反转
- 数字选台
- 节目单
- 开机自启
- 自动更新
- 多订阅源
- 多线路
- 自定义订阅源
- 多节目单
- 自定义节目单
- 频道收藏
- 应用自定义设置

## 技术栈

- 语言: Kotlin
- 框架: Android Jetpack Compose
- 架构: MVVM
- 网络: OkHttp
- 数据解析: Kotlin Serialization
- 视频播放: ExoPlayer

## 核心组件解析

### 1. 数据层 (data/)
- **entities/**: 定义数据模型，如IPTV、EPG节目单等
- **repositories/**: 数据访问层，包括IPTV源解析、文件缓存等
- **utils/**: 常量定义，如默认直播源地址、节目单地址等

### 2. UI层 (ui/)
- **screens/leanback/**: 使用Android TV的Leanback库构建的界面
  - **components/**: 可复用的UI组件
  - **main/**: 主界面逻辑
  - **panel/**: 选台面板
  - **settings/**: 设置界面
  - **video/**: 视频播放界面
  - **toast/**: 提示信息组件

### 3. 工具类 (utils/)
包含各种辅助功能，如HTTP请求、文件操作、缓存管理等

## 配置说明

### 默认配置 (Constants.kt)
- IPTV源地址: `http://1.2.3.4/live.m3u` (占位符)
- 节目单XML地址: `http://epg.51zmt.top:8000/e.xml.gz`
- 应用标题: "我的电视"

### 用户配置 (SP.kt)
通过SharedPreferences管理用户设置，包括:
- 直播源配置
- 节目单设置
- 界面参数
- 播放偏好

## 构建与运行

### 环境要求
- Android Studio (建议最新版本)
- Android SDK 34
- 最低支持 Android 5.0 (API 21)

### 构建步骤
1. 打开项目: 在Android Studio中打开项目根目录
2. 同步项目: 点击"Sync Project with Gradle Files"
3. 构建APK: Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 运行应用: 连接设备或启动模拟器，点击运行按钮

### Gradle配置说明
- 编译SDK: 34
- 最低SDK: 21
- 目标SDK: 34
- 使用Compose构建UI
- 启用代码混淆和资源压缩（发布版本）

## 项目特色功能

### 1. 多订阅源支持
- 支持M3U和TVBox格式
- 历史记录管理
- 自动缓存机制

### 2. 多线路播放
- 同一频道多个播放地址
- 自动线路切换
- 基于域名的可播放列表

### 3. 网页端设置
- 通过`http://<设备IP>:10481`访问
- 支持自定义订阅源、节目单等

### 4. 节目单功能
- 支持XML和XML.GZ格式
- 当天节目单显示
- 节目时间表

## 开发建议

### 对于新手
1. 先熟悉Android Jetpack Compose基础
2. 了解Leanback库用于电视界面开发
3. 理解MVVM架构模式
4. 掌握Kotlin协程和Flow

### 代码阅读顺序
1. 从[MyTVApplication.kt](app/src/main/java/top/yogiczy/mytv/MyTVApplication.kt)开始了解应用启动流程
2. 阅读[LeanbackApp.kt](app/src/main/java/top/yogiczy/mytv/ui/LeanbackApp.kt)理解UI架构
3. 查看各[repositories](app/src/main/java/top/yogiczy/mytv/data/repositories/)了解数据处理
4. 研究[settings](app/src/main/java/top/yogiczy/mytv/ui/screens/leanback/settings/)组件了解功能实现

## 调试技巧

1. 日志查看: 使用Android Studio的Logcat
2. 网络调试: 检查IPTV源和EPG源的访问情况
3. 性能优化: 关注低端设备的性能表现
4. 兼容性测试: 在不同Android版本和设备上测试

## 扩展开发

- 添加新的IPTV源解析器: 实现IptvParser接口
- 自定义UI组件: 扩展Leanback组件
- 添加新功能: 遵循现有架构模式
- 优化播放体验: 改进ExoPlayer集成

## 常见问题

1. **网络问题**: 确保网络支持IPv6（对于默认源）
2. **EPG解析错误**: 检查节目单格式和频道名称匹配
3. **性能问题**: 优化视频解码和UI渲染
4. **遥控器适配**: 确保所有功能支持遥控器操作