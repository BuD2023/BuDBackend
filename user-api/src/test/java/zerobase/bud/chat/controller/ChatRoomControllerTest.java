package zerobase.bud.chat.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.ChatRoomStatusDto;
import zerobase.bud.chat.dto.CreateChatRoom;
import zerobase.bud.chat.service.ChatRoomService;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.type.ChatType;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(ChatRoomController.class)
@AutoConfigureRestDocs
class ChatRoomControllerTest {

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${develop.server.scheme}")
    private String scheme;

    @Value("${develop.server.host}")
    private String host;

    @Value("${develop.server.port}")
    private int port;

    private static String token = "임의의accesstoken";

    @BeforeEach
    void init(WebApplicationContext context, RestDocumentationContextProvider contextProvider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(contextProvider))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("abcde.jpg")
                .nickname("안뇽")
                .job("시스템프로그래머")
                .oAuthAccessToken("tokenvalue")
                .build();

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Authentication authentication = new UsernamePasswordAuthenticationToken(member, "",
                List.of(MemberStatus.VERIFIED.getKey()).stream().map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));

        given(tokenProvider.getAuthentication("임의의토큰")).willReturn(authentication);
    }

    @Test
    @DisplayName("채팅룸 생성 성공")
    void successCreateChatRoomTest() throws Exception {
        //given
        given(chatRoomService.createChatRoom(anyString(), anyString(), any(), any())).willReturn(1L);
        //when
        //then
        this.mockMvc.perform(post("/chatrooms")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CreateChatRoom.Request.builder()
                                        .title("챗지비티는 거짓말쟁이")
                                        .description("챗지비티와 인공지능")
                                        .hashTag(Arrays.asList("인공지능", "챗지비티", "ai"))
                                        .build()
                        ))
                )
                .andExpect(status().isCreated())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );


    }

    @Test
    @DisplayName("채팅룸 검색 성공")
    void successSearchChatRoomsTest() throws Exception {
        //given
        List<ChatRoomDto> dtos = Arrays.asList(
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("챗지비티에 관한 소통방")
                        .description("챗지비티에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("챗지비티", "어쩌구", "ai"))
                        .numberOfMembers(2)
                        .hostName("안뇽")
                        .hostProfileUrl("/image.jpg")
                        .hostId(1L)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(2L)
                        .title("주니어 프엔들 모여")
                        .description("주니어 프엔에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("주니어", "웤라이프", "프론트엔드"))
                        .numberOfMembers(12)
                        .hostName("페리에")
                        .hostProfileUrl("/image.jpg")
                        .hostId(2L)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("챗지비티에 관한 소통방")
                        .description("챗지비티에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("챗지비티", "어쩌구", "ai"))
                        .numberOfMembers(4)
                        .hostName("라임")
                        .hostProfileUrl("/image.jpg")
                        .hostId(3L)
                        .build()
        );

        given(chatRoomService.searchChatRooms(anyString(), anyInt(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));
        //when
        //then
        this.mockMvc.perform(get("/chatrooms/search")
                        .param("keyword", "word")
                        .param("page", "0")
                        .param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].chatRoomId").type(JsonFieldType.NUMBER)
                                                .description("채팅룸의 id"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("채팅룸의 제목"),
                                        fieldWithPath("content[].description").type(JsonFieldType.STRING)
                                                .description("채팅룸의 설명"),
                                        fieldWithPath("content[].numberOfMembers").type(JsonFieldType.NUMBER)
                                                .description("채팅룸에 속한 사람의 수"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
                                                .description("채팅방 개설 시"),
                                        fieldWithPath("content[].hashTags").type(JsonFieldType.ARRAY)
                                                .description("해시태그"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("number").type(JsonFieldType.NUMBER)
                                                .description("현재 몇번째 페이지인지"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("하나의 페이지 안에 몇개의 채팅룸이 들어갔는지")
                                )
                        )
                );

    }

    @Test
    @DisplayName("전체 채팅룸 읽기 성공")
    void successReadChatRoomsTest() throws Exception {
        //given
        List<ChatRoomDto> dtos = Arrays.asList(
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("챗지비티에 관한 소통방")
                        .description("챗지비티에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("챗지비티", "어쩌구", "ai"))
                        .numberOfMembers(2)
                        .hostName("머리끈")
                        .hostProfileUrl("/image.jpg")
                        .hostId(1L)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(2L)
                        .title("주니어 프엔들 모여")
                        .description("주니어 프엔에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("주니어", "웤라이프", "프론트엔드"))
                        .numberOfMembers(12)
                        .hostName("페리에")
                        .hostProfileUrl("/image.jpg")
                        .hostId(2L)
                        .build(),
                ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("챗지비티에 관한 소통방")
                        .description("챗지비티에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("챗지비티", "어쩌구", "ai"))
                        .numberOfMembers(4)
                        .hostName("머야")
                        .hostProfileUrl("/image.jpg")
                        .hostId(3L)
                        .build()
        );

        given(chatRoomService.readChatRooms(anyInt(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));
        //when
        //then
        this.mockMvc.perform(get("/chatrooms")
                        .param("page", "0")
                        .param("size", "4")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].chatRoomId").type(JsonFieldType.NUMBER)
                                                .description("채팅룸의 id"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("채팅룸의 제목"),
                                        fieldWithPath("content[].description").type(JsonFieldType.STRING)
                                                .description("채팅룸의 설명"),
                                        fieldWithPath("content[].numberOfMembers").type(JsonFieldType.NUMBER)
                                                .description("채팅룸에 속한 사람의 수"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
                                                .description("채팅방 개설 시"),
                                        fieldWithPath("content[].hashTags").type(JsonFieldType.ARRAY)
                                                .description("해시태그"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("number").type(JsonFieldType.NUMBER)
                                                .description("현재 몇번째 페이지인지"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("하나의 페이지 안에 몇개의 채팅룸이 들어갔는지")
                                )
                        )
                );

    }

    @Test
    @DisplayName("채팅방 정보 읽기 성공")
    void successReadChatRoomTest() throws Exception {
        //given
        given(chatRoomService.readChatRoom(anyLong()))
                .willReturn(ChatRoomDto.builder()
                        .chatRoomId(1L)
                        .title("챗지비티에 관한 소통방")
                        .description("챗지비티에 관해 이야기 나놔보아요")
                        .createdAt(LocalDateTime.now())
                        .hashTags(Arrays.asList("챗지비티", "어쩌구", "ai"))
                        .hostName("페리에")
                        .hostProfileUrl("/image.jpg")
                        .hostId(2L)
                        .numberOfMembers(2)
                        .build()
                );
        //when
        //then
        this.mockMvc.perform(get("/chatrooms/{chatroomId}", 32L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER)
                                                .description("채팅룸의 id"),
                                        fieldWithPath("title").type(JsonFieldType.STRING)
                                                .description("채팅룸의 제목"),
                                        fieldWithPath("description").type(JsonFieldType.STRING)
                                                .description("채팅룸의 설명"),
                                        fieldWithPath("numberOfMembers").type(JsonFieldType.NUMBER)
                                                .description("채팅룸에 속한 사람의 수"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING)
                                                .description("채팅방 개설 시"),
                                        fieldWithPath("hashTags").type(JsonFieldType.ARRAY)
                                                .description("해시태그")
                                )
                        )
                );
    }

    @Test
    @DisplayName("채팅방 내 채팅 리스트 읽기 성공")
    void successReadChatsTest() throws Exception {
        //given
        List<ChatDto> dtos = Arrays.asList(
                ChatDto.builder()
                        .chatId(1L)
                        .chatroomId(32L)
                        .chatType(ChatType.MESSAGE)
                        .createdAt(TimeUtil.caculateTerm(LocalDateTime.now()))
                        .message("어쩌구저쩌구~")
                        .userId(1L)
                        .userName("닉넴")
                        .userProfileUrl("/image.jpg")
                        .build(),
                ChatDto.builder()
                        .chatId(2L)
                        .chatroomId(32L)
                        .chatType(ChatType.IMAGE)
                        .createdAt(TimeUtil.caculateTerm(LocalDateTime.now()))
                        .imageUrl("/s3/fdsa.jpg")
                        .userId(1L)
                        .userName("닉넴")
                        .userProfileUrl("/image.jpg")
                        .build(),
                ChatDto.builder()
                        .chatId(3L)
                        .chatType(ChatType.MESSAGE)
                        .chatroomId(32L)
                        .createdAt(TimeUtil.caculateTerm(LocalDateTime.now()))
                        .message("아그랬구나아하")
                        .userId(1L)
                        .userName("닉넴")
                        .userProfileUrl("/image.jpg")
                        .build()
        );
        given(chatRoomService.readChats(anyLong(), anyInt(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));
        //when
        //then
        this.mockMvc.perform(get("/chatrooms/{chatroomId}/chats", 32L)
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(document("{class-name}/{method-name}",
                        preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        relaxedResponseFields(
                                fieldWithPath("content[].chatId").type(JsonFieldType.NUMBER)
                                        .description("채팅의 id"),
                                fieldWithPath("content[].message").type(JsonFieldType.STRING).optional()
                                        .description("메세지"),
                                fieldWithPath("content[].imageUrl").type(JsonFieldType.STRING).optional()
                                        .description("이미지 url"),
                                fieldWithPath("content[].chatType").type(JsonFieldType.STRING)
                                        .description("채팅 타입"),
                                fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
                                        .description("보낸시간"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                        .description("첫번째 페이지인지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                        .description("마지막 페이지인지 여부"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER)
                                        .description("현재 몇번째 페이지인지"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("하나의 페이지 안에 몇개의 채팅이 들어갔는지")
                        )
                ));
    }

    @Test
    @DisplayName("채팅방 현황 읽기 성공")
    void successChatRoomsStatusTest() throws Exception {
        //given
        given(chatRoomService.chatRoomsStatus()).willReturn(
                ChatRoomStatusDto.builder()
                        .numberOfChatRooms(1)
                        .numberOfUsers(2).build());
        //when
        //then
        this.mockMvc.perform(get("/chatrooms/status")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }


}