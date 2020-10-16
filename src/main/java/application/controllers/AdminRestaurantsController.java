package application.controllers;

import application.JavaFXApplication;
import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.RestaurantDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import application.services.reports_factory.Report;
import application.services.reports_factory.ReportsFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
@FxmlView("adminRestaurants.fxml")
public class AdminRestaurantsController implements Initializable {

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
    public TextField idField;

    @FXML
    public TextField nameFieldCreate;

    @FXML
    public TextField nameFieldUpdate;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
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

    public void createRestaurant() throws IOException
    {
        String name = nameFieldCreate.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(name);
        Message toSend = Message.builder().header("createRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage!=null) {
            if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            }
        }

        updateTable();
        nameFieldCreate.clear();
    }

    public void updateRestaurant() throws IOException
    {
        String idString = idField.getText();
        String name = nameFieldUpdate.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        jsonList.add(name);
        Message toSend = Message.builder().header("updateRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage!=null)
        {
            if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
            }
        }

        updateTable();
        idField.clear();
        nameFieldUpdate.clear();
    }

    public void deleteRestaurant() throws IOException
    {
        String idString = idField.getText();

        boolean answer = ConfirmBox.display("Delete restaurant", "Are you sure you want to delete restaurant " + idString + "?");
        if(answer) {
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(idString);
            Message toSend = Message.builder().header("deleteRestaurant").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if(receivedMessage!=null)
            {
                if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                    AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
                } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                    AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
                }
            }
        }

        updateTable();
        idField.clear();
    }

    public void viewRestaurantItems() throws IOException
    {
        String idString = idField.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        Message toSend = Message.builder().header("getSelRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage.getHeader().equals(RestaurantDTO.class.getName()))
        {
            RestaurantItemsController.setSelectedRestaurant(mapper.readValue(receivedMessage.getObjectsJson().get(0), RestaurantDTO.class));

            Stage currentScene = (Stage) table.getScene().getWindow();
            currentScene.close();
            Scene newScene = JavaFXApplication.changeScene(RestaurantItemsController.class);
            newScene.getWindow().setOnCloseRequest(windowEvent -> {
                try {
                    JavaFXApplication.changeScene(AdminRestaurantsController.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        else if(receivedMessage.getHeader().equals(InvalidDataException.class.getName()))
        {
            AlertBox.display("Invalid Data",receivedMessage.getObjectsJson().get(0));
        }
        else if(receivedMessage.getHeader().equals(EntityNotFoundException.class.getName()))
        {
            AlertBox.display("Restaurant not found",receivedMessage.getObjectsJson().get(0));
        }

        updateTable();
        idField.clear();
    }

    public void generateTxtRestaurantReport() throws IOException
    {
        String idString = idField.getText();
        ReportsFactory reportsFactory = new ReportsFactory();
        Report txtReport = reportsFactory.getReport("txt");

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        Message toSend = Message.builder().header("getSelRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);


        if(receivedMessage.getHeader().equals(RestaurantDTO.class.getName()))
        {
            RestaurantDTO restaurantDTO = mapper.readValue(receivedMessage.getObjectsJson().get(0),RestaurantDTO.class);
            FileChooser fileChooser = new FileChooser();
            String path = fileChooser.showSaveDialog(table.getScene().getWindow()).getPath();
            txtReport.generateReport(restaurantDTO,path);
        }
        else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName()))
        {
            AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
        }
        else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName()))
        {
            AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
        }

        idField.clear();
    }

    public void generatePdfRestaurantReport() throws IOException
    {
        String idString = idField.getText();
        ReportsFactory reportsFactory = new ReportsFactory();
        Report pdfReport = reportsFactory.getReport("pdf");

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        Message toSend = Message.builder().header("getSelRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);


        if(receivedMessage.getHeader().equals(RestaurantDTO.class.getName()))
        {
            RestaurantDTO restaurantDTO = mapper.readValue(receivedMessage.getObjectsJson().get(0),RestaurantDTO.class);
            FileChooser fileChooser = new FileChooser();
            String path = fileChooser.showSaveDialog(table.getScene().getWindow()).getPath();
            pdfReport.generateReport(restaurantDTO,path);
        }
        else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName()))
        {
            AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
        }
        else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName()))
        {
            AlertBox.display("Restaurant not found", receivedMessage.getObjectsJson().get(0));
        }

        idField.clear();
    }

    public void viewDiscounts() throws IOException
    {
        String idString = idField.getText();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(idString);
        Message toSend = Message.builder().header("getSelRestaurant").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        if(receivedMessage.getHeader().equals(RestaurantDTO.class.getName()))
        {
            AdminDiscountsController.setSelectedRestaurant(mapper.readValue(receivedMessage.getObjectsJson().get(0), RestaurantDTO.class));
            Scene newScene = JavaFXApplication.changeScene(AdminDiscountsController.class);

        }
        else if(receivedMessage.getHeader().equals(InvalidDataException.class.getName()))
        {
            AlertBox.display("Invalid Data",receivedMessage.getObjectsJson().get(0));
        }
        else if(receivedMessage.getHeader().equals(EntityNotFoundException.class.getName()))
        {
            AlertBox.display("Restaurant not found",receivedMessage.getObjectsJson().get(0));
        }

        updateTable();
        idField.clear();
    }
}
