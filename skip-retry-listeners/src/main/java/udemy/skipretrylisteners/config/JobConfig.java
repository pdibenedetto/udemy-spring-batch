package udemy.skipretrylisteners.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import udemy.skipretrylisteners.components.CustomException;
import udemy.skipretrylisteners.components.CustomSkipListener;
import udemy.skipretrylisteners.components.SkipItemProcessor;
import udemy.skipretrylisteners.components.SkipItemWriter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class JobConfig {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public ListItemReader<String> reader() {
        List<String> items = new ArrayList<>();
        for (int i=0; i<100; i++) {
            items.add(String.valueOf(i));
        }
        return new ListItemReader<>(items);
    }

    @Bean
    @StepScope
    public SkipItemProcessor processor() {
        return new SkipItemProcessor();
    }

    @Bean
    @StepScope
    public SkipItemWriter writer() {
        return new SkipItemWriter();
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String,String>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .faultTolerant()
                .skip(CustomException.class)
                .skipLimit(15)
                .listener(new CustomSkipListener())
                .build();

    }

    @Bean
    public Job job() {
        String jobName = String.format("skip-listener-job");
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();
    }
}
