@echo off

set SOURCE=dist\admingui
set DEST1=..\template-auth-service
set DEST2=..\template-eureka
set DEST3=..\template-scalable-service
set SITEPATH=\src\main\resources\public\admin-gui

call ng build --prod --configuration=production

IF EXIST %DEST1% (
	IF EXIST %DEST1%%SITEPATH% RMDIR /S /Q %DEST1%%SITEPATH%
	IF NOT EXIST %DEST1%%SITEPATH% MD %DEST1%%SITEPATH%
	xcopy %SOURCE% %DEST1%%SITEPATH% /S /E /H
	colorecho "Site copied to %DEST1%%SITEPATH%" 14
)

IF EXIST %DEST2% (
	IF EXIST %DEST2%%SITEPATH% RMDIR /S /Q %DEST2%%SITEPATH%
	IF NOT EXIST %DEST2%%SITEPATH% MD %DEST2%%SITEPATH%
	xcopy %SOURCE% %DEST2%%SITEPATH% /S /E /H
	colorecho "Site copied to %DEST2%%SITEPATH%" 14
)

IF EXIST %DEST3% (
	IF EXIST %DEST3%%SITEPATH% RMDIR /S /Q %DEST3%%SITEPATH%
	IF NOT EXIST %DEST3%%SITEPATH% MD %DEST3%%SITEPATH%
	xcopy %SOURCE% %DEST3%%SITEPATH% /S /E /H
	colorecho "Site copied to %DEST3%%SITEPATH%" 14
)

