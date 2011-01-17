@rem example: launch the freakin' server
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

cd "%~dps0\.."
%JAVACMD% "-Xmx512m" "-Djava.util.logging.config.file=config\logging.properties" "-Dlittleware.home=config" "-Djava.security.auth.login.config=config\login.config" "-Dderby.system.home=data\javadb" -cp "lib\*" littleware.bootstrap.server.CliServer %*

