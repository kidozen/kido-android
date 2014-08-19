package kidozen.client;

import java.util.EventListener;

/**
 * Generic listener
 */
public interface ServiceEventListener extends EventListener {
	public void onFinish(ServiceEvent e);

}