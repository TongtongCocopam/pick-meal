package kongju.pickmeal.api.family;

import kongju.pickmeal.application.family.data.FamiliesResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.application.family.data.FamiliesRequest;

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
            FamiliesRequest.Create request = FamiliesRequest.Create.builder()
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
            FamiliesRequest.Create request = FamiliesRequest.Create.builder()
                    .familyName("고양이")
                    .build();

            FamiliesResponse.Create response = FamiliesResponse.Create.builder()
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


}
