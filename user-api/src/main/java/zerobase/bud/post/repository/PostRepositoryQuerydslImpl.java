package zerobase.bud.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostSortType;

import java.util.List;

import static zerobase.bud.post.domain.QPost.post;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

@Repository
@RequiredArgsConstructor
public class PostRepositoryQuerydslImpl implements PostRepositoryQuerydsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> findAllByPostStatus(String keyword, PostSortType sortType,
                                          Order order, Pageable pageable) {
        return new PageImpl<>(
                searchPosts(keyword, sortType, order, pageable),
                pageable,
                searchPostsCount(keyword)
        );
    }

    private List<Post> searchPosts(String keyword, PostSortType sortType,
                                Order order, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(post)
                .where(search(keyword), eqStatus())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortPostsList(sortType, order))
                .fetch();
    }

    private Long searchPostsCount(String keyword) {
        return jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(search(keyword))
                .fetchOne();
    }

    private BooleanBuilder search(String keyword) {
        return keyword == null ? null :
                new BooleanBuilder()
                        .or(post.content.contains(keyword))
                        .or(post.title.contains(keyword))
                        ;
    }

    private BooleanExpression eqStatus() {
        return post.postStatus.eq(ACTIVE);
    }

    private OrderSpecifier<?> sortPostsList(PostSortType sortType, Order order) {
        switch (sortType) {
            case HIT:
                return new OrderSpecifier<>(order, post.hitCount);
            case LIKE:
                return new OrderSpecifier<>(order, post.likeCount);
            case DATE:
                return new OrderSpecifier<>(order, post.createdAt);
            default:
                return new OrderSpecifier<>(Order.DESC, post.createdAt);
        }
    }
}
