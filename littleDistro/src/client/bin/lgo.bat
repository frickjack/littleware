@rem example: lgo help 
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

@rem echo %JAVACMD% "-Xmx512m" "-Djava.util.logging.config.file=%~dps0..\config\logging.properties" -cp "%~dps0..\lib\*" littleware.apps.lgo.LgoAssetCLI %*
%JAVACMD% "-Xmx512m" "-Djava.util.logging.config.file=%~dps0..\config\logging.properties" -cp "%~dps0..\lib\*" littleware.apps.lgo.LgoAssetCLI %*

