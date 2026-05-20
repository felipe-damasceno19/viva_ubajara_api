package io.github.parqueubajara.api.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_page_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PageConfig {
    @Id
    @Column(name = "page_key", length = 50)
    private String pageKey; // "ESTABELECIMENTOS", "EVENTOS", "DEPOIMENTOS"

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
