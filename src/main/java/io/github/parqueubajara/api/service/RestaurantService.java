package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.RestaurantUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.RestaurantMapper;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository repository;
    private final RestaurantMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Restaurant> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Restaurant findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante de ID: "+id+" encontrado!"));
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    @Transactional
    public Restaurant save(Restaurant restaurant){
        if (restaurant.getEmail() != null && !restaurant.getEmail().trim().isEmpty()) {
            if (repository.existsByEmail(restaurant.getEmail())) {
                throw new RuntimeException("E-mail já cadastrado");
            }
        }
        return repository.save(restaurant);
    }

    @Transactional
    public void update(UUID id, RestaurantUpdateDTO updateDTO){
        Restaurant restaurant = findById(id);
        mapper.updateEntityFromDto(updateDTO, restaurant);
    }

    @Transactional
    public void delete(UUID id){
        Restaurant restaurant = findById(id);
        repository.delete(restaurant);
    }
}
