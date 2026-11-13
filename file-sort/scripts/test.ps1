$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")

$classPath = (Join-Path $projectRoot "build/classes/main") + [IO.Path]::PathSeparator +
    (Join-Path $projectRoot "build/classes/test")
& java "-Dfile.encoding=UTF-8" -ea -cp $classPath com.example.fileorganizer.FileOrganizerTest
if ($LASTEXITCODE -ne 0) { throw "테스트에 실패했습니다." }

