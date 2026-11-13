$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$mainOutput = Join-Path $projectRoot "build/classes/main"
New-Item -ItemType Directory -Force -Path $mainOutput | Out-Null

$sources = Get-ChildItem -LiteralPath (Join-Path $projectRoot "src/main/java") -Filter *.java -Recurse |
    ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $mainOutput $sources
if ($LASTEXITCODE -ne 0) { throw "Java 소스 컴파일에 실패했습니다." }

Write-Host "빌드 성공: $mainOutput"

