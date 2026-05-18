package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.ContactsUpdateDTO;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.ContactsMapper;
import io.github.parqueubajara.api.model.Contacts;
import io.github.parqueubajara.api.repository.ContactsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactsService {

    private final ContactsRepository repository;
    private final ContactsMapper mapper;

    @Transactional(readOnly = true)
    public Optional<Contacts> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Contacts findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato com o ID: "+ id +" não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Contacts> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    @Transactional
    public Contacts save(Contacts contacts){
        if(contacts.getEmail() != null && !contacts.getEmail().trim().isEmpty()){
            if(repository.existsByEmail(contacts.getEmail())){
                throw new RuntimeException("E-mail já cadastrado");
            }
        }
        return repository.save(contacts);
    }

    @Transactional
    public void update(UUID id, ContactsUpdateDTO updateDTO){
        Contacts contacts = findById(id);
        mapper.updateEntityFromDto(updateDTO, contacts);
        repository.save(contacts);
    }

    @Transactional
    public void delete(UUID id){
        Contacts contacts = findById(id);
        repository.delete(contacts);
    }
}
