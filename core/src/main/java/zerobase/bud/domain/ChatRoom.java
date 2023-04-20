package zerobase.bud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zerobase.bud.type.ChatRoomStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;

    private String hashTag;

    @NotNull
    private String description;

    @ManyToOne
    private Member member;

    public void delete() {
        status = ChatRoomStatus.DELETED;
    }

    public void modifyHost(Member host) {
        member = host;
    }
}
