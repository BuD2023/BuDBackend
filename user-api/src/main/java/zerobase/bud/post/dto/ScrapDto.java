package zerobase.bud.post.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.post.domain.Post;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class ScrapDto {
    private Long scrapId;

    private Post post;

    private boolean isPostLike;
    private boolean isPostRegisterMemberFollow;

    private LocalDateTime createdAt;

    @QueryProjection
    public ScrapDto(Long scrapId, Post post, boolean isPostLike,
                    boolean isPostRegisterMemberFollow,
                    LocalDateTime createdAt) {
        this.scrapId = scrapId;
        this.post = post;
        this.isPostLike = isPostLike;
        this.isPostRegisterMemberFollow = isPostRegisterMemberFollow;
        this.createdAt = createdAt;
    }
}
