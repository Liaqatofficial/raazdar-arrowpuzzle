# Runs host-side (JVM) model tests for the rebuild app. Cross-platform pwsh.
$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot
$Src  = Join-Path $Root "app\src\main\java\com\raazdar\arrowpuzzlegame\Game.java"
$Test = Join-Path $Root "app\tests\HostTest.java"
$Out  = Join-Path $PSScriptRoot ".build\host-tests"

Remove-Item -Recurse -Force $Out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $Out | Out-Null

javac -d $Out $Src $Test
if ($LASTEXITCODE -ne 0) { throw "test compilation failed" }
java -cp $Out HostTest
if ($LASTEXITCODE -ne 0) { throw "host tests failed" }

