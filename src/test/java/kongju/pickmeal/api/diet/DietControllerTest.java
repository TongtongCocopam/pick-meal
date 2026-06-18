package kongju.pickmeal.api.diet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


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
    }


}
