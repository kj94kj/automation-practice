package com.example.sales;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class CsvSalesReader {
    private static final List<String> EXPECTED_HEADER =
            List.of("date", "category", "product", "quantity", "unit_price");

    public List<SalesRecord> read(Path csvPath) throws IOException {
        if (!Files.isRegularFile(csvPath)) {
            throw new IOException("CSV 파일이 없습니다: " + csvPath.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new CsvDataException(1, "헤더가 없습니다.");
        }

        String headerLine = stripBom(lines.get(0));
        List<String> header = parseCsvLine(headerLine, 1);
        if (!header.equals(EXPECTED_HEADER)) {
            throw new CsvDataException(1, "헤더는 date,category,product,quantity,unit_price여야 합니다.");
        }

        List<SalesRecord> records = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = lines.get(index);
            if (line.isBlank()) {
                throw new CsvDataException(lineNumber, "빈 행은 허용되지 않습니다.");
            }
            List<String> columns = parseCsvLine(line, lineNumber);
            if (columns.size() != EXPECTED_HEADER.size()) {
                throw new CsvDataException(lineNumber, "열 개수는 5개여야 하지만 " + columns.size() + "개입니다.");
            }
            records.add(toRecord(columns, lineNumber));
        }

        if (records.isEmpty()) {
            throw new CsvDataException(2, "분석할 데이터 행이 없습니다.");
        }
        return List.copyOf(records);
    }

    private SalesRecord toRecord(List<String> columns, int lineNumber) throws CsvDataException {
        LocalDate date;
        int quantity;
        BigDecimal unitPrice;

        try {
            date = LocalDate.parse(columns.get(0));
        } catch (DateTimeParseException exception) {
            throw new CsvDataException(lineNumber, "date는 YYYY-MM-DD 형식의 유효한 날짜여야 합니다.");
        }
        if (columns.get(1).isBlank()) {
            throw new CsvDataException(lineNumber, "category가 비어 있습니다.");
        }
        if (columns.get(2).isBlank()) {
            throw new CsvDataException(lineNumber, "product가 비어 있습니다.");
        }
        try {
            quantity = Integer.parseInt(columns.get(3));
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new CsvDataException(lineNumber, "quantity는 1 이상의 정수여야 합니다.");
        }
        try {
            unitPrice = new BigDecimal(columns.get(4));
            if (unitPrice.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new CsvDataException(lineNumber, "unit_price는 0 이상의 숫자여야 합니다.");
        }

        return new SalesRecord(date, columns.get(1), columns.get(2), quantity, unitPrice);
    }

    private List<String> parseCsvLine(String line, int lineNumber) throws CsvDataException {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new CsvDataException(lineNumber, "닫히지 않은 큰따옴표가 있습니다.");
        }
        values.add(value.toString().trim());
        return values;
    }

    private String stripBom(String value) {
        return value.startsWith("\uFEFF") ? value.substring(1) : value;
    }
}

