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

	public void load (File localizedFile) throws IOException {
		i18n.load(localizedFile);
	}

	public void initialize(InputStream is) throws IOException {
		i18n.initialize(is);
	}

	public void load (InputStream is) throws IOException {
		i18n.load(is);
	}

	public String translate (String key) {
		return i18n.translate(key);
	}

	public String translateDefault (String key) {
		return i18n.translateDefault(key);
	}

	/**
	 * @return Returns the initialized.
	 */
	public boolean isInitialized() {
		return i18n.isInitialized();
	}

	/**
	 * @return Returns the localized.
	 */
	public boolean isLocalized() {
		return i18n.isLocalized();
	}
}
