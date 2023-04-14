package zerobase.bud.post.repository;


import com.querydsl.core.types.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostSortType;

public interface PostRepositoryQuerydsl {
    Page<Post> findAllByPostStatus(String keyword,
                                   PostSortType sortType,
                                   Order order,
                                   Pageable pageable);
}
