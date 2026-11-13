package com.example.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record SalesRecord(
        LocalDate date,
        String category,
        String product,
        int quantity,
        BigDecimal unitPrice) {

    public SalesRecord {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (category.isBlank()) {
            throw new IllegalArgumentException("category는 비어 있을 수 없습니다.");
        }
        if (product.isBlank()) {
            throw new IllegalArgumentException("product는 비어 있을 수 없습니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unit_price는 0 이상이어야 합니다.");
        }
    }

    public BigDecimal revenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

