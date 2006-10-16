package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.plugins.AzSMRCInterface;
import lbms.azsmrc.remote.client.plugins.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.plugins.PluginManager;
import lbms.azsmrc.remote.client.plugins.event.PluginListener;
import lbms.azsmrc.remote.client.pluginsimpl.ui.swt.UISWTManagerImpl;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.SplashScreen;

/**
 * @author Damokles
 *
 */
public class PluginManagerImpl implements PluginManager {

	private Map<String, PluginInterfaceImpl> pluginMap = new TreeMap<String, PluginInterfaceImpl>();
	private PluginInterface[] emptyArray = new PluginInterface[0];
	private AzSMRCInterface azsmrcInterface;
	private PluginClientImpl plClient;
	private UISWTManagerImpl uiManager;
	private RCMain rcMain;

	private List<PluginListener> listeners = new ArrayList<PluginListener>();

	public PluginInterfaceImpl addPlugin (Plugin plug, Properties props, String dir) {
		PluginInterfaceImpl pI = new PluginInterfaceImpl (this ,
				plug, props.getProperty("plugin.id"), props.getProperty("plugin.name"), props.getProperty("plugin.version"), dir);
		pluginMap.put(pI.getPluginID(), pI);
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

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginManager#addPluginListener(lbms.azsmrc.remote.client.plugins.event.PluginListener)
	 */
	public void addPluginListener(PluginListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.PluginManager#removePluginListener(lbms.azsmrc.remote.client.plugins.event.PluginListener)
	 */
	public void removePluginListener(PluginListener listener) {
		listeners.remove(listener);
	}

	public void callStartupCompleted () {
		for (int i=0;i<listeners.size();i++)
			listeners.get(i).startupCompleted();
	}

	public void callShutdownInitiated () {
		for (int i=0;i<listeners.size();i++)
			listeners.get(i).shutdownInitiated();
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

	protected RCMain getRcMain() {
		return rcMain;
	}

	/**
	 * Initializes the Interfaces and Plugins
	 *
	 * The Client has to be created before this is called.
	 *
	 * @param rcMain
	 */
	public void initialize (RCMain rcMain) {
		this.rcMain = rcMain;
		this.plClient = new PluginClientImpl(rcMain.getClient());
		this.azsmrcInterface = new AzSMRCInterfaceImpl(rcMain);
		this.uiManager = new UISWTManagerImpl();

		for (PluginInterfaceImpl pi:pluginMap.values()) {
			try {
				SplashScreen.setText("Initializing Plugin: "+pi.getPluginName());
				pi.initializePlugin();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public UISWTManagerImpl getUIManager() {
		return uiManager;
	}

}
