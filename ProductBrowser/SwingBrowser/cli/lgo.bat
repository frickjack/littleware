@rem example: ./lgo.bat help
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

%JAVACMD% -Djava.util.logging.config.file="%~dps0/logging.properties" -cp "%~dps0\SwingBrowser.jar" littleware.lgo.LgoCommandLine %*

