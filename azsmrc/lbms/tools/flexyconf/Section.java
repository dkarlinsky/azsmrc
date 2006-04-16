package lbms.tools.flexyconf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

public class Section {
	private Section parent;
	private List<Section> children;
	private Map<String, Entry> entries = new HashMap<String, Entry>();
	private String name;
	private FCInterface fci;
	private DisplayAdapterSection displayAdapter;

	public Section () {

	}

	public Section (Element e) {

	}

	public Section (Element e, Section parent) {
		this.parent = parent;
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

	public void setFCInterface (FCInterface fci) {
		this.fci = fci;
	}

	protected void checkDependency(String key,boolean enabled) {
		Set<String> keys = entries.keySet();
		for (String k:keys) {
			entries.get(k).checkDependency(key, enabled);
		}
	}
}
