package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.model.PageConfig;
import io.github.parqueubajara.api.repository.PageConfigRepository;
import io.github.parqueubajara.api.service.infra.FileValidationService;
import io.github.parqueubajara.api.service.infra.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageConfigService {

    private final PageConfigRepository repository;
    private final S3StorageService storageService;
    private final FileValidationService validationService;

    @Transactional(readOnly = true)
    public Optional<PageConfig> findByKey(String pageKey) {
        return repository.findById(pageKey);
    }

    @Transactional
    public String uploadCoverImage(String pageKey, MultipartFile file) throws IOException {
        validationService.validateImage(file);
        String storageKey = storageService.upload(file);
        String imageUrl = storageService.generateUrl(storageKey);

        PageConfig config = repository.findById(pageKey)
                .orElse(new PageConfig(pageKey, null, null));
        config.setImageUrl(imageUrl);
        repository.save(config);

        return imageUrl;
    }

    @Transactional
    public void removeImage(String pageKey) {
        repository.findById(pageKey).ifPresent(config -> {
            config.setImageUrl(null);
            repository.save(config);
        });
    }

    @Transactional
    public void updateDescription(String pageKey, String description) {
        PageConfig config = repository.findById(pageKey)
                .orElse(new PageConfig(pageKey, null, null));
        config.setDescription(description);
        repository.save(config);
    }
}
