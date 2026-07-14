package kongju.pickmeal.api.diet;

import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.application.diet.DietService;
import kongju.pickmeal.application.diet.data.DietMenuDto;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.support.fixture.TestSecurityConfig;
import kongju.pickmeal.api.exception.GlobalExceptionHandler;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;

import static kongju.pickmeal.support.fixture.SecurityFixture.mockLeader;
import static kongju.pickmeal.support.fixture.SecurityFixture.mockMember;


@WebMvcTest(DietController.class)
@AutoConfigureMockMvc
@Import({
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        TestSecurityConfig.class
})
public class DietControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private DietService dietService;

    @Nested
    @DisplayName("선택권 사용")
    class MenuPick {
        @Test
        @DisplayName("가족 구성원이 아닌 경우")
        @WithMockUser(roles = "GUEST")
        public void should_fail_menu_pick_when_not_family_member() throws Exception {
            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();

            mockMvc.perform(post("/api/v1/diets/menu-picks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_menu_pick() throws Exception {
            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();

            mockMvc.perform(post("/api/v1/diets/menu-picks")
                            .with(user(mockMember()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("선택한 메뉴 변경")
    class UpdatePickMenu {
        @Test
        @DisplayName("GUEST 권한은 메뉴 선택 변경에 실패")
        @WithMockUser(roles = "GUEST")
        public void should_fail_update_pick_menu_when_not_family() throws Exception {
            Long pickId = 1L;

            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            mockMvc.perform(patch("/api/v1/diets/menu-picks/{pickId}", pickId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_fail_update_pick_menu() throws Exception {
            Long pickId = 1L;

            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            MenuPickDto.UpdateResponse response = MenuPickDto.UpdateResponse.builder()
                    .menuId(2L)
                    .menuName("마라탕")
                    .build();

            given(dietService.updatePickMenu(any(), eq(pickId), any(MenuPickDto.UpdateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/diets/menu-picks/{pickId}", pickId)
                            .with(user(mockMember()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("메뉴 선택 삭제")
    class DeleteMenuPick {
        @Test
        @DisplayName("GUEST 권한은 메뉴 선택 변경에 실패")
        @WithMockUser(roles = "GUEST")
        public void should_fail_update_pick_menu_when_not_family() throws Exception {
            Long pickId = 1L;

            mockMvc.perform(delete("/api/v1/diets/menu-picks/{pickId}", pickId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_fail_update_pick_menu() throws Exception {
            Long pickId = 1L;

            MenuPickDto.UpdateResponse response = MenuPickDto.UpdateResponse.builder()
                    .menuId(2L)
                    .menuName("마라탕")
                    .build();

            given(dietService.updatePickMenu(any(), eq(pickId), any(MenuPickDto.UpdateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(delete("/api/v1/diets/menu-picks/{pickId}", pickId)
                            .with(user(mockMember()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("식단 보기")
    class DietView {
        @Test
        @DisplayName("잘못된 년월인 경우")
        public void should_fail_diet_view_when_invalid_yearMonth() throws Exception {
            mockMvc.perform(get("/api/v1/diets")
                            .param("month", "2026-13")
                            .with(user(mockMember())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_diet_view() throws Exception {
            mockMvc.perform(get("/api/v1/diets")
                            .param("month", "2026-06")
                            .with(user(mockMember())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("일일 식단 보기")
    class DailyMeal {
        @Test
        @DisplayName("멤버 권한이 없는 경우")
        @WithMockUser(roles = "GUEST")
        public void should_fail_daily_meal_when_not_member() throws Exception {
            mockMvc.perform(get("/api/v1/diets/{date}", LocalDate.now()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("날짜 형식이 맞지 않는경우")
        public void should_fail_daily_meal_when_invalid_day() throws Exception {
            mockMvc.perform(get("/api/v1/diets/{date}", "2026.07.01")
                            .with(user(mockMember())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_daily_meal() throws Exception {
            mockMvc.perform(get("/api/v1/diets/{date}", LocalDate.now())
                            .with(user(mockMember())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

    }

    @Nested
    @DisplayName("ai생성 식단 메뉴 대체")
    class ReplaceMenu {
        @Test
        @DisplayName("dietId 형식이 맞지 않는 경우")
        public void should_fail_replace_menu_when_invalid_menuId() throws Exception {
            DietMenuDto.ReplaceRequest request = DietMenuDto.ReplaceRequest.builder()
                    .menuId(1L)
                    .build();

            mockMvc.perform(patch("/api/v1/diets/{dietId}/menu", "식단아이디")
                            .with(user(mockLeader()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_replace_menu() throws Exception {
            DietMenuDto.ReplaceRequest request = DietMenuDto.ReplaceRequest.builder()
                    .menuId(2L)
                    .build();

            mockMvc.perform(patch("/api/v1/diets/{dietId}/menu", 1L)
                            .with(user(mockLeader()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("생성 식단 대체 가능 메뉴 목록")
    class ReplaceMenus {
        @Test
        @DisplayName("dietId 형식이 맞지 않는 경우")
        public void should_fail_replace_menus_when_invalid_dietId() throws Exception {
            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menus", "식단아이디")
                            .with(user(mockLeader()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_replace_menus() throws Exception {
            Long dietId = 1234L;
            Long userId = 1L;

            DietMenuDto.ReplacementMenuListResponse response =
                    DietMenuDto.ReplacementMenuListResponse.builder()
                            .dietId(dietId)
                            .keyword("김치")
                            .dishType(DishType.SOUP)
                            .menus(List.of(
                                    DietMenuDto.ReplacementMenuResponse.builder()
                                            .menuId(10L)
                                            .menuName("김치찌개")
                                            .kcal(BigDecimal.valueOf(320.5))
                                            .build(),
                                    DietMenuDto.ReplacementMenuResponse.builder()
                                            .menuId(11L)
                                            .menuName("참치김치찌개")
                                            .kcal(BigDecimal.valueOf(290.0))
                                            .build()
                            ))
                            .pageInfo(DietMenuDto.PageInfoResponse.builder()
                                    .currentPage(1)
                                    .totalPages(1)
                                    .totalElements(2L)
                                    .build())
                            .build();

            given(dietService.replacementMenus(eq(userId), eq(dietId), eq("김치"), any(Pageable.class))).willReturn(response);

            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menus", dietId)
                            .with(user(mockLeader()))
                            .param("keyword", "김치")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "id,asc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dietId").value(1234))
                    .andExpect(jsonPath("$.data.keyword").value("김치"))
                    .andExpect(jsonPath("$.data.dishType").value("SOUP"))
                    .andExpect(jsonPath("$.data.menus[0].menuId").value(10))
                    .andExpect(jsonPath("$.data.menus[0].menuName").value("김치찌개"))
                    .andExpect(jsonPath("$.data.menus[0].kcal").value(320.5))
                    .andExpect(jsonPath("$.data.menus[1].menuId").value(11))
                    .andExpect(jsonPath("$.data.menus[1].menuName").value("참치김치찌개"))
                    .andExpect(jsonPath("$.data.menus[1].kcal").value(290.0))
                    .andExpect(jsonPath("$.data.pageInfo.currentPage").value(1))
                    .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("대체 식단 상세 정보")
    class ReplaceMenuDetails {
        @Test
        @DisplayName("리더가 아닌 경우")
        public void should_fail_replace_menu_detail_when_not_leader() throws Exception {
            Long dietId = 1234L;
            Long menuId = 1L;
            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menus/{menuId}", dietId, menuId)
                            .with(user(mockMember())))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_replace_menu_detail() throws Exception {
            Long dietId = 1234L;
            Long menuId = 1L;
            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menus/{menuId}", dietId, menuId)
                            .with(user(mockLeader())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

        }
    }

    @Nested
    @DisplayName("대체 메뉴 추천")
    class MenuSuggestion {
        @Test
        @DisplayName("리더가 아닌 경우")
        public void should_fail__menu_suggestion_when_not_leader() throws Exception {
            Long dietId = 1234L;
            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menu-suggestions", dietId)
                            .with(user(mockMember())))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_menu_suggestion() throws Exception {
            Long dietId = 1234L;
            Long userId = 1L;

            DietMenuDto.IngredientResponse ingredientResponse = DietMenuDto.IngredientResponse.builder().name("두부").build();
            DietMenuDto.RecommendationResponse response = DietMenuDto.RecommendationResponse.builder()
                    .menuName("된장국")
                    .dishType(DishType.SOUP)
                    .menus(
                            List.of(DietMenuDto.CandidateResponse.builder()
                                            .menuId(2L)
                                            .menuName("동태탕")
                                            .kcal(BigDecimal.valueOf(300))
                                            .carbs(BigDecimal.valueOf(300))
                                            .protein(BigDecimal.valueOf(300))
                                            .fat(BigDecimal.valueOf(300))
                                            .sodium(BigDecimal.valueOf(300))
                                            .ingredients(List.of(ingredientResponse))
                                            .build(),
                                    DietMenuDto.CandidateResponse.builder()
                                            .menuId(3L)
                                            .menuName("감자탕")
                                            .kcal(BigDecimal.valueOf(300))
                                            .carbs(BigDecimal.valueOf(300))
                                            .protein(BigDecimal.valueOf(300))
                                            .fat(BigDecimal.valueOf(300))
                                            .sodium(BigDecimal.valueOf(300))
                                            .ingredients(List.of(ingredientResponse))
                                            .build(),
                                    DietMenuDto.CandidateResponse.builder()
                                            .menuId(4L)
                                            .menuName("북어국")
                                            .kcal(BigDecimal.valueOf(300))
                                            .carbs(BigDecimal.valueOf(300))
                                            .protein(BigDecimal.valueOf(300))
                                            .fat(BigDecimal.valueOf(300))
                                            .sodium(BigDecimal.valueOf(300))
                                            .ingredients(List.of(ingredientResponse))
                                            .build())
                    )
                    .build();

            given(dietService.recommendations(userId, dietId)).willReturn(response);

            mockMvc.perform(get("/api/v1/diets/{dietId}/replacement-menu-suggestions", dietId)
                            .with(user(mockLeader()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.menuName").value("된장국"))
                    .andExpect(jsonPath("$.data.menus").isNotEmpty());

        }
    }

}
