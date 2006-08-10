/**
 * 
 */
package lbms.azsmrc.remote.client.plugins;

import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;

/**
 * @author Damokles
 *
 */
public interface PluginInterface {

	public PluginConfig getPluginConfig();

	public PluginManager getPluginManager();

	public Plugin getPlugin();

	public IPCInterface getIPCInterface();
}
