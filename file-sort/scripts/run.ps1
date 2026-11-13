param(
    [ValidateSet("preview", "execute")]
    [string]$Mode = "preview",
    [string]$BaseDir = (Get-Location).Path
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")

$modeOption = if ($Mode -eq "execute") { "--execute" } else { "--preview" }
& java "-Dfile.encoding=UTF-8" -cp (Join-Path $projectRoot "build/classes/main") com.example.fileorganizer.Main `
    $modeOption --base-dir $BaseDir
if ($LASTEXITCODE -ne 0) { throw "파일 분류기 실행에 실패했습니다." }

