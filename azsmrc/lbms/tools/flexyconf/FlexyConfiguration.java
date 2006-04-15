package lbms.tools.flexyconf;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

public class FlexyConfiguration {
	Section rootSection;
	List<EntryUpdateListener> entryUpdateListenersList = new ArrayList<EntryUpdateListener>();
	EntryUpdateListener entryUpdateListener = new EntryUpdateListener() {
		public void updated(String key, String value) {};
	};

	public FlexyConfiguration () {

	}

	public FlexyConfiguration (Document doc) {
		Element root = doc.getRootElement();
		rootSection = new Section (root.getChild("Section"));
	}

	public Document toDocument () {
		Document doc = new Document();
		Element root = new Element ("FlexyConfiguration");
		root.addContent(rootSection.toElement());
		return doc;
	}

	public Entry getEntry (String key) {
		return rootSection.getEntry(key);
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
