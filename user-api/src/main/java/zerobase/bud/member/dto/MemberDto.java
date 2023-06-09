package zerobase.bud.member.dto;

import lombok.Data;

public class MemberDto {

    @Data
    public static class Info {
        private String nickname;
        private String profileImg;
        private String job;
    }

    @Data
    public static class Mypage {
        private String nickname;
        private String profileImg;
        private String job;
        private String introduceMessage;
    }

}
