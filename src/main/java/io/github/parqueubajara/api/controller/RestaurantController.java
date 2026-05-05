package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.RestaurantRequestDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.response.RestaurantResponseDTO;
import io.github.parqueubajara.api.dto.update.RestaurantUpdateDTO;
import io.github.parqueubajara.api.mapper.RestaurantMapper;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.service.PhotoService;
import io.github.parqueubajara.api.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Gerenciamento dos restaurantes")
public class RestaurantController implements GenericController {

    private final RestaurantService service;
    private final RestaurantMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getById(@PathVariable UUID id){
        Restaurant restaurant = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(restaurant));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponseDTO>> getAll(@PageableDefault(size = 10)Pageable pageable){
        Page<Restaurant> pageEntity = service.findAll(pageable);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponseDTO> save(@RequestBody @Valid RestaurantRequestDTO requestDTO){
        Restaurant restaurant = mapper.toEntity(requestDTO);
        service.save(restaurant);
        URI location = generateHeaderLocation(restaurant.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(restaurant));
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForRestaurant(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody RestaurantUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
