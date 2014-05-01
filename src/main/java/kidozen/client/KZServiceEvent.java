package kidozen.client;

/**
 * Created by christian on 4/30/14.
 */
public interface KZServiceEvent<T> {
    public void Fire(T message);
}
