# 场景化Demo
此项目包含多个场景Demo，可以输出一个整理APK，也可输出单个场景APK。在 **gradle.properties** 中 **isModule** 进行设置。

目前包含以下场景

|场景|工程名称|
|----|----|
|单主播直播|[SingleHostLive](./Scene-EXamples/Live/)|
|PK直播|[LivePK](./Scene-EXamples/PKLive/)|
|小班课|[BreakoutRoom](./Scene-EXamples/BreakOutRoom/)|

# 前提条件
开始前，请确保你的开发环境满足如下条件：
- XCode 10.0 或以上版本。

# 使用
#### 注册Agora
前往 [Agora官网](https://console.agora.io/) 注册项目，生产appId，然后替换工程**Common**中 **KeyCenter.swift** 中 **AppId**，如果启用了token模式，需要替换 **Token**, 否则设置成nil。

#### 运行示例项目
1. 先在根目录运行 ``pod install``
2. 使用XCode打开Scene-Examples.xcworkspace项目文件
3. 在XCode， Signing and Capabilities设置中。配置您自己的开发者证书。
4. 点击 Run app 按钮。运行一段时间后，应用就安装到 iOS 设备上了。
5. 打开应用，即可使用。
