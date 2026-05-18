package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.TestimonialUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.TestimonialMapper;
import io.github.parqueubajara.api.model.Testimonial;
import io.github.parqueubajara.api.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository repository;
    private final TestimonialMapper mapper;

    @Transactional(readOnly = true)
    public Testimonial findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial with ID: " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<Testimonial> findAll(Pageable pageable, Boolean approved) {
        if (approved != null) {
            return repository.findByApproved(approved, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public Testimonial save(Testimonial testimonial) {
        testimonial.setApproved(false);
        return repository.save(testimonial);
    }

    @Transactional
    public void update(UUID id, TestimonialUpdateDTO updateDTO) {
        Testimonial testimonial = findById(id);
        mapper.updateEntityFromDto(updateDTO, testimonial);
        repository.save(testimonial);
    }

    @Transactional
    public void delete(UUID id) {
        Testimonial testimonial = findById(id);
        repository.delete(testimonial);
    }
}
