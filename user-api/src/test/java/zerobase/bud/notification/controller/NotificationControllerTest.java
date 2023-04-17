package zerobase.bud.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.notification.dto.GetNotifications;
import zerobase.bud.notification.service.NotificationService;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@WebMvcTest(NotificationController.class)
@AutoConfigureRestDocs
class NotificationControllerTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final static String TOKEN = "BEARER TOKEN";

    @Test
    @WithMockUser
    void success_getNotifications() throws Exception {
        //given
        String notificationId = makeNotificationId();

        given(notificationService.getNotifications(any(), any()))
            .willReturn(new SliceImpl<>(
                    List.of(GetNotifications.Response.builder()
                        .senderNickName("nickName")
                        .notificationId(notificationId)
                        .notificationType(NotificationType.POST)
                        .pageType(PageType.QNA)
                        .pageId(1L)
                        .notificationDetailType(NotificationDetailType.ANSWER)
                        .notificationStatus(NotificationStatus.UNREAD)
                        .notifiedAt(LocalDateTime.now())
                        .build())
                )
            );

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(get("/notifications")
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.[0].senderNickName").value("nickName"))
            .andExpect(
                jsonPath("$.content.[0].notificationId").value(notificationId))
            .andExpect(jsonPath("$.content.[0].notificationType").value("POST"))
            .andExpect(jsonPath("$.content.[0].pageType").value("QNA"))
            .andExpect(jsonPath("$.content.[0].pageId").value(1))
            .andExpect(jsonPath("$.content.[0].notificationDetailType").value(
                "ANSWER"))
            .andExpect(
                jsonPath("$.content.[0].notificationStatus").value("UNREAD"))
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );

    }

    @Test
    @WithMockUser
    void success_updateNotificationStatusRead() throws Exception {
        //given
        String notificationId = makeNotificationId();
        given(notificationService.updateNotificationStatusRead(anyString(), any()))
            .willReturn(notificationId);

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(put("/notifications/"+notificationId+"/read")
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$").value(notificationId))
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );

    }

    @Test
    @WithMockUser
    void success_deleteNotification() throws Exception {
        //given
        String notificationId = makeNotificationId();
        given(notificationService.deleteNotification(anyString(), any()))
            .willReturn(notificationId);

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(delete("/notifications/"+notificationId)
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$").value(notificationId))
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );

    }

    private static String makeNotificationId() {
        return UUID.randomUUID().toString()
            .replaceAll(REPLACE_EXPRESSION, "")
            .concat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
    }
}