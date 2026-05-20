package io.github.parqueubajara.api.model;

import jakarta.persistence.*;
import lombok.*;

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
    private String avgPrice;

    @Column(name = "accepts_reservation")
    private Boolean acceptsReservation;

    @Column(name = "star_rating")
    private Integer starRating;

}
