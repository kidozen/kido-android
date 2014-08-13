package kidozen.client;

/**
 * Created by christian on 7/28/14.
 */
public class InitializationException extends Exception {

    public InitializationException(String message, Exception inner) {
        super(message,inner);
    }
    public InitializationException(String message) {
        super(message);
    }
}
