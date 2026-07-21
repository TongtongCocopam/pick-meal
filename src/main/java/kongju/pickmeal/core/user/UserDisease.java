package kongju.pickmeal.core.user;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.common.BaseEntity;
import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.core.user.type.DiseaseCategory;


@Entity
@Getter
@Table(
        name = "user_diseases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_disease_user_detail",
                        columnNames = {"user_id", "category", "detail_name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_disease_user",
                        columnList = "user_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDisease extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiseaseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "detail_name", nullable = false, length = 50)
    private DiseaseName detailName;
    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private UserDisease(DiseaseCategory category, DiseaseName detailName, String description, User user) {
        this.category = category;
        this.detailName = detailName;
        this.description = description;
        this.user = user;
    }

    public static UserDisease create(DiseaseCategory category, DiseaseName diseaseName, String description, User user) {
        return UserDisease.builder()
                .category(category)
                .detailName(diseaseName)
                .description(description)
                .user(user)
                .build();
    }
}
