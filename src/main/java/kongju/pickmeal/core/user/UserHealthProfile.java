package kongju.pickmeal.core.user;


import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.user.type.Gender;
import kongju.pickmeal.core.common.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserHealthProfile extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private Double height;
    private Double weight;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserHealthProfile(Gender gender, Double height, Double weight, User user) {
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.user = user;
    }
}
