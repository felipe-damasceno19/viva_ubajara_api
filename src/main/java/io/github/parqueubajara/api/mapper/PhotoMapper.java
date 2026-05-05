package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.model.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(config = CentralMapperConfig.class)
public interface PhotoMapper {


    @Mapping(target = "ownerId", expression = "java(resolveOwnerId(photo))")
    @Mapping(target = "ownerType", expression = "java(resolveOwnerType(photo))")
    PhotoResponseDTO toResponseDTO(Photo photo);

    default UUID resolveOwnerId(Photo photo) {
        if (photo.getEvent() != null) return photo.getEvent().getId();
        if (photo.getTouristSpot() != null) return photo.getTouristSpot().getId();
        if (photo.getRestaurant() != null) return photo.getRestaurant().getId();
        if (photo.getAirport() != null) return photo.getAirport().getId();
        if (photo.getAttraction() != null) return photo.getAttraction().getId();
        if (photo.getHostPoint() != null) return photo.getHostPoint().getId();
        if (photo.getTourGuide() != null) return photo.getTourGuide().getId();
        return null;
    }

    default String resolveOwnerType(Photo photo) {
        if (photo.getEvent() != null) return "EVENT";
        if (photo.getTouristSpot() != null) return "TOURIST_SPOT";
        if (photo.getRestaurant() != null) return "RESTAURANT";
        if (photo.getAirport() != null) return "AIRPORT";
        if (photo.getAttraction() != null) return "ATTRACTION";
        if (photo.getHostPoint() != null) return "HOST_POINT";
        if (photo.getTourGuide() != null) return "TOUR_GUIDE";
        return null;
    }
}