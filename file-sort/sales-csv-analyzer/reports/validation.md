# 테스트 및 검증 결과

- 검증일: 2026-11-14
- 실행 환경: Java 17.0.12, Windows PowerShell
- 테스트 도구: JUnit Platform Console Standalone 1.11.4
- 실행 명령: `.\scripts\test.ps1`

## JUnit 실행 결과

| 항목 | 결과 |
|---|---:|
| 발견한 테스트 | 5개 |
| 실행한 테스트 | 5개 |
| 성공 | 5개 |
| 실패 | 0개 |
| 건너뜀 | 0개 |
| 중단 | 0개 |

JUnit 실제 실행 요약:

```text
Test run finished after 197 ms
[         5 tests found           ]
[         0 tests skipped         ]
[         5 tests started         ]
[         0 tests aborted         ]
[         5 tests successful      ]
[         0 tests failed          ]
```

## 검증한 항목

| 테스트 | 검증 내용 | 결과 |
|---|---|---|
| `calculatesTotalRevenueExactlyWithBigDecimal` | 소수 단가를 포함한 전체 매출 합계와 수량이 `BigDecimal`로 정확히 계산되는지 확인 | 성공 |
| `aggregatesMonthlyRevenue` | 여러 행이 월별로 정확히 합산되는지 확인 | 성공 |
| `calculatesMonthOverMonthAmountAndRate` | 증가와 감소의 전월 대비 증감액 및 증감률을 확인 | 성공 |
| `sortsTopFiveProductsByRevenueDescending` | 상품 6개 중 매출액 기준 상위 5개가 내림차순으로 선택되는지 확인 | 성공 |
| `rejectsInvalidCsvAndReportsLineNumber` | 잘못된 날짜와 수량이 있는 CSV에서 예외가 발생하고 오류 행 번호가 표시되는지 확인 | 성공 |

## 실제 샘플 실행 결과

`data/sales.csv`를 입력으로 `.\scripts\run.ps1`을 실행해 `reports/sales-report.md`를 생성했습니다.

| 항목 | 확인 결과 |
|---|---:|
| CSV 데이터 행 | 108행 |
| 분석 기간 | 2026-01-02 ~ 2026-06-19 |
| 전체 판매 수량 | 756개 |
| 전체 매출액 | 23,160,000원 |
| 월별 집계 | 2026년 1월~6월, 6개 월 |
| 상품 TOP 5 | 5개 상품 출력 |
| 카테고리 집계 | 3개 카테고리 출력 |

PowerShell의 `Import-Csv`로 Java 분석과 별개로 원본을 다시 합산한 결과도 108행, 756개, 23,160,000원으로 일치했습니다. 월별 매출 역시 1월부터 6월까지 각각 2,335,000원, 2,945,000원, 4,165,000원, 3,555,000원, 5,385,000원, 4,775,000원으로 보고서와 일치했습니다.

## CSV 변경 반영 검증

원본 파일은 유지하고 `build/changed-sales.csv` 복사본의 첫 데이터 행 수량만 2개에서 3개로 변경한 뒤 다른 출력 경로로 다시 실행했습니다.

| 항목 | 원본 분석 | 변경 데이터 분석 | 차이 |
|---|---:|---:|---:|
| 전체 판매 수량 | 756개 | 757개 | +1개 |
| 전체 매출액 | 23,160,000원 | 23,249,000원 | +89,000원 |

변경 보고서에서 `757개`와 `23,249,000원`을 확인했으며, 원본 `data/sales.csv`의 첫 행 수량은 2개로 유지됐습니다. 따라서 CSV를 수정하고 다시 실행하면 변경된 분석 결과로 보고서가 갱신되는 동작을 확인했습니다.

샘플 실행과 JUnit 테스트에서 실패하거나 실행하지 못한 항목은 없습니다.
