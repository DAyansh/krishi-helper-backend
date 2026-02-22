package com.ayansh.Backend.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;


public interface FileStorageService {
    Map<String,String> upload(MultipartFile file, String keyPrefix) throws IOException;
    void delete(String fileUrl) throws IOException;
}
