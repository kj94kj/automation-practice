package com.example.sales;

import java.io.IOException;

public final class CsvDataException extends IOException {
    public CsvDataException(int lineNumber, String message) {
        super("CSV " + lineNumber + "행 오류: " + message);
    }
}

