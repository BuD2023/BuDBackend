package zerobase.bud.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.chat.dto.CreateChatRoomRequest;
import zerobase.bud.chat.service.ChatService;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    //TODO: member 추가
    @PostMapping("/chatroom")
    private ResponseEntity createChatRoom(
            @RequestBody @Valid CreateChatRoomRequest request) {
        Long id = chatService.createChatRoom(request.getTitle());
        return ResponseEntity.created(URI.create("/chat/" + id)).build();
    }

    @GetMapping("/chatroom/search")
    private ResponseEntity searchChatRoom(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(chatService.searchChatRoom(keyword, page));
    }

    @GetMapping("/chatroom")
    private ResponseEntity readChatRoom(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(chatService.getChatRoom(page));
    }
}
