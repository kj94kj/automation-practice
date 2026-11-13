param(
    [Alias("Input")]
    [string]$CsvPath = "data/sales.csv",
    [Alias("Output")]
    [string]$ReportPath = "reports/sales-report.md"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot "build.ps1")
Push-Location $projectRoot
try {
    & java "-Dfile.encoding=UTF-8" -cp "build/classes/main" com.example.sales.Main `
        --input $CsvPath --output $ReportPath
    if ($LASTEXITCODE -ne 0) { throw "매출 분석 실행에 실패했습니다." }
} finally {
    Pop-Location
}

