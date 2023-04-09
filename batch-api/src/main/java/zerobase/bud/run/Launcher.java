package zerobase.bud.run;

import static zerobase.bud.type.ErrorCode.BATCH_JOB_FAILED;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;
import zerobase.bud.exception.BudException;
import zerobase.bud.job.GetCommitLogConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class Launcher {

    private final JobLauncher jobLauncher;

    private final GetCommitLogConfig getCommitLogConfig;
    private static final String time = "time";

    public void runGetCommitLogJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put(time, new JobParameter(LocalDateTime.now().toString()));
        JobParameters jobParameters = new JobParameters(confMap);

        try {
            jobLauncher.run(getCommitLogConfig.getCommitLogJob(), jobParameters);
        } catch (JobExecutionAlreadyRunningException |
                 JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
            throw new BudException(BATCH_JOB_FAILED);
        }
    }
}
