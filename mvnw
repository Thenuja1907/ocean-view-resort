#!/usr/bin/env sh
basedir=$(cd "$(dirname "$0")" && pwd -P)
# Launch the Takari Maven wrapper main class directly (jar has no Main-Class)
exec java -Dmaven.multiModuleProjectDirectory="$basedir" -cp "$basedir/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
