
@ECHO OFF
ECHO Searching for Java 9 SDK

FOR /D %%a IN ("%ProgramFiles%\Java\jdk*9*") DO @set javaFolder=%%a\bin

IF NOT DEFINED javaFolder (
	ECHO Java 9 SDK not found, searching for Java 8 SDK
	FOR /D %%a IN ("%ProgramFiles%\Java\jdk*8*") DO @set javaFolder=%%a\bin
)

IF DEFINED javaFolder (
	ECHO Removing previously compiled classes
	del "%cd%\server\*.class" 2>nul
	ECHO Java SDK found, compiling server classes
	"%javaFolder%\javac.exe" *.java
	"%javaFolder%\java.exe"  BikeServer
) ELSE (
	ECHO Java SDK folder not found
	ECHO Please install Java SDK version 8 minimum
	pause
)