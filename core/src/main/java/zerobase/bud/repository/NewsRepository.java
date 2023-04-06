package zerobase.bud.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.News;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findByLink(String link);

    List<News> findAllByRegisteredAtIsBetweenOrderByHitCountDesc(
            PageRequest pageRequest,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<News> findAllByRegisteredAtIsBetweenOrderByRegisteredAtDesc(
            PageRequest pageRequest,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<News> findAllByTitleContainingAndRegisteredAtIsBetweenOrderByHitCountDesc(
            PageRequest pageRequest,
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<News> findAllByTitleNotContainingAndRegisteredAtIsBetweenOrderByRegisteredAtDesc(
            PageRequest pageRequest,
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
