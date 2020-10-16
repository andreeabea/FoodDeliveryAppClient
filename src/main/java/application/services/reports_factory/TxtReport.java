package application.services.reports_factory;

import application.dto.ItemDTO;
import application.dto.RestaurantDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TxtReport implements Report {
    @Override
    public void generateReport(RestaurantDTO restaurant, String path) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY hh:mm:ss");
        String date = formatter.format(LocalDateTime.now())+"\n";
        String line1 = "Restaurant "+ restaurant.getName() + " with id " + restaurant.getId() + "\n";
        String line2 = "Number of items: " + restaurant.getItems().size();
        String line3 = "Items: ";

        List<String> lines = new ArrayList<String>();
        lines.add(date);
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);

        for(ItemDTO item : restaurant.getItems())
        {
            String line = item.getName()+" with id: " + item.getId() + ", stock: " + item.getStock()
                    +", price: "+ item.getPrice();
            lines.add(line);
        }

        try {
            Files.write(Paths.get(path + restaurant.getName() + ".txt"), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
