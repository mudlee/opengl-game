# Instructions

Artemis is a non modularised jar, so we have to update it to be able to use jlink.

Source:  https://examples.javacodegeeks.com/core-java/java-9-jdeps-example/

## Requirements

Clone out https://github.com/junkdog/artemis-odb and copy artemis-core/artemis/src's content under src

## Commands to run

```bash
jdeps --module-path automatic-modules --generate-module-info . automatic-modules/artemis-odb-2.2.0-SNAPSHOT.jar
javac -d artemis.odb/ --source-path src/ artemis.odb/module-info.java
```