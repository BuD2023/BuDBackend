package zerobase.bud.notification.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.notification.domain.NotificationInfo;

@Repository
public interface NotificationInfoRepository extends
    JpaRepository<NotificationInfo, Long> {

    Optional<NotificationInfo> findByMemberId(Long memberId);
}
