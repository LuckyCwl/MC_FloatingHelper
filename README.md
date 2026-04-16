# FloatingHelper

Minecraft `1.21.10` Fabric 客户端模组。

当前版本行为：

- 只在游戏主界面显示
- 图标固定在右上角
- 使用 `src/main/resources/assets/floatinghelper/textures/gui/icon.png`
- 不再显示在游戏内 HUD
- 已接入 Mod Menu 配置入口
- 可在配置界面切换“是否在主界面显示”
- 可上传自定义图案并立即预览
- 可在遮罩编辑层中拖拽位置与四角缩放尺寸

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

## 配置说明

在安装 `Mod Menu` 后，可以在模组列表中打开 `FloatingHelper` 的配置页。

- `主界面显示`：控制图标是否出现在标题界面
- `编辑位置与大小`：进入遮罩编辑层，拖动图标移动，拖动红色角点缩放
- `上传`：导入自定义图案
- `使用内置图案`：恢复项目自带图标

自定义图案会保存到游戏 `config/floatinghelper/` 目录下。
