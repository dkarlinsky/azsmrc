package lbms.tools.flexyconf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class Section {
	Section parent;
	List<Section> children;
	Map<String, Entry> entries = new HashMap<String, Entry>();
	String name;

	public Section () {

	}

	public Section (Element e) {

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
}
