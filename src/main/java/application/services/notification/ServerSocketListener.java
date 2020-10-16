package application.services.notification;

import application.JavaFXApplication;
import application.controllers.DeliveryUserController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runnable listening for incoming messages from the server
 */
public class ServerSocketListener implements Runnable {

    private final Socket serverSocket;

    private String received;

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    public ServerSocketListener(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            while(true){
                received = in.readLine();
                ObjectMapper mapper = new ObjectMapper();
                Message receivedMessage = mapper.readValue(received,Message.class);
                if(receivedMessage!=null && receivedMessage.getHeader()!=null && receivedMessage.getHeader().equals("courierNotification"))
                {
                    JavaFXApplication.notifyDeliveryUser(receivedMessage.getObjectsJson().get(0));
                }
                else
                {
                    queue.offer(received);
                }
                synchronized (queue){
                    System.out.println("Notifying...");
                    queue.notify();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("The server is no longer available");

        }
    }

    public String getReceived() throws InterruptedException {
        System.out.println("Waiting for new message.");
        synchronized (queue) {
            queue.wait();
        }
        return queue.poll();
    }

}