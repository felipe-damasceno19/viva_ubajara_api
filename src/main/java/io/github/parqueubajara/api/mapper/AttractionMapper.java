package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.AttractionRequestDTO;
import io.github.parqueubajara.api.dto.response.AttractionResponseDTO;
import io.github.parqueubajara.api.dto.response.TouristSpotSummaryDTO;
import io.github.parqueubajara.api.dto.update.AttractionUpdateDTO;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.TouristSpot;
import org.mapstruct.*;

@Mapper(config = CentralMapperConfig.class)
public interface AttractionMapper {

    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "linkedSpots", ignore = true)
    Attraction toEntity(AttractionRequestDTO requestDTO);

    @Mapping(target = "subAttractions", source = "subAttractions")
    @Mapping(target = "linkedSpots", source = "linkedSpots")
    AttractionResponseDTO toResponseDTO(Attraction entity);

    TouristSpotSummaryDTO toSummaryDTO(TouristSpot spot);

    void updateEntityFromDto(AttractionUpdateDTO updateDTO, @MappingTarget Attraction entity);
}
