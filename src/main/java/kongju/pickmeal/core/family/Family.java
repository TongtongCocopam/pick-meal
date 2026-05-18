package kongju.pickmeal.core.family;

import java.time.LocalDateTime;

import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import kongju.pickmeal.core.common.BaseTimeEntity;

@Entity
@Table(name = "families")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Family extends BaseTimeEntity {
    @Column(nullable = false, unique = true)
    private String familyName;
    @Column(nullable = false, unique = true)
    private String invitationCode;
    private Long pickCount = 0L;
    private Long leaderId;
    private LocalDateTime invitationCodeUpdatedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    @Builder
    public Family(String familyName, String invitationCode, Long leaderId) {
        this.familyName = familyName;
        this.invitationCode = invitationCode;
        this.pickCount = 0L;
        this.leaderId = leaderId;
    }

    /**
     * 초대코드 재발급 시간 텀 10분
     * @param invitationCode 초대코드
     */
    public void reissueInvitationCode(String invitationCode) {
        if (this.invitationCodeUpdatedAt != null &&
                this.invitationCodeUpdatedAt.isAfter(LocalDateTime.now().minusMinutes(10))) {
            throw new BusinessException(ErrorCode.INVITATION_CODE_REISSUE_TOO_FAST);
        }
        this.invitationCode = invitationCode;
        this.invitationCodeUpdatedAt = LocalDateTime.now();
    }
}
