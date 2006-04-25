package lbms.tools.flexyconf;

public interface ContentProvider {

	public String getValue (String key, int type);
	
	public String getDefaultValue(String key, int type);

	public void setValue (String key, String value, int type);
}
