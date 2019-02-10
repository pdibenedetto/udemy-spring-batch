package udemy.flatfileoutput.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import udemy.flatfileoutput.domain.Customer;
import udemy.flatfileoutput.domain.CustomerLineAggregator;
import udemy.flatfileoutput.domain.CustomerRowMapper;

import javax.sql.DataSource;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
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
    public JdbcPagingItemReader<Customer> pagingItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(10);
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
    public FlatFileItemWriter<Customer> flatFileItemWriter() throws Exception {
        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();

        // this line aggregator will call 'toString' on each item passed in
//        writer.setLineAggregator(new PassThroughLineAggregator<>());

        writer.setLineAggregator(new CustomerLineAggregator());
        String customerOutputPath = File.createTempFile("customerOutput",".out").getAbsolutePath();
        log.info(">>> output path {}", customerOutputPath);
        writer.setResource(new FileSystemResource(customerOutputPath));
        writer.afterPropertiesSet();

        return writer;
    }

    @Bean
    public Step step() throws Exception {
        return stepBuilderFactory.get("step")
                .<Customer,Customer>chunk(10)
                .reader(pagingItemReader())
                .writer(flatFileItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("flat-file-output-job-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step())
                .build();
    }
}
