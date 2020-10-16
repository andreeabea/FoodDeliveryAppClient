package application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountDTO {

    private int id;
    private String discount;
}
