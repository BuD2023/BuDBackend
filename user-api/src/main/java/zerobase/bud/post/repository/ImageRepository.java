package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

}
