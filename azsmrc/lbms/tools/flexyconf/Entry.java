package lbms.tools.flexyconf;

import org.jdom.Element;

public class Entry {
	public static final int TYPE_STRING 	= 1;
	public static final int TYPE_INT		= 2;
	public static final int TYPE_BOOLEAN	= 3;
	public static final int TYPE_FLOAT		= 4;
	public static final int TYPE_DOUBLE		= 5;
	public static final int TYPE_LONG		= 6;


	private String label;
	String key;
	private String dependsOn;
	private int type;
	private String rule;
	private Validator validator;

	private String	value;
	private DisplayAdapterEntry displayAdapter;
	private Section section;
	private FCInterface fci;

	public Entry () {

	}

	public Entry (Section s) {

	}

	public Element toElement() {
		return null;
	}

	public void readFromElement(Element e) {

	}

	public void checkDependency (String key,boolean enabled) {
		if (dependsOn == null || dependsOn.equals("")) return;
		else if(dependsOn.equalsIgnoreCase(key)) {
			if(displayAdapter!=null)
				displayAdapter.setEnabled(enabled);
		}
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

	public void setValue(String v) {
		if (validator!=null)
			if (!validator.validate(v)) {
				setValueQuiet(fci.getContentProvider().getDefaultValue(getKey(), getType()));
				return;
			}
		if (fci!=null)
			fci.callEntryUpdateListener(getKey(), getValue());
		setValueQuiet(v);
	}

	protected void setValueQuiet(String v) {
		value = v;
		if(displayAdapter!=null)
			displayAdapter.updateValue();
		if (type==TYPE_BOOLEAN && section!=null)
			section.checkDependency(getKey(), Boolean.parseBoolean(getValue()));
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

	/**
	 * @return the section
	 */
	public Section getSection() {
		return section;
	}

	/**
	 * @return the fci
	 */
	public FCInterface getFCInterface() {
		return fci;
	}

	/**
	 * @param fci the FCInterface to set
	 */
	public void setFCInterface(FCInterface fci) {
		this.fci = fci;
	}


}
