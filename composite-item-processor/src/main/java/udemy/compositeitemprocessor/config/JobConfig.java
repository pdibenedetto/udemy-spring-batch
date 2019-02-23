package udemy.compositeitemprocessor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import udemy.compositeitemprocessor.domain.*;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;

@Configuration
@Slf4j
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
    public FlatFileItemWriter<Customer> jsonItemWriter() throws Exception {
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
    public CompositeItemProcessor<Customer,Customer> itemProcessor() throws Exception {
        List<ItemProcessor<Customer,Customer>> delegates = new ArrayList<>(2);

        delegates.add(new FilteringItemProcessor());
        delegates.add(new UpperCaseItemProcessor());

        CompositeItemProcessor<Customer,Customer> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(delegates);
        compositeItemProcessor.afterPropertiesSet();

        return compositeItemProcessor;


    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .<Customer,Customer>chunk(10)
                .reader(pagingItemReader())
                .processor(itemProcessor())
                .writer(jsonItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("composite-item-processor-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();

    }

}
