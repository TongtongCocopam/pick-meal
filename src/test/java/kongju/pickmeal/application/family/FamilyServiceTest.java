package kongju.pickmeal.application.family;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.family.FamilyRepository;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.FamiliesRequest;
import kongju.pickmeal.application.family.data.FamiliesResponse;

import java.util.Optional;


@ExtendWith(SpringExtension.class)
public class FamilyServiceTest {
    @Mock
    private FamilyRepository familyRepository;

    @InjectMocks
    private FamilyService familyService;

    @Nested
    @DisplayName("가족 그룹 생성 테스트")
    class FamilyCreate {
        @Test
        @DisplayName("소속된 가족이 있을 경우")
        public void should_fail_already_exist_family() {
            FamiliesRequest.Create request = FamiliesRequest.Create.builder()
                    .familyName("고양이")
                    .build();

            User user = User.builder()
                    .loginId("testUser")
                    .build();

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

            User user = User.builder()
                    .loginId("testUser")
                    .build();

            FamiliesResponse.Create response = familyService.createFamily(request, user);
            assertEquals(request.familyName(), response.familyName());
            verify(familyRepository, times(1)).save(any(Family.class));
        }

    }

    @Nested
    @DisplayName("가족 합류 신청")
    class FamilyApply{
        @Test
        @DisplayName("이미 가족이 있을 경우")
        public void should_fail_apply_already_exist_family() {
            FamiliesRequest.Apply request = FamiliesRequest.Apply.builder()
                    .invitationCode("초대코드라는뜻")
                    .build();

            User user = User.builder()
                    .loginId("testUser")
                    .build();

            user.joinFamilyMember(12L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                familyService.apply(request, user);
            });

            assertEquals(ErrorCode.ALREADY_HAS_FAMILY, exception.getErrorCode());
        }

        @Test
        @DisplayName("잘못된 초대 코드일 경우")
        public void should_fail_apply_unavailable_invitation_code(){

        }

        @Test
        @DisplayName("초대 코드를 찾지 못한 경우")
        public void should_fail_apply_invitation_code_not_found(){

        }

        @Test
        @DisplayName("이미 신청한 경우")
        public void should_fail_apply_already_exists(){

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_apply(){

        }
    }


}
