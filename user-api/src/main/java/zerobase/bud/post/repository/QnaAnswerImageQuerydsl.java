package zerobase.bud.post.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static zerobase.bud.post.domain.QQnaAnswerImage.qnaAnswerImage;

@RequiredArgsConstructor
@Repository
public class QnaAnswerImageQuerydsl {
    private final JPAQueryFactory jpaQueryFactory;

    public List<String> findImagePathAllByPostId(Long qnaAnswerId) {
        return jpaQueryFactory
                .select(qnaAnswerImage.imagePath)
                .from(qnaAnswerImage)
                .where(qnaAnswerImage.qnaAnswer.id.eq(qnaAnswerId))
                .fetch();
    }
}
