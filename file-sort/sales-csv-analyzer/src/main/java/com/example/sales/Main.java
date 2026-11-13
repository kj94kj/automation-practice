package com.example.sales;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.help()) {
                printHelp();
                return;
            }
            var records = new CsvSalesReader().read(options.input());
            SalesAnalysis analysis = new SalesAnalyzer().analyze(records);
            new MarkdownReportWriter().write(analysis, options.output());

            System.out.println("매출 분석을 완료했습니다.");
            System.out.println("입력: " + options.input().toAbsolutePath().normalize());
            System.out.println("행 수: " + analysis.rowCount());
            System.out.println("전체 판매 수량: " + analysis.totalQuantity());
            System.out.println("전체 매출액: " + analysis.totalRevenue().toPlainString() + "원");
            System.out.println("보고서: " + options.output().toAbsolutePath().normalize());
        } catch (IllegalArgumentException | IOException exception) {
            System.err.println("오류: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("사용법: Main [--input CSV경로] [--output 보고서경로]");
        System.out.println("기본 입력: data/sales.csv");
        System.out.println("기본 출력: reports/sales-report.md");
    }

    private record Options(Path input, Path output, boolean help) {
        private static Options parse(String[] args) {
            Path input = Path.of("data", "sales.csv");
            Path output = Path.of("reports", "sales-report.md");
            boolean help = false;

            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--input" -> {
                        if (++index >= args.length) {
                            throw new IllegalArgumentException("--input 뒤에 경로가 필요합니다.");
                        }
                        input = Path.of(args[index]);
                    }
                    case "--output" -> {
                        if (++index >= args.length) {
                            throw new IllegalArgumentException("--output 뒤에 경로가 필요합니다.");
                        }
                        output = Path.of(args[index]);
                    }
                    case "--help", "-h" -> help = true;
                    default -> throw new IllegalArgumentException("알 수 없는 옵션: " + args[index]);
                }
            }
            return new Options(input, output, help);
        }
    }
}

