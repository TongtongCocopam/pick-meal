package kongju.pickmeal.application.family;

import java.util.UUID;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.family.*;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.user.type.UserRole;
import kongju.pickmeal.application.family.data.*;
import kongju.pickmeal.core.user.PickCountHistory;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.core.family.repository.FamilyRepository;
import kongju.pickmeal.core.family.repository.FamilyJoinRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final UserReader userReader;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final DietRepository dietRepository;
    private final FamilyRepository familyRepository;
    private final FamilyJoinRepository familyJoinRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;
    private final UserPickCountRepository userPickCountRepository;
    private final DietGenerationRepository dietGenerationRepository;
    private final PickCountHistoryRepository pickCountHistoryRepository;

    /**
     * 가족 만들기
     *
     * @param request 가족 이름
     * @param userId  그룹을 만든 유저
     * @return 그룹 이름과 초대코드 반환
     */
    public FamilyDto.CreateResponse createFamily(FamilyDto.CreateRequest request, Long userId) {
        // 초대 코드 생성
        String invitationCode = invitationCodeGenerator.generateUniqueCode();
        // 가족 엔티티 생성
        Family family = Family.create(request.familyName(), invitationCode);

        // 저장
        familyRepository.save(family);
        // 현재 유저를 리더로 가족 아이디와 연결
        User user = userReader.getById(userId);

        user.joinFamilyLeader(family);

        return FamilyDto.CreateResponse.builder()
                .familyName(family.getFamilyName())
                .invitationCode(invitationCode)
                .build();
    }

    /**
     * 가족 합류 신청
     *
     * @param request 초대 코드
     * @param userId  신청한 유저 정보
     */
    public void joinRequest(FamilyJoinRequestDto.CreateRequest request, Long userId) {
        User user = userReader.getById(userId);

        // 가족 여부 확인
        Family family = validationJoinRequest(request.invitationCode(), user);

        // 신청 테이블 만들기
        FamilyJoinRequest familyJoinRequest = FamilyJoinRequest.create(user, family);

        familyJoinRepository.save(familyJoinRequest);
    }


    /**
     * 가족 여부와 신청 확인
     *
     * @param invitationCode 초대 코드
     * @param user           신청한 유저
     * @return Family객체 반환
     */
    private Family validationJoinRequest(String invitationCode, User user) {
        // 가족이 이미 있음
        if (user.getFamily() != null) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }

        // 초대코드와 일치하는 가족그룹이 없는 경우
        Family family = familyRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITATION_CODE));

        // 이미 신청한 경우
        if (familyJoinRepository.checkPendingRequest(user, family, ApplyStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        return family;
    }

    /**
     * 가족 신청 목록 불러오기
     *
     * @param userId 리더 정보
     * @return 신청 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<FamilyJoinRequestDto.Summary> loadJoinRequestSummary(Long userId) {
        User user = userReader.getById(userId);

        // 가족이 없는지 확인
        Family family = validationFamily(user.getFamily());

        // 유저 패밀리와 연관된 신청 리스트 가져오기
        return familyJoinRepository.findAllByFamilyAndStatus(family, ApplyStatus.PENDING)
                .stream()
                .map(FamilyJoinRequestDto.Summary::from)
                .toList();
    }

    /**
     * 가족 있는지 여부 확인
     *
     * @param family 해당 유저의 가족 객체
     * @return 가족 아이디 반환
     */
    private Family validationFamily(Family family) {
        if (family == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
        return family;
    }

    /**
     * 가족 합류 신청 승인, 거절
     *
     * @param requestId 요청 아이디
     * @param request   승인, 거절 여부
     * @param userId    리더 userId
     * @return 승인, 닉네임, 요청 아이디
     */
    public FamilyJoinRequestDto.ProcessResponse processJoinRequest(
            Long requestId,
            FamilyJoinRequestDto.ProcessRequest request,
            Long userId
    ) {
        User user = userReader.getById(userId);

        validateDecision(request.decision());

        // 존재하는 요청 아이디인지 확인하고 테이블 불러오기
        FamilyJoinRequest familyJoinRequest = getFamilyJoinRequest(requestId, user.getFamily());

        // 가족 구성원인지 확인 and 일반 user 인지도 확인?
        User joinRequestUser = familyJoinRequest.getUser();
        validationUser(joinRequestUser);

        // 거절하는 경우 테이블 상태 변경
        if (request.decision() == JoinRequestStatus.REJECTED) {
            familyJoinRequest.reject();

            return toProcessRequest(
                    requestId,
                    joinRequestUser.getNickname(),
                    JoinRequestStatus.REJECTED
            );
        }

        // 승인하는 경우 테이블 상태 변경
        familyJoinRequest.accept();

        // user테이블에 가족 테이블 연결, 멤버로 상태 변경
        joinRequestUser.joinFamilyMember(user.getFamily());

        return toProcessRequest(
                requestId,
                joinRequestUser.getNickname(),
                JoinRequestStatus.APPROVED
        );
    }

    /**
     * 승인, 거절 형식 확인
     *
     * @param decision 승인, 거절 여부
     */
    private void validateDecision(JoinRequestStatus decision) {
        if (decision == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 요청 기록 확인
     *
     * @param requestId 요청 아이디
     * @param family    가족 객체
     * @return 요청 테이블
     */
    private FamilyJoinRequest getFamilyJoinRequest(Long requestId, Family family) {
        FamilyJoinRequest joinRequest = familyJoinRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));

        if (!joinRequest.getFamily().equals(family)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_FAMILY_REQUEST);
        }

        return joinRequest;
    }

    /**
     * 유저 권한 검증, 가족이 없는지 확인
     *
     * @param user 신청 유저
     */
    private void validationUser(User user) {
        if (user.getFamily() != null || user.getRole() != UserRole.GUEST) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }
    }

    /**
     * 승인, 거절 응답 생성
     *
     * @param requestId 요청 아이디
     * @param nickname  닉네임
     * @param status    승인 거절 여부
     * @return dto객체
     */
    private FamilyJoinRequestDto.ProcessResponse toProcessRequest(Long requestId, String nickname, JoinRequestStatus status) {
        return FamilyJoinRequestDto.ProcessResponse.builder()
                .requestId(requestId)
                .nickname(nickname)
                .decision(status)
                .build();
    }

    /**
     * 가족이 있는지 체크 후 가족 객체 반환
     *
     * @param user 가족 체크할 유저 객체
     * @return family 객체
     */
    private Family validationFamily(User user) {
        if (user.getFamily() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }

        Long familyId = user.getFamily().getId();

        return familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
    }

    /**
     * 초대 코드 재발급
     *
     * @param userId 재발급 리더
     * @return 재발급한 초대코드
     */
    public FamilyInvitationDto.CodeResponse createInvitationCode(Long userId) {
        User user = userReader.getById(userId);

        String invitationCode = invitationCodeGenerator.generateUniqueCode();

        Family family = validationFamily(user);

        family.reissueInvitationCode(invitationCode);

        return FamilyInvitationDto.CodeResponse.builder()
                .newInvitationCode(invitationCode)
                .build();
    }


    /**
     * 가족 멤버 리스트 불러오기
     *
     * @param userId 가족이 있는 유저
     * @return 가족 구성원 리스트
     */
    @Transactional(readOnly = true)
    public List<FamilyMemberDto.ListItem> getMembers(Long userId) {
        User user = userReader.getById(userId);
        // 가족이 있는지 확인
        Family family = validationFamily(user);

        // 가족 id를 외래키로 가지고 있는 user리스트 가져오기
        return userRepository.findAllByFamily(family)
                .stream()
                .map(member -> FamilyMemberDto.ListItem.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .build())
                .toList();
    }

    /**
     * 가족 그룹 삭제
     *
     * @param userId 유저 객체
     */
    public void disbandFamily(Long userId) {
        User user = userReader.getById(userId);
        // 가족 그룹이 있는지 확인하고 family가져오기
        Family family = validationFamily(user);

        // member가 있는지 확인
        List<User> userList = userRepository.findAllByFamily(family);

        // 멤버가 있으면 실패
        if (userList.size() != 1) {
            throw new BusinessException(ErrorCode.FAMILY_MEMBER_EXISTS);
        }

        deleteFamilyRelatedData(family);
        // member가 없으면 없애기
        familyRepository.delete(family);
        user.leaveFamily();
    }

    private void deleteFamilyRelatedData(Family family) {
        familyJoinRepository.deleteAllByFamily(family);
        // 가족 메뉴 삭제
        List<Menu> menus = menuRepository.findAllByFamily(family);
        // 가족 메뉴 재료 연결 테이블 삭제
        menuRepository.deleteAll(menus);
        // pickCountHistory 삭제
        pickCountHistoryRepository.deleteAllByUser_Family(family);
        // userPickCount 삭제
        userPickCountRepository.deleteAllByUser_Family(family);
        // diet 삭제
        dietRepository.deleteAllByFamily(family);
        // dietGeneration 삭제
        dietGenerationRepository.deleteAllByFamily(family);
    }

    /**
     * 멤버 방출
     * @param userId 방출할 멤버
     * @param leaderId 리더
     * @return 방출 멤버 이름
     */
    public FamilyMemberDto.KickResponse kickMember(Long userId, Long leaderId) {
        // 아이디 확인
        User member = userReader.getById(userId);

        User leader = userReader.getById(leaderId);

        // 패밀리 멤버가 맞는지 확인
        if (!userRepository.existsByIdAndFamily(userId, leader.getFamily())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "해당 가족의 리더가 아닙니다.");
        }

        // 멤버 제거, 권한 제거
        member.leaveFamily();
        return FamilyMemberDto.KickResponse.builder()
                .kickedNickname(member.getNickname())
                .build();
    }

    /**
     * 가족 그룹을 탈퇴하는 경우
     *
     * @param userId 탈퇴 유저
     */
    public void leaveMember(Long userId) {
        User user = userReader.getById(userId);
        // 가족이 없는 경우
        validationFamily(user);

        // 권한 변경
        user.leaveFamily();
    }

    /**
     * 선택권 분배
     *
     * @param userId  리더
     * @param request 선택권 개수
     * @return 자동 분배 여부
     */
    public FamilyPickDto.ConfigResponse pickConfig(Long userId, FamilyPickDto.UpdateConfigRequest request) {
        User user = userReader.getById(userId);

        // 자동 분배가 true인지 확인
        boolean isAuto = request.isAutoAllocations();

        if (isAuto) {
            // true라면 기본 값 들어왔는지 확인
            Long defaultCount = request.defaultAllocations();

            // 멤버들 기본값에 따라 설정
            userRepository.findAllByFamily(user.getFamily())
                    .forEach(member -> {
                        distributePickCount(member, defaultCount);
                        distributePickCountHistory(userId, defaultCount);
                    });

        } else {
            // false라면 멤버별 선택권 넣기
            List<FamilyPickDto.UpdateConfigRequest.pickAllocations> pickAllocations = request.pickAllocations();

            pickAllocations
                    .forEach(pick -> {
                        User member = userReader.getById(pick.userId());

                        if (!Objects.equals(member.getFamily(), user.getFamily())) {
                            throw new BusinessException(ErrorCode.NOT_YOUR_FAMILY_MEMBER);
                        }
                        Long count = pick.pickCount();

                        distributePickCount(member, count);
                        distributePickCountHistory(userId, count);
                    });
        }

        return FamilyPickDto.ConfigResponse.builder()
                .isAutoAllocations(isAuto)
                .build();
    }

    /**
     * 선택권 분배
     * @param user 유저
     * @param count 선택권 개수
     */
    private void distributePickCount(User user, Long count) {
        UserPickCount userPickCount = userPickCountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 선택권 정보를 찾을 수 없습니다."));

        userPickCount.resetCount();
        userPickCount.restoreCount(count);
    }

    /**
     * 선택권 분배 히스토리 생성
     * @param userId 유저 id
     * @param count 선택권 개수
     */
    private void distributePickCountHistory(Long userId, Long count) {
        User user = userReader.getById(userId);
        UUID transactionId = UUID.randomUUID();

        PickCountHistory resetHistory = PickCountHistory.reset(user, transactionId);
        PickCountHistory creditHistory = PickCountHistory.credit(user, count, transactionId);

        pickCountHistoryRepository.saveAll(List.of(resetHistory, creditHistory));
    }

    /**
     * 선택권 초기화
     * @param userId 리더 id
     * @return 초기화된 멤버 수
     */
    public FamilyPickDto.ResetResponse resetConfig(Long userId) {
        User user = userReader.getById(userId);

        // 멤버 목록 불러와 선택권 초기화
        List<User> userList = userRepository.findAllByFamily(user.getFamily());

        userList.forEach(member -> {
            resetPickCount(member);
            resetPickCountHistory(member.getId());
        });

        return FamilyPickDto.ResetResponse.builder()
                .resetMember(userList.size())
                .resetAt(String.valueOf(LocalDateTime.now()))
                .build();
    }

    /**
     * 선택권 초기화
     * @param user 유저
     */
    private void resetPickCount(User user) {
        UserPickCount userPickCount = userPickCountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "유저 선택권 정보를 찾을 수 없습니다."));

        userPickCount.resetCount();
    }

    /**
     * 선택권 초기화 히스토리
     * @param userId 유저 아이디
     */
    private void resetPickCountHistory(Long userId) {
        User user = userReader.getById(userId);
        UUID transactionId = UUID.randomUUID();

        PickCountHistory resetHistory = PickCountHistory.reset(user, transactionId);
        pickCountHistoryRepository.save(resetHistory);
    }

}
