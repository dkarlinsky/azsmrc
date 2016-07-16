package lbms.azsmrc.remote.client.plugins;

/**
 * I18N for your plugin
 * 
 * @author Damokles
 *
 */
public interface PluginI18N {

	/**
	 * Translates the text.
	 * 
	 * Returns the translated text or the key if no
	 * entry found.
	 * 
	 * @param key The key indentifying the entry
	 * @return entry or key
	 */
	public String translate (String key);
}
