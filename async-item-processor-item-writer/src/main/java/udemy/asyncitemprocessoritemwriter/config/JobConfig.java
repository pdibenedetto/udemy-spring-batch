package udemy.asyncitemprocessoritemwriter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import udemy.asyncitemprocessoritemwriter.domain.Customer;
import udemy.asyncitemprocessoritemwriter.domain.CustomerRowMapper;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Configuration
@Slf4j
public class JobConfig {

    private final int CHUNK_SIZE = 1000;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }
    @Bean
    public JdbcPagingItemReader<Customer> pagingItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(CHUNK_SIZE);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>(1);

        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public ItemProcessor itemProcessor() {
        return new ItemProcessor<Customer,Customer>() {
            @Override
            public Customer process(Customer customer) throws Exception {
                // sleep up to 10 milliseconds - simulate network latency, etc.
                Thread.sleep(new Random().nextInt(10));
                return Customer.builder()
                        .id(customer.getId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .birthDate(customer.getBirthDate())
                        .build();
            }
        };
    }

    @Bean
    public AsyncItemProcessor asyncItemProcessor() throws Exception{
        AsyncItemProcessor<Customer,Customer> asyncItemProcessor = new AsyncItemProcessor<>();

        asyncItemProcessor.setDelegate(itemProcessor());
        // for every task, it launches a new thread
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        asyncItemProcessor.afterPropertiesSet();
        return asyncItemProcessor;

    }

    @Bean
    public JdbcBatchItemWriter<Customer> customerJdbcBatchItemWriter() {
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("insert into new_customer values (:id, :firstName, :lastName, :birthDate)");
        // the point of this is to map the bean values to the sql parameters
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();

        return writer;
    }

    @Bean
    public AsyncItemWriter<Customer> asyncItemWriter() throws Exception{
        AsyncItemWriter<Customer> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(customerJdbcBatchItemWriter());
        asyncItemWriter.afterPropertiesSet();
        return asyncItemWriter;
    }

    @Bean
    public Step step() throws Exception {
        return stepBuilderFactory.get("step")
                .<Customer,Customer>chunk(CHUNK_SIZE)
                .reader(pagingItemReader())
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("multithread-item-job-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step())
                .build();
    }
}
