
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

@rem echo "mojo:  .\runTestCase.bat 2>&1 | out-file output.txt utf8"



@rem %JAVACMD% -Xmx1024m -Xms1024m "-Djava.util.logging.config.file=%~dps0..\..\..\properties\logging.properties"  -cp "%SCALA_HOME%\lib\*;%~dps0..\ivy\test\*;%~dps0\..\build\classes" scala.tools.nsc.MainGenericRunner -usejavacp %*
%JAVACMD% -Xmx1024m -Xms1024m "-Djava.util.logging.config.file=%~dps0logging.properties"  -cp "%SCALA_HOME%\lib\*;%~dps0\lib\*;%~dps0\*;." scala.tools.nsc.MainGenericRunner -usejavacp %*


