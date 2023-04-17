package zerobase.bud.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.util.TimeUtil;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long commentId;
    private String content;
    private int numberOfLikes;
    private String memberName;
    private String memberProfileUrl;
    private Long memberId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isPinned;
    private Boolean isReader;
    private Boolean isReaderLiked;
    private String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentDto> reComments;

    public static CommentDto of(Comment comment, boolean isReader, boolean isReaderLiked){
        return CommentDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .numberOfLikes(comment.getLikeCount())
                .memberName(comment.getMember().getNickname())
                .memberProfileUrl(comment.getMember().getProfileImg())
                .memberId(comment.getMember().getId())
                .createdAt(TimeUtil.caculateTerm(comment.getCreatedAt()))
                .isReader(isReader)
                .isReaderLiked(isReaderLiked)
                .build();
    }

    public static CommentDto of(Comment comment, boolean isReader, boolean isReaderLiked, boolean isPinned, List<CommentDto> reComments){
        return CommentDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .numberOfLikes(comment.getLikeCount())
                .memberName(comment.getMember().getNickname())
                .memberProfileUrl(comment.getMember().getProfileImg())
                .memberId(comment.getMember().getId())
                .createdAt(TimeUtil.caculateTerm(comment.getCreatedAt()))
                .isReader(isReader)
                .isPinned(isPinned)
                .isReaderLiked(isReaderLiked)
                .reComments(reComments)
                .build();
    }
}
