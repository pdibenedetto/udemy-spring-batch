package udemy.flatfileoutput.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomerLineAggregator implements LineAggregator<Customer> {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String aggregate(Customer customer) {
        try {
            return objectMapper.writeValueAsString(customer);
        } catch (JsonProcessingException e){
            throw new RuntimeException("unable to serialize customer", e);
        }
    }
}
