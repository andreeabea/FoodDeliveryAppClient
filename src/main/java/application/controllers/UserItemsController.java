package application.controllers;

import application.controllers.util.AlertBox;
import application.controllers.util.ConfirmBox;
import application.dto.DiscountDTO;
import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import application.dto.UserDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.notification.Message;
import application.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
@FxmlView("userViewItems.fxml")
public class UserItemsController implements Initializable {

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
    private TableView<ItemDTO> orderTable;

    @FXML
    public TableColumn<ItemDTO, Integer> id1;

    @FXML
    public TableColumn<ItemDTO, String> name1;

    @FXML
    public TableColumn<ItemDTO, String> quantity;

    @FXML
    public TableColumn<ItemDTO, Float> price1;

    @FXML
    TextField quantityField;

    @FXML
    Text currentWalletAmount;

    private static RestaurantDTO restaurantDTO;

    private static UserDTO currentUser;

    @FXML
    private TextArea discounts;

    @Autowired
    private NotificationService notificationService;

    public static Map<ItemDTO,String> itemsToOrder = new HashMap<>();

    public static void setSelectedRestaurant(RestaurantDTO restaurant)
    {
        restaurantDTO = restaurant;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateTable();
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(String.valueOf(restaurantDTO.getId()));
            Message toSend = Message.builder().header("getDiscounts").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            List<String> discountStrings = receivedMessage.getObjectsJson();
            String discountsText = "Discounts: \n";
            for(String s : discountStrings)
            {
                discountsText+=mapper.readValue(s,DiscountDTO.class).getDiscount()+"\n";
            }

            if(discountStrings.size()==0)
            {
                discountsText+="No discounts currently available.";
            }
            discounts.setText(discountsText);

        } catch (IOException e) {
            e.printStackTrace();
        }

        discounts.setEditable(false);
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

        List<ItemDTO> items = new ArrayList<>();
        jsonList = receivedMessage.getObjectsJson();

        for(String s : jsonList)
        {
            items.add(mapper.readValue(s,ItemDTO.class));
        }

        table.getItems().setAll(items);

