package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.RecommendedItemUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.RecommendedItemMapper;
import io.github.parqueubajara.api.model.RecommendedItem;
import io.github.parqueubajara.api.repository.RecommendedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendedItemService {

    private final RecommendedItemRepository repository;
    private final RecommendedItemMapper mapper;

    @Transactional(readOnly = true)
    public Page<RecommendedItem> findAll(Pageable pageable, Boolean active, Boolean featured) {
        if (active != null && featured != null) return repository.findByActiveAndFeatured(active, featured, pageable);
        if (active != null) return repository.findByActive(active, pageable);
        if (featured != null) return repository.findByFeatured(featured, pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<RecommendedItem> findByIdOptional(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public RecommendedItem findById(UUID id) {
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item recomendado de ID: " + id + " não encontrado"));
    }

    @Transactional
    public RecommendedItem save(RecommendedItem item) {
        return repository.save(item);
    }

    @Transactional
    public void update(UUID id, RecommendedItemUpdateDTO dto) {
        RecommendedItem item = findById(id);
        mapper.updateEntityFromDto(dto, item);
        repository.save(item);
    }

    @Transactional
    public void delete(UUID id) {
        RecommendedItem item = findById(id);
        repository.delete(item);
    }
}
