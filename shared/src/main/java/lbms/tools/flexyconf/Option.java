package lbms.tools.flexyconf;

import org.jdom.Element;

public class Option implements Comparable<Option>{

	public static final Option[] EMPTY_OPT_ARRAY = new Option[0];

	private String label;
	private String value;
	private FCInterface fci;
	private int index;

	public Option () {

	}

	public Option (Element e, FCInterface fci) {
		this.fci = fci;
		this.label = e.getAttributeValue("label");
		this.value = e.getAttributeValue("value");
		String indexString = e.getAttributeValue("index");
		if (indexString!=null)index = Integer.parseInt(indexString);
		else index = 0;
	}

	public Element toElement() {
		Element e = new Element ("Option");
		e.setAttribute("label", label);
		e.setAttribute("value", value);
		e.setAttribute("index", Integer.toString(index));
		return e;
	}

	public int compareTo(Option o) {
		return index-o.index;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return fci.getI18NProvider().translate(label);
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
