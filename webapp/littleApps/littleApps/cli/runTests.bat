@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

echo "mojo:  .\runTestCase.bat 2>&1 | out-file output.txt utf8"

%JAVACMD% -cp "%~dps0lib\*;%~dps0littleApps.jar;%~dps0." -Xms300m -Xmx300m "-Dlittleware.properties=../../../properties/littleware.properties" "-Djava.util.logging.config.file=../../../properties/logging.properties" junit.swingui.TestRunner -noloading littleware.apps.message.test.PackageTestSuite

