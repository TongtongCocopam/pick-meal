package kongju.pickmeal.core.family;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

import static kongju.pickmeal.support.fixture.FamilyFixture.family;


public class FamilyTest {
    @Test
    @DisplayName("초대 코드 재발급 한적이 없는경우")
    public void  reissue_invitation_code_not_use(){
        Family family = family();
        family.reissueInvitationCode("st1454fs");
        assertThat(family.getInvitationCode()).isEqualTo("st1454fs");
    }

    @Test
    @DisplayName("초대 코드 재발급 한지 10분이 지나지 않은 경우")
    public void reissue_invitation_code_10분_안지남(){
        Family family = family();
        family.reissueInvitationCode("st1454fs");
        BusinessException exception = assertThrows(BusinessException.class,()->{
            family.reissueInvitationCode("11sdhg23");
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVITATION_CODE_REISSUE_TOO_FAST);
    }

    @Test
    @DisplayName("초대 코드 재발급 한지 10분이 지난 경우")
    public void reissue_invitation_code_10분_지남(){
        Family family = family();
        family.reissueInvitationCode("st1454fs");
        ReflectionTestUtils.setField(
                family,
                "invitationCodeUpdatedAt",
                LocalDateTime.now().minusMinutes(11)
        );
        // 11분 뒤...
        family.reissueInvitationCode("11sdhg23");

        assertThat(family.getInvitationCode()).isEqualTo("11sdhg23");
    }
}
