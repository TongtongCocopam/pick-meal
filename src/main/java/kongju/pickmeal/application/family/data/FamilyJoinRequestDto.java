package kongju.pickmeal.application.family.data;

import lombok.Builder;
import jakarta.validation.constraints.Size;
import kongju.pickmeal.core.family.FamilyJoinRequest;
import jakarta.validation.constraints.NotBlank;


public class FamilyJoinRequestDto {
    @Builder
    public record CreateRequest(
            @NotBlank(message = "초대 코드는 필수입니다.")
            @Size(min = 8, max = 8, message = "초대 코드는 8자리여야 합니다.")
            String invitationCode
    ){}

    @Builder
    public record Summary(
            Long requestId,
            String nickname,
            String email
    ){
        public static FamilyJoinRequestDto.Summary from(FamilyJoinRequest familyJoinRequest) {
            return FamilyJoinRequestDto.Summary.builder()
                    .requestId(familyJoinRequest.getId())
                    .email(maskEmail(familyJoinRequest.getUser().getEmail()))
                    .nickname(familyJoinRequest.getUser().getNickName())
                    .build();
        }

        public static String maskEmail(String email) {
            if (email == null || !email.contains("@")) {
                return email;
            }

            String[] parts = email.split("@");
            String id = parts[0];
            String mail = parts[1];

            if (id.length() <= 4) {
                return id.charAt(0) + "****@" + mail;
            }

            String prefix = id.substring(0, 2);
            String suffix = id.substring(id.length() - 2);
            String asterisks = "*".repeat(id.length() - 4);

            return prefix + asterisks + suffix + "@" + mail;
        }
    }
    public record DecisionRequest(){}
    public record DecisionResponse(){}

}
