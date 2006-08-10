/**
 * 
 */
package lbms.azsmrc.remote.client.pluginsimpl;

import java.util.Properties;

import lbms.azsmrc.remote.client.plugins.PluginConfig;
import lbms.tools.ExtendedProperties;

/**
 * @author Damokles
 *
 */
public class PluginConfigImpl 	extends ExtendedProperties
								implements	PluginConfig {

	public PluginConfigImpl() {
		super();
	}

	public PluginConfigImpl(Properties defaults) {
		super(defaults);
	}
}
