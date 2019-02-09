package udemy.databaseoutput.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import udemy.databaseoutput.domain.Customer;
import udemy.databaseoutput.domain.CustomerFieldSetMapper;

import javax.sql.DataSource;
import java.util.Date;

@Slf4j
@Component
public class JobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public FlatFileItemReader<Customer> customerItemReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("/data/customer.csv"));

        // maps a line to a domain object
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();

        // default tokenizer uses commas
        // also regex line tokenenizers
        // also, fixed length line tokenizer
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","firstName","lastName","birthdate"});

        mapper.setFieldSetMapper(new CustomerFieldSetMapper());
        mapper.setLineTokenizer(tokenizer);
        mapper.afterPropertiesSet();

        reader.setLineMapper(mapper);

        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> customerJdbcBatchItemWriter() {
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("insert into customer values (:id, :firstName, :lastName, :birthDate)");
        // the point of this is to map the bean values to the sql parameters
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();

        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Customer,Customer>chunk(10)
                .reader(customerItemReader())
                .writer(customerJdbcBatchItemWriter())
                .build();
    }

    @Bean
    public Job job() {
        String jobName = String.format("db-output-job-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();
    }
}
