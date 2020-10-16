package application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Builder
@ToString
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDTO {

    private int id;

    private UserDTO customer;

    private UserDTO courier;

    private String datetime;

    private Status status;

    public StringProperty getCustomerNameProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        sp.setValue(customer.getName());
        return sp;
    }

    public StringProperty getStatusProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        sp.setValue(status.toString());
        return sp;
    }
}
