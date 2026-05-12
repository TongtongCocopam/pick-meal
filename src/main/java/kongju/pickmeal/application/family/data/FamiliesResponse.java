package kongju.pickmeal.application.family.data;

import lombok.Builder;

public class FamiliesResponse {
    @Builder
    public record Create(
            String familyName,
            String invitationCode
    ) {
    }

    @Builder
    public record ApplySummary(
            Long applyId,
            String nickname,
            String email
    ) {
    }
}
