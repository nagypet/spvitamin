@echo off

set SOURCE=dist\admingui
set DEST1=..\spvitamin-spring-admin
set SITEPATH=\src\main\resources\public\admin-gui

call ng build --prod --configuration=production

IF EXIST %DEST1% (
	IF EXIST %DEST1%%SITEPATH% RMDIR /S /Q %DEST1%%SITEPATH%
	IF NOT EXIST %DEST1%%SITEPATH% MD %DEST1%%SITEPATH%
	xcopy %SOURCE% %DEST1%%SITEPATH% /S /E /H
	colorecho "Site copied to %DEST1%%SITEPATH%" 14
)
