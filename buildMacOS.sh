#!/bin/sh

[[ -z "$JAVA_HOME" ]] && { echo "JAVA_HOME is not set" ; exit 1; }

mvn clean
mvn -Plwjgl-natives-macos compile
mvn -Plwjgl-natives-macos package
jlink --module-path $JAVA_HOME/jmods:build/mods --add-modules spck.game --launcher APP=spck.game/spck.game.Main --compress 2 --no-header-files --no-man-pages --strip-debug --output build/release
sed -i -e 's/$JLINK_VM_OPTIONS/-XstartOnFirstThread -Dorg.lwjgl.util.DebugLoader=true -Dorg.lwjgl.util.Debug=true -Dorg.lwjgl.opengl.Display.enableHighDPI=true -Dorg.lwjgl.opengl.Display.enableOSXFullscreenModeAPI=true/g' build/release/bin/APP