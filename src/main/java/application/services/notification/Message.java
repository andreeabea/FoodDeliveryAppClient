package application.services.notification;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {

    private String header;
    private List<String> objectsJson;
}
