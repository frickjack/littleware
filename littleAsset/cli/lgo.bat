@rem example: lgo help 
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

%JAVACMD% "-Xmx512m" "-Djava.util.logging.config.file=%~dps0\logging.properties" -cp %~dps0\littleAssetClient.jar littleware.apps.lgo.LgoAssetCLI %*

