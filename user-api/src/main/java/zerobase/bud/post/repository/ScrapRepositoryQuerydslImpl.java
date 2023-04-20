package zerobase.bud.post.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.dto.QScrapDto;
import zerobase.bud.post.dto.ScrapDto;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.ScrapSortType;

import java.util.ArrayList;
import java.util.List;

import static zerobase.bud.post.domain.QPostLike.postLike;
import static zerobase.bud.post.domain.QScrap.scrap;
import static zerobase.bud.user.domain.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class ScrapRepositoryQuerydslImpl implements ScrapRepositoryQuerydsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ScrapDto> findAllByMemberIdAndPostStatus(
            Long memberId,
            Pageable pageable
    ) {
        return new PageImpl<>(
                searchScraps(memberId, pageable),
                pageable,
                countScraps(memberId)
        );
    }

    private List<ScrapDto> searchScraps(
            Long memberId,
            Pageable pageable
    ) {
        List<OrderSpecifier<?>> orders = getOrders(pageable);

        return jpaQueryFactory
                .select(
                        new QScrapDto(
                                scrap.id,
                                scrap.post,
                                isUserScrapPostLike(memberId, scrap.post.id),
                                isUserPostRegisterUserFollow(memberId,
                                        scrap.post.member.id),
                                scrap.createdAt
                        )
                )
                .from(scrap)
                .where(eqUserId(memberId), neStatus())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .fetch();
    }

    private Long countScraps(
            Long memberId
    ) {
        return jpaQueryFactory
                .select(scrap.count())
                .from(scrap)
                .where(neStatus(), eqUserId(memberId))
                .fetchOne();
    }

    private BooleanExpression eqUserId(Long memberId) {
        return scrap.member.id.eq(memberId);
    }

    private BooleanExpression neStatus() {
        return scrap.post.postStatus.ne(PostStatus.INACTIVE);
    }

    private Expression<Boolean> isUserScrapPostLike(
            Long memberId,
            NumberPath<Long> postId
    ) {
        return ExpressionUtils.as(
                JPAExpressions.select(postLike.count()
                                .when(0L)
                                .then(false)
                                .otherwise(true))
                        .from(postLike)
                        .where(postLike.post.id.eq(postId),
                                postLike.member.id.eq(memberId)), "isLike");
    }

    private Expression<Boolean> isUserPostRegisterUserFollow(
            Long memberId,
            NumberPath<Long> qnaAnswerMemberId
    ) {
        return ExpressionUtils.as(
                JPAExpressions.select(follow.count()
                                .when(0L)
                                .then(false)
                                .otherwise(true))
                        .from(follow)
                        .where(follow.target.id.eq(qnaAnswerMemberId),
                                follow.member.id.eq(memberId)), "isFollow");

    }

    private List<OrderSpecifier<?>> getOrders(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            Order direction =
                    order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            ScrapSortType scrapSortType =
                    ScrapSortType.valueOf(order.getProperty());

            orders.add(sortPostsList(scrapSortType, direction));
        }

        return orders;
    }

    private OrderSpecifier<?> sortPostsList(
            ScrapSortType sortType,
            Order order
    ) {
        switch (sortType) {
            case SCRAP_DATE:
                return new OrderSpecifier<>(order, scrap.createdAt);
            case POST_HIT:
                return new OrderSpecifier<>(order, scrap.post.hitCount);
            case POST_LIKE:
                return new OrderSpecifier<>(order, scrap.post.likeCount);
            case POST_DATE:
                return new OrderSpecifier<>(order, scrap.post.createdAt);
            default:
                return new OrderSpecifier<>(Order.DESC, scrap.createdAt);
        }
    }
}
