package kongju.pickmeal.application.family.data;

import lombok.Builder;

public class FamiliesResponse {
    @Builder
    public record Create(
            String familyName,
            String invitationCode
    ) {
    }
}
