package io.github.parqueubajara.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_recommended_item")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RecommendedItem extends TouristSpot {

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    @Column(name = "category")
    private String category;

    @Column(name = "featured")
    private Boolean featured;
}
