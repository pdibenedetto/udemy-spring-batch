package udemy.skipretrylisteners.components;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
@Setter
public class SkipItemWriter implements ItemWriter<String> {
    private int attemptCount = 0;

    @Override
    public void write(List<? extends String> list) throws Exception {

        for (String item : list) {
            log.info("writing item " + item);
            if (item.equalsIgnoreCase("-84")) {

                log.warn("writing of item " + item + " failed");
                throw  new CustomException("write failed. attempt = " + attemptCount);
            }
            log.info(item);
        }

    }
}
