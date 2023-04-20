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
    private SearchPost.Response post;
    private LocalDateTime createdAt;

    public static ScrapDto of(Scrap scrap, List<Image> images) {
        return ScrapDto.builder()
                .id(scrap.getId())
                .post(SearchPost.Response.of(PostDto.of(scrap.getPost()), images))
                .createdAt(scrap.getCreatedAt())
                .build();
    }
}
