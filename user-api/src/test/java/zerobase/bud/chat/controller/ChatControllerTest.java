package zerobase.bud.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.CreateChatRoomRequest;
import zerobase.bud.chat.service.ChatService;


import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureRestDocs
class ChatControllerTest {

    @MockBean
    private ChatService chatService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("채팅룸 생성 성공")
    void successCreateChatRoomTest() throws Exception {
        //given
        given(chatService.createChatRoom(anyString())).willReturn(1L);
        //when
        //then
        this.mockMvc.perform(post("/chatroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateChatRoomRequest("챗지비티그거어쩌구")
                        ))
                )
                .andExpect(status().isCreated())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );


    }

    @Test
    @DisplayName("채팅룸 검색 성공")
    void successSearchChatRoomTest() throws Exception {
        //given
        List<ChatRoomDto> dtos = Arrays.asList(
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("하이하이")
                        .numberOfMembers(2)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(2L)
                        .title("안녕안녕")
                        .numberOfMembers(3)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("제목제목")
                        .numberOfMembers(4)
                        .build()
        );

        given(chatService.searchChatRoom(anyString(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));
        //when
        //then
        this.mockMvc.perform(get("/chatroom/search")
                        .param("keyword","word")
                        .param("page", "0")
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].chatRoomId").type(JsonFieldType.NUMBER)
                                                .description("채팅룸의 id"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("채팅룸의 제목"),
                                        fieldWithPath("content[].numberOfMembers").type(JsonFieldType.NUMBER)
                                                .description("채팅룸에 속한 사람의 수"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("number").type(JsonFieldType.NUMBER)
                                                .description("현재 몇번째 페이지인지"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("하나의 페이지 안에 몇개의 채팅룸이 들어가 있는지")
                                )
                        )
                );

    }

    @Test
    @DisplayName("전체 채팅룸 읽기 성공")
    void successReadChatRoomTest() throws Exception {
        //given
        List<ChatRoomDto> dtos = Arrays.asList(
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("하이하이")
                        .numberOfMembers(2)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(2L)
                        .title("안녕안녕")
                        .numberOfMembers(3)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("제목제목")
                        .numberOfMembers(4)
                        .build()
        );

        given(chatService.getChatRoom(anyInt()))
                .willReturn(new SliceImpl<>(dtos));
        //when
        //then
        this.mockMvc.perform(get("/chatroom")
                        .param("page", "0"))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].chatRoomId").type(JsonFieldType.NUMBER)
                                                .description("채팅룸의 id"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("채팅룸의 제목"),
                                        fieldWithPath("content[].numberOfMembers").type(JsonFieldType.NUMBER)
                                                .description("채팅룸에 속한 사람의 수"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("number").type(JsonFieldType.NUMBER)
                                                .description("현재 몇번째 페이지인지"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("하나의 페이지 안에 몇개의 채팅룸이 들어가 있는지")
                                )
                        )
                );

    }

}