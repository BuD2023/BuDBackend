package zerobase.bud.Listener;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.stereotype.Component;
import zerobase.bud.domain.GithubInfo;

@Slf4j
@Component
public class GetCommitLogSkipListener extends
    SkipListenerSupport<GithubInfo, GithubInfo> {

    @Override
    public void onSkipInRead(@NonNull Throwable t) {
    }

    @Override
    public void onSkipInWrite(@NonNull GithubInfo item, @NonNull Throwable t) {
    }

    @Override
    public void onSkipInProcess(@NonNull GithubInfo item, @NonNull Throwable t) {
        log.error(
            "Failed to process GithubInfo with userId: {}. Skipping to the next item.",
            item.getUserId());
    }
}
