package kongju.pickmeal.api.family;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.application.family.data.FamilyDto;
import kongju.pickmeal.application.family.data.FamilyJoinRequestDto;


@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FamilyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FamilyService familyService;

    @Nested
    @DisplayName("가족 그룹 생성 테스트")
    class CreateFamily {
        @Test
        @DisplayName("파라미터 누락")
        public void should_fail_params_missing() throws Exception {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("")
                    .build();

            mockMvc.perform(post("/api/v1/families")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());

        }


        @Test
        @DisplayName("성공 케이스")
        public void should_success_create_Family() throws Exception {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("고양이")
                    .build();

            FamilyDto.CreateResponse response = FamilyDto.CreateResponse.builder()
                    .familyName("고양이")
                    .invitationCode("dfd12345e")
                    .build();

            given(familyService.createFamily(any(), any())).willReturn(response);

            mockMvc.perform(post("/api/v1/families")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.familyName").value("고양이"));
        }


    }


    @Nested
    @DisplayName("가족 합류 신청")
    class FamilyApply {
        @Test
        @DisplayName("파라미터 형식에 맞지 않는 경우")
        public void should_fail_apply_params_not_valid() throws Exception {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("")
                    .build();

            mockMvc.perform(post("/api/v1/families/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.detail").value("초대 코드는 8자리여야 합니다."))
                    .andExpect(jsonPath("$.error.message").value("입력 형식이 올바르지 않습니다."));

        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_apply() throws Exception {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("초대코드대충8자")
                    .build();

            mockMvc.perform(post("/api/v1/families/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

    }

}
