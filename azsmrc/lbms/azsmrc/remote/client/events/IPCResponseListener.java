/**
 * 
 */
package lbms.azsmrc.remote.client.events;

import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public interface IPCResponseListener {

	/**
	 * @param status the response status @see RemoteConstants.IPC_*
	 * @param senderID the ID of the caller
	 * @param pluginID the called pluginID
	 * @param method the method that was called
	 * @param parameter null or a  parameter Element
	 */
	public void handleIPCResponse (int status, String senderID, String pluginID, String method, Element parameter);
}
