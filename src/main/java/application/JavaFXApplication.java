package application;

import application.controllers.DeliveryUserController;
import application.controllers.LoginController;
import application.controllers.util.AlertBox;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class JavaFXApplication extends Application {

    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(Client.class)
                .run(args);
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }

    @Override
    public void start(Stage stage) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(LoginController.class);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("application/controllers/Style.css");
        stage.setScene(scene);
        stage.show();
    }

    //T extends Controller
    public static <T> Scene changeScene(Class<T> clasz) throws IOException {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(clasz);

        Stage stage = new Stage();
        Scene newScene = new Scene(root);
        newScene.getStylesheets().add("application/controllers/Style.css");
        stage.setScene(newScene);
        stage.show();

        return newScene;
    }

    public static void notifyDeliveryUser(String message) throws IOException {
        Platform.runLater(() ->
        {
            Stage stage = AlertBox.display("New order!", message);
        });
//        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
//        DeliveryUserController controller = (DeliveryUserController) (fxWeaver.loadController(DeliveryUserController.class));
//        try {
//            controller.updateTable();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
