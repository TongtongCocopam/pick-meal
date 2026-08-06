package kongju.pickmeal.api.family;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

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

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import kongju.pickmeal.application.family.data.*;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.support.fixture.TestSecurityConfig;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.api.exception.GlobalExceptionHandler;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;

import static kongju.pickmeal.support.fixture.SecurityFixture.*;


@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc
@Import({
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        TestSecurityConfig.class
})
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
        @WithMockUser(roles = "GUEST")
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

            given(familyService.createFamily(any(FamilyDto.CreateRequest.class), eq(1L)))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/families")
                            .with(user(mockGuest()))
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
        @WithMockUser(roles = "GUEST")
        public void should_fail_apply_params_not_valid() throws Exception {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode(" ")
                    .build();

            mockMvc.perform(post("/api/v1/families/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.detail").exists())
                    .andExpect(jsonPath("$.error.message").value("입력 형식이 올바르지 않습니다."));
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_apply() throws Exception {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("초대코드대충8자")
                    .build();

            mockMvc.perform(post("/api/v1/families/applications")
                            .with(user(mockGuest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
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
                            .with(user(mockMember()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_roadApply() throws Exception {

            FamilyJoinRequestDto.Summary summary = FamilyJoinRequestDto.Summary.builder()
                    .requestId(1L)
                    .email("dfdf@gmmail.com")
                    .nickname("apppp")
                    .build();

            List<FamilyJoinRequestDto.Summary> joinRequestSummary = List.of(summary);
            given(familyService.loadJoinRequestSummary(any())).willReturn(joinRequestSummary);

            mockMvc.perform(get("/api/v1/families/me/applications")
                            .with(user(mockLeader()))
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

            given(familyService.processJoinRequest(eq(requestId), any(FamilyJoinRequestDto.ProcessRequest.class), any()))
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

            given(familyService.processJoinRequest(any(), any(), any()))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/families/me/applications/{requestId}", requestId)
                            .with(user(mockLeader()))
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
            mockMvc.perform(post("/api/v1/families/me/invitation-code"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reissue_invitation() throws Exception {
            FamilyInvitationDto.CodeResponse response = FamilyInvitationDto.CodeResponse.builder()
                    .newInvitationCode("1sdd12d")
                    .build();

            given(familyService.createInvitationCode(any())).willReturn(response);

            mockMvc.perform(post("/api/v1/families/me/invitation-code")
                            .with(user(mockLeader())))
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
        public void should_success_get_members() throws Exception {
            List<FamilyMemberDto.ListItem> listItems = new ArrayList<>();

            given(familyService.getMembers(any())).willReturn(listItems);

            mockMvc.perform(get("/api/v1/families/me/members")
                            .with(user(mockMember())))
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
        public void should_success_disband_family() throws Exception {
            mockMvc.perform(delete("/api/v1/families/me")
                            .with(user(mockLeader())))
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
        public void should_success_kick_member() throws Exception {
            Long userId = 1L;

            FamilyMemberDto.KickResponse response = FamilyMemberDto.KickResponse.builder()
                    .kickedNickname("testNickname")
                    .build();

            given(familyService.kickMember(any(), any())).willReturn(response);

            mockMvc.perform(delete("/api/v1/families/me/members/{userId}", userId)
                            .with(user(mockLeader())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("그룹 나가기")
    class LeaveFamily {
        @Test
        @DisplayName("멤버 권한이 아닌경우")
        @WithMockUser("GEUST")
        public void should_fail_leave_family_family_not_exist() throws Exception {
            mockMvc.perform(delete("/api/v1/families/me/membership"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_leave_family() throws Exception {
            mockMvc.perform(delete("/api/v1/families/me/membership")
                            .with(user(mockMember())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("선택권 분배")
    class PickAllocation {
        @Test
        @DisplayName("리더가 아닌 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_pick_allocation_not_reader() throws Exception {
            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .pickAllocations(null)
                    .isAutoAllocations(true)
                    .defaultAllocations(1L)
                    .build();

            mockMvc.perform(patch("/api/v1/families/me/picks/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_pick_allocation() throws Exception {
            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .pickAllocations(null)
                    .isAutoAllocations(true)
                    .defaultAllocations(1L)
                    .build();

            FamilyPickDto.ConfigResponse response = FamilyPickDto.ConfigResponse.builder()
                    .isAutoAllocations(true)
                    .build();

            given(familyService.pickConfig(any(), any())).willReturn(response);

            mockMvc.perform(patch("/api/v1/families/me/picks/config")
                            .with(user(mockLeader()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());

        }
    }

    @Nested
    @DisplayName("선택권 초기화")
    class ResetAllocation {
        @Test
        @DisplayName("리더가 아닌 경우")
        @WithMockUser(roles = "MEMBER")
        public void should_fail_reset_allocation_not_leader() throws Exception {
            mockMvc.perform(post("/api/v1/families/me/picks/reset"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reset_allocation() throws Exception {
            FamilyPickDto.ResetResponse response = FamilyPickDto.ResetResponse.builder()
                    .resetMember(6)
                    .resetAt(String.valueOf(LocalDateTime.now()))
                    .build();

            given(familyService.resetConfig(any())).willReturn(response);

            mockMvc.perform(post("/api/v1/families/me/picks/reset")
                            .with(user(mockLeader())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.resetMember").value(6))
                    .andExpect(jsonPath("$.data.resetAt").exists());
        }
    }
}
