package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.AttractionUpdateDTO;
import io.github.parqueubajara.api.exception.DuplicateEmailException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.AttractionMapper;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.enums.AttractionType;
import io.github.parqueubajara.api.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository repository;
    private final AttractionMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Attraction> findByIdOptional(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Attraction findById(UUID id) {
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atração com ID: " + id + " não encontrada!"));
    }

    @Transactional(readOnly = true)
    public Page<Attraction> findAll(Pageable pageable, AttractionType category, Boolean active){
        if (category != null && active != null) {
            return repository.findByParentIsNullAndCategoryAndActive(category, active, pageable);
        }
        if (category != null) {
            return repository.findByParentIsNullAndCategory(category, pageable);
        }
        if (active != null) {
            return repository.findByParentIsNullAndActive(active, pageable);
        }
        return repository.findByParentIsNull(pageable);
    }

    @Transactional
    public Attraction save(Attraction attraction) {
        if(attraction.getEmail() != null && !attraction.getEmail().trim().isEmpty()) {
            if(repository.existsByEmail(attraction.getEmail())){
                throw new DuplicateEmailException("E-mail já cadastrado");
            }
        }
        return repository.save(attraction);
    }

    @Transactional
    public Attraction addSubAttraction(UUID parentId, Attraction child) {
        Attraction parent = findById(parentId);
        child.setParent(parent);
        if (child.getEmail() != null && !child.getEmail().trim().isEmpty()) {
            if (repository.existsByEmail(child.getEmail())) {
                throw new DuplicateEmailException("E-mail já cadastrado");
            }
        }
        return repository.save(child);
    }

    @Transactional
    public void update(UUID id, AttractionUpdateDTO updateDTO){
        Attraction attraction = findById(id);
        mapper.updateEntityFromDto(updateDTO, attraction);
        repository.save(attraction);
    }

    @Transactional
    public void delete(UUID id){
        Attraction attraction = findById(id);
        repository.delete(attraction);
    }
}