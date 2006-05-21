package lbms.tools.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class I18NTranslator {

	private Map<String, String> defaultMessages = new HashMap<String, String>();
	private Map<String, String> localizedMessages = new HashMap<String, String>();

	private boolean initialized, localized;

	public void initialize(File defaultFile) throws IOException {
		load (new BufferedReader(new FileReader(defaultFile)), defaultMessages);
		initialized = true;
	}

	public void load (File localizedFile) throws IOException {
		load (new BufferedReader(new FileReader(localizedFile)), localizedMessages);
		localized = true;
	}

	public void initialize(InputStream is) throws IOException {
		load (new BufferedReader(new InputStreamReader(is)), defaultMessages);
		initialized = true;
	}

	public void load (InputStream is) throws IOException {
		load (new BufferedReader(new InputStreamReader(is)), localizedMessages);
		localized = true;
	}

	protected static void load (BufferedReader ir, Map<String,String> map) throws IOException {
		try {
			map.clear();
			do {
				String line = ir.readLine();
				if (line == null) break; //EOF
				if (line.indexOf("//") == 0 || line.indexOf("#") == 0) continue; //ignore comments
				if (!line.contains("=")) continue; //invalid line
				String[] parts = line.split("=", 2);
				map.put(parts[0], parts[1].replace("\\n", "\n"));
			} while (true);

		} finally {
			if (ir!=null) ir.close();
		}
	}

	public String translate (String key) {
		if (localized && localizedMessages.containsKey(key))
			return localizedMessages.get(key);
		else
			return translateDefault(key);
	}

	public String translateDefault (String key) {
		if (initialized && defaultMessages.containsKey(key))
			return defaultMessages.get(key);
		else
			return key;
	}

	/**
	 * @return Returns the initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return Returns the localized.
	 */
	public boolean isLocalized() {
		return localized;
	}
}
