package com.example.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SalesAnalyzer {
    public SalesAnalysis analyze(List<SalesRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("분석할 매출 데이터가 없습니다.");
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalQuantity = 0;
        var startDate = records.get(0).date();
        var endDate = records.get(0).date();
        Map<YearMonth, MutableSales> monthTotals = new TreeMap<>();
        Map<String, MutableSales> productTotals = new TreeMap<>();
        Map<String, MutableSales> categoryTotals = new TreeMap<>();

        for (SalesRecord record : records) {
            BigDecimal revenue = record.revenue();
            totalRevenue = totalRevenue.add(revenue);
            totalQuantity += record.quantity();
            if (record.date().isBefore(startDate)) {
                startDate = record.date();
            }
            if (record.date().isAfter(endDate)) {
                endDate = record.date();
            }
            add(monthTotals, YearMonth.from(record.date()), record.quantity(), revenue);
            add(productTotals, record.product(), record.quantity(), revenue);
            add(categoryTotals, record.category(), record.quantity(), revenue);
        }

        TreeMap<YearMonth, SalesAnalysis.MonthlySales> monthly = buildMonthly(
                YearMonth.from(startDate), YearMonth.from(endDate), monthTotals);
        List<SalesAnalysis.ProductSales> topProducts = productTotals.entrySet().stream()
                .map(entry -> new SalesAnalysis.ProductSales(
                        entry.getKey(), entry.getValue().quantity, entry.getValue().revenue))
                .sorted(Comparator.comparing(SalesAnalysis.ProductSales::revenue).reversed()
                        .thenComparing(SalesAnalysis.ProductSales::product))
                .limit(5)
                .toList();

        BigDecimal finalTotalRevenue = totalRevenue;
        List<SalesAnalysis.CategorySales> categories = categoryTotals.entrySet().stream()
                .map(entry -> new SalesAnalysis.CategorySales(
                        entry.getKey(),
                        entry.getValue().quantity,
                        entry.getValue().revenue,
                        percentage(entry.getValue().revenue, finalTotalRevenue)))
                .sorted(Comparator.comparing(SalesAnalysis.CategorySales::revenue).reversed()
                        .thenComparing(SalesAnalysis.CategorySales::category))
                .toList();

        return new SalesAnalysis(
                startDate,
                endDate,
                records.size(),
                totalQuantity,
                totalRevenue,
                Collections.unmodifiableSortedMap(monthly),
                List.copyOf(topProducts),
                List.copyOf(categories));
    }

    private TreeMap<YearMonth, SalesAnalysis.MonthlySales> buildMonthly(
            YearMonth firstMonth,
            YearMonth lastMonth,
            Map<YearMonth, MutableSales> totals) {
        TreeMap<YearMonth, SalesAnalysis.MonthlySales> result = new TreeMap<>();
        BigDecimal previousRevenue = null;

        for (YearMonth month = firstMonth; !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            MutableSales current = totals.getOrDefault(month, new MutableSales());
            BigDecimal changeAmount = previousRevenue == null ? null : current.revenue.subtract(previousRevenue);
            BigDecimal changeRate = previousRevenue == null ? null : percentage(changeAmount, previousRevenue);
            result.put(month, new SalesAnalysis.MonthlySales(
                    month, current.quantity, current.revenue, changeAmount, changeRate));
            previousRevenue = current.revenue;
        }
        return result;
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() == 0) {
            return null;
        }
        return numerator.multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private <T> void add(Map<T, MutableSales> map, T key, int quantity, BigDecimal revenue) {
        MutableSales aggregate = map.computeIfAbsent(key, ignored -> new MutableSales());
        aggregate.quantity += quantity;
        aggregate.revenue = aggregate.revenue.add(revenue);
    }

    private static final class MutableSales {
        private long quantity;
        private BigDecimal revenue = BigDecimal.ZERO;
    }
}

