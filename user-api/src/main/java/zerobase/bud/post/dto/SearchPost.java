package zerobase.bud.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

import java.time.LocalDateTime;
import java.util.List;

public class SearchPost {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private long id;

        private Member member;

        private String title;
        private List<String> imageUrls;
        private String content;

        private long commentCount;
        private long likeCount;
        private long scrapCount;
        private long hitCount;

        private boolean isLike;
        private boolean isScrap;
        private boolean isFollow;

        private PostStatus postStatus;
        private PostType postType;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response of(PostDto post, List<String> imageUrls) {
            return Response.builder()
                    .id(post.getId())
                    .member(post.getMember())
                    .title(post.getTitle())
                    .imageUrls(imageUrls)
                    .content(post.getContent())
                    .commentCount(post.getCommentCount())
                    .likeCount(post.getLikeCount())
                    .scrapCount(post.getScrapCount())
                    .hitCount(post.getHitCount())
                    .postStatus(post.getPostStatus())
                    .postType(post.getPostType())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .isLike(post.isLike())
                    .isScrap(post.isScrap())
                    .isFollow(post.isFollow())
                    .build();
        }
    }
}
