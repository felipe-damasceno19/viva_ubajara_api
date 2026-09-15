package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.HostPointUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.HostPointMapper;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.HostType;
import io.github.parqueubajara.api.repository.HostPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostPointService {

    private final HostPointRepository repository;
    private final HostPointMapper mapper;

    @Transactional(readOnly = true)
    public Optional<HostPoint> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public HostPoint findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de hospedagem de ID: "+id+" não encontrado!"));
    }

    @Transactional(readOnly = true)
    public Page<HostPoint> findAll(Pageable pageable, HostType type){
        if(type != null){
            return repository.findByHostType(type, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<HostPoint> findAllPublic(Pageable pageable, HostType type){
        if(type != null){
            return repository.findByHostTypeAndActiveTrue(type, pageable);
        }
        return repository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public HostPoint findByIdPublic(UUID id){
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de hospedagem de ID: "+id+" não encontrado!"));
    }

    @Transactional
    public HostPoint save(HostPoint hostPoint){
        if(hostPoint.getEmail() != null && !hostPoint.getEmail().trim().isEmpty()) {
            if (repository.existsByEmail(hostPoint.getEmail())) {
                throw new RuntimeException("E-mail já cadastrado no sistema!");
            }
        }
        return repository.save(hostPoint);
    }

    @Transactional
    public void update(UUID id, HostPointUpdateDTO updateDTO){
        HostPoint hostPoint = findById(id);
        mapper.updateEntityFromDto(updateDTO, hostPoint);
        repository.save(hostPoint);
    }

    @Transactional
    public void delete(UUID id){
        HostPoint hostPoint = findById(id);
        repository.delete(hostPoint);
    }

    @Transactional
    public void updateApprovalStatus(UUID id, ApprovalStatus status){
        HostPoint hostPoint = findById(id);
        hostPoint.setApprovalStatus(status);
        hostPoint.setActive(status == ApprovalStatus.APPROVED);
        repository.save(hostPoint);
    }

}
