package udemy.itemprocessorinterface.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import udemy.itemprocessorinterface.domain.Customer;
import udemy.itemprocessorinterface.domain.CustomerRowMapper;
import udemy.itemprocessorinterface.processor.UpperCaseItemProcessor;

import javax.sql.DataSource;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public StaxEventItemWriter<Customer> xmlItemWriter() throws Exception {
        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        Map<String,Class> alias = new HashMap<>();
        alias.put("customer", Customer.class);
        xStreamMarshaller.setAliases(alias);

        StaxEventItemWriter<Customer> staxEventItemWriter = new StaxEventItemWriter<>();
        staxEventItemWriter.setRootTagName("customers");
        staxEventItemWriter.setMarshaller(xStreamMarshaller);

        String customerOutputPath = File.createTempFile("customerOutput", ".xml").getAbsolutePath();
        log.info("customerOutputPath={}", customerOutputPath);

        staxEventItemWriter.setResource(new FileSystemResource(customerOutputPath));
        staxEventItemWriter.afterPropertiesSet();

        return staxEventItemWriter;
    }

    @Bean
    public UpperCaseItemProcessor itemProcessor() {
        return new UpperCaseItemProcessor();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .<Customer,Customer>chunk(10)
                .reader(pagingItemReader())
                .processor(itemProcessor())
                .writer(xmlItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("xml-and-json-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step1())
                .build();

    }

}