        currentWalletAmount.setText("Current wallet amount: "+ currentUser.getWallet()+ " RON");
    }

    public void updateOrderTable()
    {
        id1.setCellValueFactory(new PropertyValueFactory<ItemDTO, Integer>("id"));
        name1.setCellValueFactory(new PropertyValueFactory<ItemDTO, String>("name"));
        price1.setCellValueFactory(new PropertyValueFactory<ItemDTO, Float>("price"));
        quantity.setCellValueFactory(item -> item.getValue().getQuantityProperty());

        orderTable.getItems().setAll(new ArrayList<>(itemsToOrder.keySet()));
    }

    private float computeOrderTotal()
    {
        float sum = 0.0f;
        for(Map.Entry<ItemDTO,String> entry : itemsToOrder.entrySet())
        {
            sum+=entry.getKey().getPrice()*Integer.parseInt(entry.getValue());
        }
        return sum;
    }

    public void addToOrder() throws IOException
    {
        ItemDTO selected = table.getSelectionModel().getSelectedItem();
        String quantityString = quantityField.getText();

        if(quantityString.equals("") || !quantityString.matches("-?\\d+(\\.\\d+)?")
                || Integer.parseInt(quantityString)<=0)
        {
            AlertBox.display("Invalid data", "Invalid quantity! Try again.");
        }
        else if(selected==null)
        {
            AlertBox.display("Invalid data", "No item selected!");
        }
        else {
            String idString = String.valueOf(selected.getId());
            ObjectMapper mapper = new ObjectMapper();
            List<String> jsonList = new ArrayList<>();
            jsonList.add(mapper.writeValueAsString(restaurantDTO));
            jsonList.add(idString);
            Message toSend = Message.builder().header("getItem").objectsJson(jsonList).build();
            String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            Message receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage.getHeader().equals("ItemDTO")) {
                ItemDTO item = mapper.readValue(receivedMessage.getObjectsJson().get(0), ItemDTO.class);
                if(Integer.parseInt(quantityString)>item.getStock())
                {
                    AlertBox.display("Invalid Data", "Invalid quantity for item " + item.getName() + ". Not enough in stock.");
                }
                else {
                    item.setQuantity(quantityString);
                    List<ItemDTO> duplicateItem = itemsToOrder.keySet().stream().filter(i->i.getId()==item.getId()).collect(Collectors.toList());
                    //if the item already exists between the items to order
                    if(duplicateItem.size()>0)
                    {
                        itemsToOrder.replace(duplicateItem.get(0),quantityString);
                        duplicateItem.get(0).setQuantity(quantityString);
                    }
                    else
                    {
                        itemsToOrder.put(item,quantityString);
                    }

                    updateOrderTable();
                }
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Entity not found", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            }

            table.getSelectionModel().clearSelection();
            quantityField.clear();
            updateTable();
        }
    }

    public void orderItems() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonList = new ArrayList<>();
        jsonList.add(String.valueOf(restaurantDTO.getId()));
        int nbItems=0;
        for (Map.Entry<ItemDTO, String> entry : itemsToOrder.entrySet()) {
            nbItems+=Integer.parseInt(entry.getValue());
        }
        jsonList.add(String.valueOf(nbItems));
        Message toSend = Message.builder().header("getDiscount").objectsJson(jsonList).build();
        String received = notificationService.sendObject(mapper.writeValueAsString(toSend));
        Message receivedMessage = mapper.readValue(received, Message.class);

        float totalAmount = computeOrderTotal();

        boolean answer=false;

        if(receivedMessage.getHeader()!=null)
        {
            Integer percentage = mapper.readValue(receivedMessage.getObjectsJson().get(0),Integer.class);

            totalAmount = totalAmount - percentage*totalAmount/100;

            answer = ConfirmBox.display("Place order","Are you sure you want to place the order? " +
                    "Total: "+ totalAmount + ". A discount of " + percentage + "% was applied.");
        }
        else
        {
            answer = ConfirmBox.display("Place order","Are you sure you want to place the order? " +
                    "Total: "+ totalAmount);
        }

        if(answer) {
            jsonList = new ArrayList<>();
            jsonList.add(mapper.writeValueAsString(currentUser));
            jsonList.add(mapper.writeValueAsString(restaurantDTO));
            Map<String, String> idQuantityMap = new HashMap<>();
            for (Map.Entry<ItemDTO, String> entry : itemsToOrder.entrySet()) {
                idQuantityMap.put(String.valueOf(entry.getKey().getId()), entry.getValue());
            }
            jsonList.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(idQuantityMap));
            toSend = Message.builder().header("orderItems").objectsJson(jsonList).build();
            received = notificationService.sendObject(mapper.writeValueAsString(toSend));
            receivedMessage = mapper.readValue(received, Message.class);

            if (receivedMessage.getHeader().equals("currentUser")) {
                currentUser = mapper.readValue(receivedMessage.getObjectsJson().get(0), UserDTO.class);
                UserController.setCurrentUser(currentUser);
                AlertBox.display("Order placed", "The order was placed successfully! The courier will contact " +
                        "you when it will be delivered");
                updateTable();
                itemsToOrder = new HashMap<>();
                orderTable.getItems().clear();
            } else if (receivedMessage.getHeader().equals(EntityNotFoundException.class.getName())) {
                AlertBox.display("Entity not found", receivedMessage.getObjectsJson().get(0));
            } else if (receivedMessage.getHeader().equals(InvalidDataException.class.getName())) {
                AlertBox.display("Invalid Data", receivedMessage.getObjectsJson().get(0));
            }
        }
    }

    public static void setCurrentUser(UserDTO user)
    {
        currentUser=user;
    }

    public void removeItemFromOrder()
    {
        ItemDTO selected = orderTable.getSelectionModel().getSelectedItem();

        if(selected==null)
        {
            AlertBox.display("Invalid data", "No item selected!");
        }
        else {
            int id = selected.getId();
            if (id < 0) {
                AlertBox.display("Invalid data", "Invalid id! Try again.");
            } else {
                List<ItemDTO> foundItem = itemsToOrder.keySet().stream().filter(item -> item.getId() == id).collect(Collectors.toList());
                boolean answer = ConfirmBox.display("Delete item from order", "Are you sure you want to delete "
                        + foundItem.get(0).getName() + " from your order?");
                if (answer) {
                    itemsToOrder.remove(foundItem.get(0));
                    updateOrderTable();
                    orderTable.getSelectionModel().clearSelection();
                }
            }
        }
    }
}
