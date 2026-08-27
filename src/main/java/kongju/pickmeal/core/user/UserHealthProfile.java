package kongju.pickmeal.core.user;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.user.type.Gender;
import kongju.pickmeal.core.common.BaseEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserHealthProfile extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;
    @Column(precision = 4, scale = 1)
    private BigDecimal height;
    @Column(precision = 4, scale = 1)
    private BigDecimal weight;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private UserHealthProfile(Gender gender, BigDecimal height, BigDecimal weight, User user) {
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.user = user;
    }

    public static UserHealthProfile create(Gender gender, BigDecimal height, BigDecimal weight, User user) {
        return UserHealthProfile.builder().gender(gender).height(height).weight(weight).user(user).build();
    }
    public void update(Gender gender, BigDecimal height, BigDecimal weight){
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }
}
