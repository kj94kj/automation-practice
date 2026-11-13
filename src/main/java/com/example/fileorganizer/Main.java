package com.example.fileorganizer;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.help()) {
                printUsage();
                return;
            }

            FileOrganizer.RunSummary summary = new FileOrganizer(options.baseDirectory())
                    .organize(options.preview());
            printSummary(summary);
            if (summary.failed() > 0) {
                System.exit(2);
            }
        } catch (IllegalArgumentException | IOException exception) {
            System.err.println("오류: " + exception.getMessage());
            System.err.println("사용법은 --help로 확인하세요.");
            System.exit(1);
        }
    }

    private static void printSummary(FileOrganizer.RunSummary summary) {
        System.out.println(summary.preview() ? "[미리보기 모드] 실제 파일은 이동하지 않습니다." : "[실행 모드]");
        for (FileOrganizer.MoveResult result : summary.results()) {
            if (result.target() == null) {
                System.out.printf("%-9s %s (%s)%n", result.status(), result.source().getFileName(), result.message());
            } else {
                System.out.printf("%-9s %s -> %s%n",
                        result.status(),
                        result.source().getFileName(),
                        result.target());
            }
        }
        System.out.printf("결과: 분류 %d개, 건너뜀 %d개, 실패 %d개%n",
                summary.classified(), summary.skipped(), summary.failed());
        System.out.println("로그: " + summary.logFile().toAbsolutePath());
    }

    private static void printUsage() {
        System.out.println("파일 자동 분류기");
        System.out.println("사용법: java ... Main [--preview | --execute] [--base-dir 경로]");
        System.out.println("  --preview       이동 결과만 표시합니다(기본값).");
        System.out.println("  --execute       input의 지원 파일을 실제로 이동합니다.");
        System.out.println("  --base-dir PATH input 폴더가 있는 기준 경로입니다(기본값: 현재 경로).");
        System.out.println("  --help          도움말을 표시합니다.");
    }

    private record Options(Path baseDirectory, boolean preview, boolean help) {
        private static Options parse(String[] args) {
            Path baseDirectory = Path.of(".");
            boolean preview = true;
            boolean modeSpecified = false;
            boolean help = false;

            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--preview" -> {
                        if (modeSpecified && !preview) {
                            throw new IllegalArgumentException("--preview와 --execute는 함께 사용할 수 없습니다.");
                        }
                        preview = true;
                        modeSpecified = true;
                    }
                    case "--execute" -> {
                        if (modeSpecified && preview) {
                            throw new IllegalArgumentException("--preview와 --execute는 함께 사용할 수 없습니다.");
                        }
                        preview = false;
                        modeSpecified = true;
                    }
                    case "--base-dir" -> {
                        if (++index >= args.length) {
                            throw new IllegalArgumentException("--base-dir 뒤에 경로가 필요합니다.");
                        }
                        try {
                            baseDirectory = Path.of(args[index]);
                        } catch (InvalidPathException exception) {
                            throw new IllegalArgumentException("올바르지 않은 경로입니다: " + args[index]);
                        }
                    }
                    case "--help", "-h" -> help = true;
                    default -> throw new IllegalArgumentException("알 수 없는 옵션: " + args[index]);
                }
            }
            return new Options(baseDirectory, preview, help);
        }
    }
}

