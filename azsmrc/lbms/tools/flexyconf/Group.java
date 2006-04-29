package lbms.tools.flexyconf;

import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class Group extends AbstractEntryContainer implements ConfigEntity {
	private FCInterface fci;
	private Section section;
	private String label;
	private int index;

	public Group (Element e, Section parent, FCInterface fci) {
		this.fci = fci;
		this.section = parent;
		label = e.getAttributeValue("label");
		List<Element> elems = e.getChildren("Entry");
		for (Element elem:elems) {
			Entry en = new Entry(elem,section,fci);
			entries.put(en.getKey(), en);
		}
	}

	public Element toElement() {
		Element s = new Element ("Group");
		s.setAttribute("label", label);
		Set<String> keys = entries.keySet();
		for (String k:keys) {
			s.addContent(entries.get(k).toElement());
		}
		return s;
	}

	public int compareTo(ConfigEntity o) {
		return getIndex()-o.getIndex();
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return fci.getI18NProvider().translate(label);
	}


}
