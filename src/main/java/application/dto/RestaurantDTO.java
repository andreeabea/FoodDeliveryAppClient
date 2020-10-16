package application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

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
public class RestaurantDTO {

    private int id;
    private String name;

    private List<ItemDTO> items;

    private String rating="0";

    public StringProperty getItemsNumberProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        String numberString = items.size()+" ";
        sp.setValue(numberString);
        return sp;
    }

    public StringProperty getRatingProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        String numberString = rating;
        sp.setValue(numberString);
        return sp;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", name=" + name;
    }
}
