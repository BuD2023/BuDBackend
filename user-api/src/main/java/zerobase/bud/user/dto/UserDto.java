package zerobase.bud.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import zerobase.bud.domain.Member;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String userId;
    private String nickName;
    private String description;
    private Long level;
    private Long numberOfFollowers;
    private Long numberOfFollows;
    private Long numberOfPosts;
    private String job;
    private String profileUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFollowing;

    public static UserDto of(Member member, boolean isReader, boolean isFollowing,
                             Long numberOfFollowrs, Long numberOfFollows, Long numberOfPosts){
        return UserDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .nickName(member.getNickname())
                .description(member.getIntroduceMessage())
                .level(member.getLevel().getLevelNumber())
                .profileUrl(member.getProfileImg())
                .numberOfFollowers(numberOfFollowrs)
                .numberOfFollows(numberOfFollows)
                .numberOfPosts(numberOfPosts)
                .isReader(isReader)
                .isFollowing(isFollowing)
                .job(member.getJob())
                .build();
    }

    public static UserDto of(Member member, Long numberOfFollowrs,
                             Long numberOfFollows, Long numberOfPosts){
        return UserDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .nickName(member.getNickname())
                .description(member.getIntroduceMessage())
                .level(member.getLevel().getLevelNumber())
                .profileUrl(member.getProfileImg())
                .numberOfFollowers(numberOfFollowrs)
                .numberOfFollows(numberOfFollows)
                .job(member.getJob())
                .numberOfPosts(numberOfPosts)
                .build();
    }
}
