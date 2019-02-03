package udemy.itemstream.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Date;
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
    @StepScope
    public StatefulItemReader itemReader() {
        List<String> items = new ArrayList<>(100);
        for (int i=1; i<=100; i++ ) {
            items.add(String.valueOf(i));
        }

        return new StatefulItemReader(items);
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return list -> {
            list.forEach( i -> {
                log.info(">>> {}", i);
            });
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String,String>chunk(10)
                .reader(itemReader())
                .writer(itemWriter())
                .stream(itemReader())
                .build();
    }


    @Bean
    public Job job() {
//        String jobName = String.format("item-stream-%d", new Date().getTime());
        String jobName = "statefulJob";
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();
    }
}
