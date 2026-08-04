package kongju.pickmeal.application.family;

import java.util.UUID;
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
import static kongju.pickmeal.support.fixture.MenuFixture.menu;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import kongju.pickmeal.core.family.*;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.user.type.UserRole;
import kongju.pickmeal.application.family.data.*;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.user.PickCountHistory;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.core.family.repository.FamilyRepository;
import kongju.pickmeal.core.family.repository.FamilyJoinRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;

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
    @Mock
    private DietRepository dietRepository;
    @Mock
    private DietGenerationRepository dietGenerationRepository;
    @Mock
    private UserPickCountRepository userPickCountRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PickCountHistoryRepository pickCountHistoryRepository;
    @Mock
    private UserReader userReader;

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
            given(userReader.getById(any())).willReturn(user);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createFamily(request, 1L));

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 그룹 성공적으로 생성")
        public void should_success_create_family() {
            FamilyDto.CreateRequest request = FamilyDto.CreateRequest.builder()
                    .familyName("고양이")
                    .build();

            User user = user();
            given(userReader.getById(any())).willReturn(user);

            FamilyDto.CreateResponse response = familyService.createFamily(request, 1L);
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

            given(userReader.getById(any())).willReturn(user);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, 1L)
            );

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("초대 코드를 찾지 못한 경우")
        public void should_fail_apply_invitation_code_not_found() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .build();

            User user = user();
            given(userReader.getById(any())).willReturn(user);

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, 1L)
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

            Family family = family();
            given(userReader.getById(any())).willReturn(user);

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family), eq(ApplyStatus.PENDING)))
                    .willReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.joinRequest(request, 1L)
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

            Family family = family();

            given(userReader.getById(any())).willReturn(user);

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyJoinRepository.checkPendingRequest(eq(user), eq(family), eq(ApplyStatus.PENDING)))
                    .willReturn(false);

            // 오류 없이 실행되었는지 체크
            assertDoesNotThrow(() -> familyService.joinRequest(request, 1L));

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

            given(userReader.getById(any())).willReturn(user);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.loadJoinRequestSummary(1L)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_roadApply() {
            User user = user();
            Family family = family();
            user.joinFamilyMember(family);

            given(userReader.getById(any())).willReturn(user);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(user, family);

            List<FamilyJoinRequest> familyJoinRequestList = new ArrayList<>();
            familyJoinRequestList.add(familyJoinRequest);

            given(familyJoinRepository.findAllByFamilyAndStatus(any(), any())).willReturn(familyJoinRequestList);

            assertDoesNotThrow(() -> familyService.loadJoinRequestSummary(1L));
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

            given(userReader.getById(any())).willReturn(user);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, 2L)
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
            given(userReader.getById(any())).willReturn(user);

            given(familyJoinRepository.findById(any())).willReturn(Optional.empty());
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, 2L)
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
            given(userReader.getById(any())).willReturn(user);

            Family family = family();
            user.joinFamilyLeader(family);

            Family family1 = family();
            User user2 = user("custom", "custom1234@gmail.com", "냠냠짬", "password1234");
            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(user2, family1);

            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, 2L)
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
            given(userReader.getById(any())).willReturn(user);

            Family family = family();
            user.joinFamilyLeader(family);

            User user2 = user("custom", "custom1234@gmail.com", "배고파", "password1234");
            user2.joinFamilyLeader(family);

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(user2, family);

            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.processJoinRequest(1L, request, 2L)
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
            given(userReader.getById(any())).willReturn(user);

            Family family = family();
            user.joinFamilyLeader(family);

            User user2 = user("custom", "custom1234@gmail.com", "배불러", "password1234");

            FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(user2, family);

            given(familyJoinRepository.findById(any())).willReturn(Optional.ofNullable(familyJoinRequest));

            FamilyJoinRequestDto.ProcessResponse response = familyService.processJoinRequest(1L, request, 2L);

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

            given(userReader.getById(any())).willReturn(user);
            given(familyRepository.findById(any())).willReturn(Optional.of(family));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createInvitationCode(1L)
            );

            assertEquals(ErrorCode.INVITATION_CODE_REISSUE_TOO_FAST, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 아이디가 없는 경우")
        public void should_fail_reissue_invitation_not_found_family() {
            User user = user();

            given(userReader.getById(any())).willReturn(user);
            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.createInvitationCode(1L)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reissue_invitation() {
            Family family = family();

            User user = user();
            given(userReader.getById(any())).willReturn(user);

            user.joinFamilyLeader(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(invitationCodeGenerator.generateUniqueCode()).willReturn("12dd1sxg");

            FamilyInvitationDto.CodeResponse response = familyService.createInvitationCode(1L);

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

            given(userReader.getById(any())).willReturn(user);
            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.getMembers(1L)
            );
            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("멤버가 없을 경우")
        public void should_success_get_members_not_found_members() {
            User user = user();

            given(userReader.getById(any())).willReturn(user);

            Family family = family();
            user.joinFamilyLeader(family);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(family)).willReturn(List.of(user));

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(1L);

            assertThat(response.getFirst().nickname()).isEqualTo(user.getNickname());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_get_members() {
            User user = user("유저1", "testUser1", "test1111@gmail.com", "password1234");
            User user2 = user("유저2", "testUser2", "test2222@gmail.com", "password1234");
            User user3 = user("유저3", "testUser3", "test3333@gmail.com", "password1234");

            given(userReader.getById(any())).willReturn(user);

            Family family = family();
            user.joinFamilyLeader(family);
            user2.joinFamilyLeader(family);
            user3.joinFamilyLeader(family);

            List<User> userList = List.of(user, user2, user3);
            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(userList);

            List<FamilyMemberDto.ListItem> response = familyService.getMembers(1L);
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

            given(userReader.getById(any())).willReturn(user);
            given(familyRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(1L));

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

            given(userReader.getById(any())).willReturn(user);
            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user, user2));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.disbandFamily(1L));

            assertEquals(ErrorCode.FAMILY_MEMBER_EXISTS, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_disband_family() {
            User user = user();
            Family family = family();

            user.joinFamilyLeader(family);

            given(userReader.getById(any())).willReturn(user);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user));
            Menu menu1 = menu();
            Menu menu2 = menu();

            given(menuRepository.findAllByFamily(any())).willReturn(List.of(menu1, menu2));

            assertDoesNotThrow(() -> familyService.disbandFamily(1L));

            verify(familyRepository).delete(family);
        }

    }

    @Nested
    @DisplayName("멤버 방출")
    class KickMember {
        @Test
        @DisplayName("존재하지 않는 아이디인 경우")
        public void should_fail_kick_member_not_found() {
            given(userReader.getById(1L)).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            User user = user();
            given(userReader.getById(2L)).willReturn(user);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.kickMember(1L, 2L)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("가족 멤버가 아닌 경우")
        public void should_fail_kick_member_not_my_family() {
            User member = user();
            given(userReader.getById(1L)).willReturn(member);

            User leader = user();
            given(userReader.getById(2L)).willReturn(leader);
            given(userRepository.existsByIdAndFamily(any(), any())).willReturn(false);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.kickMember(1L, 2L)
            );

            assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_kick_member() {
            User member = user("testNickname", "test", "test1234@gmail.com", "password1234");
            User leader = user("testNickname1", "test1", "test12222@gmail.com", "password1234");
            given(userReader.getById(1L)).willReturn(member);
            given(userReader.getById(2L)).willReturn(leader);

            given(userRepository.existsByIdAndFamily(any(), any())).willReturn(true);

            UserPickCount userPickCount = UserPickCount.initialize(member);
            given(userPickCountRepository.findByUser(member)).willReturn(Optional.of(userPickCount));

            FamilyMemberDto.KickResponse response = familyService.kickMember(1L, 2L);
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

            given(userReader.getById(1L)).willReturn(user);
            given(familyRepository.findById(any())).willReturn(null);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.leaveMember(1L)
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
            given(userReader.getById(1L)).willReturn(user);

            given(familyRepository.findById(any())).willReturn(Optional.of(family));
            UserPickCount userPickCount = UserPickCount.initialize(user);
            given(userPickCountRepository.findByUser(user)).willReturn(Optional.of(userPickCount));

            assertDoesNotThrow(() -> familyService.leaveMember(1L));
            assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
        }
    }

    @Nested
    @DisplayName("선택권 분배")
    class PickAllocation {
        @Test
        @DisplayName("유저를 찾지 못한 경우")
        public void should_fail_pick_allocation_user_not_found() {
            Long leaderId = 1L;
            Long userId = 2L;

            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            given(userReader.getById(leaderId)).willReturn(user);

            FamilyPickDto.UpdateConfigRequest.PickAllocations pickAllocations =
                    FamilyPickDto.UpdateConfigRequest.PickAllocations.builder()
                            .userId(userId)
                            .pickCount(null)
                            .build();

            FamilyPickDto.UpdateConfigRequest request =
                    FamilyPickDto.UpdateConfigRequest.builder()
                            .isAutoAllocations(false)
                            .defaultAllocations(2L)
                            .pickAllocations(List.of(pickAllocations))
                            .build();

            given(userReader.getById(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.pickConfig(leaderId, request)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("내 가족이 아닌 경우")
        public void should_fail_pick_allocation_not_my_family() {
            Long leaderId = 1L;
            Long userId = 2L;

            User user = user();
            Family family = family("family");
            user.joinFamilyLeader(family);

            given(userReader.getById(leaderId)).willReturn(user);

            User user2 = user("test22", "test2222@gmail.com", "testNickname", "password1234");
            Family family2 = family("family2");
            user2.joinFamilyLeader(family2);

            given(userReader.getById(userId)).willReturn(user2);

            FamilyPickDto.UpdateConfigRequest.PickAllocations pickAllocations =
                    FamilyPickDto.UpdateConfigRequest.PickAllocations.builder()
                            .userId(userId)
                            .pickCount(2L)
                            .build();

            FamilyPickDto.UpdateConfigRequest request = FamilyPickDto.UpdateConfigRequest.builder()
                    .isAutoAllocations(false)
                    .defaultAllocations(null)
                    .pickAllocations(List.of(pickAllocations))
                    .build();

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> familyService.pickConfig(leaderId, request)
            );
            assertEquals(ErrorCode.NOT_YOUR_FAMILY_MEMBER, exception.getErrorCode());
        }

        @Test
        @DisplayName("선택권 분배")
        public void should_success_pick_allocation() {
            Long leaderId = 1L;
            Long userId = 2L;

            User user = user();
            Family family = family("family");
            user.joinFamilyLeader(family);

            given(userReader.getById(leaderId)).willReturn(user);

            User user2 = user("test22", "test2222@gmail.com",
                    "testNickname", "password1234");
            user2.joinFamilyLeader(family);

            FamilyPickDto.UpdateConfigRequest.PickAllocations pickAllocations =
                    FamilyPickDto.UpdateConfigRequest.PickAllocations.builder()
                            .userId(userId)
                            .pickCount(2L)
                            .build();

            FamilyPickDto.UpdateConfigRequest request =
                    FamilyPickDto.UpdateConfigRequest.builder()
                            .isAutoAllocations(false)
                            .defaultAllocations(null)
                            .pickAllocations(List.of(pickAllocations))
                            .build();

            given(userReader.getById(userId)).willReturn(user2);

            UserPickCount userPickCount = UserPickCount.initialize(user2);

            given(userPickCountRepository.findByUser(any())).willReturn(Optional.ofNullable(userPickCount));

            given(pickCountHistoryRepository.saveAll(any())).willReturn(List.of());

            FamilyPickDto.ConfigResponse response = familyService.pickConfig(leaderId, request);

            assertEquals(false, response.isAutoAllocations());
        }
    }

    @Nested
    @DisplayName("선택권 초기화")
    class ResetAllocation {
        @Test
        @DisplayName("유저가 한명 밖에 없을 경우")
        public void should_success_reset_allocation_user_not_found() {
            Long leaderId = 1L;

            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);

            given(userReader.getById(leaderId)).willReturn(user);
            given(userRepository.findAllByFamily(any())).willReturn(List.of(user));

            UserPickCount userPickCount = UserPickCount.initialize(user);
            given(userPickCountRepository.findByUser(any())).willReturn(Optional.of(userPickCount));

            PickCountHistory resetHistory = PickCountHistory.reset(user, UUID.randomUUID());
            given(pickCountHistoryRepository.save(any())).willReturn(resetHistory);

            familyService.resetConfig(leaderId);

            verify(userReader).getById(leaderId);
            verify(userRepository).findAllByFamily(family);
            verify(pickCountHistoryRepository).save(any());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_reset_allocation() {
            Long leaderId = 1L;

            User leader = user();
            Family family = family("family");
            leader.joinFamilyLeader(family);

            given(userReader.getById(leaderId)).willReturn(leader);

            User member1 = user("test1", "test1@mail", "test1", "password1234");
            member1.joinFamilyLeader(family);

            User member2 = user("test2", "test2@mail", "test2", "password1234");
            member2.joinFamilyLeader(family);

            given(userRepository.findAllByFamily(any())).willReturn(List.of(leader, member1, member2));

            UserPickCount userPickCount = UserPickCount.initialize(member1);
            given(userPickCountRepository.findByUser(any())).willReturn(Optional.of(userPickCount));

            UserPickCount userPickCount1 = UserPickCount.initialize(member2);
            given(userPickCountRepository.findByUser(any())).willReturn(Optional.of(userPickCount1));

            given(pickCountHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            familyService.resetConfig(leaderId);

            verify(userReader).getById(leaderId);
            verify(userPickCountRepository).findByUser(member1);
            verify(userPickCountRepository).findByUser(member2);
            verify(pickCountHistoryRepository, times(3)).save(any(PickCountHistory.class));
        }

    }

}
