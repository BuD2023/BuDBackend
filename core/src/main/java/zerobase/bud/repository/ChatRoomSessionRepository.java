package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.ChatRoomSession;

import java.util.Optional;

@Repository
public interface ChatRoomSessionRepository extends JpaRepository<ChatRoomSession, Long> {
    Optional<ChatRoomSession> findBySessionId(String sessionId);
}
