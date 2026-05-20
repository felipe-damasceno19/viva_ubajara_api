package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.model.GastronomyItem;
import io.github.parqueubajara.api.repository.GastronomyItemRepository;
import io.github.parqueubajara.api.service.infra.FileValidationService;
import io.github.parqueubajara.api.service.infra.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GastronomyItemService {

    private final GastronomyItemRepository repository;
    private final S3StorageService storageService;
    private final FileValidationService validationService;

    @Transactional(readOnly = true)
    public List<GastronomyItem> findAllActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<GastronomyItem> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public GastronomyItem findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item gastronômico de ID: " + id + " não encontrado"));
    }

    @Transactional
    public GastronomyItem save(GastronomyItem item) {
        return repository.save(item);
    }

    @Transactional
    public void update(UUID id, String name, Integer displayOrder, Boolean active) {
        GastronomyItem item = findById(id);
        if (name != null) item.setName(name);
        if (displayOrder != null) item.setDisplayOrder(displayOrder);
        if (active != null) item.setActive(active);
        repository.save(item);
    }

    @Transactional
    public String uploadImage(UUID id, MultipartFile file) throws IOException {
        validationService.validateImage(file);
        GastronomyItem item = findById(id);
        String storageKey = storageService.upload(file);
        String imageUrl = storageService.generateUrl(storageKey);
        item.setImageUrl(imageUrl);
        repository.save(item);
        return imageUrl;
    }

    @Transactional
    public void removeImage(UUID id) {
        GastronomyItem item = findById(id);
        item.setImageUrl(null);
        repository.save(item);
    }

    @Transactional
    public void delete(UUID id) {
        GastronomyItem item = findById(id);
        repository.delete(item);
    }
}
