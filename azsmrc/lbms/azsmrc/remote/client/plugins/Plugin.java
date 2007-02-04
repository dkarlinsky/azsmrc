package lbms.azsmrc.remote.client.plugins;

/**
 * @author Damokles
 *
 */
public interface Plugin {

	/**
	 * This method is called when the PluginManager fires it up
	 * 
	 * @param pluginInterface
	 */
	public void initialize (PluginInterface pluginInterface);
}
