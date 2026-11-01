package com.app.old_riders_bk.driver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Receives one driver document photo from the driver sign-up screen.
 * POST /api/v1/drivers/documents/upload  (multipart, field name "file")
 * Responds with {"url": "https://.../files/driver-documents/<name>"}.
 */
@RestController
@RequestMapping("/api/v1/drivers/documents")
public class DriverDocumentController {

    private static final long MAX_BYTES = 10L * 1024 * 1024;

    @Value("${app.upload-dir:uploads/driver-documents}")
    private String uploadDir;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file was sent."));
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("message", "The photo is larger than 10 MB."));
        }
        // Check the file name first: Flutter often sends application/octet-stream.
        String extension = extensionOf(file.getOriginalFilename(), file.getContentType());
        if (extension == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only JPG, PNG, WEBP or HEIC photos are allowed."));
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String fileName = UUID.randomUUID() + extension;
        Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok(Map.of("url", publicBaseUrl + "/files/driver-documents/" + fileName));
    }

    private static String extensionOf(String originalName, String contentType) {
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return ".jpg";
        if (name.endsWith(".png")) return ".png";
        if (name.endsWith(".webp")) return ".webp";
        if (name.endsWith(".heic") || name.endsWith(".heif")) return ".heic";

        if (contentType == null) return null;
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic", "image/heif" -> ".heic";
            default -> null;
        };
    }
}