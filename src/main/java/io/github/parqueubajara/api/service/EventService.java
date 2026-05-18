package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.EventUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.EventMapper;
import io.github.parqueubajara.api.model.Event;
import io.github.parqueubajara.api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Event> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Event findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento de ID: "+ id +" não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Event> findAll(Pageable pageable, LocalDateTime start, LocalDateTime end, Boolean active){
        if (active != null && start != null && end != null) {
            return repository.findByActiveAndStartDateBetween(active, start, end, pageable);
        }
        if (active != null) {
            return repository.findByActive(active, pageable);
        }
        if (start != null && end != null) {
            return repository.findByStartDateBetween(start, end, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public Event save(Event event){
        return repository.save(event);
    }

    @Transactional
    public void update(UUID id, EventUpdateDTO updateDTO){
        Event event = findById(id);
        mapper.updateEntityFromDto(updateDTO, event);
        repository.save(event);
    }

    @Transactional
    public void delete(UUID id){
        Event event = findById(id);
        repository.delete(event);
    }
}
