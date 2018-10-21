# SPCK Vulkan Framework

It's a lightweight Vulkan framework, using LWJGL3.

## Running from IntelliJ IDEA

Run with VM parameters:

```
-XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true
```

Note: ```-XstartOnFirstThread``` is needed only on MacOS.

## Limitations

### MacOS
https://github.com/KhronosGroup/MoltenVK/blob/master/Docs/MoltenVK_Runtime_UserGuide.md#limitations

## Building

- Windows: TODO
- Linux: ```./buildLinux.sh```
- MacOS: ```./buildMacOS.sh```

## Running

```bash
./build/release/bin/APP
```

## TODOs
- build for windows
- render triangle
- fully customisable vulkan
- default, replacable gameloop
- somehow separate the engine code and be able to include it with maven

## Using Automatic Modules

### Step 1 - Patch Your not Modularised Jar

TODO: https://examples.javacodegeeks.com/core-java/java-9-jdeps-example/

### Step 2 - Extend Your pom.xml
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