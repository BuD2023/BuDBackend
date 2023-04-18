package zerobase.bud.user.dto;

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
public class FollowDto {
    private Long id;
    private Long memberId;
    private String userId;
    private String nickName;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFollowing;
    private String profileUrl;

    public static FollowDto of(Member member, boolean isReader, boolean isFollowing){
        return FollowDto.builder()
                .memberId(member.getId())
                .id(member.getId())
                .userId(member.getUserId())
                .nickName(member.getNickname())
                .description(member.getIntroduceMessage())
                .isReader(isReader)
                .isFollowing(isFollowing)
                .profileUrl(member.getProfileImg())
                .build();
    }

    public static FollowDto of(Member member){
        return FollowDto.builder()
                .memberId(member.getId())
                .id(member.getId())
                .userId(member.getUserId())
                .nickName(member.getNickname())
                .description(member.getIntroduceMessage())
                .isFollowing(true)
                .profileUrl(member.getProfileImg())
                .build();
    }
}
