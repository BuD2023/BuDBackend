package zerobase.bud.post.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.post.domain.Post;
import zerobase.bud.type.MemberStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class ScrapDto {
    private Long scrapId;

    private Post post;

    private MemberStatus postRegisterMemberStatus;

    private boolean isPostLike;
    private boolean isPostRegisterMemberFollow;

    private LocalDateTime createdAt;

    @QueryProjection
    public ScrapDto(Long scrapId, Post post, MemberStatus postRegisterMemberStatus,
        boolean isPostLike, boolean isPostRegisterMemberFollow, LocalDateTime createdAt) {
        this.scrapId = scrapId;
        this.post = post;
        this.postRegisterMemberStatus = postRegisterMemberStatus;
        this.isPostLike = isPostLike;
        this.isPostRegisterMemberFollow = isPostRegisterMemberFollow;
        this.createdAt = createdAt;
    }
}
