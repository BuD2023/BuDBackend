package zerobase.bud.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zerobase.bud.tasklet.GetCommitInfoTasklet;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GetCommitLogConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final GetCommitInfoTasklet getCommitInfoService;

    @Bean
    public Job getCommitLogJob() {
        return jobBuilderFactory.get("getCommitLog")
            .start(getCommitLogStep())
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step getCommitLogStep() {
        return stepBuilderFactory.get("step")
            .tasklet(((contribution, chunkContext) -> {
                log.info(">> step start");
                getCommitInfoService.getCommitInfo();
                return RepeatStatus.FINISHED;
            }))
            .build();
    }

}
