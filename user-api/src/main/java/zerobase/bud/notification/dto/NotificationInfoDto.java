package zerobase.bud.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInfoDto {

    private Boolean isPostPushAvailable;

    private Boolean isFollowPushAvailable;

}
