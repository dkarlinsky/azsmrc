/**
 * 
 */
package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.plugins.AzSMRCInterface;
import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.plugins.PluginManager;
import lbms.azsmrc.remote.client.swtgui.RCMain;

/**
 * @author Damokles
 *
 */
public class PluginManagerImpl implements PluginManager {

	private Map<String, PluginInterfaceImpl> pluginMap = new TreeMap<String, PluginInterfaceImpl>();
	private PluginInterface[] emptyArray = new PluginInterface[0];
	private AzSMRCInterface azsmrcInterface;
	private PluginClientImpl plClient;

	public PluginInterfaceImpl addPlugin (Plugin plug, Properties props,String dir) {
		PluginInterfaceImpl pI = new PluginInterfaceImpl (this ,
				plug, props.getProperty("plugin.id"), props.getProperty("plugin.version"), dir);
		pluginMap.put(pI.getPluginId(), pI);
		return pI;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginManager#getPluginInterfaceByID(java.lang.String)
	 */
	public PluginInterface getPluginInterfaceByID(String ID) {
		return pluginMap.get(ID);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginManager#getPluginInterfaces()
	 */
	public PluginInterface[] getPluginInterfaces() {
		return pluginMap.values().toArray(emptyArray);
	}

	/**
	 * @return the azsmrcInterface
	 */
	public AzSMRCInterface getAzsmrcInterface() {
		return azsmrcInterface;
	}

	/**
	 * @return the pluginClient
	 */
	public PluginClientImpl getPluginClient () {
		return plClient;
	}

	public void initialize (RCMain rcMain) {
		this.plClient = new PluginClientImpl(rcMain.getClient());
		this.azsmrcInterface = new AzSMRCInterfaceImpl(rcMain);

		for (PluginInterfaceImpl pi:pluginMap.values()) {
			try {
				pi.initializePlugin();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
