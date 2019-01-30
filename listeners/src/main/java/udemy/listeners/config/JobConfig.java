package udemy.listeners.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import udemy.listeners.listeners.ChunkListener;
import udemy.listeners.listeners.JobListener;

import java.util.Arrays;
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
    public ItemReader<String> reader() {
        return new ListItemReader<>(Arrays.asList("one","two","three"));
    }

    @Bean
    public ItemWriter<String> writer() {
        return list -> {
            for (String item : list) {
                log.info("Writing item {}", item);
            }
        };
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .<String,String>chunk(2)
                .faultTolerant()
                .listener(new ChunkListener())
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Job job(JavaMailSender mailSender, @Value("${udemy.recipient}") String recipient) {
        return jobBuilderFactory.get("job")
                .start(step1())
                .listener(new JobListener(mailSender, recipient))
                .build();
    }
}
