# SPCK Vulkan Framework

It's a lightweight Vulkan framework, using LWJGL3.

# Running from IntelliJ IDEA

Run with VM parameters:

```java
-XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true
```
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

```bash
jlink --module-path $JAVA_HOME/jmods:build/mods --add-modules com.intermetto.game --launcher APP=com.intermetto.game/com.intermetto.game.Main --output app
```

## Run

```bash
java -XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true --module-path build/mods -m com.intermetto.game/com.intermetto.game.Main -XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true
```