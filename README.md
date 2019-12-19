# Spck

This is a WIP project. I'm working on it at nights, when I have time. Usually I don't have. 

It will be a RPG game somewhere in the far future.

## Links

### Lan-Lot to 2D

- https://www.baeldung.com/java-convert-latitude-longitude
- https://gis.stackexchange.com/questions/20686/mercator-projection-problem-with-latitude-formula
- https://math.stackexchange.com/questions/2673075/mercator-projection-use-existing-equation-to-solve-for-degrees/2673128
- https://blog.batchgeo.com/latitude-longitude-map/

### Ballistic Missile

- https://en.wikipedia.org/wiki/Projectile_motion

## Technology

The "engine" or rather the "framework" is written by me in Java. I use LWJGL as a wrapper and OpenGL as a renderer.

Currentl I implemented the following features:
- It's a standalone, Java11 application, hence I'll be able to ship it without expecting the gamer having Java installed
- ECS with Artemis
- Blinn-Phong lightning (directional, point, spot)
- Model loading from OBJ files with Assimp
- A free and an RPG camera controller
- AABB for future raycasting (it also has an AABB renderer layer. You can turn it off and on with F11)
- Polygon debug rendering (it can be turned on and off with F12)

### TODOs

- Shadows
- Remove all TODOs from the code (including this :))
- If I have more time (haha), implement a Vulkan renderer

### GUI

- https://www.thecodingfox.com/nuklear-usage-guide-lwjgl
- https://github.com/kotlin-graphics/imgui


## Running

### Running from IntelliJ IDEA

Run with VM parameters:

```bash
# On OSX
-XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true
# On Linux/Windows
-Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true
```

### Building

- Windows: TODO
- Linux: ```./buildLinux.sh```
- MacOS: ```./buildMacOS.sh```

#### Run the Built Game

```bash
./build/release/bin/APP
```

### Using Automatic Modules

#### Step 1 - Patch not Modularised Jar(s)

Follow this article: https://examples.javacodegeeks.com/core-java/java-9-jdeps-example/

#### Step 2 - Extend pom.xml
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

## License

MIT