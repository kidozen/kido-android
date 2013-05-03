package kidozen.client;

import java.util.EventListener;

public interface ServiceEventListener extends EventListener {
	public void onFinish(ServiceEvent e);
}