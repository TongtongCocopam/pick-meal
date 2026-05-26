package kongju.pickmeal.core.family;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Table(name = "family_join_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyJoinRequest extends BaseTimeEntity {
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Builder
    public FamilyJoinRequest(User user, Family family, ApplyStatus status) {
        this.user = user;
        this.family = family;
        this.status = status;
    }

    public void accept(){
        this.status = ApplyStatus.APPROVED;
    }

    public void reject(){
        this.status = ApplyStatus.REJECTED;
    }
}
