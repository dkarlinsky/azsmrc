/**
 * 
 */
package lbms.azsmrc.remote.client.plugins;

import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;

/**
 * @author Damokles
 *
 */
public interface PluginInterface {

	public PluginConfig getPluginConfig();

	public PluginManager getPluginManager();

	public PluginClient getPluginClient();

	public AzSMRCInterface getAzSMRCInterface();

	public Plugin getPlugin();

	public IPCInterface getIPCInterface();

	public String getPluginDir();

	public String getPluginID();

	public String getPluginVersion();

	public String getPluginName();

	public UISWTManager getUIManager();
}
