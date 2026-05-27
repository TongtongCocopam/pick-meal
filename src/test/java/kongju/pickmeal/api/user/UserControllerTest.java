package kongju.pickmeal.api.user;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import kongju.pickmeal.application.user.data.UserDietProfileDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.type.DiseaseCategory;
import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import kongju.pickmeal.application.user.UserService;
import kongju.pickmeal.application.user.data.UserDto;

import static kongju.pickmeal.fixture.MemberFixture.createRequest;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // 스프링을 띄운 상태에서 사용
    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("회원가입 테스트")
    class Signup {
        @Test
        @DisplayName("아이디가 6자 미만이면 에러 반환")
        public void should_fail_invalid_loginId() throws Exception {
            UserDto.SignupRequest request = createRequest("test", "test0000!!", "test0000!!", "test@test.com", "test@test.com", "tester", LocalDate.now());

            mockMvc.perform(post("/api/v1/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists())
                    .andExpect(jsonPath("$.error.detail").exists());

            // 서비스가 한번도 실행되지 않았음을 검증
            verify(userService, never()).signup(any());
        }

        @Test
        @DisplayName("비밀번호 불일치")
        public void should_fail_mismatch_password() throws Exception {
            UserDto.SignupRequest request = createRequest("test", "test0000!!", "wrong!!", "test@test.com", "test@test.com", "tester", LocalDate.now());

            mockMvc.perform(post("/api/v1/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());

            // 서비스가 한번도 실행되지 않았음을 검증
            verify(userService, never()).signup(any());
        }

        @Test
        @DisplayName("회원가입 성공")
        public void should_success_signup() throws Exception {
            UserDto.SignupRequest request = createRequest();

            UserDto.SignupResponse mockResponse = UserDto.SignupResponse.builder()
                    .userId(1L)
                    .nickName("tester")
                    .build();

            given(userService.signup(request)).willReturn(mockResponse);

            mockMvc.perform(post("/api/v1/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.nickName").value("tester"));
        }
    }

    @Nested
    @DisplayName("유저 건강 정보 수정")
    class DiseaseProfile {
        @Test
        @DisplayName("파라미터 누락")
        public void should_fail_diet_profile_when_param_is_empty() throws Exception {
            UserDietProfileDto.UpdateDiseaseRequest request = UserDietProfileDto.UpdateDiseaseRequest.builder().build();

            doThrow(new BusinessException(ErrorCode.INVALID_INPUT, "질병 분류는 필수입니다."))
                    .when(userService).updateDisease(any(), any());

            mockMvc.perform(patch("/api/v1/users/me/diseases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_diet_profile() throws Exception {
            UserDietProfileDto.DiseaseRequest disease = UserDietProfileDto.DiseaseRequest.builder()
                    .category(DiseaseCategory.DIGESTIVE)
                    .detailName(DiseaseName.ANEMIA)
                    .description("대충 병")
                    .build();
            UserDietProfileDto.UpdateDiseaseRequest request = UserDietProfileDto.UpdateDiseaseRequest.builder()
                    .diseases(List.of(disease))
                    .build();


            mockMvc.perform(patch("/api/v1/users/me/diseases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

    }

    @Nested
    @DisplayName("선호 식품 정보 수정")
    class PreferenceProfile {
        @Test
        @DisplayName("파라미터 누락")
        public void should_fail_diet_profile_when_param_is_empty() throws Exception {
            UserDietProfileDto.UpdateIngredientPreferenceRequest request = UserDietProfileDto.UpdateIngredientPreferenceRequest.builder()
                    .build();

            doThrow(new BusinessException(ErrorCode.INVALID_INPUT, "변경할 데이터가 존재하지 않습니다."))
                    .when(userService).updateIngredientPreference(any(), any());

            mockMvc.perform(patch("/api/v1/users/me/ingredient-preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_diet_profile() throws Exception {
            UserDietProfileDto.IngredientPreferenceRequest preference = UserDietProfileDto.IngredientPreferenceRequest.builder()
                    .ingredientId(1L)
                    .preference(FoodPreferenceType.PREFERRED)
                    .build();

            UserDietProfileDto.UpdateIngredientPreferenceRequest request = UserDietProfileDto.UpdateIngredientPreferenceRequest.builder()
                    .preferences(List.of(preference))
                    .build();

            mockMvc.perform(patch("/api/v1/users/me/ingredient-preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

    }
}
