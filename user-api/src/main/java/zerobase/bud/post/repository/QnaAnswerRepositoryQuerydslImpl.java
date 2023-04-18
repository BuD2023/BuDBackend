package zerobase.bud.post.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.dto.QnaAnswerDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;
import static zerobase.bud.post.domain.QQnaAnswer.qnaAnswer;
import static zerobase.bud.post.domain.QQnaAnswerPin.qnaAnswerPin;
import static zerobase.bud.post.type.QnaAnswerStatus.INACTIVE;

@Repository
@RequiredArgsConstructor
public class QnaAnswerRepositoryQuerydslImpl
        implements QnaAnswerRepositoryQuerydsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<QnaAnswerDto> findAllByPostIdAndQnaAnswerStatusNotLike(
            Long postId, Pageable pageable) {

        return new PageImpl<>(
                searchQnaAnswers(postId, pageable),
                pageable,
                countQnaAnswers(postId)
        );
    }

    private List<QnaAnswerDto> searchQnaAnswers(Long postId,
                                                Pageable pageable) {
        List<OrderSpecifier<?>> orders = getOrders(pageable);

        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                QnaAnswerDto.class,
                                qnaAnswer.id,
                                qnaAnswer.member,
                                qnaAnswer.content,
                                qnaAnswer.commentCount,
                                qnaAnswer.likeCount,
                                qnaAnswer.qnaAnswerStatus,
                                qnaAnswer.createdAt,
                                qnaAnswer.updatedAt,
                                qnaAnswerPin.id
                        )
                )
                .from(qnaAnswer)
                .leftJoin(qnaAnswerPin)
                .on(qnaAnswer.id.eq(qnaAnswerPin.qnaAnswer.id))
                .where(eqId(postId), neStatus())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .fetch();
    }

    private Long countQnaAnswers(Long postId) {
        return jpaQueryFactory
                .select(qnaAnswer.count())
                .from(qnaAnswer)
                .leftJoin(qnaAnswerPin)
                .on(qnaAnswer.id.eq(qnaAnswerPin.qnaAnswer.id))
                .where(eqId(postId), neStatus())
                .fetchOne();
    }

    private BooleanExpression eqId(Long postId) {
        return qnaAnswer.post.id.eq(postId);
    }

    private BooleanExpression neStatus() {
        return qnaAnswer.qnaAnswerStatus.ne(INACTIVE);
    }

    private List<OrderSpecifier<?>> getOrders(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        orders.add(new OrderSpecifier<>(Order.DESC, qnaAnswerPin.createdAt));

        if (isEmpty(pageable.getSort())) {
            return orders;
        }

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.getDirection().isAscending() ?
                    Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "DATE":
                    orders.add(new OrderSpecifier<>(direction, qnaAnswer.createdAt));
                    break;
                case "LIKE":
                    orders.add(new OrderSpecifier<>(direction, qnaAnswer.likeCount));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(direction, qnaAnswer.createdAt));
                    break;
            }
        }

        return orders;
    }
}
