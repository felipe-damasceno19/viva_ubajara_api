package io.github.parqueubajara.api.controller;


import io.github.parqueubajara.api.dto.request.ContactsRequestDTO;
import io.github.parqueubajara.api.dto.request.RestaurantRequestDTO;
import io.github.parqueubajara.api.dto.response.ContactsResponseDTO;
import io.github.parqueubajara.api.dto.response.RestaurantResponseDTO;
import io.github.parqueubajara.api.dto.update.ContactsUpdateDTO;
import io.github.parqueubajara.api.dto.update.RestaurantUpdateDTO;
import io.github.parqueubajara.api.mapper.ContactsMapper;
import io.github.parqueubajara.api.model.Contacts;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.service.ContactsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Contatos úteis para turistas")
public class ContactsController implements GenericController {

    private final ContactsService service;
    private final ContactsMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<ContactsResponseDTO> getById(@PathVariable UUID id){
        Contacts contacts = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(contacts));
    }

    @GetMapping
    public ResponseEntity<Page<ContactsResponseDTO>> getAll(@PageableDefault(size = 10) Pageable pageable){
        Page<Contacts> pageEntity = service.findAll(pageable);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactsResponseDTO> save(@RequestBody @Valid ContactsRequestDTO requestDTO){
        Contacts contacts = mapper.toEntity(requestDTO);
        service.save(contacts);
        URI location = generateHeaderLocation(contacts.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(contacts));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody ContactsUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
