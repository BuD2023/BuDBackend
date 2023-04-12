package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.Image;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    void deleteAllByPostId(Long postId);
    List<Image> findAllByPostId(Long postId);
}
