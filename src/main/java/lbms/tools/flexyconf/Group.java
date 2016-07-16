package lbms.tools.flexyconf;

import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class Group extends AbstractEntryContainer implements ConfigEntity {
	private FCInterface fci;
	private Section section;
	private String label;
	private int index;

	public Group (Section parent, String label) {
		this.section = parent;
		this.label = label;
		this.fci = parent.getFCInterface();
		this.index = parent.getEntries().length;
		parent.addGroup(this);
	}

	protected Group (Element e, Section parent, FCInterface fci) {
		this.fci = fci;
		this.section = parent;
		label = e.getAttributeValue("label");
		String indexString = e.getAttributeValue("index");
		if (indexString!=null)index = Integer.parseInt(indexString);
		else index = 0;
		List<Element> elems = e.getChildren("Entry");
		for (Element elem:elems) {
			Entry en = new Entry(elem,section,fci);
			entries.put(en.getKey(), en);
		}
	}

	public Element toElement() {
		Element s = new Element ("Group");
		s.setAttribute("label", label);
		s.setAttribute("index", Integer.toString(index));
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

	public Section getParent () {
		return section;
	}


}
