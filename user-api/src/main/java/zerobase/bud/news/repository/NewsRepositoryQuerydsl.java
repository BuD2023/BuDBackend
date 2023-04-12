package zerobase.bud.news.repository;

import com.querydsl.core.types.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.type.NewsSortType;

import java.time.LocalDateTime;

public interface NewsRepositoryQuerydsl {
    Page<News> findAll(Pageable pageable,
                       String keyword,
                       NewsSortType sort,
                       Order order,
                       LocalDateTime startDate,
                       LocalDateTime endDate);
}
