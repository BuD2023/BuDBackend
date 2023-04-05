package zerobase.bud.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Chat;
import zerobase.bud.domain.ChatRoom;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Slice<Chat> findAllByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);
}
