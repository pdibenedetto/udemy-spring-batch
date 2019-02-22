package udemy.filteringitemprocessor.domain;

import org.springframework.batch.item.ItemProcessor;

public class FilteringItemProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        if (customer.getId() % 2 == 0){
            return null;
        }
        return customer;
    }
}
