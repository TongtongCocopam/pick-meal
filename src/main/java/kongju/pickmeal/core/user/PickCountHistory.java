package kongju.pickmeal.core.user;

import java.util.UUID;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.core.user.type.PickCountType;
import kongju.pickmeal.common.exception.BusinessException;


@Entity
@Getter
@Table(name = "pick_count_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickCountHistory extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PickCountType type;

    @Column(nullable = false)
    private Long count;

    @Column(name = "transaction_id", nullable = false, columnDefinition = "uuid")
    private UUID transactionId;

    @Builder(access = AccessLevel.PRIVATE)
    private PickCountHistory(User user, PickCountType type, Long count, UUID transactionId) {
        this.user = user;
        this.type = type;
        this.count = count;
        this.transactionId = transactionId;
    }

    public static PickCountHistory credit(User user, Long count, UUID transactionId) {
        validateCount(count);

        return PickCountHistory.builder()
                .user(user)
                .type(PickCountType.CREDIT)
                .count(count)
                .transactionId(transactionId)
                .build();
    }

    public static PickCountHistory debit(User user, Long count, UUID transactionId) {
        validateCount(count);

        return PickCountHistory.builder()
                .user(user)
                .type(PickCountType.DEBIT)
                .count(count)
                .transactionId(transactionId)
                .build();
    }

    public static PickCountHistory refund(User user, Long count, UUID transactionId) {
        validateCount(count);

        return PickCountHistory.builder()
                .user(user)
                .type(PickCountType.REFUND)
                .count(count)
                .transactionId(transactionId)
                .build();
    }

    public static PickCountHistory reset(User user, UUID transactionId) {
        return PickCountHistory.builder()
                .user(user)
                .type(PickCountType.RESET)
                .count(0L)
                .transactionId(transactionId)
                .build();
    }

    private static void validateCount(Long count){
        if(count == null || count <= 0L){
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택권 수량은 1 이상이어야 합니다.");
        }
    }
}
