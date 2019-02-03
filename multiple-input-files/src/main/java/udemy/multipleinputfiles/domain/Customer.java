package udemy.multipleinputfiles.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer implements ResourceAware {
    private long id;
    private String firstName;
    private String lastName;
    private Date birthDate;
    // optional - ResourceAware - but may be useful in troubleshooting where an item came from
    private Resource resource;

}