package kongju.pickmeal.application.family.data;

import lombok.Builder;

import kongju.pickmeal.core.family.JoinApply;

public class FamiliesResponse {
    @Builder
    public record Create(
            String familyName,
            String invitationCode
    ) {
    }

    @Builder
    public record ApplyInfo(
            Long applyId,
            String nickname,
            String email
    ) {

        public static ApplyInfo from(JoinApply joinApply) {
            return ApplyInfo.builder()
                    .applyId(joinApply.getId())
                    .email(maskEmail(joinApply.getUser().getEmail()))
                    .nickname(joinApply.getUser().getNickName())
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


}
