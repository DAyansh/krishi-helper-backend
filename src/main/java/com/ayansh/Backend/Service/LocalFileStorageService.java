package com.ayansh.Backend.Service;

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
import java.util.*;
import java.nio.file.*;


@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Value("${app.upload.max-file-size-bytes:5242880}")
    private long maxFileSize;

    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    ));

    @Override
    public Map<String, String> upload(MultipartFile file, String keyPrefix) throws IOException {

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

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(original);
        ext = ext == null || ext.isBlank() ? "" : "." + ext;

        String filename = UUID.randomUUID() + ext;

        String safePrefix = (keyPrefix == null || keyPrefix.isBlank())
                ? ""
                : keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");

        Path dir = Paths.get(uploadDir, safePrefix);
        Files.createDirectories(dir);

        Path dest = dir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = safePrefix.isEmpty()
                ? filename
                : safePrefix + "/" + filename;

        String url = baseUrl.endsWith("/")
                ? baseUrl + relativePath
                : baseUrl + "/" + relativePath;


        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        response.put("publicId", relativePath);

        return response;
    }

    @Override
    public void delete(String publicId) throws IOException {

        if (publicId == null || publicId.isBlank()) return;

        Path path = Paths.get(uploadDir, publicId);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IOException("Failed to delete file: " + path, e);
        }
    }
}
