package kongju.pickmeal.application.family.data;

import lombok.Builder;

public class FamilyInvitationDto {
    @Builder
    public record CurrentCodeResponse(
            String invitationCode
    ){}

    @Builder
    public record CodeResponse(
            String newInvitationCode
    ){}
}
