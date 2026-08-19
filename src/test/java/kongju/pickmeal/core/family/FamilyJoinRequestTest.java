package kongju.pickmeal.core.family;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

import static kongju.pickmeal.support.fixture.UserFixture.user;
import static kongju.pickmeal.support.fixture.FamilyFixture.family;


public class FamilyJoinRequestTest {
    @Test
    @DisplayName("가족 신청 거부")
    void join_request_reject(){
        FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(
                user(),
                family()
        );

        familyJoinRequest.reject();
        assertThat(familyJoinRequest.getStatus()).isEqualTo(ApplyStatus.REJECTED);
    }
}
