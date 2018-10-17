# SPCK Vulkan Framework

It's a lightweight Vulkan framework, using LWJGL3.

# Running from IntelliJ IDEA

Run with VM parameters:

```java
-XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true
```
# Limitations

## OSX
https://github.com/KhronosGroup/MoltenVK/blob/master/Docs/MoltenVK_Runtime_UserGuide.md#limitations

# Running from command line

## Packaging

### Create Modules

```bash
mvn clean
mvn compile
mvn package
```

### Setup automatic modules

### Link

#### MacOS
```bash
./buildOSX.sh
```

#### Linux
```bash
TODO
```

#### Windows
```bash
TODO
jlink --module-path "%JAVA_HOME%"\jmods;build\mods --add-modules spck.game --launcher APP=spck.game/spck.game.Main --output app
```

## Run

#### Linux/OSX
```bash
build/release/bin/java -XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true --module-path build/mods -m spck.game/spck.game.Main
```

#### Windows
```bash
build\release\bin\java -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true --module-path build\mods -m spck.game/spck.game.Main
```