package zerobase.bud.config;

import static zerobase.bud.common.type.ErrorCode.FIREBASE_INIT_FAILED;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import zerobase.bud.common.exception.BudException;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.key.path}")
    private String fcmPrivateKeyPath;

    @Value("${fcm.key.scope}")
    private List<String> fireBaseScope;

    // fcm 기본 설정 진행
    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(
                        fcmPrivateKeyPath).getInputStream())
                    .createScoped(fireBaseScope))
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BudException(FIREBASE_INIT_FAILED);
        }
    }

}
