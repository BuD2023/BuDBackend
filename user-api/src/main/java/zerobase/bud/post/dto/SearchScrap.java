package zerobase.bud.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SearchScrap {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long scrapId;

        private Long postId;

        private Long postRegisterMemberId;
        private String postRegisterMemberNickname;
        private String postRegisterMemberProfileImg;

        private String title;

        private String content;

        private List<String> imageUrls;

        private long commentCount;

        private long likeCount;

        private long scrapCount;

        private long hitCount;

        private PostStatus postStatus;

        private PostType postType;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private boolean isLike;
        private boolean isFollow;

        private LocalDateTime scrapCreatedAt;

        public static Response of(ScrapDto scrapDto, List<Image> images) {
            Post post = scrapDto.getPost();

            List<String> imagePaths = images.stream()
                    .map(Image::getImagePath)
                    .collect(Collectors.toList());

            return Response.builder()
                    .scrapId(scrapDto.getScrapId())
                    .postId(post.getId())
                    .postRegisterMemberId(post.getMember().getId())
                    .postRegisterMemberNickname(post.getMember().getNickname())
                    .postRegisterMemberProfileImg(post.getMember()
                            .getProfileImg())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .imageUrls(imagePaths)
                    .commentCount(post.getCommentCount())
                    .likeCount(post.getLikeCount())
                    .scrapCount(post.getScrapCount())
                    .hitCount(post.getHitCount())
                    .postStatus(post.getPostStatus())
                    .postType(post.getPostType())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .isLike(scrapDto.isPostLike())
                    .isFollow(scrapDto.isPostRegisterMemberFollow())
                    .scrapCreatedAt(scrapDto.getCreatedAt())
                    .build();
        }
    }
}