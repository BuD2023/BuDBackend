package zerobase.bud.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_INITIAL_VALUE("잘못된 초기값입니다."),
    INVALID_TOTAL_COMMIT_COUNT("유효하지 않은 총 커밋 수 입니다."),
    USER_NOT_FOUND("해당 유저가 없습니다."),
    CHATROOM_NOT_FOUND("해당 채팅방 정보가 없습니다."),
    WRONG_REQUEST_TYPE_ERROR("잘못된 요청 타입입니다."),
    FAILED_GET_COMMIT_INFO("커밋 정보를 가져오는 데 실패했습니다."),
    FAILED_CONNECT_GITHUB("깃헙과 연결에 실패했습니다."),
    NOT_REGISTERED_MEMBER("등록되지 않은 회원입니다."),
    NOT_REGISTERED_GITHUB_USER_ID("등록되지 않은 깃헙 유저아이디입니다."),
    INTERNAL_ERROR("내부 서버 오류가 발생했습니다.");

    private final String description;
}
