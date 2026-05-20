package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.AttractionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_attraction")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Attraction extends TouristSpot {

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    @Column(name = "open_to_public")
    private Boolean openToPublic;

    @Column(name = "free_access")
    private Boolean freeAccess;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Attraction parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Attraction> subAttractions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_attraction_tourist_spot_link",
            joinColumns = @JoinColumn(name = "attraction_id"),
            inverseJoinColumns = @JoinColumn(name = "tourist_spot_id")
    )
    private List<TouristSpot> linkedSpots = new ArrayList<>();

    public void linkSubAttractions(Attraction attraction) {
        subAttractions.add(attraction);
    }

}
