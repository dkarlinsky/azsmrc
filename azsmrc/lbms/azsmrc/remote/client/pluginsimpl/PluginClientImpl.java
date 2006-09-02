package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.events.ClientEventListener;
import lbms.azsmrc.remote.client.events.IPCResponseListener;
import lbms.azsmrc.remote.client.plugins.PluginClient;
import lbms.azsmrc.remote.client.plugins.event.PluginClientListener;

/**
 * @author Damokles
 *
 */
public class PluginClientImpl implements PluginClient, ClientEventListener, IPCResponseListener {

	private Client client;
	private static Logger logger = Logger.getLogger(PluginClientImpl.class);
	private Map<String, PluginClientListener> listeners = new HashMap<String, PluginClientListener>();

	public PluginClientImpl (Client client) {
		this.client = client;
		client.addIPCResponseListener(this);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#getSendElement(java.lang.String)
	 */
	public Element getSendElement(String id) {
		Element sendElement = client.getSendElement(id);
		return sendElement;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#sendCustomRequest(org.jdom.Element)
	 */
	public void sendCustomRequest(Element e) {
		client.enqueue(e);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#sendIPCCall(java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public void sendIPCCall(String pluginID, String senderID, String method,
			Object[] params) {
		client.sendIPCCall(pluginID, senderID, method, params);
		logger.debug("PluginIPCCall: pid ["+pluginID+"] sid ["+senderID+"] method ["+method+"]");
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#isConnected()
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#addListener(lbms.azsmrc.remote.client.plugins.event.PluginClientListener)
	 */
	public void addListener(PluginClientListener listener) {
		listeners.put(listener.getID(), listener);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginClient#removeListener(lbms.azsmrc.remote.client.plugins.event.PluginClientListener)
	 */
	public void removeListener(PluginClientListener listener) {
		listeners.remove(listener.getID());
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.events.ClientEventListener#handleEvent(int, long, org.jdom.Element)
	 */
	public void handleEvent(int type, long time, Element event) {
		String id = event.getAttributeValue("targetID");
		if (id == null) return;
		if (listeners.containsKey(id)) {
			try {
				listeners.get(id).handleRemoteEvent((Element)event.getChildren().get(0));
			} catch (Throwable e) {
				logger.warn("PluginClient Handle Event Exception: ", e);
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.events.IPCResponseListener#handleResponse(int, java.lang.String, java.lang.String, java.lang.String, org.jdom.Element)
	 */
	public void handleIPCResponse(int status, String senderID, String pluginID, String method, Element parameter) {
		if (listeners.containsKey(pluginID)) {
			try {
				listeners.get(pluginID).handleIPCResponse(status, senderID, pluginID, method, parameter);
			} catch (Throwable e) {
				logger.warn("PluginClient IPC Handler Exception: ", e);
				e.printStackTrace();
			}
		}
	}
}
