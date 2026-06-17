package kongju.pickmeal.core.diet;

import java.util.List;

public interface UserMenuPickRepository {
    List<UserMenuPick> saveAll(List<UserMenuPick> userMenuPicks);
}
