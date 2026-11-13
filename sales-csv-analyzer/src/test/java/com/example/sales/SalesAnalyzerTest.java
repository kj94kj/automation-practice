package com.example.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SalesAnalyzerTest {
    private final SalesAnalyzer analyzer = new SalesAnalyzer();

    @Test
    void calculatesTotalRevenueExactlyWithBigDecimal() {
        SalesAnalysis result = analyzer.analyze(List.of(
                record("2026-01-01", "A", "상품1", 3, "0.10"),
                record("2026-01-02", "A", "상품2", 2, "10.25")));

        assertEquals(0, new BigDecimal("20.80").compareTo(result.totalRevenue()));
        assertEquals(5, result.totalQuantity());
    }

    @Test
    void aggregatesMonthlyRevenue() {
        SalesAnalysis result = analyzer.analyze(List.of(
                record("2026-01-01", "A", "상품", 2, "100"),
                record("2026-01-31", "A", "상품", 1, "50"),
                record("2026-02-01", "A", "상품", 3, "100")));

        assertMoney("250", result.monthlySales().get(YearMonth.of(2026, 1)).revenue());
        assertMoney("300", result.monthlySales().get(YearMonth.of(2026, 2)).revenue());
    }

    @Test
    void calculatesMonthOverMonthAmountAndRate() {
        SalesAnalysis result = analyzer.analyze(List.of(
                record("2026-01-01", "A", "상품", 1, "100"),
                record("2026-02-01", "A", "상품", 1, "150"),
                record("2026-03-01", "A", "상품", 1, "75")));

        var february = result.monthlySales().get(YearMonth.of(2026, 2));
        var march = result.monthlySales().get(YearMonth.of(2026, 3));
        assertMoney("50", february.changeAmount());
        assertMoney("50.00", february.changeRate());
        assertMoney("-75", march.changeAmount());
        assertMoney("-50.00", march.changeRate());
    }

    @Test
    void sortsTopFiveProductsByRevenueDescending() {
        SalesAnalysis result = analyzer.analyze(List.of(
                record("2026-01-01", "A", "상품F", 1, "100"),
                record("2026-01-01", "A", "상품B", 1, "500"),
                record("2026-01-01", "A", "상품D", 1, "300"),
                record("2026-01-01", "A", "상품A", 1, "600"),
                record("2026-01-01", "A", "상품E", 1, "200"),
                record("2026-01-01", "A", "상품C", 1, "400")));

        assertEquals(List.of("상품A", "상품B", "상품C", "상품D", "상품E"),
                result.topProducts().stream().map(SalesAnalysis.ProductSales::product).toList());
    }

    @Test
    void rejectsInvalidCsvAndReportsLineNumber(@TempDir Path directory) throws Exception {
        Path csv = directory.resolve("invalid.csv");
        Files.writeString(csv, """
                date,category,product,quantity,unit_price
                2026-01-01,전자기기,키보드,2,89000
                2026-02-30,전자기기,마우스,abc,32000
                """, StandardCharsets.UTF_8);

        CsvDataException exception = assertThrows(CsvDataException.class, () -> new CsvSalesReader().read(csv));
        assertTrue(exception.getMessage().contains("3행"));
    }

    private SalesRecord record(String date, String category, String product, int quantity, String price) {
        return new SalesRecord(LocalDate.parse(date), category, product, quantity, new BigDecimal(price));
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}

