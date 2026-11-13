package com.example.sales;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class SampleDataGenerator {
    private static final List<Product> PRODUCTS = List.of(
            new Product("전자기기", "기계식 키보드", 89_000),
            new Product("전자기기", "노트북 거치대", 45_000),
            new Product("전자기기", "무선 마우스", 32_000),
            new Product("사무용품", "데스크 정리함", 19_000),
            new Product("사무용품", "볼펜 세트", 8_500),
            new Product("사무용품", "프리미엄 노트", 4_500),
            new Product("생활용품", "LED 데스크 램프", 55_000),
            new Product("생활용품", "보온 텀블러", 28_000),
            new Product("생활용품", "여행용 파우치", 24_000));
    private static final int[] MONTH_FACTORS = {2, 3, 5, 4, 7, 6};

    private SampleDataGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("data", "sales.csv") : Path.of(args[0]);
        Path parent = output.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder csv = new StringBuilder("date,category,product,quantity,unit_price\n");
        int rows = 0;
        for (int month = 1; month <= 6; month++) {
            for (int productIndex = 0; productIndex < PRODUCTS.size(); productIndex++) {
                Product product = PRODUCTS.get(productIndex);
                for (int occurrence = 0; occurrence < 2; occurrence++) {
                    int day = 2 + productIndex * 2 + occurrence;
                    int quantity = MONTH_FACTORS[month - 1] + (productIndex % 3) * 2 + occurrence;
                    LocalDate date = LocalDate.of(2026, month, day);
                    csv.append(date).append(',')
                            .append(product.category()).append(',')
                            .append(product.name()).append(',')
                            .append(quantity).append(',')
                            .append(product.unitPrice()).append('\n');
                    rows++;
                }
            }
        }
        Files.writeString(output, csv.toString(), StandardCharsets.UTF_8);
        System.out.println("샘플 CSV 생성 완료: " + output.toAbsolutePath().normalize());
        System.out.println("데이터 행 수: " + rows);
    }

    private record Product(String category, String name, int unitPrice) {
    }
}

