package zerobase.bud.job;

import java.time.LocalDate;
import java.util.Collections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.service.GithubApi;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GetCommitLogConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final GithubInfoRepository githubInfoRepository;

    private final GithubApi githubApi;

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
            .<GithubInfo, GithubInfo>chunk(1)
            .reader(githubInfoReader())
            .processor(githubInfoProcessor())
            .writer(dummyWriter()) // 더미 Writer를 등록
            .build();
    }

    @Bean
    public ItemReader<GithubInfo> githubInfoReader() {
        return new RepositoryItemReaderBuilder<GithubInfo>()
            .name("githubInfoReader")
            .repository(githubInfoRepository)
            .methodName("findAll")
            .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
            .build();
    }

    @Bean
    public ItemProcessor<GithubInfo, GithubInfo> githubInfoProcessor() {
        return new ItemProcessor<GithubInfo, GithubInfo>() {
            @Override
            public GithubInfo process(@NonNull GithubInfo githubInfo) {
                try {
                    githubApi.saveCommitInfoFromLastCommitDate(githubInfo,
                        LocalDate.now().minusDays(1));
                } catch (Exception e) {
                    log.error(githubInfo.getUsername() + "님의 깃헙 연동에 실패하였습니다.");
                }
                return githubInfo;
            }
        };
    }

    @Bean(name = "dummyWriter")
    @StepScope
    public ItemWriter<GithubInfo> dummyWriter() {
        return items -> {
        };
    }

}
