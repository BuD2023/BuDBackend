package zerobase.bud.news.repository;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.type.NewsSortType;

import java.time.LocalDateTime;
import java.util.List;

import static zerobase.bud.news.domain.QNews.news;

@Repository
@RequiredArgsConstructor
public class NewsRepositoryQuerydslImpl implements NewsRepositoryQuerydsl {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<News> findAll(Pageable pageable, String keyword,
                              NewsSortType sort, Order order,
                              LocalDateTime startDate, LocalDateTime endDate) {
        return new PageImpl<>(
                searchNews(keyword, sort, order, pageable, startDate, endDate),
                pageable,
                searchNewsCount(keyword, startDate, endDate)
        );
    }

    private List<News> searchNews(String keyword, NewsSortType sort, Order order,
                                  Pageable pageable, LocalDateTime startDate,
                                  LocalDateTime endDate) {
        return jpaQueryFactory
                .selectFrom(news)
                .where(eqTitle((keyword)), eqDate(startDate, endDate))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortNewsList(sort, order))
                .fetch();
    }


    private BooleanExpression eqTitle(String keyword) {
        return keyword == null ? null : news.title.contains(keyword);
    }

    private BooleanExpression eqDate(LocalDateTime start, LocalDateTime end) {
        return news.registeredAt.between(start, end);
    }

    private OrderSpecifier<?> sortNewsList(NewsSortType sort, Order order) {
        switch (sort) {
            case HIT:
                return new OrderSpecifier<>(order, news.hitCount);
            case DATE:
                return new OrderSpecifier<>(order, news.registeredAt);
            default:
                return new OrderSpecifier<>(Order.DESC, news.registeredAt);
        }
    }

    private Long searchNewsCount(String keyword, LocalDateTime start,
                                 LocalDateTime end) {
        return jpaQueryFactory
                .select(news.count())
                .from(news)
                .where(eqTitle((keyword)), eqDate(start, end))
                .fetchOne();
    }
}
