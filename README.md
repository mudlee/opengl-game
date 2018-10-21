# SPCK Vulkan Framework

It's a lightweight Vulkan framework, using LWJGL3.

# Running from IntelliJ IDEA

Run with VM parameters:

```
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
build/release/bin/java -XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true --module-path build/mods --add-modules org.lwjgl.vulkan.natives -m spck.game/spck.game.Main
```

#### Windows
```bash
build\release\bin\java -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true --module-path build\mods -m spck.game/spck.game.Main
```

# TODOs
- Java 11 build
- build for windows
- render triangle
- fully customisable vulkan
- default, replacable gameloop
- somehow separate the engine code and be able to include it with maven

# Using Automatic Modules

## Extend Your pom.xml
```xml
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <version>${maven.plugins.version}</version>
    <executions>
        <execution>
            <id>copy-resources</id>
            <phase>package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <overwrite>true</overwrite>
                <outputDirectory>../build/mods</outputDirectory>
                <resources>
                    <resource>
                        <directory>../automatic-modules/THE_MODULE</directory>
                        <include>THE_MODULE.jar</include>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Patch Your not Modularised Jar

TODO: https://examples.javacodegeeks.com/core-java/java-9-jdeps-example/
