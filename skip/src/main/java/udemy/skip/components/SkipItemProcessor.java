package udemy.skip.components;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@Setter
public class SkipItemProcessor implements ItemProcessor<String,String> {
    private boolean skip = false;
    private int attemptCount = 0;

    @Override
    public String process(String s) throws Exception {
        log.info("processing item " + s);

        if (skip && s.equalsIgnoreCase("42")) {
            attemptCount++;

            log.warn("processing of item " + s + " failed ");
            throw new CustomRetryableException("Processing failed.  Attempt: " + attemptCount);
        }

        return String.valueOf(Integer.valueOf(s) * -1);
    }
}
