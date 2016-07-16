package lbms.tools.flexyconf;

public interface I18NProvider {

	/**
	 * This method should be used to provide
	 * I18N capabilities to the FlexyConf
	 * 
	 * @param key Key to the message
	 * @return Translated Message
	 */
	public String translate (String key);
}
