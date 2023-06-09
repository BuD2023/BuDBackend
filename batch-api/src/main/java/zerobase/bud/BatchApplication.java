package zerobase.bud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@EnableJpaAuditing
@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {

    public static void main(String[] args) {
        int exit = SpringApplication.exit(
            SpringApplication.run(BatchApplication.class, args));
        log.info("exit = {}", exit);
        System.exit(exit);
    }
}