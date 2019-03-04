package udemy.retry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import udemy.retry.components.RetryItemProcessor;
import udemy.retry.components.RetryItemWriter;
import udemy.retry.exception.CustomRetryableException;

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
    public ListItemReader<String> reader() {
        List<String> items = new ArrayList<>();
        for (int i=0; i<100; i++) {
            items.add(String.valueOf(i));
        }
        return new ListItemReader<>(items);
    }


    /*
    Running from command line
    java -jar target/retry-0.0.1-SNAPSHOT.jar -retry=processor
     */
    @Bean
    @StepScope
    public RetryItemProcessor processor(@Value("#{jobParameters['retry']}") String retry ){
        RetryItemProcessor processor = new RetryItemProcessor();
        processor.setRetry(StringUtils.hasText(retry) && retry.equalsIgnoreCase("processor"));
        return processor;
    }

    @Bean
    @StepScope
    public RetryItemWriter writer(@Value("#{jobParameters['retry']}") String retry ) {

        RetryItemWriter writer = new RetryItemWriter();
        boolean doRetry = StringUtils.hasText(retry) && retry.equalsIgnoreCase("writer");
        log.info("retry? " + retry);
        log.info("WRITER DO RETRY " + doRetry);
        writer.setRetry(doRetry);
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step")
                .<String,String>chunk(10)
                .reader(reader())
                .processor(processor(null))
                .writer(writer(null))
                // returns a faulttolerant step builder
                .faultTolerant()
                .retry(CustomRetryableException.class)
                // at most retry fifteen times
                .retryLimit(15)
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("retry-job-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();
    }

}
