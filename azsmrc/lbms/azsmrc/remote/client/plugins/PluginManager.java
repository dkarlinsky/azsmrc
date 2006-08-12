/**
 * 
 */
package lbms.azsmrc.remote.client.plugins;

import lbms.azsmrc.remote.client.plugins.event.PluginListener;

/**
 * @author Damokles
 *
 */
public interface PluginManager {

	public PluginInterface getPluginInterfaceByID(String ID);

	public PluginInterface[] getPluginInterfaces();

	public void addPluginListener (PluginListener listener);

	public void removePluginListener (PluginListener listener);
}
