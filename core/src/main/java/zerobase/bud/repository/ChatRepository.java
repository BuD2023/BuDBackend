package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
}
