package application.controllers;

import application.JavaFXApplication;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@FxmlView("admin.fxml")
public class AdminController {

    @FXML
    public Button usersButton;

    @FXML
    public Button restaurantsButton;

    public void viewRegularUsers() throws IOException
    {
        Scene scene = JavaFXApplication.changeScene(AdminUsersController.class);
    }

    public void viewRestaurants() throws IOException
    {
        Scene scene = JavaFXApplication.changeScene(AdminRestaurantsController.class);
    }
}
