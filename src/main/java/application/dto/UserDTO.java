package application.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserDTO {

    private int id;
    private String name;
    private String username;
    private String password;
    private float wallet;

    protected UserType userType;

}
