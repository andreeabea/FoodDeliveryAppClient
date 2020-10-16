package application.services.exceptions;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(String message)
    {
        super(message);
    }
}
