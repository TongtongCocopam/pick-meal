package kongju.pickmeal.application.family;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import kongju.pickmeal.core.family.*;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.type.UserRole;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.application.family.data.*;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

import static kongju.pickmeal.support.fixture.UserFixture.user;
import static kongju.pickmeal.support.fixture.FamilyFixture.family;


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

    @Nested
    @DisplayName("가족 그룹 생성 테스트")
    class FamilyCreate {
        @Test
        @DisplayName("소속된 가족이 있을 경우")
        public void should_fail_already_exist_family() {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("family")
                    .build();

            User user = user();
            Family family = family();

            user.joinFamilyLeader(family);

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

            User user = user();

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

            User user = user();

            Family family = family();
            user.joinFamilyMember(family);

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

            User user = user();

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

            User user = user();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family), eq(ApplyStatus.PENDING)))
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

            User user = user();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family), eq(ApplyStatus.PENDING)))
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
            User user = user();

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.loadJoinRequestSummary(user)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_roadApply() {
            User user = user();
            Family family = family();
            user.joinFamilyMember(family);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .family(family)
                    .status(ApplyStatus.PENDING)
                    .user(user)
                    .build();

            List<FamilyJoinRequest> familyJoinRequestList = new ArrayList<>();
            familyJoinRequestList.add(familyJoinRequest);

            given(familyJoinRepository.findAllByFamilyAndStatus(any(), any())).willReturn(familyJoinRequestList);

            assertDoesNotThrow(() -> familyService.loadJoinRequestSummary(user));
            verify(familyJoinRepository, times(1)).findAllByFamilyAndStatus(any(), any());
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

            User user = user();
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

            User user = user();
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

            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            Family family1 = Family.builder().build();
            User user2 = user("custom", "custom1234@gmail.com", "냠냠짬", "password1234");
            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .family(family1)
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

            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            User user2 = user("custom", "custom1234@gmail.com", "배고파", "password1234");
            user2.joinFamilyLeader(family);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .family(family)
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

            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            User user2 = user("custom", "custom1234@gmail.com", "배불러", "password1234");

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.builder()
                    .family(family)
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
            Family family = family();

            User user = user();
            user.joinFamilyLeader(family);

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
            User user = user();

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
            Family family = family();

            User user = user();
            user.joinFamilyLeader(family);

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
            User user = user();
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
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(family)).willReturn(List.of(user));

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(user);

            assertThat(response.getFirst().nickname()).isEqualTo(user.getNickName());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_get_members() {
            User user = user("testUser1", "test1111@gmail.com", "유저1", "password1234");
            User user2 = user("testUser2", "test2222@gmail.com", "유저2", "password1234");
            User user3 = user("testUser3", "test3333@gmail.com", "유저3", "password1234");

            Family family = family();
            user.joinFamilyLeader(family);
            user2.joinFamilyLeader(family);
            user3.joinFamilyLeader(family);

            List<User> userList = List.of(user, user2, user3);
            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(userList);

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(user);
            System.out.println(response);
            assertThat(response.getFirst().nickname()).isEqualTo("유저1");
        }
    }

    @Nested
    @DisplayName("가족 그룹 삭제")
    class DisbandFamily {
        @Test
        @DisplayName("가족 아이디가 없는 경우")
        public void should_fail_disband_family_not_family() {
            User user = user();
            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(user));

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 그룹원이 남아 있는 경우")
        public void should_fail_disband_family_exists_member() {
            Family family = family();

            User user = user();
            user.joinFamilyLeader(family);

            User user2 = user();
            user2.joinFamilyMember(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user, user2));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(user));

            assertEquals(ErrorCode.FAMILY_MEMBER_EXISTS, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_disband_family() {
            User user = user();
            Family family = Family.builder()
                    .build();
            user.joinFamilyLeader(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user));

            assertDoesNotThrow(() -> familyService.disbandFamily(user));

            verify(familyRepository).delete(family);
        }

    }

    @Nested
    @DisplayName("멤버 방출")
    class KickMember {
        @Test
        @DisplayName("존재하지 않는 아이디인 경우")
        public void should_fail_kick_member_not_found() {
            User user = user();
            given(userRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.kickMember(1L, user)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 멤버가 아닌 경우")
        public void should_fail_kick_member_not_my_family() {
            User user = user();
            given(userRepository.findById(any())).willReturn(Optional.of(user));
            given(userRepository.existsByIdAndFamily(any(), any())).willReturn(false);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.kickMember(1L, user)
            );

            assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_kick_member() {
            User user = user("test", "test1234@gmail.com", "testNickname", "password1234");
            given(userRepository.findById(any())).willReturn(Optional.of(user));
            given(userRepository.existsByIdAndFamily(any(), any())).willReturn(true);

            FamilyMemberDto.KickResponse response = familyService.kickMember(1L, user);
            assertThat(response.kickedNickname()).isEqualTo("testNickname");
        }
    }

    @Nested
    @DisplayName("그룹 나가기")
    class LeaveFamily {
        @Test
        @DisplayName("가족 멤버가 아닌 경우")
        public void should_fail_kick_member_not_my_family() {
            User user = user();

            given(userRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.leaveMember(user)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_kick_member() {
            Family family = family();

            User user = user();
            User user2 = user("test", "test1234@gmail.com", "testNickname", "password1234");
            user.joinFamilyLeader(family);
            user2.joinFamilyMember(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));

            assertDoesNotThrow(() -> familyService.leaveMember(user));
            assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
        }
    }

    @Nested
    @DisplayName("선택권 분배")
    class PickAllocation {
        @Test
        @DisplayName("유저를 찾지 못한 경우")
        public void should_fail_pick_allocation_user_not_found() {
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);
            User user2 = user("test22", "test2222@gmail.com", "testNickname", "password1234");
            user2.joinFamilyLeader(family);

            FamilyPickDto.UpdateConfigRequest.pickAllocations pickAllocations = FamilyPickDto.UpdateConfigRequest.pickAllocations.builder()
                    .userId(user2.getId())
                    .pickCount(null)
                    .build();


            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .isAutoAllocations(false)
                    .defaultAllocations(2L)
                    .pickAllocations(List.of(pickAllocations))
                    .build();

            given(userRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.pickConfig(user, request)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("내 가족이 아닌 경우")
        public void should_fail_pick_allocation_not_my_family() {
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            Family family2 = family();
            User user2 = user("test22", "test2222@gmail.com", "testNickname", "password1234");
            user2.joinFamilyLeader(family2);

            FamilyPickDto.UpdateConfigRequest.pickAllocations pickAllocations = FamilyPickDto.UpdateConfigRequest.pickAllocations.builder()
                    .userId(user2.getId())
                    .pickCount(2L)
                    .build();


            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .isAutoAllocations(false)
                    .defaultAllocations(null)
                    .pickAllocations(List.of(pickAllocations))
                    .build();

            given(userRepository.findById(any())).willReturn(Optional.of(user2));


            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.pickConfig(user, request)
            );

            assertEquals(ErrorCode.NOT_YOUR_FAMILY_MEMBER, exception.getErrorCode());
        }

        @Test
        @DisplayName("선택권 분배")
        public void should_success_pick_allocation() {
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);
            User user2 = user("test22", "test2222@gmail.com", "testNickname", "password1234");
            user2.joinFamilyLeader(family);

            FamilyPickDto.UpdateConfigRequest.pickAllocations pickAllocations = FamilyPickDto.UpdateConfigRequest.pickAllocations.builder()
                    .userId(user2.getId())
                    .pickCount(2L)
                    .build();


            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .isAutoAllocations(false)
                    .defaultAllocations(null)
                    .pickAllocations(List.of(pickAllocations))
                    .build();

            given(userRepository.findById(any())).willReturn(Optional.of(user2));


            familyService.pickConfig(user, request);

            assertEquals(2L, user2.getPickCount());
        }

    }

    @Nested
    @DisplayName("선택권 초기화")
    class ResetAllocation {
        @Test
        @DisplayName("유저가 한명 밖에 없을 경우")
        public void should_success_reset_allocation_user_not_found() {
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);
            user.setPickCount(5L);
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user));

            familyService.resetConfig(user);

            assertThat(user.getPickCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reset_allocation() {
            User leader = user();
            Family family = family();
            leader.joinFamilyLeader(family);
            leader.setPickCount(5L);

            User member1 = user("test1", "test1@mail", "test1", "password1234");
            member1.joinFamilyLeader(family);

            User member2 = user("test2", "test2@mail", "test2", "password1234");
            member2.joinFamilyLeader(family);

            given(userRepository.findAllByFamily(any())).willReturn(List.of(leader, member1, member2));

            familyService.resetConfig(leader);

            assertThat(leader.getPickCount()).isEqualTo(0);
            assertThat(member1.getPickCount()).isEqualTo(0);
            assertThat(member2.getPickCount()).isEqualTo(0);
        }

    }

}
