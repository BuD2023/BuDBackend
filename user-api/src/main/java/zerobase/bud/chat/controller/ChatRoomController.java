package zerobase.bud.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.chat.dto.CreateChatRoomRequest;
import zerobase.bud.chat.service.ChatRoomService;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    //TODO: member 추가
    @PostMapping("/chatroom")
    private ResponseEntity createChatRoom(
            @RequestBody @Valid CreateChatRoomRequest request) {
        Long id = chatRoomService
                .createChatRoom(request.getTitle(), request.getDescription(), request.getHashTag());
        return ResponseEntity.created(URI.create("/chatroom/" + id)).build();
    }

    @GetMapping("/chatroom/search")
    private ResponseEntity searchChatRoom(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(chatRoomService.searchChatRooms(keyword, page));
    }

    @GetMapping("/chatroom")
    private ResponseEntity readChatRooms(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(chatRoomService.readChatRooms(page));
    }

    @GetMapping("/chatroom/{chatroomId}")
    private ResponseEntity readChatRoom(@PathVariable Long chatroomId){
        return ResponseEntity.ok(chatRoomService.readChatRoom(chatroomId));
    }

    @GetMapping("/chatroom/{chatroomId}/chat")
    private ResponseEntity readChats(@PathVariable Long chatroomId,
                                     @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(chatRoomService.readChats(chatroomId, page));
    }
}
