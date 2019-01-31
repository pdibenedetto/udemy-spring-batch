package udemy.jobparameters.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    /* @StepScope tells the runtime to lazily instantiate the bean WHEN the bean is actually needed
        The helloWorldTasklet won't be instantiated on startup, but when step1 is executed
        A proxy is used in its place to satisfy the dependency
     */
    @StepScope
    public Tasklet helloWorldTasklet(@Value("#{jobParameters['message']}") String message) {
        return ((contribution, chunkContext) -> {
            log.info("message {}", message);
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(helloWorldTasklet(null))
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job1")
                .start(step1())
                .build();
    }
}
