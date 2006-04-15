package lbms.tools.flexyconf;

public interface ContentProvider {
	public float	getFloatValue	(String key);
	public double	getDoubleValue	(String key);
	public int		getIntValue		(String key);
	public long		getLongValue	(String key);
	public boolean	getBooleanValue	(String key);
	public String	getStringValue	(String key);

	public void setValue (String key, float		value);
	public void setValue (String key, double	value);
	public void setValue (String key, int		value);
	public void setValue (String key, long		value);
	public void setValue (String key, boolean	value);
	public void setValue (String key, String	value);
}
