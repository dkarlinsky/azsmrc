package lbms.azsmrc.remote.client.plugins;

import lbms.azsmrc.remote.client.plugins.event.PluginListener;

/**
 * @author Damokles
 *
 */
public interface PluginManager {

	/**
	 * Returns the PluginInterface with the ID or null.
	 * 
	 * @param ID of the PluginInterface
	 * @return PluginInterface or null if not found
	 */
	public PluginInterface getPluginInterfaceByID(String ID);

	/**
	 * Returns all registered PluginInterfaces
	 * 
	 * @return Array of all PluginInterfaces
	 */
	public PluginInterface[] getPluginInterfaces();

	/**
	 * Adds a PluginListener to the PluginManager
	 * 
	 * @param listener
	 */
	public void addPluginListener (PluginListener listener);

	/**
	 * Removes a PluginListener from the PluginManager
	 * 
	 * @param listener
	 */
	public void removePluginListener (PluginListener listener);
}
