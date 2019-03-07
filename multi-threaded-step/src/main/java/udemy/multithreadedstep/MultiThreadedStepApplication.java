package udemy.multithreadedstep;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class MultiThreadedStepApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiThreadedStepApplication.class, args);
    }

}
