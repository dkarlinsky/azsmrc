package lbms.azsmrc.remote.client.events;

import org.jdom.Element;

public interface ClientEventListener {
	public void handleEvent (int type, long time, Element event);
}
