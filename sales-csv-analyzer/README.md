# CSV 매출 자동 분석기

Java 17로 CSV 매출 데이터를 집계하고 Markdown 보고서를 만드는 독립형 CLI 프로젝트입니다. 외부 API, 유료 서비스, 데이터베이스를 사용하지 않습니다.

## 기능

- `BigDecimal` 기반의 정확한 매출액 계산
- 전체 매출액과 판매 수량 집계
- 월별 매출, 전월 대비 증감액과 증감률 계산
- 매출액 기준 상품 TOP 5 산출
- 카테고리별 매출액과 비중 계산
- 통계를 이용한 규칙 기반 분석 요약 생성
- 잘못된 CSV 행에 행 번호가 포함된 오류 표시
- 실행할 때마다 `reports/sales-report.md`를 최신 데이터로 갱신

## 요구 사항

- JDK 17 이상
- Windows PowerShell 5.1 이상 또는 PowerShell 7 이상
- JUnit 최초 실행 시 Maven Central에 접속할 수 있는 인터넷 연결

Maven이나 Gradle 설치는 필요하지 않습니다. JUnit 테스트 스크립트는 공식 `junit-platform-console-standalone` JAR을 프로젝트의 `tools` 폴더에 최초 한 번만 내려받습니다.

## 프로젝트 구조

```text
sales-csv-analyzer/
├─ data/sales.csv
├─ reports/
│  ├─ sales-report.md
│  └─ validation.md
├─ scripts/
├─ src/main/java/com/example/sales/
└─ src/test/java/com/example/sales/
```

## 실행

프로젝트 폴더에서 다음 명령을 실행합니다.

```powershell
.\scripts\run.ps1
```

다른 CSV나 출력 경로를 지정할 수도 있습니다.

```powershell
.\scripts\run.ps1 -CsvPath data/other.csv -ReportPath reports/other-report.md
```

입력 CSV의 헤더는 다음과 정확히 일치해야 합니다.

```csv
date,category,product,quantity,unit_price
```

- `date`: `YYYY-MM-DD` 형식의 유효한 날짜
- `quantity`: 1 이상의 정수
- `unit_price`: 0 이상의 숫자
- 쉼표가 포함된 값은 큰따옴표로 감쌀 수 있습니다.

CSV를 변경한 뒤 `run.ps1`을 다시 실행하면 보고서가 새 분석 결과로 교체됩니다.

## 테스트

```powershell
.\scripts\test.ps1
```

JUnit 5로 전체 매출 합계, 월별 집계, 전월 대비 증감률, TOP 5 정렬, 잘못된 CSV 오류 처리를 검증합니다. 실제 실행 결과는 `reports/validation.md`에 기록되어 있습니다.

## 샘플 데이터 재생성

```powershell
.\scripts\generate-sample.ps1
```

이 명령은 2026년 1월부터 6월까지 108개 데이터 행을 만들어 기존 `data/sales.csv`를 교체합니다.
