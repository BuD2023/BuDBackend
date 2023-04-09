package zerobase.bud.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String userId;
    private String nickName;
    private String description;
    private String level;
    private int numberOfFollowers;
    private int numberOfFollows;
    private int posts;
    private boolean isCurrentUser;
}
