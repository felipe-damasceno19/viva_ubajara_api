package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.AirportUpdateDTO;
import io.github.parqueubajara.api.exception.DuplicateRegistry;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.AirportMapper;
import io.github.parqueubajara.api.model.Airport;
import io.github.parqueubajara.api.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository repository;
    private final AirportMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Airport> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Airport findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de ID: "+ id +" não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Airport> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    @Transactional
    public Airport save(Airport airport){
        if (repository.existsByIataCode(airport.getIataCode())) {
            throw new DuplicateRegistry("Aeroporto de codigo IATA: "+airport.getIataCode()+" já registrado");
        }
        return repository.save(airport);
    }

    @Transactional
    public void update(UUID id, AirportUpdateDTO updateDTO){
        Airport airport = findById(id);
        mapper.updateEntityFromDto(updateDTO, airport);
        repository.save(airport);
    }

    @Transactional
    public void delete(UUID id){
        Airport airport = findById(id);
        repository.delete(airport);
    }

}
