param(
    [string]$Root = (Join-Path (Split-Path -Parent $PSScriptRoot) "demo-workspace")
)

$ErrorActionPreference = "Stop"
$resolvedParent = [IO.Path]::GetFullPath((Split-Path -Parent $Root))
$projectRoot = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
if (-not $resolvedParent.StartsWith($projectRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "데모 폴더는 프로젝트 내부에만 만들 수 있습니다: $Root"
}
if (Test-Path -LiteralPath $Root) {
    throw "기존 경로를 보호하기 위해 중단했습니다. 폴더가 이미 있습니다: $Root"
}

$inputDirectory = New-Item -ItemType Directory -Path (Join-Path $Root "input")
$documentsDirectory = New-Item -ItemType Directory -Path (Join-Path $Root "documents")

$fileNames = @(
    "report.pdf", "manual.PDF", "invoice.pdf",
    "photo.jpg", "logo.PNG", "diagram.svg", "banner.webp",
    "sales.csv", "users.CSV", "inventory.csv"
)
foreach ($fileName in $fileNames) {
    Set-Content -LiteralPath (Join-Path $inputDirectory.FullName $fileName) -Value "sample:$fileName" -Encoding UTF8
}
Set-Content -LiteralPath (Join-Path $documentsDirectory.FullName "report.pdf") -Value "original-report" -Encoding UTF8

Write-Host "예제 파일 10개와 중복 검증용 기존 파일을 생성했습니다: $Root"

