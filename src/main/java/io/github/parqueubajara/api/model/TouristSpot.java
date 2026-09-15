package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_tourist_spot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "photos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.JOINED)
public class TouristSpot extends BaseEntity{

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "web_url")
    private String webUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "maps_url")
    private String mapsUrl;

    @Column(name = "active")
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private SystemUser owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @OneToMany(mappedBy = "touristSpot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos;

    @PrePersist
    @PreUpdate
    private void preFormat() {
        if(this.email != null && this.email.trim().isEmpty()) {
            this.email = null;
        }
    }
}