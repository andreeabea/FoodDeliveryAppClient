package application.controllers;

import application.JavaFXApplication;
import application.controllers.util.AlertBox;
import application.dto.UserDTO;
import application.dto.UserType;
import application.services.exceptions.IncorrectPasswordException;
import application.services.exceptions.UserNotFoundException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
@FxmlView("login.fxml")
public class LoginController {
	
	@FXML
	public Button loginButton;
	
	@FXML
	public TextField username;
	
	@FXML
	public TextField password;

	@Autowired
	NotificationService notificationService;

	public void login() throws IOException {

		String usernameText = username.getText();
		String passwordText = password.getText();
		UserDTO userDTO = UserDTO.builder().username(usernameText).password(passwordText).build();

		if(usernameText.equals("") || passwordText.equals(""))
		{
			AlertBox.display("No input", "You forgot to write your mail/password");
		}
		else
		{
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> jsonList = new ArrayList<>();
			jsonList.add(mapper.writeValueAsString(userDTO));
			Message toSend = Message.builder().header("login").objectsJson(jsonList).build();
			String msg = mapper.writeValueAsString(toSend);
			String received = notificationService.sendObject(msg);
			Message receivedMessage = mapper.readValue(received,Message.class);

			if(receivedMessage.getHeader().equals(UserNotFoundException.class.getName()))
			{
				AlertBox.display("Invalid email",receivedMessage.getObjectsJson().get(0));
			}
			else if(receivedMessage.getHeader().equals(IncorrectPasswordException.class.getName()))
			{
				AlertBox.display("Invalid password", receivedMessage.getObjectsJson().get(0));
			}
			else {
				userDTO = mapper.readValue(receivedMessage.getObjectsJson().get(0),UserDTO.class);
				if (userDTO.getUserType() == UserType.ADMIN) {
					JavaFXApplication.changeScene(AdminController.class);
				} else if (userDTO.getUserType() == UserType.REGULAR) {
					UserController.setCurrentUser(userDTO);
					JavaFXApplication.changeScene(UserController.class);
				} else if (userDTO.getUserType() == UserType.DELIVERY) {
					JavaFXApplication.changeScene(DeliveryUserController.class);
				}
			}
		}
    }

    public void registerRegularUser() throws IOException {
		JavaFXApplication.changeScene(RegisterController.class);
	}
}
