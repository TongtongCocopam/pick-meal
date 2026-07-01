package kongju.pickmeal.api.diet;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import kongju.pickmeal.application.diet.DietService;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.support.fixture.TestSecurityConfig;
import kongju.pickmeal.api.exception.GlobalExceptionHandler;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;

import static kongju.pickmeal.support.fixture.SecurityFixture.mockMember;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


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

}
