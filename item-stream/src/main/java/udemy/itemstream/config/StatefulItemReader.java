package udemy.itemstream.config;

import org.springframework.batch.item.*;

import java.util.List;

public class StatefulItemReader implements ItemStreamReader<String> {
    private final List<String> items;
    private int curIndex = 0;
    private boolean restart = false;

    public StatefulItemReader(List<String> items) {
        this.items = items;
        this.curIndex = 0;
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        String item  = null;
        if (this.curIndex < this.items.size() ){
            item = this.items.get(this.curIndex);
            this.curIndex++;
        }

        // simulate a failure
        if (this.curIndex == 42 && !restart) {
            throw new RuntimeException("Douglas Adams, yo");
        }
        return item;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // if this property exists, we're assuming it's a restart
        if (executionContext.containsKey("curIndex")) {
            this.curIndex = executionContext.getInt("curIndex");
            // prevents the exception from being rethrown above.
            this.restart = true;
        } else {
            this.curIndex = 0;
            executionContext.put("curIndex", this.curIndex);
        }
    }

    // this is where the current good state is saved after each transaction
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("curIndex", this.curIndex);
    }

    @Override
    public void close() throws ItemStreamException {

    }
}
