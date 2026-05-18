package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.TourGuideUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.TourGuideMapper;
import io.github.parqueubajara.api.model.TourGuide;
import io.github.parqueubajara.api.repository.TourGuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourGuideService {

    private final TourGuideRepository repository;
    private final TourGuideMapper mapper;

    @Transactional(readOnly = true)
    public Optional<TourGuide> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public TourGuide findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guia de ID: "+ id +" não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<TourGuide> findAll(Pageable pageable, Boolean active){
        if (active != null) {
            return repository.findByActive(active, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public TourGuide save(TourGuide guide){
        if (guide.getEmail() != null && !guide.getEmail().trim().isEmpty()){
            if (repository.existsByEmail(guide.getEmail())) {
                throw new RuntimeException("E-mail já cadastrado");
            }
        }
        return repository.save(guide);
    }

    @Transactional
    public void update(UUID id, TourGuideUpdateDTO updateDTO){
        TourGuide guide = findById(id);
        mapper.updateEntityFromDto(updateDTO, guide);
        repository.save(guide);
    }

    @Transactional
    public void delete(UUID id){
        TourGuide guide = findById(id);
        repository.delete(guide);
    }
}
