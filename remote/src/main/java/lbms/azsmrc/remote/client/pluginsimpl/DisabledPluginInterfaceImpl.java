package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.Properties;

import lbms.azsmrc.remote.client.plugins.AzSMRCInterface;
import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginClient;
import lbms.azsmrc.remote.client.plugins.PluginConfig;
import lbms.azsmrc.remote.client.plugins.PluginI18N;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.plugins.PluginManager;
import lbms.azsmrc.remote.client.plugins.download.DownloadManager;
import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;
import lbms.tools.flexyconf.FlexyConfiguration;

import org.apache.log4j.Logger;

/**
 * @author Damokles
 *
 */
public class DisabledPluginInterfaceImpl implements PluginInterface {

	private PluginManagerImpl manager;
	private String id;
	private String version;
	private String pluginDir;
	private String name;

	public DisabledPluginInterfaceImpl(PluginManagerImpl manager, Properties props, String pluginDir) {
		this.manager = manager;
		this.id = props.getProperty("plugin.id");
		this.name = props.getProperty("plugin.name");
		this.version = props.getProperty("plugin.version");
		this.pluginDir = pluginDir;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getIPCInterface()
	 */
	public IPCInterface getIPCInterface() {

		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPlugin()
	 */
	public Plugin getPlugin() {
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPluginConfig()
	 */
	public PluginConfig getPluginConfig() {
		return null;
	}

	public void savePluginConfig() {
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPluginManager()
	 */
	public PluginManager getPluginManager() {
		return null;
	}

	/**
	 * @return the id
	 */
	public String getPluginID() {
		return id;
	}

	/**
	 * @return the pluginDir
	 */
	public String getPluginDir() {
		return pluginDir;
	}

	/**
	 * @return the version
	 */
	public String getPluginVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPluginName()
	 */
	public String getPluginName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#isDisabled()
	 */
	public boolean isDisabled() {
		return manager.isDisabled(getPluginID());
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		manager.setDisabled(getPluginID(), disabled);
	}

	/**
	 * Returns the AzSMRC interface.
	 *
	 * You can use popup windows and set the Statusbar text with it.
	 *
	 * @return AzSMRC interface
	 */
	public AzSMRCInterface getAzSMRCInterface () {
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getAzSMRCVersion()
	 */
	public String getAzSMRCVersion() {
		return null;
	}

	/**
	 * The Plugin client is the Transport to Azureus
	 *
	 * @return PluginClient
	 */
	public PluginClient getPluginClient () {
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getUIManager()
	 */
	public UISWTManager getUIManager() {
		return null;
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	public DownloadManager getDownloadManager() {
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getI18N()
	 */
	public PluginI18N getI18N() {

		return null;
	}

	//--------------------------------------------------//

	public void initializePlugin() {

	}

	public FlexyConfiguration getPluginFlexyConf() {
		return null;
	}
}
