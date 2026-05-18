package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.config.CentralMapperConfig;
import io.github.parqueubajara.api.dto.request.TestimonialRequestDTO;
import io.github.parqueubajara.api.dto.response.TestimonialResponseDTO;
import io.github.parqueubajara.api.dto.update.TestimonialUpdateDTO;
import io.github.parqueubajara.api.model.Testimonial;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface TestimonialMapper {

    Testimonial toEntity(TestimonialRequestDTO requestDTO);

    TestimonialResponseDTO toResponseDTO(Testimonial entity);

    void updateEntityFromDto(TestimonialUpdateDTO updateDTO, @MappingTarget Testimonial entity);
}
