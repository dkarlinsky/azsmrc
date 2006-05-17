package lbms.tools.flexyconf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Element;

public class Section extends AbstractEntryContainer implements Comparable<Section>{
	private Set<Section> children = new TreeSet<Section>();
	private List<Group> groups = new ArrayList<Group>();
	private String label;
	private FCInterface fci;
	private DisplayAdapterSection displayAdapter;
	private int index;

	public Section () {

	}

	public Section (Element e, FCInterface fci) {
		this.fci = fci;
		label = e.getAttributeValue("label");
		String indexString = e.getAttributeValue("index");
		if (indexString!=null)index = Integer.parseInt(indexString);
		else index = 0;
		List<Element> elems = e.getChildren("Section");
		for (Element elem:elems) {
			children.add(new Section(elem,fci));
		}
		elems = e.getChildren("Group");
		for (Element elem:elems) {
			groups.add(new Group(elem,this,fci));
		}
		elems = e.getChildren("Entry");
		for (Element elem:elems) {
			Entry en = new Entry(elem,this,fci);
			entries.put(en.getKey(), en);
		}
	}

	public Element toElement() {
		Element s = new Element ("Secetion");
		s.setAttribute("label", label);
		s.setAttribute("index", Integer.toString(index));
		Set<String> keys = entries.keySet();
		for (String k:keys) {
			s.addContent(entries.get(k).toElement());
		}
		for (Section child:children) {
			s.addContent(child.toElement());
		}
		for (Group child:groups) {
			s.addContent(child.toElement());
		}
		return s;
	}

	public FCInterface getFCInterface() {
		return fci;
	}

	@Override
	public Entry getEntry (String key) {
		if (entries.containsKey(key))
			return super.getEntry(key);
		else {
			for (Section child:children) {
				Entry e = child.getEntry(key);
				if (e!=null) return e;
			}
			for (Group g:groups) {
				Entry e = g.getEntry(key);
				if (e!=null) return e;
			}
			return null;
		}
	}

	/**
	 * @return the configEntities
	 */
	public ConfigEntity[] getConfigEntities() {
		Set<String> keys = entries.keySet();
		ConfigEntity[] eArray = new ConfigEntity[keys.size()+groups.size()];
		int i = 0;
		for (String key:keys) {
			eArray[i++] = entries.get(key);
		}
		for (Group g:groups) {
			eArray[i++] = g;
		}
		return eArray;
	}

	/**
	 * @return the configEntities sorted by Index
	 */
	public ConfigEntity[] getSortedConfigEntities() {
		ConfigEntity[] e = getConfigEntities();
		Arrays.sort(e);
		return e;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return fci.getI18NProvider().translate(label);
	}

	/**
	 * @return the children
	 */
	public Set<Section> getChildren() {
		return children;
	}

	public void setFCInterface (FCInterface fci) {
		this.fci = fci;
	}

	@Override
	protected void checkDependency(String key, boolean enabled) {
		super.checkDependency(key, enabled);
		for (Group g:groups) {
			g.checkDependency(key, enabled);
		}
	}

	@Override
	public void init() {
		super.init();
		for (Group g:groups) {
			g.init();
		}
	}

	public void initAll() {
		init();
		for (Section child:children) {
			child.initAll();
		}
	}

	public int compareTo(Section o) {
		return index-o.index;
	}
}
