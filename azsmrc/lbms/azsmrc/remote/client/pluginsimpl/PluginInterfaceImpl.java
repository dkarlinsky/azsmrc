package lbms.azsmrc.remote.client.pluginsimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import lbms.azsmrc.remote.client.plugins.AzSMRCInterface;
import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginClient;
import lbms.azsmrc.remote.client.plugins.PluginConfig;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.plugins.PluginManager;
import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;
import lbms.azsmrc.remote.client.pluginsimpl.ipc.IPCInterfaceImpl;

/**
 * @author Damokles
 *
 */
public class PluginInterfaceImpl implements PluginInterface {

	private PluginManagerImpl manager;
	private Plugin plugin;
	private String id;
	private String version;
	private String pluginDir;
	private IPCInterface ipcInterface;
	private PluginConfigImpl config;
	private File configFile;

	public PluginInterfaceImpl(PluginManagerImpl manager, Plugin plugin, String id, String version, String pluginDir) {
		this.manager = manager;
		this.plugin = plugin;
		this.id = id;
		this.version = version;
		this.pluginDir = pluginDir;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getIPCInterface()
	 */
	public IPCInterface getIPCInterface() {
		if (ipcInterface == null) {
			ipcInterface = new IPCInterfaceImpl(plugin);
		}
		return ipcInterface;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPlugin()
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPluginConfig()
	 */
	public PluginConfig getPluginConfig() {
		if (config == null) {
			config = new PluginConfigImpl();
			configFile = new File (pluginDir,"plugin.cfg");
			if (configFile.exists()) {
				try {
					FileInputStream is = new FileInputStream(configFile);
					config.load(is);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getPluginManager()
	 */
	public PluginManager getPluginManager() {
		return manager;
	}

	/**
	 * @return the id
	 */
	public String getPluginId() {
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

	/**
	 * Returns the AzSMRC interface.
	 * 
	 * You can use popup windows and set the Statusbar text with it.
	 * 
	 * @return AzSMRC interface
	 */
	public AzSMRCInterface getAzSMRCInterface () {
		return manager.getAzsmrcInterface();
	}

	/**
	 * The Plugin client is the Transport to Azureus
	 * 
	 * @return PluginClient 
	 */
	public PluginClient getPluginClient () {
		return manager.getPluginClient();
	}

	//--------------------------------------------------//

	public void initializePlugin() {
		plugin.initialize(this);
	}
}
