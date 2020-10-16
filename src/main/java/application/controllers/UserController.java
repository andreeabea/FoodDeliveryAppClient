package application.controllers;

import application.JavaFXApplication;
import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.RestaurantDTO;
import application.dto.UserDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@FxmlView("user.fxml")
public class UserController implements Initializable {

    @FXML
    private TableView<RestaurantDTO> table;

    @FXML
    public TableColumn<RestaurantDTO, Integer> id;

    @FXML
    public TableColumn<RestaurantDTO, String> name;

    @FXML
    public TableColumn<RestaurantDTO, String> items;

    @FXML
    public TableColumn<RestaurantDTO, String> rating;

    @FXML
    private TableView<RestaurantDTO> table1;

    @FXML
    public TableColumn<RestaurantDTO, Integer> id1;

    @FXML
    public TableColumn<RestaurantDTO, String> name1;

    @FXML
    public TableColumn<RestaurantDTO, String> items1;

    @FXML
    public TableColumn<RestaurantDTO, String> rating1;

    @FXML
    public ComboBox<Integer> ratingBox;

    @FXML
    public TextField searchRatingBox;

    private static UserDTO currentUser;

    @Autowired
    NotificationService notificationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<Integer> possibleRates = new ArrayList<>();
            for(int i=1;i<=5;i++)
            {
                possibleRates.add(i);
            }
            ratingBox.getItems().setAll(possibleRates);
            updateTable();
            updateFavouritesTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTable() throws IOException{
        id.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, Integer>("id"));
        name.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, String>("name"));
        items.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getItemsNumberProperty());
        rating.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getRatingProperty());

        ObjectMapper mapper = new ObjectMapper();
        Message toSend = Message.builder().header("getRestaurants").build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
        List<String> restaurantStrings = receivedMessage.getObjectsJson();
        for(String s : restaurantStrings)
        {
            restaurantDTOS.add(mapper.readValue(s,RestaurantDTO.class));
        }
        table.getItems().setAll(restaurantDTOS);
    }

    public void updateFavouritesTable() throws IOException{
        id1.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, Integer>("id"));
        name1.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, String>("name"));
        items1.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getItemsNumberProperty());
        rating1.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getRatingProperty());

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(currentUser.getId()));
        Message toSend = Message.builder().header("getFavouriteRestaurants").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
        List<String> restaurantStrings = receivedMessage.getObjectsJson();
        for(String s : restaurantStrings)
        {
            restaurantDTOS.add(mapper.readValue(s,RestaurantDTO.class));
        }
        table1.getItems().setAll(restaurantDTOS);
    }

    public static void setCurrentUser(UserDTO user)
    {
        currentUser = user;
    }

    public void viewRestaurantItems() throws IOException
    {
        RestaurantDTO selected = table.getSelectionModel().getSelectedItem();
        if(selected==null)
        {
            AlertBox.display("Restaurant not selected", "No restaurant from restaurants table selected!");
        }
        else {
            String idString = String.valueOf(selected.getId());
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(idString);
            Message toSend = Message.builder().header("getSelRestaurant").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage.getHeader().equals(RestaurantDTO.class.getName())) {
                UserItemsController.setSelectedRestaurant(mapper.readValue(receivedMessage.getObjectsJson().get(0), RestaurantDTO.class));
                UserItemsController.setCurrentUser(currentUser);
                Scene newScene = JavaFXApplication.changeScene(UserItemsController.class);
                newScene.getWindow().setOnCloseRequest(windowEvent -> UserItemsController.itemsToOrder = new HashMap<>());
            } else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
            }
            updateTable();
        }
    }

    public void addRestaurantToFavourites() throws IOException
    {
        RestaurantDTO selected = table.getSelectionModel().getSelectedItem();
        if(selected==null)
        {
            AlertBox.display("Restaurant not selected", "No restaurant from restaurants table selected!");
        }
        else {
            String idString = String.valueOf(selected.getId());
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(idString);
            jsonList.add(String.valueOf(currentUser.getId()));
            Message toSend = Message.builder().header("addFavouriteRestaurant").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage.getHeader().equals("UserDTO")) {
                currentUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
                table.getSelectionModel().clearSelection();
                updateFavouritesTable();
            } else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
            }
        }
    }

    public void deleteFavouriteRestaurant() throws IOException
    {
        RestaurantDTO selected = table1.getSelectionModel().getSelectedItem();
        if(selected==null)
        {
            AlertBox.display("Restaurant not selected", "No restaurant from favourites table selected!");
        }
        else {
            String idString = String.valueOf(selected.getId());
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(idString);
            jsonList.add(String.valueOf(currentUser.getId()));
            Message toSend = Message.builder().header("deleteFavouriteRestaurant").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage.getHeader().equals("UserDTO")) {
                currentUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
                table1.getSelectionModel().clearSelection();
                updateFavouritesTable();
            } else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
            }
        }
    }

    public void rateRestaurant() throws IOException
    {
        Integer rate = ratingBox.getSelectionModel().getSelectedItem();
        RestaurantDTO selected = table.getSelectionModel().getSelectedItem();
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
                            + selected.getName() +" with " + rate + " stars?");
            if(answer)
            {
                ObjectMapper mapper = new ObjectMapper();
                List<String> jsonList = new ArrayList<>();
                jsonList.add(String.valueOf(selected.getId()));
                jsonList.add(String.valueOf(rate));
                jsonList.add(String.valueOf(currentUser.getId()));
                Message toSend = Message.builder().header("rateRestaurant").objectsJson(jsonList).build();
                String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
                Message receivedMessage = mapper.readValue(received, Message.class);

                if(receivedMessage.getHeader().equals("UserDTO"))
                {
                    currentUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
                    AlertBox.display("Successful rating added!", "Restaurant "+ selected.getName()
                            + " was successfully rated with "+ rate + " stars.");
                    table.getSelectionModel().clearSelection();
                    updateTable();
                    updateFavouritesTable();
                    ratingBox.getSelectionModel().clearSelection();
                }else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
                }
            }
        }
    }

    public void updateTable(float rate) throws IOException{
        id.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, Integer>("id"));
        name.setCellValueFactory(new PropertyValueFactory<RestaurantDTO, String>("name"));
        items.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getItemsNumberProperty());
        rating.setCellValueFactory(restaurantDto -> restaurantDto.getValue().getRatingProperty());

        ObjectMapper mapper = new ObjectMapper();
        Message toSend = Message.builder().header("getRestaurants").build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
        List<String> restaurantStrings = receivedMessage.getObjectsJson();
        for(String s : restaurantStrings)
        {
            RestaurantDTO restaurantDTO = mapper.readValue(s,RestaurantDTO.class);
            String[] splitted =restaurantDTO.getRating().split("\\s+");
            float currentRate = Float.parseFloat(splitted[0]);
            if(currentRate==rate)
            {
                restaurantDTOS.add(restaurantDTO);
            }
        }
        table.getItems().setAll(restaurantDTOS);
    }

    public void searchByRating() throws IOException {
        String rateString = searchRatingBox.getText();
        if(rateString==null || rateString.equals("") || !rateString.matches("-?\\d+(\\.\\d+)?"))
        {
            AlertBox.display("Invalid data", "Introduce a valid rating to search restaurants by!");
        }
        else
        {
            float rate = Float.parseFloat(rateString);
            updateTable(rate);
        }
    }
}
