package zerobase.bud.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomSession implements Serializable {
    @Column
    private String userId;

    @Column
    private Long chatroomId;
}
