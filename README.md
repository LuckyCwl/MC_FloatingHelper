# FloatingHelper

FloatingHelper is a Fabric mod for Minecraft `1.21.10`.

## Features

- Shows a floating icon on the title screen
- Lets you toggle the icon on or off from Mod Menu
- Lets you open a layout editor to move and resize the icon
- Keeps the icon off the in-game HUD

## Environment

```properties
minecraft_version=1.21.10
yarn_mappings=1.21.10+build.3
loader_version=0.18.4
fabric_version=0.138.4+1.21.10
gradle=9.2.1
java=21
```

## Build

```powershell
.\gradlew.bat build
```

Build output is generated in `build/libs/`.

## Mod Menu

After installing `Mod Menu`, open the `FloatingHelper` config page to access:

- `Show / Hide`: toggles whether the floating icon is visible on the title screen
- `Edit Position & Size`: opens the layout editor so you can drag the icon and resize it from the corners
