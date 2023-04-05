package zerobase.bud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zerobase.bud.type.ChatType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Chat extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String message;

    @ManyToOne(fetch = FetchType.LAZY) // chatRoom이 필요하면 쿼리 나가기
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    //TODO: member 추가
}
