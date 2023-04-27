package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.type.PostStatus;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Long countByMember(Member member);

    Optional<Post> findByIdAndPostStatus(Long id, PostStatus postStatus);

    @Modifying
    @Query(value = "delete from post where id=:postId" , nativeQuery = true)
    void deleteAllByPostId(Long postId);
}
