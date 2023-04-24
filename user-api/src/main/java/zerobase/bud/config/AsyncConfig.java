package zerobase.bud.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "asyncNotification")
    public Executor threadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processors count {}",processors);
        executor.setThreadNamePrefix("Async-"); // thread 이름 설정
        executor.setCorePoolSize(processors); // 기본 스레드 수
        executor.setMaxPoolSize(processors*2); // 최대 스레드 개수
        executor.setQueueCapacity(processors*6); // 최대 큐 수
        executor.initialize(); // 초기화후 반환
        return executor;
    }

}
