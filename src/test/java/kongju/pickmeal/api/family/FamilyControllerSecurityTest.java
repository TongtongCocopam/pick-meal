package kongju.pickmeal.api.family;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;
import kongju.pickmeal.application.family.data.FamilyMemberDto;
import kongju.pickmeal.application.family.data.JoinRequestStatus;
import kongju.pickmeal.application.family.data.FamilyInvitationDto;
import kongju.pickmeal.application.family.data.FamilyJoinRequestDto;

import java.util.ArrayList;
import java.util.List;


@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc
@Import({CustomAccessDeniedHandler.class, FamilyControllerSecurityTest.TestSecurityConfig.class})
public class FamilyControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    FamilyService familyService;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http,
                CustomAccessDeniedHandler customAccessDeniedHandler
        ) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler(customAccessDeniedHandler)
                    )
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }


    @Nested
    @DisplayName("가족 합류 신청 목록")
    class JoinSummary {
        @Test
        @DisplayName("리더 권한이 없을 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_roadApply_not_reader() throws Exception {
            mockMvc.perform(get("/api/v1/families/me/applications")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "LEADER")
        public void should_success_roadApply() throws Exception {
            mockMvc.perform(get("/api/v1/families/me/applications")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("가족 합류 승인, 거절")
    class JoinRequestProcess {
        @Test
        @DisplayName("리더 권한이 없을 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_join_request_process_not_reader() throws Exception {
            Long requestId = 1L;
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            given(familyService.processJoinRequest(eq(requestId), any(FamilyJoinRequestDto.ProcessRequest.class), any(User.class)))
                    .willThrow(new BusinessException(ErrorCode.ACCESS_DENIED));

            mockMvc.perform(post("/api/v1/families/me/applications/{requestId}", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "LEADER")
        public void should_success_join_request_process() throws Exception {
            Long requestId = 1L;
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            FamilyJoinRequestDto.ProcessResponse response =
                    FamilyJoinRequestDto.ProcessResponse.builder()
                            .requestId(requestId)
                            .nickname("배고픈동생")
                            .decision(JoinRequestStatus.APPROVED)
                            .build();

            // 제대로 하려면 유저 권한을 leader로 인증을 만들어서 user와 함께 주입할 것
            given(familyService.processJoinRequest(any(), any(), nullable(User.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/families/me/applications/{requestId}", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andDo(print())
                    .andExpect(jsonPath("$.data.requestId").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("배고픈동생"))
                    .andExpect(jsonPath("$.data.decision").value("APPROVED"));
        }

    }

    @Nested
    @DisplayName("초대코드 재발급")
    class ReissueInvitation {
        @Test
        @DisplayName("리더가 아닌 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_reissue_invitation_not_reader() throws Exception {
            mockMvc.perform(patch("/api/v1/families/me/invitation-code"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());

        }

        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "LEADER")
        public void should_success_reissue_invitation() throws Exception {
            FamilyInvitationDto.CodeResponse response = FamilyInvitationDto.CodeResponse.builder()
                    .newInvitationCode("1sdd12d")
                    .build();

            given(familyService.createInvitationCode(any())).willReturn(response);

            mockMvc.perform(patch("/api/v1/families/me/invitation-code"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }
    }


    @Nested
    @DisplayName("가족 멤버 리스트 불러오기")
    class getMembers {
        @Test
        @DisplayName("가족 구성원이 아닌 경우")
        @WithMockUser(roles = "GUEST")
        public void should_fail_get_members_not_found() throws Exception {
            mockMvc.perform(get("/api/v1/families/me/members"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "MEMBER")
        public void should_success_get_members() throws Exception {
            List<FamilyMemberDto.ListItem> listItems = new ArrayList<>();

            given(familyService.getMembers(any())).willReturn(listItems);

            mockMvc.perform(get("/api/v1/families/me/members"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("가족 그룹 삭제")
    class DisbandFamily {
        @Test
        @DisplayName("리더가 아닌 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_disband_family_not_reader() throws Exception {
            mockMvc.perform(delete("/api/v1/families/me"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "LEADER")
        public void should_success_disband_family() throws Exception {
            mockMvc.perform(delete("/api/v1/families/me"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

    }

    @Nested
    @DisplayName("멤버 방출")
    class KickMember {
        @Test
        @DisplayName("권한 부족")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_kick_member_not_reader() throws Exception {
            Long userId = 1L;

            mockMvc.perform(delete("/api/v1/families/me/members/{userId}", userId))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("성공 케이스")
        @WithMockUser(roles = "LEADER")
        public void should_success_kick_member() throws Exception {
            Long userId = 1L;

            FamilyMemberDto.KickResponse response = FamilyMemberDto.KickResponse.builder()
                    .kickedNickname("testNickname")
                    .build();

            given(familyService.kickMember(any(), any())).willReturn(response);

            mockMvc.perform(delete("/api/v1/families/me/members/{userId}", userId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }
    }
}
