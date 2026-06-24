package kongju.pickmeal.core.diet;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.common.BaseTimeEntity;


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

    private LocalDateTime mealDate;

    @Builder(access = AccessLevel.PRIVATE)
    private UserMenuPick(User user, Menu menu) {
        this.user = user;
        this.menu = menu;
        this.mealDate = LocalDateTime.now();
    }

    public static UserMenuPick create(User user, Menu menu){
        return UserMenuPick.builder()
                .menu(menu)
                .user(user)
                .build();
    }

    public void update(Menu menu) {
        this.menu = menu;
    }
}
