package zerobase.bud.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_FILE_FORMAT("유효하지 않은 파일 형식입니다."),
    BATCH_JOB_FAILED("배치작업에 실패하였습니다"),
    NOT_REGISTERED_MEMBER("등록되지 않은 회원입니다."),
    FAILED_GET_COMMIT_INFO("커밋 정보를 가져오는 데 실패했습니다."),
    FAILED_CONNECT_GITHUB("깃헙과 연결에 실패했습니다."),
    INTERNAL_ERROR("내부 서버 오류가 발생했습니다.");

    private final String description;
}
