@echo off
set /p VERSION=<version.ver
java.exe -jar -Duser.country=US -Duser.language=en %~dp0\pwdtool-%VERSION%-all.jar %1 %2 %3 %4

