package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.awss3.AwsS3Api;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.common.exception.ChatException;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Chat;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.type.ChatType;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;

import static zerobase.bud.type.ChatRoomStatus.ACTIVE;
import static zerobase.bud.util.Constants.ALLOW_IMAGE_CODES;
import static zerobase.bud.util.Constants.CHATS;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final MemberRepository memberRepository;

    private final AwsS3Api awsS3Api;

    private final ChannelTopic channelTopic;

    private final RedisTemplate<String, ?> redisTemplate;

    @Transactional
    public void chatting(String message, Long roomId, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(roomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(ErrorCode.CHATROOM_NOT_FOUND));

        Member member = memberRepository.findById(senderId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));


        ChatDto dto = ChatDto.from(
                chatRepository.save(
                        Chat.builder()
                                .chatRoom(chatRoom)
                                .message(message)
                                .member(member)
                                .type(ChatType.MESSAGE)
                                .build()));

        redisTemplate.convertAndSend(channelTopic.getTopic(), dto);
    }

    @Transactional
    public void image(String imageStr, Long roomId, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(roomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(ErrorCode.CHATROOM_NOT_FOUND));

        Member member = memberRepository.findById(senderId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        String[] imageCodes = imageStr.split(",");

        String extension = imageCodes[0]
                .substring(imageCodes[0].indexOf("/") + 1, imageCodes[0].indexOf(";"));


        if (!ALLOW_IMAGE_CODES.contains(extension)) {
            throw new ChatException(ErrorCode.NOT_SUPPORTED_IMAGE);
        }

        String filepath;

        try {
            byte[] imageByte = DatatypeConverter.parseBase64Binary(imageCodes[1]);
            File temp = File.createTempFile("image", "." + extension);
            FileUtils.writeByteArrayToFile(temp, imageByte);

            filepath = awsS3Api.uploadFileImage(temp, CHATS);
            FileUtils.delete(temp);
        } catch (IOException e) {
            throw new ChatException(ErrorCode.CANNOT_COVERT_IMAGE);
        }

        ChatDto dto = ChatDto.from(
                chatRepository.save(
                        Chat.builder()
                                .chatRoom(chatRoom)
                                .message(filepath)
                                .member(member)
                                .type(ChatType.IMAGE)
                                .build())
        );

        redisTemplate.convertAndSend(channelTopic.getTopic(), dto);
    }
}
