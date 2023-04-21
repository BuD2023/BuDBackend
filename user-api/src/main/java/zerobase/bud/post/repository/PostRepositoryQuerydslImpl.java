package zerobase.bud.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.QPostDto;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static zerobase.bud.post.domain.QPost.post;
import static zerobase.bud.post.domain.QPostLike.postLike;
import static zerobase.bud.post.domain.QScrap.scrap;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.post.type.PostStatus.INACTIVE;
import static zerobase.bud.user.domain.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class PostRepositoryQuerydslImpl implements PostRepositoryQuerydsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<PostDto> findAllByPostStatus(
            Long memberId,
            String keyword,
            PostSortType sortType,
            Order order,
            Pageable pageable,
            PostType postType
    ) {
        return new PageImpl<>(
                searchPosts(memberId, keyword, sortType, order, pageable, postType),
                pageable,
                searchPostsCount(keyword, postType)
        );
    }

    @Override
    public Page<PostDto> findAllByMyPagePost(
            Long memberId,
            Long myPageUserId,
            PostType postType,
            Pageable pageable
    ) {
        return new PageImpl<>(
                searchMyPagePosts(memberId, myPageUserId, postType, pageable),
                pageable,
                countMyPagePosts(memberId, postType)
        );
    }

    @Override
    public PostDto findByPostId(
            Long memberId,
            Long postId
    ) {
        return jpaQueryFactory
                .select(new QPostDto(
                        post.id,
                        post.member,
                        post.title,
                        post.content,
                        post.commentCount,
                        post.likeCount,
                        post.scrapCount,
                        post.hitCount,
                        post.postStatus,
                        post.postType,
                        post.createdAt,
                        post.updatedAt,
                        isUserPostLike(memberId),
                        isUserPostScrap(memberId),
                        isUserPostRegisterUserFollow(memberId)
                ))
                .from(post)
                .where(post.id.eq(postId), eqStatus())
                .fetchOne();
    }


    private List<PostDto> searchPosts(
            Long memberId,
            String keyword,
            PostSortType sortType,
            Order order,
            Pageable pageable,
            PostType postType
    ) {
        return jpaQueryFactory
                .select(new QPostDto(
                        post.id,
                        post.member,
                        post.title,
                        post.content,
                        post.commentCount,
                        post.likeCount,
                        post.scrapCount,
                        post.hitCount,
                        post.postStatus,
                        post.postType,
                        post.createdAt,
                        post.updatedAt,
                        isUserPostLike(memberId),
                        isUserPostScrap(memberId),
                        isUserPostRegisterUserFollow(memberId)
                ))
                .from(post)
                .where(search(keyword), neStatus(), eqPostType(postType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortPostsList(sortType, order))
                .fetch();
    }

    private Long searchPostsCount(String keyword, PostType postType) {
        return jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(search(keyword), neStatus(), eqPostType(postType))
                .fetchOne();
    }

    private List<PostDto> searchMyPagePosts(
            Long memberId,
            Long myPageUserId,
            PostType postType,
            Pageable pageable
    ) {
        List<OrderSpecifier<?>> orders = getOrders(pageable);

        return jpaQueryFactory
                .select(new QPostDto(
                        post.id,
                        post.member,
                        post.title,
                        post.content,
                        post.commentCount,
                        post.likeCount,
                        post.scrapCount,
                        post.hitCount,
                        post.postStatus,
                        post.postType,
                        post.createdAt,
                        post.updatedAt,
                        isUserPostLike(memberId),
                        isUserPostScrap(memberId),
                        isUserPostRegisterUserFollow(memberId)
                ))
                .from(post)
                .where(neStatus(), eqPostRegisterMemberId(myPageUserId), eqPostType(postType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .fetch();
    }

    private Long countMyPagePosts(Long memberId, PostType postType) {
        return jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(neStatus(), eqPostRegisterMemberId(memberId), eqPostType(postType))
                .fetchOne();
    }

    private Expression<Boolean> isUserPostLike(Long memberId) {
        return ExpressionUtils.as(
                JPAExpressions.select(postLike.count()
                                .when(0L)
                                .then(false)
                                .otherwise(true)
                        )
                        .from(postLike)
                        .where(postLike.post.id.eq(post.id),
                                postLike.member.id.eq(memberId)), "isLike");

    }

    private Expression<Boolean> isUserPostScrap(Long memberId) {
        return ExpressionUtils.as(
                JPAExpressions.select(scrap.count()
                                .when(0L)
                                .then(false)
                                .otherwise(true)
                        )
                        .from(scrap)
                        .where(scrap.post.id.eq(post.id),
                                scrap.member.id.eq(memberId)), "isScrap");

    }

    private Expression<Boolean> isUserPostRegisterUserFollow(Long memberId) {
        return ExpressionUtils.as(
                JPAExpressions.select(follow.count()
                                .when(0L)
                                .then(false)
                                .otherwise(true)
                        )
                        .from(follow)
                        .where(follow.target.id.eq(post.member.id),
                                follow.member.id.eq(memberId)), "isFollow");

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

    private BooleanExpression eqPostType(PostType type) {
        return type == null || type.equals(PostType.ALL) ? null : post.postType.eq(type);
    }

    private BooleanExpression neStatus() {
        return post.postStatus.ne(INACTIVE);
    }

    private BooleanExpression eqPostRegisterMemberId(Long memberId) {
        return post.member.id.eq(memberId);
    }

    private List<OrderSpecifier<?>> getOrders(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            Order direction =
                    order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            PostSortType postSortType =
                    PostSortType.valueOf(order.getProperty());

            orders.add(sortPostsList(postSortType, direction));
        }

        return orders;
    }

    private OrderSpecifier<?> sortPostsList(
            PostSortType sortType,
            Order order
    ) {
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