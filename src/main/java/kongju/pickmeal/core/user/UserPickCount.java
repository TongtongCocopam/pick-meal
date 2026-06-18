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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long totalPickCount;

    @LastModifiedDate
    private LocalDateTime updateAt;

    @Builder(access = AccessLevel.PRIVATE)
    public UserPickCount(User user, Long totalPickCount) {
        this.user = user;
        this.totalPickCount = totalPickCount;
    }

    public static UserPickCount initialize(User user){
        return UserPickCount.builder()
                .user(user)
                .totalPickCount(0L)
                .build();
    }

    public void useCount(Long count){
        if(this.totalPickCount < count){
            throw new BusinessException(ErrorCode.TOO_MANY_SELECTIONS);
        }
        this.totalPickCount -= count;

    }

    public void restoreCount(Long count){
        this.totalPickCount += count;
    }

    public void resetCount(){
        this.totalPickCount = 0L;
    }

}
