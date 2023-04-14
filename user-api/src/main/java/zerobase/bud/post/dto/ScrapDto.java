package zerobase.bud.post.dto;

import lombok.*;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Scrap;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapDto {
    private Long id;
    private PostDto post;
    private LocalDateTime createdAt;

    public static ScrapDto fromScrapWithImages(Scrap scrap, List<Image> images) {
        return ScrapDto.builder()
                .id(scrap.getId())
                .post(PostDto.fromEntity(scrap.getPost(), images))
                .createdAt(scrap.getCreatedAt())
                .build();
    }
}
