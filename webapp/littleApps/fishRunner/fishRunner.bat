@rem example: lgo help 
@echo off

if not defined JAVACMD set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"

@rem %JAVACMD% "-Xmx512m" "-Djava.util.logging.config.file=%~dps0\logging.properties" -cp %~dps0\littleAssetClient.jar littleware.apps.lgo.LgoAssetCLI %*
%JAVACMD% -cp 'target/*;.' littleware.apps.fishRunner.FishApp S3_CREDSFILE s:\aws\reuben.properties WAR_URI .\appsWeb-1.0-SNAPSHOT.war LOGIN_URI .\login.config CONTEXT_ROOT littleware_services DATABASE_URL 'postgres://littleware_user:littleware_user_password@localhost:5432/littleware'
