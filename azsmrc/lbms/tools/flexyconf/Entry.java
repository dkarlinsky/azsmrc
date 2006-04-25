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
	private String key;
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

	public Entry (Element e, FCInterface fci) {
		this.fci = fci;
		readFromElement(e);
		System.out.println("Entry "+label+" created");
	}

	public Element toElement() {
		Element e = new Element ("Entry");
		e.setAttribute("key", key);
		e.setAttribute("label", label);
		e.setAttribute("type", type2String(type));
		if (dependsOn!=null)
			e.setAttribute("dependsOn", dependsOn);
		if (rule!=null)
			e.setAttribute("validate", rule);
		return e;
	}

	public void readFromElement(Element e) {
		key = e.getAttributeValue("key");
		label = e.getAttributeValue("label");
		type = string2Type(e.getAttributeValue("type"));
		dependsOn = e.getAttributeValue("dependsOn");
		rule = e.getAttributeValue("validate");
		if (rule!=null) {
			try {
				validator = Validator.getValidator(rule, type);
			} catch (InvalidRuleException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidTypeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public int string2Type (String s) {
		if (s.equalsIgnoreCase("string")) {
			return TYPE_STRING;
		} else if (s.equalsIgnoreCase("int")) {
			return TYPE_INT;
		} else if (s.equalsIgnoreCase("boolean")) {
			return TYPE_BOOLEAN;
		} else if (s.equalsIgnoreCase("float")) {
			return TYPE_FLOAT;
		} else if (s.equalsIgnoreCase("double")) {
			return TYPE_DOUBLE;
		} else if (s.equalsIgnoreCase("long")) {
			return TYPE_LONG;
		}
		return 0;
	}

	public String type2String (int i) {
		switch (i) {
		case TYPE_STRING:
			return "string";
		case TYPE_INT:
			return "int";
		case TYPE_BOOLEAN:
			return "boolean";
		case TYPE_FLOAT:
			return "float";
		case TYPE_DOUBLE:
			return "double";
		case TYPE_LONG:
			return "long";
		}
		return "";
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
