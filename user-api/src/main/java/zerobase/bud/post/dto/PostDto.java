package zerobase.bud.post.dto;

import lombok.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private long id;

    private Member member;

    private String title;
    private String[] imageUrls;
    private String content;

    private long commentCount;
    private long likeCount;
    private long scrapCount;
    private long hitCount;

    private PostStatus postStatus;
    private PostType postType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDto fromEntity(Post post, List<Image> images) {
        String[] imageUrls = new String[images.size()];

        for (int i = 0; i < images.size(); i++) {
            imageUrls[i] = images.get(0).getImagePath();
        }

        return PostDto.builder()
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
                .build();
    }
}
