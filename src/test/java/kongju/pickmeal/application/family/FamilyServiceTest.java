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

import kongju.pickmeal.core.family.*;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.family.data.FamilyDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.FamilyJoinRequestDto;


@ExtendWith(SpringExtension.class)
public class FamilyServiceTest {
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private FamilyApplyRepository familyApplyRepository;

    @InjectMocks
    private FamilyService familyService;

    public User createUser() {
        return User.builder()
                .loginId("testUser")
                .email("test1234@gmail.com")
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

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.createFamily(request, user);
            });

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

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.joinRequest(request, user);
            });

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("초대 코드를 찾지 못한 경우")
        public void should_fail_apply_invitation_code_not_found() {
            FamilyJoinRequestDto.CreateRequest request = FamilyJoinRequestDto.CreateRequest.builder()
                    .build();

            User user = createUser();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.joinRequest(request, user);
            });

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
            given(familyApplyRepository.checkPendingApply(eq(user), eq(family.getId()), eq(ApplyStatus.PENDING))).willReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    familyService.joinRequest(request, user)
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
            given(familyApplyRepository.checkPendingApply(eq(user), eq(family.getId()), eq(ApplyStatus.PENDING))).willReturn(false);

            // 오류 없이 실행되었는지 체크
            assertDoesNotThrow(() -> familyService.joinRequest(request, user));

            verify(familyApplyRepository, times(1)).save(any(FamilyJoinRequest.class));
        }
    }

    @Nested
    @DisplayName("가족 합류 신청 목록")
    class JoinSummary {
        @Test
        @DisplayName("가족 아이디가 없을때")
        public void should_fail_roadApply_null_familyId() {
            User user = createUser();

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    familyService.loadJoinRequestSummary(user)
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

            given(familyApplyRepository.findAllByFamilyIdAndStatus(any(), any())).willReturn(familyJoinRequestList);

            assertDoesNotThrow(() -> familyService.loadJoinRequestSummary(user));
            verify(familyApplyRepository, times(1)).findAllByFamilyIdAndStatus(any(), any());
        }
    }
}
