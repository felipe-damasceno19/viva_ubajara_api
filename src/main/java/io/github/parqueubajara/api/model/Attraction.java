package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.AttractionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_attraction")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Attraction extends TouristSpot {

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "entry_price")
    private BigDecimal entryPrice;

    @Column(name = "has_guide")
    private Boolean hasGuide;

    @Column(name = "average_visit_duration")
    private Integer averageVisitDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "attraction_type", nullable = false)
    private AttractionType category;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Attraction parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Attraction> subAttractions = new ArrayList<>();

}
