package kongju.pickmeal.core.user;

import java.math.BigDecimal;

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
    @Column(precision = 4, scale = 1)
    private BigDecimal height;
    @Column(precision = 4, scale = 1)
    private BigDecimal weight;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserHealthProfile(Gender gender, BigDecimal height, BigDecimal weight, User user) {
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.user = user;
    }

    public void update(Gender gender, BigDecimal height, BigDecimal weight){
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }
}
