package lbms.tools.flexyconf;

public abstract class ContentProvider {
	public abstract float	getFloatValue	(String key);
	public abstract double	getDoubleValue	(String key);
	public abstract int		getIntValue		(String key);
	public abstract long	getLongValue	(String key);
	public abstract boolean	getBooleanValue	(String key);
	public abstract String	getStringValue	(String key);

	public abstract float	getDefaultFloatValue	(String key);
	public abstract double	getDefaultDoubleValue	(String key);
	public abstract int		getDefaultIntValue		(String key);
	public abstract long	getDefaultLongValue		(String key);
	public abstract boolean	getDefaultBooleanValue	(String key);
	public abstract String	getDefaultStringValue	(String key);

	public abstract void setValue (String key, float	value);
	public abstract void setValue (String key, double	value);
	public abstract void setValue (String key, int		value);
	public abstract void setValue (String key, long		value);
	public abstract void setValue (String key, boolean	value);
	public abstract void setValue (String key, String	value);

	public String getValue (String key, int type) {
		switch (type) {
		case Entry.TYPE_BOOLEAN:
			return Boolean.toString(getBooleanValue(key));

		case Entry.TYPE_INT:
			return Integer.toString(getIntValue(key));

		case Entry.TYPE_LONG:
			return Long.toString(getLongValue(key));

		case Entry.TYPE_FLOAT:
			return Float.toString(getFloatValue(key));

		case Entry.TYPE_DOUBLE:
			return Double.toString(getDoubleValue(key));

		case Entry.TYPE_STRING:
			return getStringValue(key);

		default:
			return null;
		}
	}

	public String getDefaultValue(String key, int type) {
		switch (type) {
		case Entry.TYPE_BOOLEAN:
			return Boolean.toString(getDefaultBooleanValue(key));

		case Entry.TYPE_INT:
			return Integer.toString(getDefaultIntValue(key));

		case Entry.TYPE_LONG:
			return Long.toString(getDefaultLongValue(key));

		case Entry.TYPE_FLOAT:
			return Float.toString(getDefaultFloatValue(key));

		case Entry.TYPE_DOUBLE:
			return Double.toString(getDefaultDoubleValue(key));

		case Entry.TYPE_STRING:
			return getDefaultStringValue(key);

		default:
			return null;
		}
	}

	public void setValue (String key, String value, int type) {
		switch (type) {
		case Entry.TYPE_BOOLEAN:
			setValue(key, Boolean.parseBoolean(value));
			break;

		case Entry.TYPE_INT:
			setValue(key, Integer.parseInt(value));
			break;

		case Entry.TYPE_LONG:
			setValue(key, Long.parseLong(value));
			break;

		case Entry.TYPE_FLOAT:
			setValue(key, Float.parseFloat(value));
			break;

		case Entry.TYPE_DOUBLE:
			setValue(key, Double.parseDouble(value));
			break;

		case Entry.TYPE_STRING:
			setValue(key, value);
			break;
		}
	}
}
