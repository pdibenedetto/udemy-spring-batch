package udemy.multipledestinations.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import udemy.multipledestinations.domain.Customer;
import udemy.multipledestinations.domain.CustomerLineAggregator;
import udemy.multipledestinations.domain.CustomerRowMapper;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;

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

//    @Bean
//    public CompositeItemWriter<Customer> itemWriter() throws Exception {
//        List<ItemWriter<? super Customer>> writers = new ArrayList<>(2);
//        writers.add(xmlItemWriter());
//        writers.add(jsonItemWriter());
//
//        CompositeItemWriter<Customer> itemWriter = new CompositeItemWriter<>();
//        itemWriter.setDelegates(writers);
//        itemWriter.afterPropertiesSet();
//
//        return itemWriter;
//    }

    /*
    We can create a custom classifier that delegates items to specific output based on your own criteria
     */
    @Bean
    public ClassifierCompositeItemWriter<Customer> itemWriter() throws Exception {
        ClassifierCompositeItemWriter<Customer> itemWriter = new ClassifierCompositeItemWriter<>();
        itemWriter.setClassifier(new CustomerClassifier(xmlItemWriter(), jsonItemWriter()));
        return itemWriter;
    }

    @Bean
    public Step step() throws Exception {
        return stepBuilderFactory.get("step")
                .<Customer,Customer>chunk(10)
                .reader(pagingItemReader())
                .writer(itemWriter())
                // Need to register these as streams
                .stream(xmlItemWriter())
                .stream(jsonItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        String jobName = String.format("xml-and-json-%d", new Date().getTime());
        return jobBuilderFactory.get(jobName)
                .start(step())
                .build();
    }

}

