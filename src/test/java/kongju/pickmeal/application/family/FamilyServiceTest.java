package kongju.pickmeal.application.family;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.core.user.UserRole;
import org.assertj.core.api.NotThrownAssert;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import kongju.pickmeal.core.family.*;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.family.data.FamilyDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.JoinRequestStatus;
import kongju.pickmeal.application.family.data.FamilyInvitationDto;
import kongju.pickmeal.application.family.data.FamilyJoinRequestDto;


@ExtendWith(SpringExtension.class)
public class FamilyServiceTest {
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private FamilyJoinRepository familyJoinRepository;
    @Mock
    private InvitationCodeGenerator invitationCodeGenerator;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FamilyService familyService;

    public User createUser() {
        return User.builder()
                .loginId("testUser")
                .email("test1234@gmail.com")
                .build();
    }

    public User createCustomUser(String loginId, String email, String nickname) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .nickName(nickname)
                .build();
    }

    @Nested
    @DisplayName("가족 그룹 생성 테스트")
    class FamilyCreate {
        @Test
        @DisplayName("소속된 가족이 있을 경우")
        public void should_fail_already_exist_family() {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("고양이")
                    .build();

            User user = createUser();

            user.joinFamilyLeader(100L);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createFamily(request, user)
            );

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 그룹 성공적으로 생성")
        public void should_success_create_family() {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("고양이")
                    .build();

            User user = createUser();

            FamilyDto.CreateResponse response = familyService.createFamily(request, user);
            assertEquals(request.familyName(), response.familyName());
            verify(familyRepository, times(1)).save(any(Family.class));
        }

    }

    @Nested
    @DisplayName("가족 합류 신청")
    class FamilyApply {
        @Test
        @DisplayName("이미 가족이 있을 경우")
        public void should_fail_apply_already_exist_family() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            user.joinFamilyMember(12L);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, user)
            );

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("초대 코드를 찾지 못한 경우")
        public void should_fail_apply_invitation_code_not_found() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .build();

            User user = createUser();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, user)
            );

            assertEquals(ErrorCode.INVALID_INVITATION_CODE, exception.getErrorCode());
        }

        @Test
        @DisplayName("이미 신청한 경우")
        public void should_fail_apply_already_exists() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family.getId()), eq(ApplyStatus.PENDING)))
                    .willReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, user)
            );

            assertEquals(ErrorCode.ALREADY_PROCESSED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_apply() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family.getId()), eq(ApplyStatus.PENDING)))
                    .willReturn(false);

            // 오류 없이 실행되었는지 체크
            assertDoesNotThrow(() -> familyService.joinRequest(request, user));

            verify(familyJoinRepository, times(1)).save(any(FamilyJoinRequest.class));
        }
    }

    @Nested
    @DisplayName("가족 합류 신청 목록")
    class JoinSummary {
        @Test
        @DisplayName("가족 아이디가 없을때")
        public void should_fail_roadApply_null_familyId() {
            User user = createUser();

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.loadJoinRequestSummary(user)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_roadApply() {
            User user = createUser();
            user.joinFamilyMember(12L);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .familyId(12L)
                    .status(ApplyStatus.PENDING)
                    .user(user)
                    .build();

            List<FamilyJoinRequest> familyJoinRequestList = new ArrayList<>();
            familyJoinRequestList.add(familyJoinRequest);

            given(familyJoinRepository.findAllByFamilyIdAndStatus(any(), any())).willReturn(familyJoinRequestList);

            assertDoesNotThrow(() -> familyService.loadJoinRequestSummary(user));
            verify(familyJoinRepository, times(1)).findAllByFamilyIdAndStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("가족 승인 거부")
    class JoinRequestProcess {
        @Test
        @DisplayName("decision이 null일경우")
        public void should_fail_join_request_process_decision_null() {
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(null)
                    .build();

            User user = createUser();
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, user)
            );

            assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        }

        @Test
        @DisplayName("신청 기록이 없는경우")
        public void should_fail_join_request_process_not_found_request() {
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            User user = createUser();
            given(familyJoinRepository.findById(any())).willReturn(Optional.empty());
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, user)
            );

            assertEquals(ErrorCode.REQUEST_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("다른 가족의 요청을 승인하려고 하는 경우")
        public void should_fail_join_request_process_another_family_join_request() {
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            User user = createUser();
            user.joinFamilyLeader(1L);

            User user2 = createCustomUser("custom", "custom1234@gmail.com", "냠냠짬");
            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .familyId(2L)
                    .user(user2)
                    .build();

            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, user)
            );

            assertEquals(ErrorCode.NOT_YOUR_FAMILY_REQUEST, exception.getErrorCode());
        }

        @Test
        @DisplayName("이미 가족이 있는 경우")
        public void should_fail_join_request_process_already_exists_family() {
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            User user = createUser();
            user.joinFamilyLeader(1L);

            User user2 = createCustomUser("custom", "custom1234@gmail.com", "배고파");
            user2.joinFamilyLeader(1L);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .familyId(1L)
                    .user(user2)
                    .build();
            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, user)
            );

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_join_request_process() {
            FamilyJoinRequestDto.ProcessRequest request = FamilyJoinRequestDto.ProcessRequest.builder()
                    .decision(JoinRequestStatus.APPROVED)
                    .build();

            User user = createUser();
            user.joinFamilyLeader(1L);

            User user2 = createCustomUser("custom", "custom1234@gmail.com", "배불러");

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .familyId(1L)
                    .user(user2)
                    .build();

            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            FamilyJoinRequestDto.ProcessResponse response = familyService.processJoinRequest(1L, request, user);

            assertEquals(1L, response.requestId());
            assertEquals(JoinRequestStatus.APPROVED, response.decision());
            assertEquals(UserRole.MEMBER, user2.getRole());
        }
    }

    @Nested
    @DisplayName("초대코드 재발급")
    class ReissueInvitation {
        @Test
        @DisplayName("재발급 신청 후 10분이 지나지 않았는데 재 신청한 경우")
        public void should_fail_reissue_invitation_process_too_fast() {
            Family family = Family.builder()
                    .invitationCode("sds1234d")
                    .build();

            User user = createUser();
            user.joinFamilyLeader(1L);
            family.reissueInvitationCode("st1454fs");

            given(familyRepository.findById(any())).willReturn(Optional.of(family));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createInvitationCode(user)
            );

            assertEquals(ErrorCode.INVITATION_CODE_REISSUE_TOO_FAST, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 아이디가 없는 경우")
        public void should_fail_reissue_invitation_not_found_family() {
            User user = createUser();

            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createInvitationCode(user)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reissue_invitation() {
            Family family = Family.builder()
                    .invitationCode("sds1234d")
                    .build();

            User user = createUser();
            user.joinFamilyLeader(1L);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(invitationCodeGenerator.generateUniqueCode()).willReturn("12dd1sxg");

            FamilyInvitationDto.CodeResponse response = familyService.createInvitationCode(user);

            assertThat("12dd1sxg").isEqualTo(response.newInvitationCode());
        }
    }

    @Nested
    @DisplayName("가족 멤버 리스트 불러오기")
    class getMembers {
        @Test
        @DisplayName("가족 구성원이 없을 경우")
        public void should_fail_get_members_not_found_family() {
            User user = createUser();
            given(familyRepository.findById(any())).willReturn(Optional.empty());
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.getMembers(user)
            );
            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("멤버가 없을 경우")
        public void should_success_get_members_not_found_members() {
            User user = createUser();
            user.joinFamilyLeader(1L);
            Family family = Family.builder()
                    .build();

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamilyId(1L)).willReturn(null);

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(user);

            assertThat(response).isEqualTo(List.of());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_get_members() {
            User user = createCustomUser("testUser1", "test1111@gmail.com", "유저1");
            User user2 = createCustomUser("testUser2", "test2222@gmail.com", "유저2");
            User user3 = createCustomUser("testUser3", "test3333@gmail.com", "유저3");

            user.joinFamilyLeader(1L);
            user2.joinFamilyLeader(1L);
            user3.joinFamilyLeader(1L);

            Family family = Family.builder()
                    .build();

            List<User> userList = List.of(user, user2, user3);
            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamilyId(any())).willReturn(userList);

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(user);
            System.out.println(response);
            assertThat(response.getFirst().nickname()).isEqualTo("유저1");
        }
    }

    @Nested
    @DisplayName("가족 그룹 삭제")
    class DisbandFamily{
        @Test
        @DisplayName("가족 아이디가 없는 경우")
        public void should_fail_disband_family_not_family() {
            User user = createUser();
            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(user));

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 그룹원이 남아 있는 경우")
        public void should_fail_disband_family_exists_member(){
            User user = createUser();
            User user2 = createUser();
            Family family = Family.builder()
                    .build();

            given(familyRepository.findById(any())).willReturn(Optional.ofNullable(family));
            given(userRepository.findAllByFamilyId(any())).willReturn(List.of(user, user2));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(user));

            assertEquals(ErrorCode.FAMILY_MEMBER_EXISTS, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_disband_family() {
            User user = createUser();
            Family family = Family.builder()
                    .build();

            given(familyRepository.findById(any())).willReturn(Optional.ofNullable(family));
            given(userRepository.findAllByFamilyId(any())).willReturn(List.of(user));

            assertDoesNotThrow(() -> familyService.disbandFamily(user));

            verify(familyRepository).delete(family);
        }

    }
}
