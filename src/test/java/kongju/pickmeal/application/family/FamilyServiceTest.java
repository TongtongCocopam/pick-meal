package kongju.pickmeal.application.family;

import java.util.Optional;

import kongju.pickmeal.core.family.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.FamiliesRequest;
import kongju.pickmeal.application.family.data.FamiliesResponse;


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
                .build();
    }

    @Nested
    @DisplayName("가족 그룹 생성 테스트")
    class FamilyCreate {
        @Test
        @DisplayName("소속된 가족이 있을 경우")
        public void should_fail_already_exist_family() {
            FamiliesRequest.Create request = FamiliesRequest.Create.builder()
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
            FamiliesRequest.Create request = FamiliesRequest.Create.builder()
                    .familyName("고양이")
                    .build();

            User user = createUser();

            FamiliesResponse.Create response = familyService.createFamily(request, user);
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
            FamiliesRequest.Apply request = FamiliesRequest.Apply.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            user.joinFamilyMember(12L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.apply(request, user);
            });

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("초대 코드를 찾지 못한 경우")
        public void should_fail_apply_invitation_code_not_found() {
            FamiliesRequest.Apply request = FamiliesRequest.Apply.builder()
                    .build();

            User user = createUser();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.apply(request, user);
            });

            assertEquals(ErrorCode.INVALID_INVITATION_CODE, exception.getErrorCode());
        }

        @Test
        @DisplayName("이미 신청한 경우")
        public void should_fail_apply_already_exists() {
            FamiliesRequest.Apply request = FamiliesRequest.Apply.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyApplyRepository.checkPendingApply(eq(user.getId()), eq(family.getId()), eq(ApplyStatus.PENDING))).willReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.apply(request, user);
            });

            assertEquals(ErrorCode.ALREADY_PROCESSED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_apply() {
            FamiliesRequest.Apply request = FamiliesRequest.Apply.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = createUser();

            Family family = Family.builder()
                    .build();

            given(familyRepository.findByInvitationCode(anyString())).willReturn(Optional.of(family));
            given(familyApplyRepository.checkPendingApply(eq(user.getId()), eq(family.getId()), eq(ApplyStatus.PENDING))).willReturn(false);

            // 오류 없이 실행되었는지 체크
            assertDoesNotThrow(() -> familyService.apply(request, user));

            verify(familyApplyRepository, times(1)).save(any(JoinApply.class));
        }
    }


}
