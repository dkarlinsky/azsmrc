package lbms.tools.flexyconf;

import java.util.ArrayList;
import java.util.List;

public class FCInterface {
	private List<EntryUpdateListener> entryUpdateListenersList = new ArrayList<EntryUpdateListener>();
	private EntryUpdateListener entryUpdateListener = new EntryUpdateListener() {
		public void updated(String key, String value) {};
	};

	private FlexyConfiguration fc;

	public FCInterface (FlexyConfiguration fc) {
		this.fc = fc;
	}

	public Entry getEntry (String key) {
		return fc.getRoot().getEntry(key);
	}

	/**
	 * @return Returns the entryUpdateListener.
	 */
	public EntryUpdateListener getEntryUpdateListener() {
		return entryUpdateListener;
	}

	public void addEntryUpdateListener (EntryUpdateListener l) {
		entryUpdateListenersList.add(l);
	}

	public void removeEntryUpdateListener (EntryUpdateListener l) {
		entryUpdateListenersList.remove(l);
	}
}
