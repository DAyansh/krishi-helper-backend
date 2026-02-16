/*package com.ayansh.Backend.Service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir; // absolute path, e.g. /var/www/app/uploads or /tmp/uploads

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;   // base URL to serve uploaded files in dev

    @Value("${app.upload.max-file-size-bytes:5242880}") // default 5MB
    private long maxFileSize;

    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    ));

    @Override
    public String upload(MultipartFile file, String keyPrefix) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File exceeds max allowed size of " + maxFileSize + " bytes");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type: " + contentType);
        }

        // Safe extension extraction
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(original);
        ext = ext == null || ext.isBlank() ? "" : "." + ext;

        // Unique filename
        String filename = UUID.randomUUID().toString() + ext;

        // Key prefix -> directory structure, normalize
        String safePrefix = (keyPrefix == null || keyPrefix.isBlank()) ? "" : keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");
        Path dir = Paths.get(uploadDir, safePrefix);
        Files.createDirectories(dir);

        Path dest = dir.resolve(filename);

        // Stream to disk
        try (InputStream in = file.getInputStream()) {
            // Use REPLACE_EXISTING to be deterministic (filename is UUID so collision unlikely)
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // Build public URL for dev server static mapping: baseUrl + "/" + safePrefix + "/" + filename
        String relativePath = safePrefix.isEmpty() ? filename : safePrefix + "/" + filename;
        String url = baseUrl.endsWith("/") ? baseUrl + relativePath : baseUrl + "/" + relativePath;
        return url;
    }

    @Override
    public void delete(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) return;
        // Try to strip baseUrl
        String prefix = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        if (fileUrl.startsWith(prefix)) {
            String relative = fileUrl.substring(prefix.length());
            if (relative.startsWith("/")) relative = relative.substring(1);
            Path path = Paths.get(uploadDir, relative);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new IOException("Failed to delete file: " + path, e);
            }
        } else {
            // Not a local URL — ignore or log
        }
    }
}*/
