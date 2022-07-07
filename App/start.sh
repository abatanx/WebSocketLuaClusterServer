#!/bin/sh

###
# Start-up script
###

cd `dirname $0`

if [ "$JAVA_HOME" = "" ]
then
    echo "[$0] Error: 'JAVA_HOME' not defined."
    exit 1
fi

if [ "$1" = "" ]
then
    PROPFILE="server.properties"
else
    PROPFILE="$1"
fi

JAVA=${JAVA_HOME}/bin/java
JAVAOPT="-server -d64"

JAR=../out/artifacts/WebSocketLuaClusterServer_main_jar/WebSocketLuaClusterServer.main.jar

echo "JVM: ${JAVA} ${JAVAOPT} -jar ${JAR} ${PROPFILE}"
${JAVA} ${JAVAOPT} -jar ${JAR} ${PROPFILE}
