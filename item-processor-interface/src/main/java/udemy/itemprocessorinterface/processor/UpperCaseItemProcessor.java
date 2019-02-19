package udemy.itemprocessorinterface.processor;

import org.springframework.batch.item.ItemProcessor;
import udemy.itemprocessorinterface.domain.Customer;

public class UpperCaseItemProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        /*
        Note we're not just modifying the same object and returning it.
        Batch expects this method to be idempotent.  That is, it should be able to execute the step multiple times
        and get the same result.  It may be called more than once on a particular item

        For example, if we were adding five days to a birthdate.  Re-running that logic on the same object
        might return a birthdate with 10 or more days added
         */
        return Customer.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName().toUpperCase())
                .lastName(customer.getLastName().toUpperCase())
                .birthDate(customer.getBirthDate())
                .build();
    }
}
