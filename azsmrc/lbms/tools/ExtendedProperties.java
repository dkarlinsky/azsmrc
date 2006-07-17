/**
 * 
 */
package lbms.tools;

import java.util.Properties;

/**
 * This class extends java.util.Properties with conversion methods.
 * 
 * The class is extended with conveniece methods for reading and
 * writing primitive types to the Property.
 * 
 * NOTE: all values are stored as Strings, you can access every key
 * with getPropertyAs* the values are converted via Integer.parseInt
 * and so forth. If a NumberFormatException occurs the default value
 * is returned.
 * 
 * @author Damokles
 *
 */
public class ExtendedProperties extends Properties {

	/**
	 * Creates an empty property list with no default values.
	 */
	public ExtendedProperties() {
	}

	/**
	 * Creates an empty property list with the specified defaults.
	 * 
	 * @param defaults
	 */
	public ExtendedProperties(Properties defaults) {
		super(defaults);
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns false if the property is not found.
	 * 
	 * @param key the property key.
	 * @return
	 */
	public boolean getPropertyAsBoolean (String key) {
		return Boolean.parseBoolean(key);
	}

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
	public boolean getPropertyAsBoolean (String key, boolean def) {
		String prop = getProperty(key);
		if (prop == null)
			return def;
		else
			return Boolean.parseBoolean(key);
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public int getPropertyAsInt (String key) {
		String prop = getProperty(key);
		if (prop == null) return 0;
		try {
			return Integer.parseInt(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

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
	public int getPropertyAsInt (String key, int def) {
		String prop = getProperty(key);
		if (prop == null) return def;
		try {
			return Integer.parseInt(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return def;
		}
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 * 
	 * @param key the property key.
	 * @return
	 */
	public long getPropertyAsLong (String key) {
		String prop = getProperty(key);
		if (prop == null) return 0;
		try {
			return Long.parseLong(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

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
	public long getPropertyAsLong (String key, long def) {
		String prop = getProperty(key);
		if (prop == null) return def;
		try {
			return Long.parseLong(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return def;
		}
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public float getPropertyAsFloat (String key) {
		String prop = getProperty(key);
		if (prop == null) return 0;
		try {
			return Float.parseFloat(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

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
	public float getPropertyAsFloat (String key, float def) {
		String prop = getProperty(key);
		if (prop == null) return def;
		try {
			return Float.parseFloat(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return def;
		}
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. 
	 * The method returns 0 if the property is not found.
	 *
	 * @param key the property key.
	 * @return
	 */
	public double getPropertyAsDouble (String key) {
		String prop = getProperty(key);
		if (prop == null) return 0;
		try {
			return Double.parseDouble(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

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
	public double getPropertyAsDouble (String key, int def) {
		String prop = getProperty(key);
		if (prop == null) return def;
		try {
			return Double.parseDouble(prop);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return def;
		}
	}

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty (String key, boolean value) {
		setProperty(key, Boolean.toString(value));
	}

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty (String key, int value) {
		setProperty(key, Integer.toString(value));
	}

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty (String key, long value) {
		setProperty(key, Long.toString(value));
	}

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty (String key, float value) {
		setProperty(key, Float.toString(value));
	}

	/**
	 * @param key the key to be placed into this property list.
	 * @param value the value corresponding to key.
	 */
	public void setProperty (String key, Double value) {
		setProperty(key, Double.toString(value));
	}
}
