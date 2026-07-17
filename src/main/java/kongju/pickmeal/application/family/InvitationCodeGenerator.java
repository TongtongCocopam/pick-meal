package kongju.pickmeal.application.family;

import java.security.SecureRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.family.repository.FamilyRepository;

@Component
@RequiredArgsConstructor
public class InvitationCodeGenerator {
    private final FamilyRepository familyRepository;

    /**
     * 중복이 아닐때까지 생성
     *
     * @return 초대 코드 반환
     */
    public String generateUniqueCode() {
        String invitationCode;
        do {
            invitationCode = generate();
        } while (familyRepository.existsByInvitationCode(invitationCode));
        return invitationCode;
    }

    /**
     * 랜덤 문자열 생성기
     *
     * @return 8자리의 문자열 반환
     */
    private String generate() {
        String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
