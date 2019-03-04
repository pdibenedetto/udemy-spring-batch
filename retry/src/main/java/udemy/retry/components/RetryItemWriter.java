package udemy.retry.components;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import udemy.retry.exception.CustomRetryableException;

import java.util.List;

@Slf4j
@Setter
public class RetryItemWriter implements ItemWriter<String> {
    private boolean retry = false;
    private int attemptCount = 0;

    @Override
    public void write(List<? extends String> list) throws Exception {
        for (String s : list) {
            if (retry && s.equalsIgnoreCase("-84")) {
                attemptCount++;

                if (attemptCount >=5) {
                    log.info("Success!");
                    retry = false;
                    log.info(s);
                } else {
                    log.warn("writing of item " + s + " failed");
                    throw new CustomRetryableException("Write failed. Attempt: " + attemptCount);
                }
            }
            else {
                log.info(s);
            }
        }
    }
}
