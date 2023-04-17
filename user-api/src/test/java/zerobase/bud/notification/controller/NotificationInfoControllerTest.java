package zerobase.bud.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.notification.dto.SaveNotificationInfo;
import zerobase.bud.notification.service.NotificationInfoService;

@WebMvcTest(NotificationInfoController.class)
@AutoConfigureRestDocs
class NotificationInfoControllerTest {

    @MockBean
    private NotificationInfoService notificationInfoService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final static String TOKEN = "BEARER TOKEN";

    @Test
    @WithMockUser
    void success_saveNotificationInfo() throws Exception {
        //given 어떤 데이터가 주어졌을 때
        String notificationId = makeNotificationId();
        given(notificationInfoService.saveNotificationInfo(any(), any()))
            .willReturn(notificationId);
        //when 어떤 경우에
        //then 이런 결과가 나온다.

        mockMvc.perform(post("/notification-info")
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new SaveNotificationInfo.Request(
                        "fcmToken", true, true
                    )
                ))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(notificationId))
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