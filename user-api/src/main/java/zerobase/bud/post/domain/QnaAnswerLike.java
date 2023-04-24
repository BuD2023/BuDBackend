package zerobase.bud.post.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;

import javax.persistence.*;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class QnaAnswerLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private QnaAnswer qnaAnswer;

    public static QnaAnswerLike of(QnaAnswer qnaAnswer, Member member) {
        return QnaAnswerLike.builder()
                .qnaAnswer(qnaAnswer)
                .member(member)
                .build();
    }
}
