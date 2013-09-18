package kidozen.client;

import java.util.Observable;

import kidozen.client.authentication.KidoZenUser;
/**
 * @author kidozen
 * @version 1.00, April 2013
 *          <p/>
 *          For internal use only. Do not override or use
 */
public class ObservableUser extends Observable {
    public void TokenUpdated(KidoZenUser kzuser) {
        this.setChanged();
        this.notifyObservers(kzuser);
    }
}