/**
 * 
 */
package lbms.azsmrc.plugin.pluginsupport;

import org.gudy.azureus2.plugins.PluginInterface;

/**
 * @author Damokles
 *
 */
public interface PluginSupport {

	/**
	 * This is called after all other plugins are loaded.
	 * 
	 * The PluginSupport should discover if the plugin it supports,
	 * is available and set its status.
	 * @param pi
	 */
	public void initialize (PluginInterface pi);

	/**
	 * @return the name of the Supporter
	 */
	public String getName();

	/**
	 * @return the pluginID of the supported Plugin
	 */
	public String getSupportedPluginID();

	/**
	 * @return minimum Version of the supported Plugin
	 */
	public String getMinPluginVersion();

	/**
	 * @return whether the supported plugin was found
	 */
	public boolean isActive();
}
