# FloatingHelper

FloatingHelper 是一个适用于 Minecraft `1.21.10` 的 Fabric 客户端模组。

## 功能特性

- 在主界面和游戏内显示 `yc_ui` 悬浮人物
- 在主界面显示跟随 `yc_ui` 的提示文字
- 进入主界面时会随机显示一句，点击 `yc_ui` 会切换到剩余句子
- 主界面的提示框使用 Minecraft 原版按钮样式，并会根据每句文字长度自动适配宽度
- 可在 Mod Menu 中切换显示/隐藏，并编辑 `yc_ui` 的位置、大小和镜像
- 支持可改绑快捷键：`X` 切换显示/隐藏，`Z` 打开配置界面
- 人物位置会随界面尺寸变化保持相对位置
- `yc_ui` 靠左时保持原图朝向，靠右时会自动左右翻转，让人物朝向屏幕内侧

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

- `显示 / 隐藏`：切换悬浮人物在主界面和游戏内的显示状态
- `编辑位置与大小`：打开布局编辑界面，编辑 `yc_ui` 的位置、尺寸和手动镜像

说明：

- 主界面的提示框不单独编辑位置与大小，而是自动跟随 `yc_ui`
- 主界面的下一句切换由点击 `yc_ui` 触发，不是点击提示框
- 提示框始终使用原版按钮风格显示，文字不会镜像

## 默认快捷键

- `X`：显示 / 隐藏悬浮界面
- `Z`：打开 FloatingHelper 配置界面

这些按键可以在游戏的“选项 -> 控制”中重新绑定。
