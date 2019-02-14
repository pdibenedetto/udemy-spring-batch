package udemy.multipledestinations.config;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import udemy.multipledestinations.domain.Customer;

public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {
    private final ItemWriter<Customer> evenWriter;
    private final ItemWriter<Customer> oddWriter;

    public CustomerClassifier(ItemWriter<Customer> evenWriter, ItemWriter<Customer> oddWriter) {
        this.evenWriter = evenWriter;
        this.oddWriter = oddWriter;
    }

    @Override
    public ItemWriter<? super Customer> classify(Customer customer) {
        return customer.getId() % 2 == 0 ? evenWriter : oddWriter;
    }
}
