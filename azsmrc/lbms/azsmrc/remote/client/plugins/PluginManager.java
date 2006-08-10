/**
 * 
 */
package lbms.azsmrc.remote.client.plugins;

/**
 * @author Damokles
 *
 */
public interface PluginManager {

	public PluginInterface getPluginInterfaceByID(String ID);

	public PluginInterface[] getPluginInterfaces();

}
