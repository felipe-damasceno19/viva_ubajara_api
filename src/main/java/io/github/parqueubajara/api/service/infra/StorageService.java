package io.github.parqueubajara.api.service.infra;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String upload(MultipartFile file) throws IOException;
    void delete(String storageKey);
    String generateUrl(String storageKey);
}
