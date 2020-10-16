package application.controllers;

import application.controllers.util.AlertBox;
import application.dto.OrderDTO;
import application.dto.Status;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
@FxmlView("deliveryuser.fxml")
public class DeliveryUserController implements Initializable {

    @Autowired
    private NotificationService notificationService;

    @FXML
    private TableView<OrderDTO> table;

    @FXML
    private TableColumn<OrderDTO,Integer> id;

    @FXML
    private TableColumn<OrderDTO,String> customer;

    @FXML
    private TableColumn<OrderDTO,String> datetime;

    @FXML
    private TableColumn<OrderDTO,String> status;

    @FXML
    private ComboBox<Status> statusComboBox;

    @FXML
    private TextField idField;

    public void updateTable() throws IOException {
        id.setCellValueFactory(new PropertyValueFactory<OrderDTO, Integer>("id"));
        customer.setCellValueFactory(order -> order.getValue().getCustomerNameProperty());
        datetime.setCellValueFactory(new PropertyValueFactory<OrderDTO, String>("datetime"));
        status.setCellValueFactory(order -> order.getValue().getStatusProperty());

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        Message toSend = Message.builder().header("getOrders").build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        List<OrderDTO> orders = new ArrayList<>();
        jsonList = receivedMessage.getObjectsJson();

        for(String s : jsonList)
        {
            orders.add(mapper.readValue(s,OrderDTO.class));
        }

        table.getItems().setAll(orders);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusComboBox.getItems().setAll(Status.values());
    }

    public void changeOrderStatus() throws IOException {
        String idString = idField.getText();
        Status status = statusComboBox.getSelectionModel().getSelectedItem();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        if(status==null)
        {
            AlertBox.display("Invalid Data","Order status not selected!");
        }
        else {
            jsonList.add(status.toString());
            Message toSend = Message.builder().header("changeOrderStatus").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage != null && receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage != null && receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Order not found", receivedMessage.getObjectsJson().get(0));
            }
            updateTable();
        }
        idField.clear();
        statusComboBox.getSelectionModel().clearSelection();
    }
}
