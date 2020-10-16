package application.controllers;

import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.DiscountDTO;
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
@FxmlView("adminDiscounts.fxml")
public class AdminDiscountsController implements Initializable {

    @FXML
    private TableView<DiscountDTO> table;

    @FXML
    private TableColumn<DiscountDTO, Integer> id;

    @FXML
    private TableColumn<DiscountDTO, String> discount;

    @FXML
    private TextField idField;

    @FXML
    private TextField itemsField;

    @FXML
    private TextField percentageField;

    private static RestaurantDTO selectedRestaurant;

    @Autowired
    private NotificationService notificationService;

    public void updateTable() throws IOException
    {
        id.setCellValueFactory(new PropertyValueFactory<DiscountDTO, Integer>("id"));
        discount.setCellValueFactory(new PropertyValueFactory<DiscountDTO, String>("discount"));

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(selectedRestaurant.getId()));
        Message toSend = Message.builder().header("getDiscounts").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<DiscountDTO> discountDTOS = new ArrayList<>();
        List<String> discountStrings = receivedMessage.getObjectsJson();
        for(String s : discountStrings)
        {
            discountDTOS.add(mapper.readValue(s,DiscountDTO.class));
        }
        table.getItems().setAll(discountDTOS);
    }

    public static void setSelectedRestaurant(RestaurantDTO restaurant)
    {
        selectedRestaurant=restaurant;
    }

    public void addDiscount() throws IOException
    {
        String itemsString = itemsField.getText();
        String percentageString = percentageField.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(selectedRestaurant.getId()));
        jsonList.add(String.valueOf(itemsString));
        jsonList.add(String.valueOf(percentageString));
        Message toSend = Message.builder().header("addDiscount").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage==null)
        {
            AlertBox.display("Discount added!", "Discount successfully added for restaurant "+ selectedRestaurant.getName() + ".");
            updateTable();
        }else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
            AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
        } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
            AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
        }
        itemsField.clear();
        percentageField.clear();
    }

    public void deleteDiscount() throws IOException
    {
        String idString = idField.getText();

        boolean answer = ConfirmBox.display("Delete discount", "Are you sure you want to delete discount " +
                idString +" for restaurant "+ selectedRestaurant.getName()+ "?");

        if(answer)
        {
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(mapper.writeValueAsString(selectedRestaurant.getId()));
            jsonList.add(idString);
            Message toSend = Message.builder().header("deleteDiscount").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if(receivedMessage!=null)
            {
                if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Discount not found", receivedMessage.getObjectsJson().get(0));
                }
            }
        }

        idField.clear();
        updateTable();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
