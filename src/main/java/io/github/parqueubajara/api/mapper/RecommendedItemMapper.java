package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.RecommendedItemRequestDTO;
import io.github.parqueubajara.api.dto.response.RecommendedItemResponseDTO;
import io.github.parqueubajara.api.dto.update.RecommendedItemUpdateDTO;
import io.github.parqueubajara.api.model.RecommendedItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface RecommendedItemMapper {

    @Mapping(target = "photos", ignore = true)
    RecommendedItem toEntity(RecommendedItemRequestDTO requestDTO);

    RecommendedItemResponseDTO toResponseDTO(RecommendedItem entity);

    void updateEntityFromDto(RecommendedItemUpdateDTO updateDTO, @MappingTarget RecommendedItem entity);
}
