package zerobase.bud.comment.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import zerobase.bud.comment.type.QnaAnswerCommentStatus;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.QnaAnswer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class QnaAnswerComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private QnaAnswer qnaAnswer;

    @ManyToOne
    private Member member;

    private String content;

    private int likeCount;
    private int commentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private QnaAnswerComment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<QnaAnswerComment> reComments = new ArrayList<>();

    @OneToMany(mappedBy = "qnaAnswerComment", cascade = CascadeType.ALL)
    private List<QnaAnswerComment> commentLikes;

    @OneToOne(mappedBy = "qnaAnswerComment", cascade = CascadeType.ALL)
    private QnaAnswerCommentPin commentPin;

    @Enumerated(EnumType.STRING)
    private QnaAnswerCommentStatus qnaAnswerCommentStatus;

    public void addLikeCount() {
        this.likeCount++;
    }

    public void minusLikeCount() {
        this.likeCount--;
    }
}
