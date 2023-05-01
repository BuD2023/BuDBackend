package zerobase.bud.notification.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zerobase.bud.notification.domain.NotificationInfo;

@Repository
public interface NotificationInfoRepository extends
    JpaRepository<NotificationInfo, Long> {

    Optional<NotificationInfo> findByMemberId(Long memberId);

    @Modifying
    @Query(value = "delete from notification_info where member_id=:memberId", nativeQuery = true)
    void deleteByMemberId(@Param("memberId") Long memberId);
}
