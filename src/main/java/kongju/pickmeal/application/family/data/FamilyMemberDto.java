package kongju.pickmeal.application.family.data;


import lombok.Builder;

public class FamilyMemberDto {
    @Builder
    public record KickResponse(
            String kickedNickname
    ){}

    @Builder
    public record ListItem(
            Long id,
            String nickname
    ) {
    }
}
