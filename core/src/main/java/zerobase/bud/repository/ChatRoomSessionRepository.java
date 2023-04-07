package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ChatRoomSessionRepository extends JpaRepository<ChatRoomSession, Long> {
    Optional<ChatRoomSession> findBySessionId(String sessionId);

    List<ChatRoomSession> findByChatRoom(ChatRoom chatRoom);
}
