package com.example.fileorganizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public final class FileOrganizerTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-14T03:04:05Z"),
            ZoneId.of("Asia/Seoul"));

    private FileOrganizerTest() {
    }

    public static void main(String[] args) throws Exception {
        Path buildDirectory = Path.of("build");
        Files.createDirectories(buildDirectory);
        Path workspace = Files.createTempDirectory(buildDirectory, "file-organizer-test-");

        try {
            previewDoesNotMoveAndExecuteSortsTenFiles(workspace);
            System.out.println("PASS: 미리보기, 10개 파일 분류, 중복 이름, 날짜별 로그 검증");
        } finally {
            deleteRecursively(workspace);
        }
    }

    private static void previewDoesNotMoveAndExecuteSortsTenFiles(Path workspace) throws Exception {
        Path input = Files.createDirectories(workspace.resolve("input"));
        Path documents = Files.createDirectories(workspace.resolve("documents"));

        List<String> names = List.of(
                "report.pdf", "manual.PDF", "invoice.pdf",
                "photo.jpg", "logo.PNG", "diagram.svg", "banner.webp",
                "sales.csv", "users.CSV", "inventory.csv");
        for (String name : names) {
            write(input.resolve(name), "sample:" + name);
        }
        write(documents.resolve("report.pdf"), "original-report");

        FileOrganizer organizer = new FileOrganizer(workspace, FIXED_CLOCK);
        FileOrganizer.RunSummary preview = organizer.organize(true);

        check(preview.preview(), "미리보기 플래그가 설정되어야 합니다.");
        check(preview.classified() == 10, "미리보기에서 10개가 분류되어야 합니다.");
        check(preview.failed() == 0, "미리보기에 실패가 없어야 합니다.");
        check(countFiles(input) == 10, "미리보기에서 input 파일이 이동하면 안 됩니다.");
        check(preview.results().stream().anyMatch(result ->
                        result.target() != null && result.target().getFileName().toString().equals("report (1).pdf")),
                "중복 파일은 report (1).pdf로 계획되어야 합니다.");

        FileOrganizer.RunSummary executed = organizer.organize(false);

        check(!executed.preview(), "실행 모드여야 합니다.");
        check(executed.classified() == 10, "실행에서 10개가 분류되어야 합니다.");
        check(executed.failed() == 0, "실행 실패가 없어야 합니다.");
        check(countFiles(input) == 0, "실행 뒤 input 폴더는 비어 있어야 합니다.");
        check(countFiles(workspace.resolve("documents")) == 4, "기존 파일 포함 PDF 4개여야 합니다.");
        check(countFiles(workspace.resolve("images")) == 4, "이미지 4개여야 합니다.");
        check(countFiles(workspace.resolve("data")) == 3, "CSV 3개여야 합니다.");
        check(read(documents.resolve("report.pdf")).equals("original-report"), "기존 파일을 덮어쓰면 안 됩니다.");
        check(read(documents.resolve("report (1).pdf")).equals("sample:report.pdf"), "중복 파일 내용이 보존되어야 합니다.");

        Path log = workspace.resolve("logs/2026-11-14.log");
        check(Files.exists(log), "날짜별 로그가 생성되어야 합니다.");
        String logText = read(log);
        check(logText.contains("PREVIEW 시작"), "미리보기 실행이 로그에 있어야 합니다.");
        check(logText.contains("EXECUTE 시작"), "실제 실행이 로그에 있어야 합니다.");
        check(logText.contains("report (1).pdf"), "중복 파일 대상이 로그에 있어야 합니다.");
    }

    private static long countFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            List<Path> entries = paths.sorted((left, right) -> right.compareTo(left)).toList();
            for (Path entry : entries) {
                Files.delete(entry);
            }
        }
    }
}

