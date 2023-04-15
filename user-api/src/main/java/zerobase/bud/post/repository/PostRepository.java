package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostStatus;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Long countByMember(Member member);

    Optional<Post> findByIdAndPostStatus(Long id, PostStatus postStatus);

}
