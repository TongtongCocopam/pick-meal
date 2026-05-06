package kongju.pickmeal.core.family;

import java.time.LocalDateTime;

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
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Family(String familyName, String invitationCode, Long leaderId) {
        this.familyName = familyName;
        this.invitationCode = invitationCode;
        this.pickCount = 0L;
        this.leaderId = leaderId;
    }
}
