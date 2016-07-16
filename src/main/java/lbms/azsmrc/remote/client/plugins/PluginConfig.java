package lbms.azsmrc.remote.client.plugins;

/**
 * The Config for Plugins
 * 
 * All values are stored as Strings, you can use the converter 
 * methods to access them as numbers or boolean.
 * 
 * @author Damokles
 *
 */
public interface PluginConfig {

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns false if the property is not found.
	 * 
	 * @param key the property key.
	 * @return
	 */
	public String getProperty (String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public String getProperty (String key, String def);

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns false if the property is not found.
	 * 
	 * @param key the property key.
	 * @return
	 */
	public boolean getPropertyAsBoolean(String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public boolean getPropertyAsBoolean(String key, boolean def);

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public int getPropertyAsInt(String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public int getPropertyAsInt(String key, int def);

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 * 
	 * @param key the property key.
	 * @return
	 */
	public long getPropertyAsLong(String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public long getPropertyAsLong(String key, long def);

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public float getPropertyAsFloat(String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public float getPropertyAsFloat(String key, float def);

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public double getPropertyAsDouble(String key);

	/**
	 * Searches for the property with the specified key in this property list. 
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked.
	 * The method returns the default value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @param def a default value.
	 * @return
	 */
	public double getPropertyAsDouble(String key, int def);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 * @return previous value
	 */
	public Object setProperty (String key, String value);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty(String key, boolean value);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty(String key, int value);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty(String key, long value);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty(String key, float value);

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty(String key, Double value);
}
