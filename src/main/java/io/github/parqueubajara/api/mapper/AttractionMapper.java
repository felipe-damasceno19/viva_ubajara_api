package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.AttractionRequestDTO;
import io.github.parqueubajara.api.dto.response.AttractionResponseDTO;
import io.github.parqueubajara.api.dto.update.AttractionUpdateDTO;
import io.github.parqueubajara.api.model.Attraction;
import org.mapstruct.*;

@Mapper(config = CentralMapperConfig.class)
public interface AttractionMapper {

    @Mapping(target = "photos", ignore = true)
    Attraction toEntity(AttractionRequestDTO requestDTO);
    @Mapping(target = "subAttractions", source = "subAttractions")
    AttractionResponseDTO toResponseDTO(Attraction entity);

    void updateEntityFromDto(AttractionUpdateDTO updateDTO, @MappingTarget Attraction entity);
}
