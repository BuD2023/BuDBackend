package zerobase.bud.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zerobase.bud.post.dto.ScrapDto;

public interface ScrapRepositoryQuerydsl {
    Page<ScrapDto> findAllByMemberIdAndPostStatus(
            Long memberId,
            Pageable pageable
    );
}
