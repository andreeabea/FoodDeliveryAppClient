package application;

import application.services.notification.ServerSocketListener;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories("application/repositories")
@EntityScan( basePackages = {"application/entities"} )
@ComponentScan( basePackages = {"application"} )
public class Client {

    public static void main(String[] args) {
        //SpringApplication.run(Client.class, args);
        Application.launch(JavaFXApplication.class, args);
    }
}