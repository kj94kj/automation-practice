package com.example.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.SortedMap;

public record SalesAnalysis(
        LocalDate startDate,
        LocalDate endDate,
        int rowCount,
        long totalQuantity,
        BigDecimal totalRevenue,
        SortedMap<YearMonth, MonthlySales> monthlySales,
        List<ProductSales> topProducts,
        List<CategorySales> categorySales) {

    public record MonthlySales(
            YearMonth month,
            long quantity,
            BigDecimal revenue,
            BigDecimal changeAmount,
            BigDecimal changeRate) {
    }

    public record ProductSales(String product, long quantity, BigDecimal revenue) {
    }

    public record CategorySales(String category, long quantity, BigDecimal revenue, BigDecimal share) {
    }
}

