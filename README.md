# FxCop plugin for C# or VB.NET projects

[![Travis Build Status](https://travis-ci.org/DanielHWe/sonar-fxcop.svg?branch=master)](https://travis-ci.org/DanielHWe/sonar-fxcop)

[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=org.sonarsource.dotnet:sonar-fxcop-plugin)](https://sonarcloud.io/dashboard?id=org.sonarsource.dotnet%3Asonar-fxcop-plugin)

### Requirements

To use the FxCop plugin, you also need to install C# plugin version &ge;5.4 and/or VB.NET plugin version &ge;3.0.

### Use with SonarQube Scanner for MSBuild v4.0

To use with **Version 4** and above you have to define "sonar.cs.fxcop.assembly", "sonar.cs.fxcop.fxCopCmdPath" and "sonar.cs.fxcop.directory" by **/d:"key:value"** for the **SonarQube.Scanner.MSBuild.exe** begin command.

**__Example:__**

SonarQube.Scanner.MSBuild.exe begin /n:"MyProject" /v:"0.9.0.99" /k:"MyProject_key" /d:"sonar.cs.fxcop.assembly=/bin/debug/My.dll" /d:"sonar.cs.fxcop.fxCopCmdPath=C:/Program Files (x86)/Microsoft Visual Studio 14.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe" /d:"sonar.cs.fxcop.directory=bin\debug\"
