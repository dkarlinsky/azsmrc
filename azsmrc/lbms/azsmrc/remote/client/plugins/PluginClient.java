package lbms.azsmrc.remote.client.plugins;

import lbms.azsmrc.remote.client.plugins.event.PluginClientListener;

import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public interface PluginClient {

	/**
	 * Returns a Element for Requests.
	 * 
	 * @param id the switch of the Element, it has to be prefixed with the pluginID.
	 * @return Element for Requests
	 */
	public Element getSendElement(String id);

	/**
	 * Sends a fully customized Request to AzSMRC plugin.
	 * 
	 * The Element should be aquired by @see getSendElement 
	 * 
	 * @param e Element to send
	 */
	public void sendCustomRequest (Element e);

	/**
	 * Send an IPC call to Azureus
	 * 
	 * @param pluginID the pluginID of the target plugin
	 * @param senderID the local (plugin)ID of the sender
	 * @param method the remote method to call
	 * @param params array of Parameters supported are: boolean, int, long, float, double, String
	 */
	public void sendIPCCall (String pluginID, String senderID, String method, Object[] params);

	/**
	 * You need to be connected to communicate with the server.
	 * 
	 * You should check this before you send any requests.
	 * 
	 * @return true if connected
	 */
	public boolean isConnected();

	/**
	 * Adds a PluginClientListener
	 * 
	 * @param listener Listener to add
	 */
	public void addListener (PluginClientListener listener);

	/**
	 * Removes a PluginClientListener
	 * 
	 * @param listener Listener to remove
	 */
	public void removeListener (PluginClientListener listener);
}
