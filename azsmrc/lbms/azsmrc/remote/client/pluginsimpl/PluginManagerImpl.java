/**
 * 
 */
package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.plugins.PluginManager;

/**
 * @author Damokles
 *
 */
public class PluginManagerImpl implements PluginManager {

	private Map<String, PluginInterfaceImpl> pluginMap = new TreeMap<String, PluginInterfaceImpl>();
	private PluginInterface[] emptyArray = new PluginInterface[0];

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

}
