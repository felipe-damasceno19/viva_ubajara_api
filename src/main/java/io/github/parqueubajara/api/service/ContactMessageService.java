package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.model.ContactMessage;
import io.github.parqueubajara.api.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository repository;

    @Transactional(readOnly = true)
    public ContactMessage findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message with ID: " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<ContactMessage> findAll(Pageable pageable, Boolean read) {
        if (read != null) {
            return repository.findByRead(read, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return repository.countByRead(false);
    }

    @Transactional
    public ContactMessage save(ContactMessage message) {
        message.setRead(false);
        return repository.save(message);
    }

    @Transactional
    public void markAsRead(UUID id) {
        ContactMessage message = findById(id);
        message.setRead(true);
        repository.save(message);
    }

    @Transactional
    public void delete(UUID id) {
        ContactMessage message = findById(id);
        repository.delete(message);
    }
}
