package udemy.itemwriter.config;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SysOutItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> list) throws Exception {
        System.out.println("The size of this chunk was " + list.size());

        list.forEach(s -> System.out.println(">>> " + s));

    }
}
