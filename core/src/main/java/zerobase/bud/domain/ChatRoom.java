package zerobase.bud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zerobase.bud.type.ChatRoomStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ChatRoom extends BaseEntity {

    @NotNull
    private String title;

    private int numberOfMembers;

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;

    //TODO: 멤버 추가
}
