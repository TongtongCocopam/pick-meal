package kongju.pickmeal.api.menu;

import java.util.List;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.application.menu.data.MenuFilterDto;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto.SaveRequest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static kongju.pickmeal.support.fixture.SecurityFixture.*;
import static kongju.pickmeal.application.menu.data.FamilyCustomMenuDto.IngredientRequest.*;


@WebMvcTest(MenuController.class)
public class MenuControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Nested
    @DisplayName("가족 메뉴 추가")
    class FamilyMenuCreate {
        @Test
        @DisplayName("request가 잘못된 경우")
        public void should_fail_family_menu_create_when_param_invalid() throws Exception {
            SaveRequest request = SaveRequest.builder().build();

            mockMvc.perform(post("/api/v1/menus/custom")
                            .with(user(mockLeader()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_family_menu_create() throws Exception {
            SaveRequest request = SaveRequest.builder()
                    .menuName("닭가슴살 김치볶음밥")
                    .dishType(DishType.MAIN_DISH)
                    .category(MenuCategory.KOREAN)
                    .kcal(BigDecimal.valueOf(520.0))
                    .carbs(BigDecimal.valueOf(65.0))
                    .protein(BigDecimal.valueOf(32.0))
                    .fat(BigDecimal.valueOf(14.0))
                    .sodium(BigDecimal.valueOf(850.0))
                    .ingredients(List.of(
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("닭가슴살")
                                    .quantity(BigDecimal.valueOf(120.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build(),
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("김치")
                                    .quantity(BigDecimal.valueOf(100.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build(),
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("밥")
                                    .quantity(BigDecimal.valueOf(200.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/v1/menus/custom")
                            .with(user(mockLeader()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("가족 메뉴 삭제")
    class FamilyMenuDelete {
        @Test
        @DisplayName("성공 케이스")
        public void should_success_family_menu_delete() throws Exception {
            SaveRequest request = SaveRequest.builder()
                    .menuName("닭가슴살 김치볶음밥")
                    .dishType(DishType.MAIN_DISH)
                    .category(MenuCategory.KOREAN)
                    .kcal(BigDecimal.valueOf(520.0))
                    .carbs(BigDecimal.valueOf(65.0))
                    .protein(BigDecimal.valueOf(32.0))
                    .fat(BigDecimal.valueOf(14.0))
                    .sodium(BigDecimal.valueOf(850.0))
                    .ingredients(List.of(
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("닭가슴살")
                                    .quantity(BigDecimal.valueOf(120.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build(),
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("김치")
                                    .quantity(BigDecimal.valueOf(100.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build(),
                            builder()
                                    .ingredientId(null)
                                    .ingredientName("밥")
                                    .quantity(BigDecimal.valueOf(200.0))
                                    .unit(IngredientUnit.G)
                                    .type(IngredientType.MAIN)
                                    .build()
                    ))
                    .build();
            Long menuId = 1L;
            mockMvc.perform(delete("/api/v1/menus/custom/{menuId}", menuId)
                            .with(user(mockLeader()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
