package lbms.tools.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class I18NTools {

	public static void writeToFile (File file,Map<String, String> map) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			SortedSet<String> keys = new TreeSet<String> (map.keySet()); //sort all entries
			for (String key:keys) {
				fw.write(key+"="+map.get(key).replace("\n", "\\n")+"\n");
			}
		} finally {
			if (fw!=null) fw.close();
		}
	}

	public static Map<String,String> readFromFile (File file) throws IOException {
		Map<String, String> map = new TreeMap<String, String>();
		I18N.load(new BufferedReader(new FileReader(file)), map);
		return map;
	}

	public static Map<String,String> duplicate (Map<String,String> map) {
		Map<String, String> target = new TreeMap<String, String>();
		SortedSet<String> keys = new TreeSet<String> (map.keySet()); //sort all entries
		for (String key:keys) {
			target.put(key, "");
		}
		return target;
	}

	public static void merge (Map<String,String> src, Map<String,String> target) {
		SortedSet<String> keys = new TreeSet<String> (src.keySet());
		for (String key:keys) {
			if (!target.containsKey(key)) {
				target.put(key, "");
			}
		}
	}
}
