package kongju.pickmeal.core.family;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Table(name = "join_applies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinApply extends BaseTimeEntity {

    @Column(nullable = false)
    private Long familyId;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Builder
    public JoinApply(User user, Long familyId, ApplyStatus status) {
        this.user = user;
        this.familyId = familyId;
        this.status = status;
    }
}
