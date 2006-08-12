/**
 * 
 */
package lbms.azsmrc.remote.client.pluginsimpl;

import org.jdom.Element;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.events.ClientEventListener;
import lbms.azsmrc.remote.client.plugins.PluginClient;

/**
 * @author Damokles
 *
 */
public class PluginClientImpl implements PluginClient, ClientEventListener {

	private Client client;

	public PluginClientImpl (Client client) {
		this.client = client;
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
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.events.ClientEventListener#handleEvent(int, long, org.jdom.Element)
	 */
	public void handleEvent(int type, long time, Element event) {
		// TODO Auto-generated method stub

	}
}
