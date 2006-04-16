package lbms.tools.flexyconf;

import org.jdom.Element;

public class Entry {
	public static final int TYPE_STRING 	= 1;
	public static final int TYPE_INT		= 2;
	public static final int TYPE_BOOLEAN	= 3;
	public static final int TYPE_FLOAT		= 4;
	public static final int TYPE_DOUBLE		= 5;
	public static final int TYPE_LONG		= 6;


	String label;
	String key;
	String dependsOn;
	int type;
	String rule;
	Validator validator;

	String	value;
	DisplayAdapterEntry displayAdapter;
	Section section;

	public Element toElement() {
		return null;
	}

	public void readFromElement(Element e) {

	}

	public boolean checkDependency () {
		if (dependsOn == null || dependsOn.equals("")) return true;
		else return false;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return Returns the lable.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Returns the rule.
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Returns the validator.
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the displayAdapter
	 */
	public DisplayAdapterEntry getDisplayAdapter() {
		return displayAdapter;
	}

	/**
	 * @param displayAdapter the displayAdapter to set
	 */
	public void setDisplayAdapter(DisplayAdapterEntry displayAdapter) {
		this.displayAdapter = displayAdapter;
	}
}
