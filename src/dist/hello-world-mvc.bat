@echo off
rem ===========================================================================
rem Hello World MVC - launcher for Windows.
rem Authors: Aritz Navarro, Brayan Romero, Ekaitz Rivero
rem
rem Runs the application with the Java runtime bundled in the "jre" folder, so no
rem Java installation is needed.
rem ===========================================================================
setlocal

rem Work from the folder of this script, so that config.properties, data\ and
rem logs\ are found whatever folder the script is started from
cd /d "%~dp0"

rem The application jar is a module; the MySQL driver in lib\ stays on the class path
"jre\bin\java.exe" --enable-native-access=javafx.graphics --module-path "hello-world-mvc.jar" --class-path "lib\*" --module tartanga.dami2.din.helloworldmvc/tartanga.dami2.din.helloworldmvc.App %*

rem Keep the window open if the application could not start, so the error can be read
if errorlevel 1 pause
endlocal
