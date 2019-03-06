package udemy.skip;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
mvn clean install

Detects failure at processing item 42, skips processing for only that item and continues
 java -jar target/skip-0.0.1-SNAPSHOT.jar -skip=processor

Detects failure at writing -84, skips writing for only that item
java -jar target/skip-0.0.1-SNAPSHOT.jar -skip=writer
 */

@SpringBootApplication
@EnableBatchProcessing
public class SkipApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkipApplication.class, args);
    }

}
