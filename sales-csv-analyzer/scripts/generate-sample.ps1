$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")
Push-Location $projectRoot
try {
    & java "-Dfile.encoding=UTF-8" -cp "build/classes/main" com.example.sales.SampleDataGenerator
    if ($LASTEXITCODE -ne 0) { throw "샘플 CSV 생성에 실패했습니다." }
} finally {
    Pop-Location
}

