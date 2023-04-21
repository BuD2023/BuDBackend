package zerobase.bud.post.repository;


import com.querydsl.core.types.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostType;

import java.util.Optional;

public interface PostRepositoryQuerydsl {
    Page<PostDto> findAllByPostStatus(
            Long memberId,
            String keyword,
            PostSortType sortType,
            Order order,
            Pageable pageable,
            PostType pageType
    );

    Page<PostDto> findAllByMyPagePost(
            Long memberId,
            Long myPageUserId,
            PostType postType,
            Pageable pageable
    );

    PostDto findByPostId(
            Long memberId,
            Long postId
    );
}