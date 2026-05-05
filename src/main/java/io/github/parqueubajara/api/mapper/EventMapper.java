package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.EventRequestDTO;
import io.github.parqueubajara.api.dto.response.EventResponseDTO;
import io.github.parqueubajara.api.dto.update.EventUpdateDTO;
import io.github.parqueubajara.api.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// Adicionamos o "uses" para ele enxergar como converter as fotos
@Mapper(config = CentralMapperConfig.class, uses = { PhotoMapper.class })
public interface EventMapper {

    @Mapping(source = "startDateTime", target = "startDate")
    @Mapping(source = "endDateTime", target = "endDate")
    @Mapping(target = "photos", ignore = true)
    Event toEntity(EventRequestDTO requestDTO);

    @Mapping(source = "startDate", target = "startDateTime")
    @Mapping(source = "endDate", target = "endDateTime")
    EventResponseDTO toResponseDTO(Event entity);

    void updateEntityFromDto(EventUpdateDTO updateDTO, @MappingTarget Event entity);
}