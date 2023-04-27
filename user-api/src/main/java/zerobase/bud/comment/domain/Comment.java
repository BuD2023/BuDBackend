package zerobase.bud.comment.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import zerobase.bud.comment.type.CommentStatus;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne
    private Member member;

    private String content;

    private int likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> reComments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CommentStatus commentStatus;

    public void addLikeCount() {
        this.likeCount++;
    }

    public void minusLikeCount() {
        this.likeCount--;
    }
}
