#!/bin/sh
# Apache Maven Wrapper script
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"
if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
    mkdir -p "$HOME/.m2/wrapper/dists"
    curl -fSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip" -o "$HOME/.m2/wrapper/dists/apache-maven-3.9.9-bin.zip"
    unzip -q -o "$HOME/.m2/wrapper/dists/apache-maven-3.9.9-bin.zip" -d "$HOME/.m2/wrapper/dists"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
