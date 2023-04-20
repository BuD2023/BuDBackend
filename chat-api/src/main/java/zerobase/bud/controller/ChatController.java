package zerobase.bud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.dto.ChatImage;
import zerobase.bud.dto.ChatMessage;
import zerobase.bud.service.ChatService;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/message")
    public void chatting(@RequestBody @Valid ChatMessage.Request request) {
        chatService.chatting(
                request.getMessage(), request.getChatroomId(), request.getSenderId());
    }

    @MessageMapping("/image")
    public void image(@RequestBody @Valid ChatImage.Request request) {
        chatService.image(request.getImageByte(), request.getChatroomId(), request.getSenderId());
    }

}
