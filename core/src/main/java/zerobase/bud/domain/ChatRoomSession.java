package zerobase.bud.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zerobase.bud.type.SessionStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ChatRoomSession extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @ManyToOne
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    public void setDelete(){
        this.status = SessionStatus.DELETED;
    }
}
