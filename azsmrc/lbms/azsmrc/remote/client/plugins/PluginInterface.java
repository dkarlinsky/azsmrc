package lbms.azsmrc.remote.client.plugins;

import org.apache.log4j.Logger;

import lbms.azsmrc.remote.client.plugins.download.DownloadManager;
import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;

/**
 * @author Damokles
 *
 */
public interface PluginInterface {

	/**
	 * Returns the Config for the Plugin
	 * @return
	 */
	public PluginConfig getPluginConfig();

	/**
	 * Returns the PluginManager
	 * @return
	 */
	public PluginManager getPluginManager();

	/**
	 * @return
	 */
	public PluginClient getPluginClient();

	/**
	 * @return
	 */
	public AzSMRCInterface getAzSMRCInterface();

	/**
	 * Returns the Plugin itself.
	 * 
	 * @return
	 */
	public Plugin getPlugin();

	/**
	 * Returns the IPC Interface for local IPC.
	 * 
	 * @return
	 */
	public IPCInterface getIPCInterface();

	/**
	 * @return directory the plugin is installed in
	 */
	public String getPluginDir();

	/**
	 * @return ID of the Plugin
	 */
	public String getPluginID();

	/**
	 * @return Version of the Plugin
	 */
	public String getPluginVersion();

	/**
	 * @return Name of the Plugin
	 */
	public String getPluginName();

	/**
	 * @return Version of AzSMRC
	 */
	public String getAzSMRCVersion();

	/**
	 * @return the UIManger
	 */
	public UISWTManager getUIManager();

	/**
	 * @return DownloadManager
	 */
	public DownloadManager getDownloadManager();

	/**
	 * @return I18N Interface
	 */
	public PluginI18N getI18N();

	/**
	 * @return logger
	 */
	public Logger getLogger();

	public boolean isDisabled();

	public void setDisabled(boolean disabled);
}
