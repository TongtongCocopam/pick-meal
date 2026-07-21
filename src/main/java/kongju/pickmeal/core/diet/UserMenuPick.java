package kongju.pickmeal.core.diet;

import java.util.UUID;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMenuPick extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserMenuPickStatus status;

    @Column(nullable = false)
    private LocalDate targetMonth;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Builder(access = AccessLevel.PRIVATE)
    private UserMenuPick(User user, Menu menu, UserMenuPickStatus status, LocalDate targetMonth, UUID transactionId) {
        this.user = user;
        this.menu = menu;
        this.status = status;
        this.targetMonth = targetMonth;
        this.transactionId = transactionId;
    }

    public static UserMenuPick create(User user, Menu menu, LocalDate targetMonth, UUID transactionId) {
        return UserMenuPick.builder()
                .menu(menu)
                .user(user)
                .status(UserMenuPickStatus.PENDING)
                .targetMonth(targetMonth)
                .transactionId(transactionId)
                .build();
    }

    public void update(Menu menu) {
        this.menu = menu;
    }

    public void used() {
        this.status = UserMenuPickStatus.USED;
    }
}
