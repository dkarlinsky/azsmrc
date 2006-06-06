package lbms.tools.flexyconf;

public interface ContentProvider {

	/**
	 * This function has to provide the Date for the
	 * FlexyConf Menu.
	 * 
	 * It MAY NOT return null,
	 * it has to return "" for type string
	 * false/true for type boolean
	 * 0 for any number type
	 * 
	 * This function has to provide default values, when
	 * no value is set. getDefaultValue is only used if
	 * the verification fails;
	 * 
	 * @param key the key to the stored parameter
	 * @param type the type of the parameter Entry.TYPE_*
	 * @return value of the stored parameter
	 */
	public String getValue (String key, int type);
	
	/**
	 * This queries the default values it is only used
	 * when the veryfication fails.
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public String getDefaultValue(String key, int type);

	/**
	 * @param key
	 * @param value
	 * @param type
	 */
	public void setValue (String key, String value, int type);
}
