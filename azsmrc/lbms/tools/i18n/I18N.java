package lbms.tools.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class I18N {

	private static Map<String, String> defaultMessages = new HashMap<String, String>();
	private static Map<String, String> localizedMessages = new HashMap<String, String>();

	private static boolean initialized, localized;

	public static void initialize(File defaultFile) throws IOException {
		load (new BufferedReader(new FileReader(defaultFile)), defaultMessages);
		initialized = true;
	}

	public static void load (File localizedFile) throws IOException {
		load (new BufferedReader(new FileReader(localizedFile)), localizedMessages);
		localized = true;
	}

	protected static void load (BufferedReader ir, Map<String,String> map) throws IOException {
		try {
			do {
				String line = ir.readLine();
				if (line == null) break; //EOF
				if (line.indexOf("//") == 0) continue; //ignore comments
				if (!line.contains("=")) continue; //invalid line
				String[] parts = line.split("=", 2);
				map.put(parts[0], parts[1]);
			} while (true);

		} finally {
			if (ir!=null) ir.close();
		}
	}

	public static String getMessage (String key) {
		if (localizedMessages.containsKey(key))
			return localizedMessages.get(key);
		else
			return getDefaultMessage(key);
	}

	public static String getDefaultMessage (String key) {
		if (defaultMessages.containsKey(key))
			return defaultMessages.get(key);
		else
			return key;
	}

	/**
	 * @return Returns the initialized.
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return Returns the localized.
	 */
	public static boolean isLocalized() {
		return localized;
	}
}
