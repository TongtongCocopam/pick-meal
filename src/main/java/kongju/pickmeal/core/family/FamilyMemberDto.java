package kongju.pickmeal.core.family;

import lombok.Builder;

public class FamilyMemberDto {
    @Builder
    public record ListItem(
            Long id,
            String nickname
    ) {
    }
}
