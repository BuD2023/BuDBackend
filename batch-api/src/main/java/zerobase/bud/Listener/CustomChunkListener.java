package zerobase.bud.Listener;

import javax.batch.api.chunk.listener.ChunkListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomChunkListener implements ChunkListener {

    @Override
    public void beforeChunk() throws Exception {}

    @Override
    public void onError(Exception ex) throws Exception {
        log.error("Error occurred while processing chunk: {}", ex.getMessage());
    }

    @Override
    public void afterChunk() throws Exception {}
}
