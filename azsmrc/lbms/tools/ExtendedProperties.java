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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsBoolean(java.lang.String)
	 */
	public boolean getPropertyAsBoolean (String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsBoolean(java.lang.String, boolean)
	 */
	public boolean getPropertyAsBoolean (String key, boolean def) {
		String prop = getProperty(key);
		if (prop == null)
			return def;
		else
			return Boolean.parseBoolean(getProperty(key));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsInt(java.lang.String)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsInt(java.lang.String, int)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsLong(java.lang.String)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsLong(java.lang.String, long)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsFloat(java.lang.String)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsFloat(java.lang.String, float)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsDouble(java.lang.String)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#getPropertyAsDouble(java.lang.String, int)
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

	/* (non-Javadoc)
	 * @see lbms.tools.Config#setProperty(java.lang.String, boolean)
	 */
	public void setProperty (String key, boolean value) {
		setProperty(key, Boolean.toString(value));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#setProperty(java.lang.String, int)
	 */
	public void setProperty (String key, int value) {
		setProperty(key, Integer.toString(value));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#setProperty(java.lang.String, long)
	 */
	public void setProperty (String key, long value) {
		setProperty(key, Long.toString(value));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#setProperty(java.lang.String, float)
	 */
	public void setProperty (String key, float value) {
		setProperty(key, Float.toString(value));
	}

	/* (non-Javadoc)
	 * @see lbms.tools.Config#setProperty(java.lang.String, java.lang.Double)
	 */
	public void setProperty (String key, Double value) {
		setProperty(key, Double.toString(value));
	}
}
