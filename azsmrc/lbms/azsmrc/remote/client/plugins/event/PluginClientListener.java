package lbms.azsmrc.remote.client.plugins.event;

import java.util.List;

import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public interface PluginClientListener {

	/**
	 * The Listener needs to identify himself.
	 * 
	 * Only events that match the id will be routed here.
	 * 
	 * @return PluginID
	 */
	public String getID();

	/**
	 * @param status the response status @see RemoteConstants.IPC_*
	 * @param senderID the ID of the caller
	 * @param pluginID the called pluginID
	 * @param method the method that was called
	 * @param parameter null or a list of parameters
	 */
	public void handleIPCResponse (int status, String senderID, String pluginID, String method, List<Element> parameter);

	/**
	 * This way the remote Client can tell the local one something.
	 * 
	 * @param e
	 */
	public void handleRemoteEvent (Element e);
}
