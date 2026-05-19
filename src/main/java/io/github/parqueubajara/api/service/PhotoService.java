package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.update.PhotoUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.PhotoMapper;
import io.github.parqueubajara.api.model.*;
import io.github.parqueubajara.api.model.RecommendedItem;
import io.github.parqueubajara.api.repository.PhotoRepository;
import io.github.parqueubajara.api.service.infra.FileValidationService;
import io.github.parqueubajara.api.service.infra.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository repository;
    private final S3StorageService storageService;
    private final PhotoMapper mapper;
    private final FileValidationService validationService;
    private final EventService eventService;
    private final AirportService airportService;
    private final AttractionService attractionService;
    private final HostPointService hostPointService;
    private final RestaurantService restaurantService;
    private final TourGuideService tourGuideService;
    private final TouristSpotService touristSpotService;
    private final RecommendedItemService recommendedItemService;

    @Transactional(readOnly = true)
    public Optional<Photo> findByIdOptional(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Photo findById(UUID id) {
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photo de ID: " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public PhotoResponseDTO findByIdDTO(UUID id) {
        return mapper.toResponseDTO(findById(id), storageService);
    }

    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(photo -> mapper.toResponseDTO(photo, storageService))
                .toList();
    }

    @Transactional
    public PhotoResponseDTO upload(MultipartFile file, String description,
                                   Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForEvent(UUID eventId, MultipartFile file,
                                           String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setEvent(eventService.findById(eventId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForAirport(UUID airportId, MultipartFile file,
                                             String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setAirport(airportService.findById(airportId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForAttraction(UUID attractionId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTouristSpot(attractionService.findById(attractionId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForHostPoint(UUID hostPointId, MultipartFile file,
                                               String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTouristSpot(hostPointService.findById(hostPointId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForRestaurant(UUID restaurantId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTouristSpot(restaurantService.findById(restaurantId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForTourGuide(UUID tourGuideId, MultipartFile file,
                                               String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTourGuide(tourGuideService.findById(tourGuideId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForRecommendedItem(UUID itemId, MultipartFile file,
                                                     String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTouristSpot(recommendedItemService.findById(itemId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO uploadForTouristSpot(UUID touristSpotId, MultipartFile file,
                                                 String description, Integer displayOrder) throws IOException {
        String storageKey = uploadAndValidate(file);
        Photo photo = buildPhoto(storageKey, description, displayOrder);
        photo.setTouristSpot(touristSpotService.findById(touristSpotId));
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public PhotoResponseDTO update(UUID id, PhotoUpdateDTO dto) {
        Photo photo = findById(id);
        if (dto.description() != null) photo.setDescription(dto.description());
        if (dto.displayOrder() != null) photo.setDisplayOrder(dto.displayOrder());
        repository.save(photo);
        return mapper.toResponseDTO(photo, storageService);
    }

    @Transactional
    public void delete(UUID id) {
        Photo photo = findById(id);
        storageService.delete(photo.getStorageKey());
        repository.delete(photo);
    }

    private String uploadAndValidate(MultipartFile file) throws IOException {
        validationService.validateImage(file);
        return storageService.upload(file);
    }

    private Photo buildPhoto(String storageKey, String description, Integer displayOrder) {
        Photo photo = new Photo();
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        return photo;
    }
}
