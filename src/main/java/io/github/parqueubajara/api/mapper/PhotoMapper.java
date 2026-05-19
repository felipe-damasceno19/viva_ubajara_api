package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.model.*;
import io.github.parqueubajara.api.service.infra.StorageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(config = CentralMapperConfig.class)
public interface PhotoMapper {


    @Mapping(target = "ownerId", expression = "java(resolveOwnerId(photo))")
    @Mapping(target = "ownerType", expression = "java(resolveOwnerType(photo))")
    @Mapping(target = "url",
            expression = "java(storageService.generateUrl(photo.getStorageKey()))")
    PhotoResponseDTO toResponseDTO(Photo photo, @Context StorageService storageService);

    default UUID resolveOwnerId(Photo photo) {
        if (photo.getEvent() != null) return photo.getEvent().getId();
        if (photo.getTouristSpot() != null) return photo.getTouristSpot().getId();
        if (photo.getTourGuide() != null) return photo.getTourGuide().getId();
        if (photo.getAirport() != null) return photo.getAirport().getId();
        return null;
    }

    default String resolveOwnerType(Photo photo) {
        if (photo.getEvent() != null) return "EVENT";
        if (photo.getTourGuide() != null) return "TOUR_GUIDE";
        if (photo.getAirport() != null) return "AIRPORT";
        if (photo.getTouristSpot() != null) {
            TouristSpot spot = photo.getTouristSpot();
            if (spot instanceof HostPoint) return "HOST_POINT";
            if (spot instanceof Attraction) return "ATTRACTION";
            if (spot instanceof Restaurant) return "RESTAURANT";
            return "TOURIST_SPOT";
        }

        return null;
    }
}