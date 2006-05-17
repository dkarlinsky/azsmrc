package lbms.tools.flexyconf;

import java.util.ArrayList;
import java.util.List;

public class FCInterface {
	private List<EntryUpdateListener> entryUpdateListenersList = new ArrayList<EntryUpdateListener>();
	private EntryUpdateListener entryUpdateListener = new EntryUpdateListener() {
		public void updated(String key, String value) {
			Entry e = getEntry(key);
			if (e!=null) {
				e.setValueQuiet(value);
				e.triggerDependencyCheck();
			}
		};
	};
	private ContentProvider cp;
	/**
	 * The domain should be used if you want to use
	 * multiple Flexyconfs in one Tree, or wherever
	 * there could be collisions
	 */
	private String domain = "";
	/**
	 * The Basic I18N Provider will simply return the key.
	 */
	private I18NProvider inp = new I18NProvider() {
		public String translate(String key) {
			return key;
		}
	};

	private FlexyConfiguration fc;

	public FCInterface (FlexyConfiguration fc) {
		this.fc = fc;
	}

	public FCInterface (FlexyConfiguration fc, String domain) {
		this.fc = fc;
		this.domain = domain;
	}

	public Entry getEntry (String key) {
		return fc.getRoot().getEntry(key);
	}


	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the cp
	 */
	public ContentProvider getContentProvider() {
		return cp;
	}

	/**
	 * @param cp the cp to set
	 */
	public void setContentProvider(ContentProvider cp) {
		this.cp = cp;
	}

	/**
	 * @return the inp
	 */
	public I18NProvider getI18NProvider() {
		return inp;
	}

	/**
	 * @param inp the inp to set
	 */
	public void setI18NProvider(I18NProvider inp) {
		this.inp = inp;
	}

	/**
	 * This EntryUpdateListener can be used as callback to set the values.
	 * 
	 * Use this listener to set the values if the
	 * ContentProvider is asynchronous.
	 * 
	 * @return Returns the entryUpdateListener.
	 */
	public EntryUpdateListener getEntryUpdateListener() {
		return entryUpdateListener;
	}

	public void addEntryUpdateListener (EntryUpdateListener l) {
		if (l==entryUpdateListener) return;
		entryUpdateListenersList.add(l);
	}

	public void removeEntryUpdateListener (EntryUpdateListener l) {
		entryUpdateListenersList.remove(l);
	}

	protected void callEntryUpdateListener (String key, String value) {
		for (EntryUpdateListener l:entryUpdateListenersList) {
			l.updated(key, value);
		}
	}
}
