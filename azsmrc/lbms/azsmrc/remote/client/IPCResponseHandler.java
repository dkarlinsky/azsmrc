/**
 * 
 */
package lbms.azsmrc.remote.client;

/**
 * @author Damokles
 *
 */
public interface IPCResponseHandler {
	public void ipcResponse (String pluginID, String method, Object response);
}
