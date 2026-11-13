package com.example.fileorganizer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Sorts files from an input directory into extension-based destination directories. */
public final class FileOrganizer {
    private static final Map<String, String> DESTINATIONS = Map.ofEntries(
            Map.entry("pdf", "documents"),
            Map.entry("csv", "data"),
            Map.entry("png", "images"),
            Map.entry("jpg", "images"),
            Map.entry("jpeg", "images"),
            Map.entry("gif", "images"),
            Map.entry("bmp", "images"),
            Map.entry("webp", "images"),
            Map.entry("svg", "images")
    );

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path baseDirectory;
    private final Clock clock;

    public FileOrganizer(Path baseDirectory) {
        this(baseDirectory, Clock.systemDefaultZone());
    }

    public FileOrganizer(Path baseDirectory, Clock clock) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        this.clock = clock;
    }

    public RunSummary organize(boolean preview) throws IOException {
        Path inputDirectory = baseDirectory.resolve("input");
        if (!Files.isDirectory(inputDirectory)) {
            throw new IOException("input 폴더가 없습니다: " + inputDirectory);
        }

        Path logsDirectory = baseDirectory.resolve("logs");
        Files.createDirectories(logsDirectory);
        Path logFile = logsDirectory.resolve(LocalDate.now(clock) + ".log");

        List<Path> inputFiles;
        try (var paths = Files.list(inputDirectory)) {
            inputFiles = paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        int classified = 0;
        int skipped = 0;
        int failed = 0;
        List<MoveResult> results = new ArrayList<>();
        Set<Path> reservedTargets = new HashSet<>();
        String mode = preview ? "PREVIEW" : "EXECUTE";

        try (BufferedWriter log = Files.newBufferedWriter(
                logFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writeLog(log, "=== " + mode + " 시작 | 기준 경로: " + baseDirectory + " ===");

            for (Path source : inputFiles) {
                String destinationName = destinationFor(source.getFileName().toString());
                if (destinationName == null) {
                    skipped++;
                    String message = "지원하지 않는 확장자";
                    results.add(new MoveResult(source, null, Status.SKIPPED, message));
                    writeLog(log, "SKIP | " + source.getFileName() + " | " + message);
                    continue;
                }

                Path destinationDirectory = baseDirectory.resolve(destinationName);
                Path target = uniqueTarget(destinationDirectory, source.getFileName().toString(), reservedTargets);
                reservedTargets.add(target.toAbsolutePath().normalize());

                if (preview) {
                    classified++;
                    results.add(new MoveResult(source, target, Status.PREVIEWED, "이동 예정"));
                    writeLog(log, "PREVIEW | " + source.getFileName() + " -> " + relative(target));
                    continue;
                }

                try {
                    Files.createDirectories(destinationDirectory);
                    Files.move(source, target);
                    classified++;
                    results.add(new MoveResult(source, target, Status.MOVED, "이동 완료"));
                    writeLog(log, "MOVED | " + source.getFileName() + " -> " + relative(target));
                } catch (FileAlreadyExistsException exception) {
                    failed++;
                    String message = "이동 직전 대상 파일이 생성되어 충돌함: " + relative(target);
                    results.add(new MoveResult(source, target, Status.FAILED, message));
                    writeLog(log, "FAILED | " + source.getFileName() + " | " + message);
                } catch (IOException exception) {
                    failed++;
                    String message = exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage();
                    results.add(new MoveResult(source, target, Status.FAILED, message));
                    writeLog(log, "FAILED | " + source.getFileName() + " | " + message);
                }
            }

            writeLog(log, "=== 완료 | 분류: " + classified + ", 건너뜀: " + skipped + ", 실패: " + failed + " ===");
        }

        return new RunSummary(preview, classified, skipped, failed, logFile, List.copyOf(results));
    }

    private Path uniqueTarget(Path destinationDirectory, String fileName, Set<Path> reservedTargets) {
        Path directTarget = destinationDirectory.resolve(fileName);
        if (isAvailable(directTarget, reservedTargets)) {
            return directTarget;
        }

        int dot = fileName.lastIndexOf('.');
        String stem = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot) : "";
        for (int sequence = 1; ; sequence++) {
            Path candidate = destinationDirectory.resolve(stem + " (" + sequence + ")" + extension);
            if (isAvailable(candidate, reservedTargets)) {
                return candidate;
            }
        }
    }

    private boolean isAvailable(Path target, Set<Path> reservedTargets) {
        Path normalized = target.toAbsolutePath().normalize();
        return !Files.exists(target) && !reservedTargets.contains(normalized);
    }

    private String relative(Path path) {
        return baseDirectory.relativize(path.toAbsolutePath().normalize()).toString();
    }

    private void writeLog(BufferedWriter log, String message) throws IOException {
        log.write(LocalDateTime.now(clock).format(TIMESTAMP));
        log.write(" | ");
        log.write(message);
        log.newLine();
        log.flush();
    }

    private static String destinationFor(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return DESTINATIONS.get(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    public enum Status {
        PREVIEWED,
        MOVED,
        SKIPPED,
        FAILED
    }

    public record MoveResult(Path source, Path target, Status status, String message) {
    }

    public record RunSummary(
            boolean preview,
            int classified,
            int skipped,
            int failed,
            Path logFile,
            List<MoveResult> results) {
    }
}

