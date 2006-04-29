package lbms.tools.flexyconf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbms.tools.flexyconf.swt.SWTEntry;

import org.jdom.Element;

public class Section {
	private List<Section> children = new ArrayList<Section>();
	private Map<String, Entry> entries = new HashMap<String, Entry>();
	private String label;
	private FCInterface fci;
	private DisplayAdapterSection displayAdapter;

	public Section () {

	}

	public Section (Element e, FCInterface fci) {
		this.fci = fci;
		label = e.getAttributeValue("label");
		List<Element> elems = e.getChildren("Section");
		for (Element elem:elems) {
			children.add(new Section(elem,fci));
		}
		elems = e.getChildren("Entry");
		for (Element elem:elems) {
			Entry en = new Entry(elem,this,fci);
			entries.put(en.getKey(), en);
		}
	}

	public Element toElement() {
		return null;
	}

	public Entry getEntry (String key) {
		if (entries.containsKey(key))
			return entries.get(key);
		else {
			for (Section child:children) {
				Entry e = child.getEntry(key);
				if (e!=null) return e;
			}
			return null;
		}
	}

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

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the children
	 */
	public List<Section> getChildren() {
		return children;
	}

	public void setFCInterface (FCInterface fci) {
		this.fci = fci;
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
}
