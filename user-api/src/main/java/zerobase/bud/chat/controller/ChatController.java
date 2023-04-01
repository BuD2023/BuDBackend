package zerobase.bud.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.chat.dto.CreateChatRoomRequest;
import zerobase.bud.chat.service.ChatService;

import javax.validation.Valid;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    //TODO: member 추가
    @PostMapping("/chatroom")
    private ResponseEntity createChatRoom(
            @RequestBody @Valid CreateChatRoomRequest request) {
        Long id = chatService.createChatRoom(request.getTitle());
        return ResponseEntity.created(URI.create("/chat/" + id)).build();
    }
}
