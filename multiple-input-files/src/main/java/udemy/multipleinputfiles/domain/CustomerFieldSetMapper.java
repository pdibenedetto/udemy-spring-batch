package udemy.multipleinputfiles.domain;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer> {
    @Override
    public Customer mapFieldSet(FieldSet fs) throws BindException {
        return Customer.builder()
                .id(fs.readLong("id"))
                .firstName(fs.readString("firstName"))
                .lastName(fs.readString("lastName"))
                .birthDate(fs.readDate("birthdate","yyyy-MM-dd HH:mm:ss"))
                .build();
    }
}
