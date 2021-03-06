@echo off

rem Copyright 2015 - 2017 AZYVA INC. INC.
rem
rem This file is part of Dragom.
rem
rem Dragom is free software: you can redistribute it and/or modify
rem it under the terms of the GNU Affero General Public License as published by
rem the Free Software Foundation, either version 3 of the License, or
rem (at your option) any later version.
rem
rem Dragom is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem GNU Affero General Public License for more details.
rem
rem You should have received a copy of the GNU Affero General Public License
rem along with Dragom.  If not, see <http://www.gnu.org/licenses/>.

rem Main start script for Dragom tools. It launches the JVM starting
rem org.azyva.dragom.tool.DragomToolInvoker.
rem
rem This script calls set-env.cmd, or set-env.cmd if the former does not exist,
rem which can set various variables to configure the Dragom execution environment.
rem See the comments in set-env.sh for more information.
rem
rem Additional parameters can be passed to the JVM with one or more --jvm-option
rem cli option specified as the first arguments to the script, before the Dragom
rem tool name.
rem
rem Synopsis:
rem
rem dragom [ [ --jvm-option <jvm-option> ] ... ] <dragom-tool> [argument] ...

setlocal EnableDelayedExpansion

set DRAGOM_HOME_DIR=%~dp0%..

rem We handle the --jvm-option cli option before sourcing dragom-set-env.cmd
rem or set-env.cmd since we want to set the Dragom tool. 

rem Extracting and handling some arguments, while leaving the remaining arguments
rem as is is difficult for various reasons, including:
rem - %* does not take into account shifted-out arguments
rem - Windows interprets = as an argument separator when parsing a command line
rem   into the positional variables %1, %2, etc.
rem The solution is to take the whole command line (%*) and parse it explicitly.
rem Here again, this is difficult since a for loop breaks on lines, not tokens. So
rem we replace spaces with newlines.

set STATE=
set CLI_JVM_OPTIONS=
set ARGS=

set "ORG_ARGS=%*"

rem The 2 empty lines after this set are important. This solution was found here:
rem http://stackovervlow.com/questions/2524928/dos-batch-iterate-through-a-delimited-string
set NEWLINE=^


rem When a variable is empty, its evaluation with replacement (see below in for)
rem does not work correctly.
rem We use delayed expansion as normal expansion does not work when the variable
rem contains some combinations of spaces and double-quotes, such as
rem -DPARAM="MY VALUE".
if "!ORG_ARGS!" == "" goto continue1

for /f %%A in ("%ORG_ARGS: =!NEWLINE!%") do (
  if "!STATE!" == "" (
    if "%%A" == "--jvm-option" (
      set STATE=jvm-option
    ) else (
      rem This state is encountered only for the very first non-option argument which
      rem corresponds to the Dragom tool.

      set "ARGS=%%A"
      set "DRAGOM_TOOL=%%A"
      set STATE=arg
    )
  ) else if "!STATE!" == "jvm-option" (
    set CLI_JVM_OPTIONS=!CLI_JVM_OPTIONS! %%A
    set STATE=
  ) else if "!STATE!" == "arg" (
    set "ARGS=!ARGS! %%A"
  )
)

:continue1

set DRAGOM_TOOL=

if exist %DRAGOM_HOME_DIR%\bin\dragom-set-env.cmd (
  call %DRAGOM_HOME_DIR%\bin\dragom-set-env.cmd
) else (
  call %DRAGOM_HOME_DIR%\bin\set-env.cmd
)

if "%JAVA_HOME%" == "" (
  echo JAVA_HOME not set.
  exit /b 1
)

rem We use a temporary file for getting variable lists in order to redirect errors
rem (when a variable does not exist) in nul, which is not possible with the
rem "for /f" command.

:retry-temp-file
set TEMP_FILE=%TEMP%\dragom-temp-%RANDOM%.tmp
if exist %TEMP_FILE% goto retry-temp-file

set SYSTEM_PROPERTIES_JVM_OPTIONS=

set SYSTEM_PROPERTY_ 2>nul 1>%TEMP_FILE%

for /f "tokens=1* delims==" %%P in (%TEMP_FILE%) do (
  set PROPERTY=%%P
  set PROPERTY=!PROPERTY:SYSTEM_PROPERTY_=!
  set SYSTEM_PROPERTIES_JVM_OPTIONS=!SYSTEM_PROPERTIES_JVM_OPTIONS! -D!PROPERTY!=%%Q
)

del %TEMP_FILE%

set DRAGOM_SYSTEM_PROPERTIES_JVM_OPTIONS=

set DRAGOM_SYSTEM_PROPERTY_ 2>nul 1>%TEMP_FILE%

for /f "tokens=1* delims==" %%P in (%TEMP_FILE%) do (
  set PROPERTY=%%P
  set PROPERTY=!PROPERTY:DRAGOM_SYSTEM_PROPERTY_=!
  set DRAGOM_SYSTEM_PROPERTIES_JVM_OPTIONS=!DRAGOM_SYSTEM_PROPERTIES_JVM_OPTIONS! -Dorg.azyva.dragom.!PROPERTY!=%%Q
)

del %TEMP_FILE%

set INIT_PROPERTIES_JVM_OPTIONS=

set INIT_PROPERTY_ 2>nul 1>%TEMP_FILE%

for /f "tokens=1* delims==" %%P in (%TEMP_FILE%) do (
  set PROPERTY=%%P
  set PROPERTY=!PROPERTY:INIT_PROPERTY_=!
  set INIT_PROPERTIES_JVM_OPTIONS=!INIT_PROPERTIES_JVM_OPTIONS! -Dorg.azyva.dragom.init-property.!PROPERTY!=%%Q
)

del %TEMP_FILE%

rem The computed arguments and options could very well include reserved characters
rem which would need to be escaped. We neglect handling them, except for ARGS
rem which could include a --reference-path-matcher option that includes ">".

rem The computed arguments and options could very well include reserved characters
rem which would need to be escaped. We neglect handling them, except for ARGS
rem which could include a --reference-path-matcher option that includes ">".
rem We use delayed expansion as normal expansion does not work when the variable
rem contains some combinations of spaces and double-quotes, such as
rem -DPARAM="MY VALUE".
if "!ARGS!" == "" goto continue2

set "ARGS=%ARGS:>=^>%"

:continue2

%JAVA_HOME%/bin/java ^
  %JVM_OPTIONS% ^
  %SYSTEM_PROPERTIES_JVM_OPTIONS% ^
  %DRAGOM_SYSTEM_PROPERTIES_JVM_OPTIONS% ^
  %INIT_PROPERTIES_JVM_OPTIONS% ^
  %CLI_JVM_OPTIONS% ^
  -classpath "%DRAGOM_HOME_DIR%\classpath;%DRAGOM_HOME_DIR%\lib\*" ^
  org.azyva.dragom.tool.DragomToolInvoker %ARGS%
