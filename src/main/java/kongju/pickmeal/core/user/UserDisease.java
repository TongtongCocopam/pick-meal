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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDisease extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiseaseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiseaseName detailName;
    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserDisease(DiseaseCategory category, DiseaseName detailName, String description, User user) {
        this.category = category;
        this.detailName = detailName;
        this.description = description;
        this.user = user;
    }
}
