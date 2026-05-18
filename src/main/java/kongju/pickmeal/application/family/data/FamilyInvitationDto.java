package kongju.pickmeal.application.family.data;

import lombok.Builder;

public class FamilyInvitationDto {
    @Builder
    public record CodeResponse(
            String newInvitationCode
    ){}
}
