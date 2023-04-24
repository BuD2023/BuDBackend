package zerobase.bud.post.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static zerobase.bud.post.domain.QImage.image;

@RequiredArgsConstructor
@Repository
public class PostImageQuerydsl {
    private final JPAQueryFactory jpaQueryFactory;

    public List<String> findImagePathAllByPostId(Long postId) {
        return jpaQueryFactory
                .select(image.imagePath)
                .from(image)
                .where(image.post.id.eq(postId))
                .fetch();
    }
}
