package zerobase.bud.notification.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.SaveNotificationInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class NotificationInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Member member;

    private String fcmToken;

    private boolean isPostPushAvailable;

    private boolean isFollowPushAvailable;


    public static NotificationInfo of(SaveNotificationInfo.Request request, Member member){
        return NotificationInfo.builder()
            .member(member)
            .fcmToken(request.getFcmToken())
            .isFollowPushAvailable(request.getIsFollowPushAvailable())
            .isPostPushAvailable(request.getIsPostPushAvailable())
            .build();
    }


}
