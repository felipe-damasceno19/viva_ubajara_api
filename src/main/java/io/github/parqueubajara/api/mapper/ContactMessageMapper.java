package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.ContactMessageRequestDTO;
import io.github.parqueubajara.api.dto.response.ContactMessageResponseDTO;
import io.github.parqueubajara.api.model.ContactMessage;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface ContactMessageMapper {

    ContactMessage toEntity(ContactMessageRequestDTO requestDTO);

    ContactMessageResponseDTO toResponseDTO(ContactMessage entity);
}
