package zerobase.bud.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.chat.dto.*;
import zerobase.bud.chat.service.ChatRoomService;
import zerobase.bud.domain.Member;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    private ResponseEntity<URI> createChatRoom(
            @RequestBody @Valid CreateChatRoom.Request request,
            @AuthenticationPrincipal Member member) {
        Long id = chatRoomService
                .createChatRoom(request.getTitle(), request.getDescription(), request.getHashTag(), member);
        return ResponseEntity.created(URI.create("/chatrooms/" + id)).build();
    }

    @PostMapping("/{chatroomId}/users/{userId}")
    private ResponseEntity<Long> modifyHost(@PathVariable Long chatroomId,
                                            @PathVariable Long userId,
                                            @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(chatRoomService.modifyHost(chatroomId, userId, member));
    }

    @GetMapping("/search")
    private ResponseEntity<Slice<ChatRoomDto>> searchChatRoom(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(chatRoomService.searchChatRooms(keyword, page, size));
    }

    @GetMapping
    private ResponseEntity<Slice<ChatRoomDto>> readChatRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(chatRoomService.readChatRooms(page, size));
    }

    @GetMapping("/{chatroomId}")
    private ResponseEntity<ChatRoomDto> readChatRoom(@PathVariable Long chatroomId) {
        return ResponseEntity.ok(chatRoomService.readChatRoom(chatroomId));
    }

    @GetMapping("/{chatroomId}/chats")
    private ResponseEntity<Slice<ChatDto>> readChats(@PathVariable Long chatroomId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(chatRoomService.readChats(chatroomId, member, page, size));
    }

    @GetMapping("/{chatroomId}/users")
    private ResponseEntity<List<ChatUserDto>> chatUsers(@PathVariable Long chatroomId,
                                                        @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(chatRoomService.readChatUsers(chatroomId, member));
    }

    @GetMapping("/status")
    private ResponseEntity<ChatRoomStatusDto> chatRoomsStatus() {
        return ResponseEntity.ok(chatRoomService.chatRoomsStatus());
    }
}
