package io.github.parqueubajara.api.model;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "tb_review", uniqueConstraints = @UniqueConstraint(
        name = "ux_review_spot_author", columnNames = {"spot_id", "author_id"}))
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"spot", "author"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Review extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TouristSpot spot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SystemUser author;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 600)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;
}
