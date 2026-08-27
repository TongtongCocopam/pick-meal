package kongju.pickmeal.core.user;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.common.exception.BusinessException;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPickCount extends BaseTimeEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long remainingPickCount;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private UserPickCount(User user, Long remainingPickCount) {
        this.user = user;
        this.remainingPickCount = remainingPickCount;
    }

    public static UserPickCount initialize(User user){
        return UserPickCount.builder()
                .user(user)
                .remainingPickCount(0L)
                .build();
    }

    public void useCount(Long count){
        validateCount(count);
        if(this.remainingPickCount < count){
            throw new BusinessException(ErrorCode.TOO_MANY_SELECTIONS);
        }
        this.remainingPickCount -= count;

    }

    private static void validateCount(Long count) {
        if (count == null || count <= 0L) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "선택권 수량은 1 이상이어야 합니다."
            );
        }
    }

    public void restoreCount(Long count){
        this.remainingPickCount += count;
    }

    public void resetCount(){
        this.remainingPickCount = 0L;
    }

}
