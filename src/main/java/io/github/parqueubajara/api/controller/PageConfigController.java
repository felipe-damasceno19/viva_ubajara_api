package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.model.PageConfig;
import io.github.parqueubajara.api.service.PageConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/page-configs")
@RequiredArgsConstructor
@Tag(name = "Page Configs", description = "Configuração de imagens de capa das páginas")
public class PageConfigController {

    private final PageConfigService service;

    @GetMapping("/{pageKey}")
    public ResponseEntity<Map<String, String>> getByKey(@PathVariable String pageKey) {
        Optional<PageConfig> config = service.findByKey(pageKey);
        if (config.isEmpty()) {
            return ResponseEntity.ok(Map.of());
        }
        PageConfig c = config.get();
        java.util.HashMap<String, String> result = new java.util.HashMap<>();
        if (c.getImageUrl() != null) result.put("imageUrl", c.getImageUrl());
        if (c.getDescription() != null) result.put("description", c.getDescription());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{pageKey}/description")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateDescription(
            @PathVariable String pageKey,
            @RequestBody Map<String, String> body
    ) {
        service.updateDescription(pageKey, body.get("description"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{pageKey}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable String pageKey,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String imageUrl = service.uploadCoverImage(pageKey, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/{pageKey}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeImage(@PathVariable String pageKey) {
        service.removeImage(pageKey);
        return ResponseEntity.noContent().build();
    }
}
