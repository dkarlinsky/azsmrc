package lbms.azsmrc.remote.client.internat;

import java.io.IOException;
import java.io.InputStream;

import lbms.tools.i18n.I18NTranslator;

public class I18N {

	private static String	defLocation;
	private static String	localizedLocation;

	private I18N() {
	}

	private static I18NTranslator	i18n	= new I18NTranslator();

	private static void initialize(InputStream is) throws IOException {
		i18n.initialize(is);
	}

	private static void load(InputStream is) throws IOException {
		i18n.load(is);
	}

	public static void reload() throws IOException {
		InputStream is = null;
		try {
			if (defLocation != null) {
				is = I18N.class.getClassLoader().getResourceAsStream(
						defLocation);
				if (is != null) {
					initialize(is);
				}
			}
			if (localizedLocation != null) {
				is = I18N.class.getClassLoader().getResourceAsStream(
						localizedLocation);
				if (is != null) {
					load(is);
				}
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static void setDefault(String location) {
		defLocation = location;
	}

	public static void setLocalized(String location) {
		localizedLocation = location;
	}

	/**
	 * Returns a Localized Message identified by the key.
	 * 
	 * Returns the key if no entry was found.
	 * 
	 * @param key the key of the message.
	 * @return Localized Message or key
	 */
	public static String translate(String key) {
		if (!isInitialized() && defLocation != null) {
			try {
				reload();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return i18n.translate(key);
	}

	/**
	 * Returns a Localized Message identified by the key.
	 * 
	 * Returns the key if no entry was found. It will replace every instance of
	 * {i} with params[i].toString(), only the length of params is respected so
	 * no ArrayOutOfBoundsException.
	 * 
	 * @param key the key of the message.
	 * @param params replacements for placeholder
	 * @return fully replaced message or key
	 */
	public String translate(String key, Object... params) {
		if (!isInitialized() && defLocation != null) {
			try {
				reload();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return i18n.translate(key, params);
	}

	/**
	 * @return Returns the initialized.
	 */
	public static boolean isInitialized() {
		return i18n.isInitialized();
	}

	/**
	 * @return Returns the localized.
	 */
	public static boolean isLocalized() {
		return i18n.isLocalized();
	}
}
