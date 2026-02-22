package com.ayansh.Backend.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Profile("prod")
public class CloudinaryFileStorageService implements FileStorageService {

    @Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @Value("${cloudinary.api_key:}")
    private String apiKey;

    @Value("${cloudinary.api_secret:}")
    private String apiSecret;

    @Value("${app.upload.max-file-size-bytes:5242880}")
    private long maxFileSize;

    private Cloudinary cloudinary;

    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    ));

    @PostConstruct
    private void init() {

        if (System.getenv("CLOUDINARY_URL") != null && !System.getenv("CLOUDINARY_URL").isBlank()) {
            cloudinary = new Cloudinary(System.getenv("CLOUDINARY_URL"));
        } else {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
        }
    }

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
        ext = (ext == null || ext.isBlank()) ? "" : ("." + ext);

        String filename = UUID.randomUUID().toString();

        String safePrefix = (keyPrefix == null || keyPrefix.isBlank())
                ? ""
                : keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");

        String publicId = safePrefix.isEmpty()
                ? filename
                : safePrefix + "/" + filename;

        Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto",
                "overwrite", true
        );

        Map uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        } catch (Exception e) {
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }

        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl == null) {
            throw new IOException("Cloudinary did not return secure_url");
        }

        Map<String, String> response = new HashMap<>();
        response.put("url", secureUrl.toString());
        response.put("publicId", publicId);

        return response;
    }

    @Override
    public void delete(String publicId) throws IOException {

        if (publicId == null || publicId.isBlank()) return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete Cloudinary resource: " + e.getMessage(), e);
        }
    }
}
