package kongju.pickmeal.api.menu;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.support.fixture.MenuFixture;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.MenuService;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.application.menu.data.MenuFilterDto;

import static kongju.pickmeal.support.fixture.SecurityFixture.mockGuest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


@WebMvcTest(MenuController.class)
public class MenuControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Nested
    @DisplayName("카테고리 목록 불러오기")
    class MenuFilterGet {
        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "GUEST")
        public void should_success_get_menu_filters() throws Exception {
            List<MenuFilterDto.CategoryResponse> categories = List.of(
                    MenuFilterDto.CategoryResponse.builder()
                            .name(MenuCategory.KOREAN)
                            .displayName("한식")
                            .build()
            );

            List<MenuFilterDto.DishTypeResponse> dishTypes = List.of(
                    MenuFilterDto.DishTypeResponse.builder()
                            .name(DishType.RICE)
                            .displayName("밥")
                            .build(),
                    MenuFilterDto.DishTypeResponse.builder()
                            .name(DishType.STEW)
                            .displayName("찌개/전골/스튜")
                            .build()
            );

            MenuFilterDto.MetadataResponse response = MenuFilterDto.MetadataResponse.builder()
                    .categories(categories)
                    .dishTypes(dishTypes)
                    .build();

            given(menuService.getFilterMetadata()).willReturn(response);

            mockMvc.perform(get("/api/v1/menus/filter-options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.categories[0].name").value("KOREAN"))
                    .andExpect(jsonPath("$.data.categories[0].displayName").value("한식"))
                    .andExpect(jsonPath("$.data.dishTypes[0].name").value("RICE"))
                    .andExpect(jsonPath("$.data.dishTypes[0].displayName").value("밥"))
                    .andExpect(jsonPath("$.data.dishTypes[1].name").value("STEW"))
                    .andExpect(jsonPath("$.data.dishTypes[1].displayName").value("찌개/전골/스튜"));

        }

    }

    @Nested
    @DisplayName("메뉴 상세 정보")
    class MenuDetail {
        @Test
        @DisplayName("성공 케이스")
        public void should_success_menu_detail() throws Exception {
            Long menuId = 1L;
            MenuDto.DetailResponse response = MenuFixture.menuDetailResponse();

            given(menuService.detailMenu(menuId)).willReturn(response);

            mockMvc.perform(get("/api/v1/menus/{menuId}", menuId)
                            .with(user(mockGuest())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));


        }

    }


}
