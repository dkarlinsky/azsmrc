package lbms.tools.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class I18N {

	private I18N ()	{}

	private static I18NTranslator i18n = new I18NTranslator();

	public static void initialize(File defaultFile) throws IOException {
		i18n.initialize(defaultFile);
	}

	public static void load (File localizedFile) throws IOException {
		i18n.load(localizedFile);
	}

	public static void initialize(InputStream is) throws IOException {
		i18n.initialize(is);
	}

	public static void load (InputStream is) throws IOException {
		i18n.load(is);
	}

	public static String translate (String key) {
		return i18n.translate(key);
	}

	public static String translateDefault (String key) {
		return i18n.translateDefault(key);
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
