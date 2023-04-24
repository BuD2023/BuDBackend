package zerobase.bud.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    ADD_IMPOSSIBLE_PINNED_ANSWER("이미 채택된 답변이 있어 답변을 달 수 없습니다."),
    CANNOT_ANSWER_YOURSELF("자신의 QnA 게시글에 답변할 수 없습니다."),
    NOT_FOUND_QNA_ANSWER_PIN("존재하지 않는 QnA 답변 핀입니다."),
    NOT_RECEIVED_NOTIFICATION_MEMBER("알림을 수신한 회원이 아닙니다."),
    NOT_FOUND_NOTIFICATION("존재하지 않는 알림입니다."),
    NOT_FOUND_NOTIFICATION_INFO("알림 정보가 없습니다."),
    NOT_FOUND_TOKEN("Firebase 토큰을 발견하지 못했습니다."),
    FIREBASE_SEND_MESSAGE_FAILED("Firebase 메시지 전송에 실패하였습니다."),
    FIREBASE_INIT_FAILED("Firebase 초기화에 실패하였습니다."),
    REQUEST_METHOD_OR_URL_ERROR("요청하신 메서드 혹은 주소가 잘못되었습니다."),
    AWS_S3_ERROR("AWS S3 오류입니다."),
    INVALID_QNA_ANSWER_STATUS("답변의 상태가 유효하지 않습니다."),
    CHANGE_IMPOSSIBLE_PINNED_ANSWER("고정된 답글은 수정하거나 삭제할 수 없습니다."),
    NOT_FOUND_QNA_ANSWER("존재하지 않는 답변입니다."),
    ALREADY_DELETE_QNA_ANSWER("이미 삭제한 답변입니다."),
    ALREADY_USING_NICKNAME("이미 사용중인 닉네임입니다."),
    INVALID_POST_STATUS("게시글의 상태가 유효하지 않습니다."),
    INVALID_POST_TYPE_FOR_ANSWER("답변을 달기에 유효하지 않은 게시글 타입입니다."),
    NOT_FOUND_POST("존재하지 않는 게시물입니다."),

    CANNOT_LIKE_WRITER_SELF("자신의 댓글을 좋아요 할 수 없습니다."),
    NOT_CHATROOM_OWNER("채팅방 호스트가 아닙니다."),
    NOT_COMMENT_OWNER("댓글 작성자가 아닙니다."),
    NOT_QNA_ANSWER_OWNER("게시글의 작성자가 아닙니다."),
    NOT_POST_OWNER("게시글의 작성자가 아닙니다."),
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),

    CANNOT_PIN_RECOMMENT("대댓글에는 핀을 할 수 없습니다"),
    INVALID_INITIAL_VALUE("잘못된 초기값입니다."),
    INVALID_TOTAL_COMMIT_COUNT("유효하지 않은 총 커밋 수 입니다."),
    CANNOT_FOLLOW_YOURSELF("자기 자신을 팔로우할 수 없습니다."),
    WEB_SOCKET_ERROR("웹 소켓 연결에 오류가 발생했습니다."),
    CHATROOM_NOT_FOUND("해당 채팅방 정보가 없습니다."),
    WRONG_REQUEST_TYPE_ERROR("잘못된 요청 타입입니다."),
    FAILED_GET_COMMIT_INFO("커밋 정보를 가져오는 데 실패했습니다."),
    FAILED_CONNECT_GITHUB("깃헙과 연결에 실패했습니다."),
    NOT_REGISTERED_MEMBER("등록되지 않은 회원입니다."),

    MEMBER_NOT_FOUND_IN_CHATROOM("현재 채팅방에 참여중인 멤버가 아닙니다"),

    REDIS_BROKER_ERROR("레디스 메세지 브로커 과정에서 실패했습니다."),
    INVALID_TOKEN("토큰이 유효하지 않습니다."),
    INTERNAL_ERROR("내부 서버 오류가 발생했습니다."),
    ELEMENT_NOT_EXIST("데이터가 존재하지 않는 문서입니다."),
    URL_ILLEGAL_ARGUMENT("잘못된 URL 형식입니다."),
    NEWS_ID_NOT_EXCEED_MIN_VALUE("뉴스 고유 아이디가 최소값을 넘지 못했습니다."),
    NEWS_NOT_FOUND("요청하신 뉴스 데이터가 존재하지 않습니다."),
    NEWS_DATE_TYPE_UN_MATCH("날짜 형식이 올바르지 않습니다. (올바른 형식: yyyy-MM-dd)"),
    ALREADY_STORED_DATA("이미 저장된 데이터 입니다."),
    DATA_TYPE_UN_MATCH("데이터 형식이 올바르지 않습니다."),

    NOT_SUPPORTED_IMAGE("지원하지 않는 이미지 타입입니다."),

    CANNOT_COVERT_IMAGE("이미지를 변환하는 데 에러가 발생했습니다."),
    NOT_REGISTERED_GITHUB_USER_ID("등록되지 않은 깃헙 유저아이디입니다."),
    HTTP_REQUEST_RESPONSE_FAIL("HTTP 요청과 응답이 실패했습니다."),
    INVALID_API_URL_ADDRESS("잘못된 API URL입니다."),
    HTTP_CONNECT_FAIL("HTTP 연결이 실패했습니다"),
    API_RESPONSE_READ_FAIL("API 응답을 읽는데 실패했습니다."),

    ALREADY_STORED_SCRAP_POST("이미 스크랩한 포스트 입니다."),
    NOT_FOUND_SCRAP_ID("존재하지 않는 스크랩 아이디입니다."),
    ;
    private final String description;
}
