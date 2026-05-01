package kongju.pickmeal.api.user;

import java.time.LocalDate;

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
import static kongju.pickmeal.fixture.MemberFixture.createRequest;
import kongju.pickmeal.application.user.data.request.MemberRequest;
import kongju.pickmeal.application.user.data.response.MemberResponse;

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

    @Test
    @DisplayName("아이디가 6자 미만이면 에러 반환")
    public void should_fail_invalid_loginId() throws Exception {
        MemberRequest.Register request = createRequest("test", "test0000!!", "test0000!!", "test@test.com", "test@test.com", "tester", LocalDate.now());

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
        MemberRequest.Register request = createRequest("test", "test0000!!", "wrong!!", "test@test.com", "test@test.com", "tester", LocalDate.now());

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
        MemberRequest.Register request = createRequest();

        MemberResponse.Register mockResponse = new MemberResponse.Register(1L, "tester");
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
