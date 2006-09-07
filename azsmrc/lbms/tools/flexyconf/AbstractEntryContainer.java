package lbms.tools.flexyconf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntryContainer {

	protected Map<String, Entry> entries = new HashMap<String, Entry>();

	/**
	 * @return the entries
	 */
	public Entry[] getEntries() {
		Set<String> keys = entries.keySet();
		Entry[] eArray = new Entry[keys.size()];
		int i = 0;
		for (String key:keys) {
			eArray[i++] = entries.get(key);
		}
		return eArray;
	}

	/**
	 * @return the entries sorted by Index
	 */
	public Entry[] getSortedEntries() {
		Entry[] e = getEntries();
		Arrays.sort(e);
		return e;
	}


	protected void checkDependency(String key,boolean enabled) {
		Set<String> keys = entries.keySet();
		for (String k:keys) {
			entries.get(k).checkDependency(key, enabled);
		}
	}

	public void init() {
		Set<String> keys = entries.keySet();
		for (String k:keys) {
			entries.get(k).init();
		}
	}

	public Entry getEntry (String key) {
		if (entries.containsKey(key))
			return entries.get(key);
		else return null;
	}

	protected void addEntry (Entry e) {
		entries.put(e.getKey(), e);
	}
}
