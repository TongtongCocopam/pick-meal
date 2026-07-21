package kongju.pickmeal.core.family;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Table(
        name = "family_join_requests",
        indexes = {
                @Index(
                        name = "idx_family_join_request_family_status",
                        columnList = "family_id, status"
                ),
                @Index(
                        name = "idx_family_join_request_user_status",
                        columnList = "user_id, status"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyJoinRequest extends BaseTimeEntity {
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplyStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Builder(access = AccessLevel.PRIVATE)
    private FamilyJoinRequest(User user, Family family, ApplyStatus status) {
        this.user = user;
        this.family = family;
        this.status = status;
    }

    public static FamilyJoinRequest create(
            User user,
            Family family
    ) {
        return FamilyJoinRequest.builder()
                .user(user)
                .family(family)
                .status(ApplyStatus.PENDING)
                .build();
    }

    public void accept(){
        this.status = ApplyStatus.APPROVED;
    }

    public void reject(){
        this.status = ApplyStatus.REJECTED;
    }
}
