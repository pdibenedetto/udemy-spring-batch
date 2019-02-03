package udemy.multipleinputfiles.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import udemy.multipleinputfiles.domain.Customer;
import udemy.multipleinputfiles.domain.CustomerFieldSetMapper;

import java.util.Date;

@Slf4j
@Configuration
public class JobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Value("classpath*:/data/customer*.csv")
    private Resource[] inputFiles;

    public JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public MultiResourceItemReader<Customer> multiResourceItemReader() {
        MultiResourceItemReader<Customer> reader = new MultiResourceItemReader<>();
        reader.setDelegate(customerItemReader());
        reader.setResources(inputFiles);

        return reader;
    }


    @Bean
    public FlatFileItemReader<Customer> customerItemReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();

        // maps a line to a domain object
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthdate"});

        mapper.setFieldSetMapper(new CustomerFieldSetMapper());
        mapper.setLineTokenizer(tokenizer);
        mapper.afterPropertiesSet();

        reader.setLineMapper(mapper);

        return reader;
    }

    @Bean
    public ItemWriter<Customer> customerItemWriter() {
        return items -> {
            items.forEach(i -> { log.info("item={}",i); });
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Customer,Customer>chunk(10)
                .reader(multiResourceItemReader())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    public Job job() {
        String jobName = String.format("flatFileJob-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();
    }
}
