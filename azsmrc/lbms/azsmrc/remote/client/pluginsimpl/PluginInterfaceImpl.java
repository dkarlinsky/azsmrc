package lbms.azsmrc.remote.client.pluginsimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

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
import lbms.azsmrc.remote.client.pluginsimpl.ipc.IPCInterfaceImpl;
import lbms.tools.flexyconf.FlexyConfiguration;

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
	private String name;
	private Properties pluginProps;
	private IPCInterface ipcInterface;
	private PluginConfigImpl config;
	private File configFile;
	private PluginI18NAdapter i18n;
	private FlexyConfiguration fc;

	public PluginInterfaceImpl(PluginManagerImpl manager, Plugin plugin, Properties props, String pluginDir) {
		this.manager = manager;
		this.plugin = plugin;
		this.pluginProps = props;
		this.id = props.getProperty("plugin.id");
		this.name = props.getProperty("plugin.name");
		this.version = props.getProperty("plugin.version");
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
			Properties defaults = null;
			String defConf = pluginProps.getProperty("azsmrc.default.config");
			if (defConf != null) {
				InputStream ris = PluginInterfaceImpl.class.getClassLoader().getResourceAsStream(defConf);
				if (ris != null) {
					defaults = new Properties();
					try {
						defaults.load(ris);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							ris.close();
						} catch (IOException e) {}
					}
				}

			}

			if (defaults != null)
				config = new PluginConfigImpl();
			else
				config = new PluginConfigImpl(defaults);

			configFile = new File (pluginDir,"plugin.cfg");
			if (configFile.exists()) {
				FileInputStream is = null;
				try {
					is = new FileInputStream(configFile);
					config.load(is);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (is!=null)
						try {
							is.close();
						} catch (IOException e) {}
				}
			}
		}
		return config;
	}

	public void savePluginConfig() {
		if (config != null) {
			configFile = new File (pluginDir,"plugin.cfg");
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(configFile);
				config.store(os, getPluginVersion());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os!=null)
					try {
						os.close();
					} catch (IOException e) {}
			}
		}
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

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getAzSMRCVersion()
	 */
	public String getAzSMRCVersion() {
		return manager.getRcMain().getAzsmrcProperties().getProperty("version","x.x");
	}

	/**
	 * The Plugin client is the Transport to Azureus
	 *
	 * @return PluginClient
	 */
	public PluginClient getPluginClient () {
		return manager.getPluginClient();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getUIManager()
	 */
	public UISWTManager getUIManager() {
		return manager.getUIManager();
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return Logger.getLogger("Plugin."+getPluginName());
	}

	public DownloadManager getDownloadManager() {
		return manager.getRcMain().getClient().getDownloadManager();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginInterface#getI18N()
	 */
	public PluginI18N getI18N() {
		if (i18n == null) {
			i18n = new PluginI18NAdapter();
			String langfilePath = pluginProps.getProperty("azsmrc.plugin.langpath");
			if (langfilePath != null) {
				langfilePath = langfilePath.replace('.', '/');
				if (langfilePath.charAt(langfilePath.length()-1) != '/') langfilePath += '/';
				if (langfilePath.charAt(0) != '/') langfilePath = '/'+langfilePath;
				InputStream is = PluginInterfaceImpl.class.getResourceAsStream(langfilePath+"default.lang");
				if (is != null) {
					try {
						i18n.initialize(is);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						is = null;
					}
				}
				is = PluginInterfaceImpl.class.getResourceAsStream(langfilePath+manager.getRcMain().getProperties().getProperty("language")+".lang");
				if (is != null) {
					try {
						i18n.load(is);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						is = null;
					}
				}
			}
		}
		return i18n;
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

	//--------------------------------------------------//

	public void initializePlugin() {
		plugin.initialize(this);
	}

	public FlexyConfiguration getPluginFlexyConf() {
		if (fc == null) {
			if (pluginProps.getProperty("azsmrc.plugin.config") == null) return null;
			InputStream is = PluginInterfaceImpl.class.getResourceAsStream(pluginProps.getProperty("azsmrc.plugin.config"));
			if (is!=null) {
				try {
					fc = FlexyConfiguration.readFromStream(is,getPluginID());
				} catch (IOException e) {
					fc = null;
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return fc;
	}
}
