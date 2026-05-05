package io.github.parqueubajara.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_restaurant")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Restaurant extends TouristSpot {

    @Column(name = "cuisine_type")
    private String cuisineType;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "avg_price")
    private BigDecimal avgPrice;

    @Column(name = "accepts_reservation")
    private Boolean acceptsReservation;

}
