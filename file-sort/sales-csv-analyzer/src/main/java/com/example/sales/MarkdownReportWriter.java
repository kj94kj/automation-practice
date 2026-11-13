package com.example.sales;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Locale;

public final class MarkdownReportWriter {
    public void write(SalesAnalysis analysis, Path outputPath) throws IOException {
        Path parent = outputPath.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, render(analysis), StandardCharsets.UTF_8);
    }

    public String render(SalesAnalysis analysis) {
        StringBuilder report = new StringBuilder();
        report.append("# 매출 분석 보고서\n\n");
        report.append("- 분석 기간: ").append(analysis.startDate()).append(" ~ ").append(analysis.endDate()).append("\n");
        report.append("- 원본 데이터: ").append(number(analysis.rowCount())).append("행\n");
        report.append("- 보고서 생성일: ").append(LocalDate.now()).append("\n\n");

        report.append("## 전체 매출 요약\n\n");
        report.append("| 항목 | 결과 |\n|---|---:|\n");
        report.append("| 전체 매출액 | ").append(won(analysis.totalRevenue())).append(" |\n");
        report.append("| 전체 판매 수량 | ").append(number(analysis.totalQuantity())).append("개 |\n");
        report.append("| 분석 행 수 | ").append(number(analysis.rowCount())).append("행 |\n\n");

        report.append("## 월별 매출 분석\n\n");
        report.append("| 월 | 판매 수량 | 매출액 | 전월 대비 증감액 | 증감률 |\n");
        report.append("|---|---:|---:|---:|---:|\n");
        for (SalesAnalysis.MonthlySales month : analysis.monthlySales().values()) {
            report.append("| ").append(month.month()).append(" | ")
                    .append(number(month.quantity())).append("개 | ")
                    .append(won(month.revenue())).append(" | ")
                    .append(month.changeAmount() == null ? "-" : signedWon(month.changeAmount())).append(" | ")
                    .append(month.changeRate() == null ? "-" : signedPercent(month.changeRate())).append(" |\n");
        }

        report.append("\n## 상품 매출 TOP 5\n\n");
        report.append("| 순위 | 상품 | 판매 수량 | 매출액 | 전체 매출 비중 |\n");
        report.append("|---:|---|---:|---:|---:|\n");
        for (int index = 0; index < analysis.topProducts().size(); index++) {
            SalesAnalysis.ProductSales product = analysis.topProducts().get(index);
            report.append("| ").append(index + 1).append(" | ").append(escape(product.product())).append(" | ")
                    .append(number(product.quantity())).append("개 | ")
                    .append(won(product.revenue())).append(" | ")
                    .append(percent(product.revenue(), analysis.totalRevenue())).append(" |\n");
        }

        report.append("\n## 카테고리별 매출 비중\n\n");
        report.append("| 카테고리 | 판매 수량 | 매출액 | 매출 비중 |\n");
        report.append("|---|---:|---:|---:|\n");
        for (SalesAnalysis.CategorySales category : analysis.categorySales()) {
            report.append("| ").append(escape(category.category())).append(" | ")
                    .append(number(category.quantity())).append("개 | ")
                    .append(won(category.revenue())).append(" | ")
                    .append(formatPercent(category.share())).append(" |\n");
        }

        report.append("\n## 데이터 기반 분석 요약\n\n");
        for (String summary : summaries(analysis)) {
            report.append("- ").append(summary).append("\n");
        }
        return report.toString();
    }

    private java.util.List<String> summaries(SalesAnalysis analysis) {
        var summaries = new java.util.ArrayList<String>();
        SalesAnalysis.MonthlySales bestMonth = analysis.monthlySales().values().stream()
                .max(Comparator.comparing(SalesAnalysis.MonthlySales::revenue)).orElseThrow();
        summaries.add("최고 매출월은 **" + bestMonth.month() + "**이며 매출액은 **" + won(bestMonth.revenue()) + "**입니다.");

        analysis.monthlySales().values().stream()
                .filter(month -> month.changeAmount() != null)
                .max(Comparator.comparing(SalesAnalysis.MonthlySales::changeAmount))
                .ifPresent(month -> summaries.add("전월 대비 매출이 가장 크게 증가한 달은 **" + month.month()
                        + "**로, **" + signedWon(month.changeAmount()) + " (" + signedPercent(month.changeRate()) + ")** 증가했습니다."));

        analysis.monthlySales().values().stream()
                .filter(month -> month.changeAmount() != null && month.changeAmount().signum() < 0)
                .min(Comparator.comparing(SalesAnalysis.MonthlySales::changeAmount))
                .ifPresent(month -> summaries.add("가장 큰 매출 감소는 **" + month.month() + "**에 발생했으며 전월 대비 **"
                        + signedWon(month.changeAmount()) + " (" + signedPercent(month.changeRate()) + ")**입니다."));

        if (!analysis.topProducts().isEmpty()) {
            SalesAnalysis.ProductSales product = analysis.topProducts().get(0);
            summaries.add("상품 매출 1위는 **" + escape(product.product()) + "**이며 전체 매출의 **"
                    + percent(product.revenue(), analysis.totalRevenue()) + "**를 차지합니다.");
        }
        if (!analysis.categorySales().isEmpty()) {
            SalesAnalysis.CategorySales category = analysis.categorySales().get(0);
            summaries.add("매출 기여도가 가장 높은 카테고리는 **" + escape(category.category()) + "**로 비중은 **"
                    + formatPercent(category.share()) + "**입니다.");
        }
        return summaries;
    }

    private String won(BigDecimal value) {
        return number(value.setScale(0, RoundingMode.HALF_UP)) + "원";
    }

    private String signedWon(BigDecimal value) {
        return (value.signum() > 0 ? "+" : "") + won(value);
    }

    private String signedPercent(BigDecimal value) {
        return (value.signum() > 0 ? "+" : "") + formatPercent(value);
    }

    private String percent(BigDecimal value, BigDecimal total) {
        if (total.signum() == 0) {
            return "0.00%";
        }
        return formatPercent(value.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
    }

    private String formatPercent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String number(long value) {
        return NumberFormat.getIntegerInstance(Locale.KOREA).format(value);
    }

    private String number(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.KOREA);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(value);
    }

    private String escape(String value) {
        return value.replace("|", "\\|");
    }
}

