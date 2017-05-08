#!/bin/sh

if [ -z ${JAVACMD} ]; then
    JAVACMD=java
fi
if [ ! -z ${JAVA_HOME} ]; then
    JAVACMD="${JAVA_HOME}/bin/java"
fi


#rem %JAVACMD% -Xmx1024m -Xms1024m "-Djava.util.logging.config.file=%~dps0..\..\..\properties\logging.properties"  -cp "%SCALA_HOME%\lib\*;%~dps0..\ivy\test\*;%~dps0\..\build\classes" scala.tools.nsc.MainGenericRunner -usejavacp %*

${JAVACMD} -Xmx1024m -Xms1024m -cp "build/lib/*:build/classes/main:build/resources/main" littleware.apps.s3Copy.view.cli.CliCopyApp $@


