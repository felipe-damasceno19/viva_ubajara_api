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
        if (config.isEmpty() || config.get().getImageUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("imageUrl", config.get().getImageUrl()));
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
