package zerobase.bud.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.post.domain.QnaAnswer;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class QnaAnswerCommentPin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private QnaAnswerComment qnaAnswerComment;

    @OneToOne
    @JoinColumn(name = "qna_answer_id", referencedColumnName = "id")
    private QnaAnswer qnaAnswer;
}
