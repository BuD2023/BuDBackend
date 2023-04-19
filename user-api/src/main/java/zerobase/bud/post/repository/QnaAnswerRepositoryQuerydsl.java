package zerobase.bud.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zerobase.bud.post.dto.QnaAnswerDto;

public interface QnaAnswerRepositoryQuerydsl {
    Page<QnaAnswerDto> findAllByPostIdAndQnaAnswerStatusNotLike(
            Long postId, Pageable pageable);
}
