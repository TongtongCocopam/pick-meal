package kongju.pickmeal.application.family;

import kongju.pickmeal.core.family.*;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.family.data.FamiliesRequest;
import kongju.pickmeal.application.family.data.FamiliesResponse;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final FamilyApplyRepository familyApplyRepository;

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

    /**
     * 가족 합류 신청
     *
     * @param request 초대 코드
     * @param user    신청한 유저 정보
     */
    public void apply(FamiliesRequest.Apply request, User user) {
        // 가족 여부 확인
        Family family = checkApply(request, user);

        // 신청 테이블 만들기
        JoinApply joinApply = JoinApply.builder()
                .user(user)
                .familyId(family.getId())
                .status(ApplyStatus.PENDING)
                .build();

        familyApplyRepository.save(joinApply);
    }


    /**
     * 가족 여부와 신청 확인
     *
     * @param request 초대 코드
     * @param user    신청한 유저
     * @return Family객체 반환
     */
    private Family checkApply(FamiliesRequest.Apply request, User user) {
        // 가족이 이미 있음
        if (user.getFamilyId() != null) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }

        // 가족이 있는 경우
        Family family = familyRepository.findByInvitationCode(request.invitationCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITATION_CODE));

        // 이미 신청한 경우
        if (familyApplyRepository.checkPendingApply(user, family.getId(), ApplyStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        return family;
    }

    /**
     * 가족 신청 목록 불러오기
     * @param user 리더 정보
     * @return 신청 리스트 반환
     */
    public List<FamiliesResponse.ApplyInfo> roadApplyList(User user) {
        // 가족이 없는지 확인
        Long familyId = checkFamily(user);

//        List<JoinApply> joinApplies = familyApplyRepository.findAllByFamilyIdAndStatus(familyId, ApplyStatus.PENDING);
//
//        List<FamiliesResponse.ApplyInfo> applyInfos = new ArrayList<>();
//        for (JoinApply joinApply : joinApplies) {
//            FamiliesResponse.ApplyInfo applyInfo = FamiliesResponse.ApplyInfo.from(joinApply);
//            applyInfos.add(applyInfo);
//        }

        // 유저 패밀리와 연관된 신청 리스트 가져오기
        return familyApplyRepository.findAllByFamilyIdAndStatus(familyId, ApplyStatus.PENDING)
                .stream()
                .map(FamiliesResponse.ApplyInfo::from)
                .toList();
    }

    /**
     * 가족 있는지 여부 확인
     * @param user 해당 유저 객체
     * @return 가족 아이디 반환
     */
    private Long checkFamily(User user) {
        Long familyId = user.getFamilyId();
        if (user.getFamilyId() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        return familyId;
    }
}
