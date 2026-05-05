package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_host_point")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HostPoint extends TouristSpot{

    @Column(name = "num_of_rooms")
    private Integer numOfRooms;

    @Column(name = "avg_price")
    private BigDecimal avgPrice;

    @Column(name = "booking_url")
    private String bookingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "host_type", nullable = false)
    private HostType hostType;

    @OneToMany(mappedBy = "hostPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();
}
