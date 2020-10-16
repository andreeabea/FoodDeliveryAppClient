package application.controllers;

import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
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

@Component
@FxmlView("adminRestaurantItems.fxml")
public class RestaurantItemsController implements Initializable {

    @FXML
    private TableView<ItemDTO> table;

    @FXML
    public TableColumn<ItemDTO, Integer> id;

    @FXML
    public TableColumn<ItemDTO, String> name;

    @FXML
    public TableColumn<ItemDTO, Integer> stock;

    @FXML
    public TableColumn<ItemDTO, Float> price;

    @FXML
    TextField idField;

    @FXML
    TextField nameFieldCreate;

    @FXML
    TextField stockFieldCreate;

    @FXML
    TextField priceFieldCreate;

    @FXML
    TextField nameFieldUpdate;

    @FXML
    TextField stockFieldUpdate;

    @FXML
    TextField priceFieldUpdate;

    private static RestaurantDTO restaurantDTO;

    @Autowired
    private NotificationService notificationService;

    public static void setSelectedRestaurant(RestaurantDTO restaurant)
    {
        restaurantDTO = restaurant;
    }

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
        id.setCellValueFactory(new PropertyValueFactory<ItemDTO, Integer>("id"));
        name.setCellValueFactory(new PropertyValueFactory<ItemDTO, String>("name"));
        stock.setCellValueFactory(new PropertyValueFactory<ItemDTO, Integer>("stock"));
        price.setCellValueFactory(new PropertyValueFactory<ItemDTO, Float>("price"));

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(restaurantDTO.getId()));
        Message toSend = Message.builder().header("getItems").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<ItemDTO> itemDTOS = new ArrayList<>();
        List<String> itemStrings = receivedMessage.getObjectsJson();
        for(String s : itemStrings)
        {
            itemDTOS.add(mapper.readValue(s,ItemDTO.class));
        }
        table.getItems().setAll(itemDTOS);
    }

    public void addNewItem() throws IOException {
        String nameString = nameFieldCreate.getText();
        String stockString = stockFieldCreate.getText();
        String priceString = priceFieldCreate.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(mapper.writeValueAsString(restaurantDTO));
        jsonList.add(nameString);
        jsonList.add(stockString);
        jsonList.add(priceString);
        Message toSend = Message.builder().header("addItem").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage!=null) {
            if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            }
        }

        nameFieldCreate.clear();
        stockFieldCreate.clear();
        priceFieldCreate.clear();
        updateTable();
    }

    public void updateExistingItem() throws IOException {
        String idString = idField.getText();
        String nameString = nameFieldUpdate.getText();
        String stockString = stockFieldUpdate.getText();
        String priceString = priceFieldUpdate.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(mapper.writeValueAsString(restaurantDTO));
        jsonList.add(idString);
        jsonList.add(nameString);
        jsonList.add(stockString);
        jsonList.add(priceString);
        Message toSend = Message.builder().header("updateItem").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage!=null)
        {
            if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Item not found", receivedMessage.getObjectsJson().get(0));
            }
        }

        idField.clear();
        nameFieldUpdate.clear();
        stockFieldUpdate.clear();
        priceFieldUpdate.clear();
        updateTable();
    }

    public void deleteItem() throws IOException {
        String idString = idField.getText();

        boolean answer = ConfirmBox.display("Delete item", "Are you sure you want to delete item " + idString + "?");

        if(answer)
        {
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(mapper.writeValueAsString(restaurantDTO));
            jsonList.add(idString);
            Message toSend = Message.builder().header("deleteItem").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if(receivedMessage!=null)
            {
                if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Item not found", receivedMessage.getObjectsJson().get(0));
                }
            }
        }

        idField.clear();
        updateTable();
    }
}
