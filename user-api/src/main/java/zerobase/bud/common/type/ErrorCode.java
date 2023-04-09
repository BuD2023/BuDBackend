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
    INTERNAL_ERROR("내부 서버 오류가 발생했습니다."),
    ELEMENT_NOT_EXIST("데이터가 존재하지 않는 문서입니다."),
    URL_ILLEGAL_ARGUMENT("잘못된 URL 형식입니다."),
    NEWS_ID_NOT_EXCEED_MIN_VALUE("뉴스 고유 아이디가 최소값을 넘지 못했습니다."),
    NEWS_NOT_FOUND("요청하신 뉴스 데이터가 존재하지 않습니다."),
    NEWS_DATE_TYPE_UN_MATCH("날짜 형식이 올바르지 않습니다. (올바른 형식: yyyy-MM-dd)"),
    ALREADY_STORED_DATA("이미 저장된 데이터 입니다."),
    DATA_TYPE_UN_MATCH("데이터 형식이 올바르지 않습니다."),

    HTTP_REQUEST_RESPONSE_FAIL("HTTP 요청과 응답이 실패했습니다."),
    INVALID_API_URL_ADDRESS("잘못된 API URL입니다."),
    HTTP_CONNECT_FAIL("HTTP 연결이 실패했습니다"),
    API_RESPONSE_READ_FAIL("API 응답을 읽는데 실패했습니다."),
    ;
    private final String description;
}

