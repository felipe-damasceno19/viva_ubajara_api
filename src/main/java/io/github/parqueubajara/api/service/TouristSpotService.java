package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.TouristSpotUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.TouristSpotMapper;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    private final TouristSpotRepository repository;
    private final TouristSpotMapper mapper;


    @Transactional(readOnly = true)
    public Optional<TouristSpot> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public TouristSpot findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto turístico de ID: "+id+" não encontrado!"));
    }

    @Transactional
    public TouristSpot save(TouristSpot spot){
        if (spot.getEmail()!= null && !spot.getEmail().trim().isEmpty()) {
            if (repository.existsByEmail(spot.getEmail())) {
                throw new RuntimeException("E-mail já cadastrado");
            }
        }
        return repository.save(spot);
    }

    @Transactional(readOnly = true)
    public Page<TouristSpot> findAll(Pageable pageable, Boolean active){
        if (active != null) return repository.findByActive(active, pageable);
        return repository.findAll(pageable);
    }

    @Transactional
    public void update(UUID id, TouristSpotUpdateDTO updateDTO){
        TouristSpot spot = findById(id);
        mapper.updateEntityFromDto(updateDTO, spot);
        repository.save(spot);
    }

    @Transactional
    public void delete(UUID id){
        TouristSpot spot = findById(id);
        repository.delete(spot);
    }


}
