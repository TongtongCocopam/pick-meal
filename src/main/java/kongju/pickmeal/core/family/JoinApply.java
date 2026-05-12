package kongju.pickmeal.core.family;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;

import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Table(name = "join_applies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinApply extends BaseTimeEntity {
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long familyId;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    @Builder
    public JoinApply(Long userId, Long familyId, ApplyStatus status) {
        this.userId = userId;
        this.familyId = familyId;
        this.status = status;
    }
}
