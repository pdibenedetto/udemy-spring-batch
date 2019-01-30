package udemy.listeners.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;

@Slf4j
public class ChunkListener {

    @BeforeChunk
    public void beforeTheChunk() {
        log.info(">>BEFORE CHUNK");
    }

    @AfterChunk
    public void afterTheChunk(){
        log.info("<<AFTER CHUNK");
    }
}
