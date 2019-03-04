package udemy.restart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.Map;

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
    @StepScope // each step will get it's own independent instance with this annotation
    public Tasklet restartTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Map<String,Object> stepExecutionContext = chunkContext.getStepContext().getStepExecutionContext();

                if (stepExecutionContext.containsKey("ran")) {
                    log.warn("This time we'll let it go");
                    return RepeatStatus.FINISHED;
                }

                log.warn("I don't think so...");
                chunkContext.getStepContext().getStepExecution().getExecutionContext().put("ran", true);
                throw new RuntimeException("Not this time");
            }
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Job job() {
        String jobName = String.format("restart-job");
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .next(step2())
                .build();

    }
}
