package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.PhotoMapper;
import io.github.parqueubajara.api.model.*;
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

    @Transactional(readOnly = true)
    public Optional<Photo> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Photo findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo de ID: "+ id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PhotoResponseDTO upload(MultipartFile file, String description,
                                    Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = instantiatePhotoTemplate(description, url, storageKey, displayOrder);
        repository.save(photo);

        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForEvent(UUID eventId, MultipartFile file,
                                           String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        Event event = eventService.findById(eventId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setEvent(event);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForAirport(UUID airportId, MultipartFile file,
                                           String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        Airport airport = airportService.findById(airportId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setAirport(airport);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForAttraction(UUID attractionId, MultipartFile file,
                                             String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        Attraction attraction = attractionService.findById(attractionId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setAttraction(attraction);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForHostPoint(UUID hostPointId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        HostPoint hostPoint = hostPointService.findById(hostPointId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setHostPoint(hostPoint);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForRestaurant(UUID restaurantId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        Restaurant restaurant = restaurantService.findById(restaurantId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setRestaurant(restaurant);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForTourGuide(UUID tourGuideId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        TourGuide tourGuide = tourGuideService.findById(tourGuideId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setTourGuide(tourGuide);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    public PhotoResponseDTO uploadForTouristSpot(UUID touristSpotId, MultipartFile file,
                                                String description, Integer displayOrder) throws IOException {
        validationService.validateImage(file);

        TouristSpot touristSpot = touristSpotService.findById(touristSpotId);

        String url = storageService.upload(file);
        String storageKey = extractStorageKey(url);

        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDescription(description);
        photo.setDisplayOrder(displayOrder);
        photo.setTouristSpot(touristSpot);

        repository.save(photo);
        return mapper.toResponseDTO(photo);
    }

    @Transactional
    public void delete(UUID id){
        Photo photo = findById(id);
        storageService.delete(photo.getStorageKey());
        repository.delete(photo);
    }

    private Photo instantiatePhotoTemplate(String description, String url
            , String storageKey, Integer displayOrder){
        Photo photo = new Photo();
        photo.setDescription(description);
        photo.setUrl(url);
        photo.setStorageKey(storageKey);
        photo.setDisplayOrder(displayOrder);

        return photo;
    }

    private String extractStorageKey(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}
