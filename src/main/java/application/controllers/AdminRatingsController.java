package application.controllers;

import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.ItemDTO;
import application.dto.RatingDTO;
import application.dto.RestaurantDTO;
import application.dto.UserDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
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

@Component
@FxmlView("adminRatings.fxml")
public class AdminRatingsController implements Initializable {

    @FXML
    private TableView<RatingDTO> table;

    @FXML
    private TableColumn<RatingDTO, Integer> id;

    @FXML
    private TableColumn<RatingDTO, String> restaurant;

    @FXML
    private TableColumn<RatingDTO, Integer> rating;

    @FXML
    private ComboBox<RestaurantDTO> restaurantsBox;

    @FXML
    private ComboBox<Integer> ratingBoxCreate;

    @FXML
    private ComboBox<Integer> ratingBoxUpdate;

    @FXML
    private TextField idField;

    private static UserDTO selectedUser;

    @Autowired
    private NotificationService notificationService;

    public void updateTable() throws IOException
    {
        id.setCellValueFactory(new PropertyValueFactory<RatingDTO, Integer>("id"));
        restaurant.setCellValueFactory(new PropertyValueFactory<RatingDTO, String>("restaurant"));
        rating.setCellValueFactory(new PropertyValueFactory<RatingDTO, Integer>("rate"));

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(selectedUser.getId()));
        Message toSend = Message.builder().header("getRatings").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<RatingDTO> ratingDTOS = new ArrayList<>();
        List<String> ratingStrings = receivedMessage.getObjectsJson();
        for(String s : ratingStrings)
        {
            ratingDTOS.add(mapper.readValue(s,RatingDTO.class));
        }
        table.getItems().setAll(ratingDTOS);
    }

    public static void setSelectedUser(UserDTO user)
    {
        selectedUser=user;
    }

    public void deleteRating() throws IOException
    {
        String idString = idField.getText();

        boolean answer = ConfirmBox.display("Delete rating", "Are you sure you want to delete rating " + idString + "?");

        if(answer)
        {
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(idString);
            jsonList.add(mapper.writeValueAsString(selectedUser));
            Message toSend = Message.builder().header("deleteRating").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if(receivedMessage!=null)
            {
                if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Rating not found", receivedMessage.getObjectsJson().get(0));
                }
            }
        }

        idField.clear();
        updateTable();
    }

    public void addRating() throws IOException
    {
        Integer rate = ratingBoxCreate.getSelectionModel().getSelectedItem();
        RestaurantDTO selected = restaurantsBox.getSelectionModel().getSelectedItem();
        if(selected==null)
        {
            AlertBox.display("Restaurant not selected", "No restaurant from restaurants table selected!");
        }
        else if(rate==null)
        {
            AlertBox.display("Rating not chosen", "Use the combo box to choose a rating!");
        }
        else
        {
            boolean answer = ConfirmBox.display("Rate restaurant","Are you sure you want to rate restaurant "
                    + selected.getName() +" with " + rate + " stars for user " + selectedUser.getUsername() + "?");
            if(answer)
            {
                ObjectMapper mapper = new ObjectMapper();
                List<String> jsonList = new ArrayList<>();
                jsonList.add(String.valueOf(selected.getId()));
                jsonList.add(String.valueOf(rate));
                jsonList.add(String.valueOf(selectedUser.getId()));
                Message toSend = Message.builder().header("addRating").objectsJson(jsonList).build();
                String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
                Message receivedMessage = mapper.readValue(received, Message.class);

                if(receivedMessage.getHeader().equals("UserDTO"))
                {
                    selectedUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
                    AlertBox.display("Successful rating added!", "Restaurant "+ selected.getName()
                            + " was successfully rated with "+ rate + " stars by user " + selectedUser.getUsername() + ".");
                    updateTable();
                }else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
                }

                ratingBoxCreate.getSelectionModel().clearSelection();
                restaurantsBox.getSelectionModel().clearSelection();
            }
        }
    }

    public void updateRating() throws IOException
    {
        String idString = idField.getText();
        Integer rate = ratingBoxUpdate.getSelectionModel().getSelectedItem();

        if(rate==null)
        {
            AlertBox.display("Rating not chosen", "Use the combo box to choose a rating!");
        }
        else{
            boolean answer = ConfirmBox.display("Update rating", "Are you sure you want to update rating " + idString + "?");

            if(answer)
            {
                ObjectMapper mapper = new ObjectMapper();
                List<String> jsonList = new ArrayList<>();
                jsonList.add(idString);
                jsonList.add(String.valueOf(rate));
                jsonList.add(mapper.writeValueAsString(selectedUser));
                Message toSend = Message.builder().header("updateRating").objectsJson(jsonList).build();
                String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
                Message receivedMessage = mapper.readValue(received, Message.class);

                if(receivedMessage!=null)
                {
                    if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                        AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                    } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                        AlertBox.display("Rating not found", receivedMessage.getObjectsJson().get(0));
                    }
                }
                else{
                    AlertBox.display("Successful rating updated!", "Rating "+ idString
                            + " was successfully updated with "+ rate + " stars for user " + selectedUser.getUsername() + ".");
                }
                idField.clear();
                ratingBoxUpdate.getSelectionModel().clearSelection();
                updateTable();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        Message toSend = Message.builder().header("getRestaurants").build();
        String received = null;
        try {
            received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);
            List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
            List<String> restaurantStrings = receivedMessage.getObjectsJson();
            for(String s : restaurantStrings)
            {
                restaurantDTOS.add(mapper.readValue(s,RestaurantDTO.class));
            }

            restaurantsBox.getItems().setAll(restaurantDTOS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Integer> possibleRates = new ArrayList<>();
        for(int i=1;i<=5;i++)
        {
            possibleRates.add(i);
        }

        ratingBoxCreate.getItems().setAll(possibleRates);
        ratingBoxUpdate.getItems().setAll(possibleRates);
    }
}
