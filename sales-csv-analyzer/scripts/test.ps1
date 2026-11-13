$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsDirectory = Join-Path $projectRoot "tools"
$junitJar = Join-Path $toolsDirectory "junit-platform-console-standalone-1.11.4.jar"
$junitUrl = "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar"

if (-not (Test-Path -LiteralPath $junitJar)) {
    New-Item -ItemType Directory -Force -Path $toolsDirectory | Out-Null
    Write-Host "JUnit 5 실행 파일을 최초 1회 다운로드합니다."
    Invoke-WebRequest -Uri $junitUrl -OutFile $junitJar
}

& (Join-Path $PSScriptRoot "build.ps1")
$testOutput = Join-Path $projectRoot "build/classes/test"
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null
$testSources = Get-ChildItem -LiteralPath (Join-Path $projectRoot "src/test/java") -Filter *.java -Recurse |
    ForEach-Object { $_.FullName }

& javac -encoding UTF-8 -cp "$junitJar;$projectRoot/build/classes/main" -d $testOutput $testSources
if ($LASTEXITCODE -ne 0) { throw "JUnit 테스트 컴파일에 실패했습니다." }

& java "-Dfile.encoding=UTF-8" -jar $junitJar execute `
    --class-path "$projectRoot/build/classes/main;$testOutput" `
    --scan-class-path --details summary --disable-banner
if ($LASTEXITCODE -ne 0) { throw "JUnit 테스트에 실패했습니다." }

