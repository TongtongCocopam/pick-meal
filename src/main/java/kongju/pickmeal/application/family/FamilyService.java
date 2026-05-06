package kongju.pickmeal.application.family;

import kongju.pickmeal.core.family.Family;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.family.FamilyRepository;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.FamiliesRequest;
import kongju.pickmeal.application.family.data.FamiliesResponse;

import java.security.SecureRandom;


@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;

    /**
     * 가족 만들기
     *
     * @param request 가족 이름
     * @param user    그룹을 만든 유저
     * @return 그룹 이름과 초대코드 반환
     */
    public FamiliesResponse.Create createFamily(FamiliesRequest.Create request, User user) {
        // 초대 코드 생성
        String invitationCode = generateInvitationCode();
        // 가족 엔티티 생성
        Family family = Family.builder()
                .familyName(request.familyName())
                .invitationCode(invitationCode)
                .leaderId(user.getId())
                .build();
        // 저장
        familyRepository.save(family);
        // 현재 유저를 리더로 가족 아이디와 연결
        user.joinFamilyLeader(family.getId());

        return FamiliesResponse.Create.builder()
                .familyName(family.getFamilyName())
                .invitationCode(invitationCode)
                .build();
    }

    /**
     * 중복이 아닐때까지 생성
     *
     * @return 초대 코드 반환
     */
    private String generateInvitationCode() {
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
