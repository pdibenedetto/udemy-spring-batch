package udemy.readingxml.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import udemy.readingxml.domain.Customer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class JobConfig {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    public JobConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean
    public StaxEventItemReader<Customer> customerItemReader() {
        XStreamMarshaller unmarshaller = new XStreamMarshaller();

        Map<String,Class>  aliaes = new HashMap<>();
        aliaes.put("customer", Customer.class);
        unmarshaller.setAliases(aliaes);

        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();
        reader.setResource(new ClassPathResource("/data/customer.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(unmarshaller);
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
                .reader(customerItemReader())
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
