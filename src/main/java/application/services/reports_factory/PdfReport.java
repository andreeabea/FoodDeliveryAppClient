package application.services.reports_factory;

import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfReport implements Report {
    @Override
    public void generateReport(RestaurantDTO restaurant, String path) {

        Document document = new Document();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream( path + restaurant.getName() + ".pdf"));

            document.open();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY hh:mm:ss");
            String date = formatter.format(LocalDateTime.now()) + "\n";
            String line1 = "Restaurant "+ restaurant.getName() + " with id " + restaurant.getId() + "\n";
            String line2 = "Number of items: " + restaurant.getItems().size();

            String line3 = "Items: ";

            document.add(new Paragraph(date));
            document.add(new Paragraph("\n"));
            document.add(new LineSeparator());
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(line1));
            document.add(new Paragraph("\n"));
            document.add(new LineSeparator());
            document.add(new Paragraph(line2));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(line3));

            for(ItemDTO item : restaurant.getItems())
            {
                String line = item.getName()+" with id: " + item.getId() + ", stock: " + item.getStock()
                        +", price: "+ item.getPrice();
                document.add(new Paragraph(line));
            }
            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
