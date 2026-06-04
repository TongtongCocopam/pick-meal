package kongju.pickmeal.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    PAGE_OUT_OF_BOUNDS(HttpStatus.BAD_REQUEST, "존재하지 않는 페이지입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "인증 기간이 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 정보입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "유효하지 않은 닉네임입니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호가 현재 비밀번호와 동일합니다."),
    MISMATCH_CONFIRM_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인용 비밀번호가 일치하지 않습니다."),
//    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 보안 정책에 부합하지 않습니다."),

    ALREADY_HAS_FAMILY(HttpStatus.BAD_REQUEST, "이미 소속된 가족이 있습니다."),
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "가입된 가족 정보가 없습니다."),
    NOT_FAMILY_MEMBER(HttpStatus.FORBIDDEN, "가족 구성원이 아닙니다."),
    INVALID_INVITATION_CODE(HttpStatus.BAD_REQUEST, "잘못된 초대 코드입니다."),
    NOT_YOUR_FAMILY_REQUEST(HttpStatus.FORBIDDEN, "해당 가족의 리더가 아닙니다."),
    NOT_YOUR_FAMILY_MEMBER(HttpStatus.FORBIDDEN, "우리 가족 구성원이 아닌 멤버가 포함되어 있습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 데이터가 존재하지 않습니다."),
    INVITATION_CODE_REISSUE_TOO_FAST(HttpStatus.BAD_REQUEST, "초대코드 재발급 제한 시간이 지나지 않았습니다."),
    FAMILY_MEMBER_EXISTS(HttpStatus.BAD_REQUEST, "구성원을 모두 방출한 후에 삭제할 수 있습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),

    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    DIET_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 식단이 이미 존재합니다."),
    DIET_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 식단 정보가 없습니다."),
    DIET_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "대상 식단을 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴 정보를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "배정할 메뉴 후보를 찾을 수 없습니다."),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료 정보가 존재하지 않습니다."),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "수정할 후보 메뉴를 찾을 수 없습니다."),
    MISSING_REPLACEMENT_DATA(HttpStatus.BAD_REQUEST, "교체할 메뉴 정보가 없습니다."),
    INVALID_MENU_DATA(HttpStatus.BAD_REQUEST, "식단 구성 정보가 올바르지 않습니다."),
    INVALID_MENU_ID(HttpStatus.BAD_REQUEST, "잘못된 메뉴 선택입니다."),
    ALLERGY_CONFLICT(HttpStatus.CONFLICT, "가족의 알러지 재료가 포함된 메뉴입니다."),
    DISH_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 요리 종류입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
    TIME_SLOT_ALREADY_FILLED(HttpStatus.CONFLICT, "해당 시간에는 이미 메뉴가 배정되었습니다."),
    TOO_MANY_SELECTIONS(HttpStatus.BAD_REQUEST, "선택권 개수를 초과하였습니다."),
    NOT_A_CANDIDATE(HttpStatus.BAD_REQUEST, "이미 식단으로 확정된 메뉴입니다."),
    AI_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 식단 생성에 실패했습니다."),
    AI_RESPONSE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI 응답이 지연되고 있습니다."),
    AI_DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "생성된 데이터가 유효하지 않습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서버 점검 중입니다."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "응답 시간이 초과되었습니다."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "서버 연결에 실패했습니다.");


    private final HttpStatus status;
    private final String message;
}
