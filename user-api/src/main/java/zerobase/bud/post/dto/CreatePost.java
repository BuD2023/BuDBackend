package zerobase.bud.post.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.post.type.PostType;

public class CreatePost {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request{

        @NotBlank
        private String title;

        private String content;

        private String imageUrl;

        @NotNull
        private PostType postType;


    }

}
