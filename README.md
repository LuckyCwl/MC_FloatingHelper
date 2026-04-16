# FloatingHelper

Minecraft `1.21.10` Fabric 客户端模组。

当前版本行为：

- 只在游戏主界面显示
- 图标固定在右上角
- 使用 `src/main/resources/assets/floatinghelper/textures/gui/icon.png`
- 不再显示在游戏内 HUD

## 环境

```properties
minecraft_version=1.21.10
yarn_mappings=1.21.10+build.3
loader_version=0.18.4
fabric_version=0.138.4+1.21.10
loom_version=1.15-SNAPSHOT
gradle=9.2.1
java=21
```

## 构建

```powershell
.\gradlew.bat build
```

构建产物位于 `build/libs/`。
