package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.Image;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Modifying
    @Query(value = "delete from image where post_id=:postId" , nativeQuery = true)
    void deleteAllByPostId(Long postId);
    List<Image> findAllByPostId(Long postId);
}
