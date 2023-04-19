package zerobase.bud.notification.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SaveNotificationInfo {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{

        @NotBlank
        private String fcmToken;

        private Boolean isPostPushAvailable;

        private Boolean isFollowPushAvailable;

    }

}
