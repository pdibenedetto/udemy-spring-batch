package udemy.skipretrylisteners.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

@Slf4j
public class CustomSkipListener implements SkipListener {
    @Override
    public void onSkipInRead(Throwable t) {

    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("skipping item " + item + " because writing caused error " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {

        log.warn("skipping item " + item + " because processing caused error " + t.getMessage());
    }
}
