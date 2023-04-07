package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

}
