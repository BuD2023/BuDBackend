package zerobase.bud.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.Member;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserDto {
    private Long id;
    private String userId;
    private String nickName;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFollowing;
    private String profileUrl;

    public static ChatUserDto of(Member member, boolean isReader, boolean isFollowing){
        return ChatUserDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .nickName(member.getNickname())
                .description(member.getIntroduceMessage())
                .isReader(isReader)
                .isFollowing(isFollowing)
                .profileUrl(member.getProfileImg())
                .build();
    }
}
