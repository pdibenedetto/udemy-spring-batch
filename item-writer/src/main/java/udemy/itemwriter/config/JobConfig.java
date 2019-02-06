package udemy.itemwriter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class JobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    @Bean
    public ListItemReader<String> listItemReader() {
        List<String> items = new ArrayList<>();
        for (int i=1; i<=100; i++){
            items.add("String " + i);
        }
        return new ListItemReader<>(items);
    }

    @Bean
    public SysOutItemWriter itemWriter() {
        return new SysOutItemWriter();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<String,String>chunk(10)
                .reader(listItemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step())
                .build();
    }
}
