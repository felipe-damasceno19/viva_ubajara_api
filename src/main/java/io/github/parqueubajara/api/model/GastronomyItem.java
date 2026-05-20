package io.github.parqueubajara.api.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tb_gastronomy_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GastronomyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active")
    private Boolean active;
}
