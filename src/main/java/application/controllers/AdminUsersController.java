package application.controllers;

import application.JavaFXApplication;
import application.controllers.util.AlertBox;
import application.dto.UserDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@FxmlView("adminUsers.fxml")
public class AdminUsersController implements Initializable {

    @FXML
    private TableView<UserDTO> table;

    @FXML
    public TableColumn<UserDTO, Integer> id;

    @FXML
    public TableColumn<UserDTO, String> name;

    @FXML
    public TableColumn<UserDTO, String> username;

    @FXML
    public TableColumn<UserDTO, Float> wallet;

    @FXML
    TextField idField;

    @FXML
    TextField amountField;

    @Autowired
    NotificationService notificationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTable() throws IOException
    {
        id.setCellValueFactory(new PropertyValueFactory<UserDTO, Integer>("id"));
        name.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("name"));
        username.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("username"));
        wallet.setCellValueFactory(new PropertyValueFactory<UserDTO, Float>("wallet"));

        ObjectMapper mapper = new ObjectMapper();
        Message toSend = Message.builder().header("getUsers").build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<UserDTO> userDTOS = new ArrayList<>();
        List<String> userStrings = receivedMessage.getObjectsJson();
        for(String s : userStrings)
        {
            userDTOS.add(mapper.readValue(s,UserDTO.class));
        }
        table.getItems().setAll(userDTOS);
    }

    public void editWalletAmount() throws IOException
    {
        String idString = idField.getText();
        String amountString = amountField.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        jsonList.add(amountString);
        Message toSend = Message.builder().header("editWallet").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage!=null)
        {
            if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("User not found", receivedMessage.getObjectsJson().get(0));
            }
        }

        idField.clear();
        amountField.clear();
        updateTable();
    }

    public void viewUserRatings() throws IOException
    {
        String idString = idField.getText();
        //find user
        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        Message toSend = Message.builder().header("getUser").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if (receivedMessage.getHeader().equals("UserDTO"))
        {
            UserDTO userDTO = mapper.readValue(receivedMessage.getObjectsJson().get(0),UserDTO.class);
            //set user
            AdminRatingsController.setSelectedUser(userDTO);
            JavaFXApplication.changeScene(AdminRatingsController.class);
        }
        else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
            AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
        } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
            AlertBox.display("User not found", receivedMessage.getObjectsJson().get(0));
        }
    }
}
