/*package com.ayansh.Backend.Service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.model.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Service
@Profile("prod")
public class S3FileStorageService implements FileStorageService {

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region:ap-south-1}")
    private String regionName;

    @Value("${app.s3.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.upload.max-file-size-bytes:5242880}") // default 5MB
    private long maxFileSize;

    private S3Client s3;

    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    ));

    @PostConstruct
    private void init() {
        Region region = Region.of(regionName);
        this.s3 = S3Client.builder().region(region).build();
    }

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

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(original);
        ext = ext == null || ext.isBlank() ? "" : "." + ext;

        String filename = UUID.randomUUID().toString() + ext;
        String safePrefix = (keyPrefix == null || keyPrefix.isBlank()) ? "" : keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");
        String key = safePrefix.isEmpty() ? filename : safePrefix + "/" + filename;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ) // make public; alternatively keep private and use signed URLs
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        try {
            s3.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload to S3: " + e.awsErrorDetails().errorMessage(), e);
        }

        // Build public URL
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length()-1) : publicBaseUrl;
            return base + "/" + key;
        } else {
            // Virtual-hosted style
            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, regionName, key);
            return url;
        }
    }

    @Override
    public void delete(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String key = null;
        // If publicBaseUrl is set and fileUrl startsWith it -> strip
        if (publicBaseUrl != null && !publicBaseUrl.isBlank() && fileUrl.startsWith(publicBaseUrl)) {
            key = fileUrl.substring(publicBaseUrl.length());
            if (key.startsWith("/")) key = key.substring(1);
        } else {
            // Try to parse virtual-hosted style: https://{bucket}.s3.{region}.amazonaws.com/{key}
            String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, regionName);
            if (fileUrl.startsWith(prefix)) {
                key = fileUrl.substring(prefix.length());
            } else {
                // Fallback: try path-style or other patterns
                // naive attempt: everything after the first occurrence of bucket + "/"
                String marker = bucket + "/";
                int idx = fileUrl.indexOf(marker);
                if (idx >= 0) {
                    key = fileUrl.substring(idx + marker.length());
                }
            }
        }

        if (key == null || key.isBlank()) {
            // Could not determine key — do nothing or log
            return;
        }

        DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try {
            s3.deleteObject(delReq);
        } catch (S3Exception e) {
            throw new IOException("Failed to delete S3 object: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}*/
