package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.update.UserProfileUpdateDTO;
import io.github.parqueubajara.api.dto.update.UserUpdateDTO;
import io.github.parqueubajara.api.exception.DuplicateEmailException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.UserMapper;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.repository.UserRepository;
import io.github.parqueubajara.api.service.infra.FileValidationService;
import io.github.parqueubajara.api.service.infra.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final S3StorageService storageService;
    private final FileValidationService validationService;

    @Transactional(readOnly = true)
    public Optional<SystemUser> findByIdOptional(UUID id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public SystemUser findById(UUID id){
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de ID: "+ id +" não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<SystemUser> findAll(Pageable pageable, String username, Role role){
        if (username != null && role != null) {
            return repository.findByUsernameContainingIgnoreCaseAndUserRole(username, role, pageable);
        } else if (username != null) {
            return repository.findByUsernameContainingIgnoreCase(username, pageable);
        } else if (role != null) {
            return repository.findByUserRole(role, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public SystemUser findByEmail(String email){
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário e/ou senha incorretos!"));
    }

    @Transactional(readOnly = true)
    public Optional<SystemUser> findByEmailOptional(String email){
        return repository.findByEmail(email);
    }

    public boolean existsByUsername(String username){
        return repository.existsByUsername(username);
    }

    @Transactional
    public SystemUser save(SystemUser user){
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (repository.existsByEmail(user.getEmail())) {
                throw new DuplicateEmailException("E-mail já cadastrado");
            }
        }
        return repository.save(user);
    }

    @Transactional
    public void update(UUID id, UserUpdateDTO updateDTO){
        SystemUser user = findById(id);
        mapper.updateEntityFromDto(updateDTO, user);
        repository.save(user);
    }

    @Transactional
    public void updateSelf(String email, UserProfileUpdateDTO dto) {
        SystemUser user = findByEmail(email);
        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        if (dto.username() != null) user.setUsername(dto.username());
        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.password() != null) user.setPassword(encoder.encode(dto.password()));
        repository.save(user);
    }

    @Transactional
    public void delete(UUID id){
        SystemUser user = findById(id);
        repository.delete(user);
    }

    @Transactional
    public void updatePhotoUrlIfEmpty(SystemUser user, String photoUrl) {
        if (photoUrl != null && user.getPhotoUrl() == null) {
            user.setPhotoUrl(photoUrl);
            repository.save(user);
        }
    }

    @Transactional
    public void changeRole(UUID id, Role role) {
        SystemUser user = findById(id);
        user.setUserRole(role);
        repository.save(user);
    }

    @Transactional
    public String uploadPhoto(String email, MultipartFile file) throws IOException {
        validationService.validateImage(file);
        SystemUser user = findByEmail(email);
        String storageKey = storageService.upload(file);
        String photoUrl = storageService.generateUrl(storageKey);
        user.setPhotoUrl(photoUrl);
        repository.save(user);
        return photoUrl;
    }

}
