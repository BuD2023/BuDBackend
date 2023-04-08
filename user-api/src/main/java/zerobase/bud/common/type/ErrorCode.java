package zerobase.bud.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("해당 유저가 없습니다."),
    WEB_SOCKET_ERROR("웹 소켓 연결에 오류가 발생했습니다."),
    CHATROOM_NOT_FOUND("해당 채팅방 정보가 없습니다."),
    WRONG_REQUEST_TYPE_ERROR("잘못된 요청 타입입니다."),
    FAILED_GET_COMMIT_INFO("커밋 정보를 가져오는 데 실패했습니다."),
    FAILED_CONNECT_GITHUB("깃헙과 연결에 실패했습니다."),
    NOT_REGISTERED_MEMBER("등록되지 않은 회원입니다."),

    INVALID_TOKEN("토큰이 유효하지 않습니다."),
    INTERNAL_ERROR("내부 서버 오류가 발생했습니다.");

    private final String description;
}
