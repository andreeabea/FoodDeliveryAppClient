package application.services.notification;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.Socket;

@Component
public class NotificationService {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private ServerSocketListener listener;

    @PostConstruct
    public void connectToNotificationServer() throws IOException {
        socket = new Socket("localhost", 8081);

        this.listener = new ServerSocketListener(socket);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //out.println("first message");
        //out.println("second message");
    }

    public String sendObject(String msg) throws IOException {
        out.println(msg);
        String received = null;
        try {
            received = receiveObject();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(received);
        return received;
    }

    public String receiveObject() throws IOException, InterruptedException {
        return listener.getReceived();
    }

}
