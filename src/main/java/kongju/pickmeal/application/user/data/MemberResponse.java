package kongju.pickmeal.application.user.data;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    public record Register(
            String nickName
    ){}
}
