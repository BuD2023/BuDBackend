package zerobase.bud.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatMessage;
import zerobase.bud.chat.service.ChatService;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message")
    public void chatting(@RequestBody @Valid ChatMessage.Request request) {
        ChatDto chat = chatService.chatting(
                request.getMessage(), request.getChatroomId(), request.getSenderId());
        messagingTemplate.convertAndSend("/chatrooms/" + request.getChatroomId(), chat);
    }
}
