# FloatingHelper

FloatingHelper 是一个适用于 Minecraft `1.21.10` 的 Fabric 模组。

## 功能特性

- 在标题界面和游戏内显示一个悬浮图标
- 可在 Mod Menu 中切换图标显示或隐藏
- 可打开布局编辑界面，拖拽、缩放并镜像翻转图标
- 支持可改绑快捷键：`X` 收回/展开、`C` 吸附到最近侧边栏、`Z` 打开配置界面
- 图标位置会随界面尺寸变化按相对位置保持

## 环境信息

```properties
minecraft_version=1.21.10
yarn_mappings=1.21.10+build.3
loader_version=0.18.4
fabric_version=0.138.4+1.21.10
gradle=9.2.1
java=21
```

## 构建

```powershell
.\gradlew.bat build
```

构建产物会生成在 `build/libs/` 目录中。

## Mod Menu 配置

安装 `Mod Menu` 后，可以在 `FloatingHelper` 的配置页中使用以下功能：

- `显示 / 隐藏`：切换悬浮图标在主界面和游戏内的显示状态
- `编辑位置与大小`：打开布局编辑界面，可拖动图标位置、通过角点缩放尺寸，并使用镜像按钮水平翻转图标

## 默认快捷键

- `X`：将当前界面收回为侧边栏，或从侧边栏展开
- `C`：吸附到左侧或右侧最近的侧边栏位置，并显示 `yc_sidebar`
- `Z`：直接打开 FloatingHelper 配置界面

这些按键可以在游戏的“选项 -> 控制”中重新绑定。
