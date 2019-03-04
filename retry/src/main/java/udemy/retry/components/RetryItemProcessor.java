package udemy.retry.components;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import udemy.retry.exception.CustomRetryableException;

@Slf4j
@Setter
public class RetryItemProcessor implements ItemProcessor<String,String> {
    private boolean retry = false;
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        log.info("processing item " + s);

        if (retry && s.equalsIgnoreCase("42")) {
            attemptCount++;

            if (attemptCount >=5) {
                log.info("Success!!!");
                retry = false;
                return String.valueOf(Integer.valueOf(s) * -1);
            }
            else {
                log.warn("Processing of item " + s + " failed");
                throw new CustomRetryableException("Process failed. Attempt: " + attemptCount);
            }
        }
        return String.valueOf(Integer.valueOf(s) * -1);
    }
}
