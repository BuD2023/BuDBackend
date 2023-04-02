package zerobase.bud.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.type.ChatRoomStatus;


@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Slice<ChatRoom> findAllByTitleContainingIgnoreCaseAndStatus(String keyword, ChatRoomStatus status, Pageable pageable);

    Slice<ChatRoom> findAllByStatus(ChatRoomStatus status, Pageable pageable);
}
