package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.persistence.*;
import lombok.*;

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
    private String avgPrice;

    @Column(name = "booking_url")
    private String bookingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "host_type", nullable = false)
    private HostType hostType;

}
