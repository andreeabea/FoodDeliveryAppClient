package application.controllers;

import application.controllers.util.AlertBox;
import application.dto.UserDTO;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@FxmlView("register.fxml")
public class RegisterController {

    @FXML
    TextField nameField;

    @FXML
    TextField usernameField;

    @FXML
    TextField passwordField;

    @FXML
    TextField passwordField2;

    @FXML
    TextField walletField;

    @Autowired
    NotificationService notificationService;

    public void register() throws IOException {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String password2= passwordField2.getText();
        String walletString = walletField.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(name);
        jsonList.add(username);
        jsonList.add(password);
        jsonList.add(password2);
        jsonList.add(walletString);

        Message toSend = Message.builder().header("register").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage.getHeader().equals("UserDTO"))
        {
            UserDTO registeredUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
            AlertBox.display("Account created", "User " + registeredUser.getUsername() + " successfully registered!");
            Stage currentScene = (Stage)walletField.getScene().getWindow();
            currentScene.close();
        }
        else {
            AlertBox.display("Invalid Data",receivedMessage.getObjectsJson().get(0));
        }

    }
}
