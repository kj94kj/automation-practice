$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$mainOutput = Join-Path $projectRoot "build/classes/main"
$testOutput = Join-Path $projectRoot "build/classes/test"

New-Item -ItemType Directory -Force -Path $mainOutput, $testOutput | Out-Null

$mainSources = Get-ChildItem -Path (Join-Path $projectRoot "src/main/java") -Filter *.java -Recurse |
    ForEach-Object { $_.FullName }
$testSources = Get-ChildItem -Path (Join-Path $projectRoot "src/test/java") -Filter *.java -Recurse |
    ForEach-Object { $_.FullName }

& javac -encoding UTF-8 -d $mainOutput $mainSources
if ($LASTEXITCODE -ne 0) { throw "main 소스 컴파일에 실패했습니다." }

& javac -encoding UTF-8 -cp $mainOutput -d $testOutput $testSources
if ($LASTEXITCODE -ne 0) { throw "test 소스 컴파일에 실패했습니다." }

Write-Host "빌드 완료: $mainOutput"

