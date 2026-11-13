# Java 파일 자동 분류기

`input` 폴더의 파일을 확장자에 따라 분류하는 Java 17 CLI 프로그램입니다. 기본 실행은 실제 파일을 옮기지 않는 미리보기 모드입니다.

## 분류 규칙

| 확장자 | 대상 폴더 |
|---|---|
| `.pdf` | `documents` |
| `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.webp`, `.svg` | `images` |
| `.csv` | `data` |

- 확장자는 대소문자를 구분하지 않습니다.
- 대상에 같은 이름이 있으면 `report (1).pdf`, `report (2).pdf`처럼 새 이름을 사용합니다.
- 기존 파일은 덮어쓰지 않습니다.
- 실행 내역은 `logs/YYYY-MM-DD.log`에 UTF-8로 이어서 기록합니다.
- 지원하지 않는 확장자는 이동하지 않고 로그에 `SKIP`으로 기록합니다.

## 요구 사항

- JDK 17 이상
- Windows PowerShell 5.1 이상 또는 PowerShell 7 이상

## 빌드와 테스트

```powershell
.\scripts\build.ps1
.\scripts\test.ps1
```

테스트는 프로젝트의 `build` 폴더 아래에 격리된 작업공간을 만들고 다음을 자동 검증합니다.

- 예제 파일 10개 분류
- 미리보기에서 파일이 이동하지 않음
- PDF, 이미지, CSV 대상 폴더와 개수
- 기존 `report.pdf`를 보존하고 새 파일을 `report (1).pdf`로 이동
- `logs/2026-11-14.log` 날짜별 로그와 실행 내용

테스트가 끝나면 테스트가 만든 임시 작업공간만 삭제합니다.

## 실행

기준 경로에는 `input` 폴더가 있어야 합니다. 먼저 미리보기로 결과를 확인하세요.

```powershell
# 기본값: 미리보기
.\scripts\run.ps1 -BaseDir C:\path\to\workspace

# 실제 이동
.\scripts\run.ps1 -Mode execute -BaseDir C:\path\to\workspace
```

Java 명령을 직접 사용할 수도 있습니다.

```powershell
java -Dfile.encoding=UTF-8 -cp build/classes/main com.example.fileorganizer.Main --preview --base-dir C:\path\to\workspace
java -Dfile.encoding=UTF-8 -cp build/classes/main com.example.fileorganizer.Main --execute --base-dir C:\path\to\workspace
```

`--preview`와 `--execute`를 생략하면 안전한 `--preview`가 적용됩니다.

## 데모 재현

아래 명령은 프로젝트 내부 `demo-workspace`에 예제 파일 10개와 중복 검증용 기존 PDF 1개를 만듭니다. 기존 폴더가 있으면 덮어쓰지 않고 중단합니다.

```powershell
.\scripts\create-demo.ps1
.\scripts\run.ps1 -BaseDir .\demo-workspace
.\scripts\run.ps1 -Mode execute -BaseDir .\demo-workspace
```

실행 뒤 예상 결과는 다음과 같습니다.

- `input`: 0개
- `documents`: 4개(기존 `report.pdf` 포함)
- `images`: 4개
- `data`: 3개
- `logs`: 실행 날짜의 `.log` 파일

## 종료 코드

- `0`: 성공
- `1`: 옵션 또는 입출력 오류
- `2`: 일부 파일 이동 실패
