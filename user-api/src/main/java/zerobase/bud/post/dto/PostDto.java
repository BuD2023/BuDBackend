package zerobase.bud.post.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class PostDto {
    private long id;

    private Member member;

    private String title;
    private String content;

    private long commentCount;
    private long likeCount;
    private long scrapCount;
    private long hitCount;

    private PostStatus postStatus;
    private PostType postType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isLike;
    private boolean isScrap;
    private boolean isFollow;

    @QueryProjection
    public PostDto(long id, Member member, String title, String content,
                   long commentCount, long likeCount, long scrapCount,
                   long hitCount, PostStatus postStatus, PostType postType,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   boolean isLike, boolean isScrap, boolean isFollow) {
        this.id = id;
        this.member = member;
        this.title = title;
        this.content = content;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.hitCount = hitCount;
        this.postStatus = postStatus;
        this.postType = postType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLike = isLike;
        this.isScrap = isScrap;
        this.isFollow = isFollow;
    }
}
