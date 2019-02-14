package udemy.multipledestinations.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
    private long id;
    private String firstName;
    private String lastName;
    private Date birthDate;

}