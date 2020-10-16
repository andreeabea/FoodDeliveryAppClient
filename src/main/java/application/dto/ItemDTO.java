package application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {

    private int id;

    private String name;
    private int stock;
    private float price;

    //quantity to order
    private String quantity;

    public StringProperty getQuantityProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        sp.setValue(quantity);
        return sp;
    }
}
