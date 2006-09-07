package lbms.tools.flexyconf;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdom.Element;

public class Entry implements ConfigEntity {

	public static final int TYPE_STRING 	= 1;
	public static final int TYPE_INT		= 2;
	public static final int TYPE_BOOLEAN	= 3;
	public static final int TYPE_FLOAT		= 4;
	public static final int TYPE_DOUBLE		= 5;
	public static final int TYPE_LONG		= 6;

	public static final int TYPE_LABEL 		= -1; //special Entry that only displays the label
	public static final int TYPE_URL 		= -2; //special Entry that displays an URL


	private String label;
	private String key;
	private String dependsOn;
	private int type;
	private String rule;
	private Validator validator;
	private int index;

	private String	value;
	private DisplayAdapterEntry displayAdapter;
	private Section section;
	private FCInterface fci;

	private SortedSet<Option> options = new TreeSet<Option>();
	private boolean option, negativeDepend;

	public Entry (String key, String label, int type, Section parent) {
		this.key = key;
		this.type = type;
		this.section = parent;
		this.fci = parent.getFCInterface();
		parent.addEntry(this);
	}

	public Entry (String key, String label, int type, Group group) {
		this.key = key;
		this.type = type;
		this.section = group.getParent();
		this.fci = section.getFCInterface();
		section.addEntry(this);
		group.addEntry(this);
	}

	protected Entry (Element e, Section parent, FCInterface fci) {
		this.fci = fci;
		this.section = parent;
		readFromElement(e);
		System.out.println("Entry "+label+" created");
	}

	public Element toElement() {
		Element e = new Element ("Entry");
		e.setAttribute("index", Integer.toString(index));
		e.setAttribute("key", key);
		e.setAttribute("type", type2String(type));
		e.setAttribute("label", label);
		if (dependsOn!=null) {
			if (negativeDepend)
				e.setAttribute("dependsOn", '^'+dependsOn);
			else
				e.setAttribute("dependsOn", dependsOn);
		}
		if (rule!=null)
			e.setAttribute("validate", rule);
		return e;
	}

	public void readFromElement(Element e) {
		key = e.getAttributeValue("key");
		label = e.getAttributeValue("label");
		type = string2Type(e.getAttributeValue("type"));
		String indexString = e.getAttributeValue("index");
		if (indexString!=null)index = Integer.parseInt(indexString);
		else index = 0;
		dependsOn = e.getAttributeValue("dependsOn");
		if (dependsOn!=null && dependsOn.indexOf('^') ==0) {
			negativeDepend = true;
			dependsOn = dependsOn.substring(1);
		}

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
		List<Element> opts = e.getChildren("Option");
		if (opts.size()>0) {
			option = true;
			for (Element opt:opts)
				options.add(new Option(opt,fci));
		}
	}

	/**
	 * @param s
	 * @return
	 */
	public int string2Type (String s) {
		if (s.equalsIgnoreCase("string")) {
			return TYPE_STRING;
		} else if (s.equalsIgnoreCase("integer")) {
			return TYPE_INT;
		} else if (s.equalsIgnoreCase("boolean")) {
			return TYPE_BOOLEAN;
		} else if (s.equalsIgnoreCase("float")) {
			return TYPE_FLOAT;
		} else if (s.equalsIgnoreCase("double")) {
			return TYPE_DOUBLE;
		} else if (s.equalsIgnoreCase("long")) {
			return TYPE_LONG;
		} else if (s.equalsIgnoreCase("label")) {
			return TYPE_LABEL;
		} else if (s.equalsIgnoreCase("url")) {
			return TYPE_URL;
		}
		return 0;
	}

	/**
	 * @param i
	 * @return
	 */
	public String type2String (int i) {
		switch (i) {
		case TYPE_STRING:
			return "string";
		case TYPE_INT:
			return "integer";
		case TYPE_BOOLEAN:
			return "boolean";
		case TYPE_FLOAT:
			return "float";
		case TYPE_DOUBLE:
			return "double";
		case TYPE_LONG:
			return "long";
		case TYPE_LABEL:
			return "label";
		case TYPE_URL:
			return "url";
		}
		return "";
	}

	public void checkDependency (String key,boolean enabled) {
		if (dependsOn == null || dependsOn.equals("")) return;
		else if(dependsOn.equalsIgnoreCase(key)) {
			if(displayAdapter!=null)
				displayAdapter.setEnabled(negativeDepend ? !enabled : enabled);
		}
	}

	protected void triggerDependencyCheck() {
		if (getType()==TYPE_BOOLEAN)
			section.checkDependency(getKey(), Boolean.parseBoolean(getValue()));
	}

	public int compareTo(Entry o) {
		return index-o.index;
	}

	public int compareTo(ConfigEntity o) {
		return getIndex()-o.getIndex();
	}

	public int getIndex() {
		return index;
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
		return fci.getI18NProvider().translate(label);
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
		if (getType() == TYPE_LABEL || getType() == TYPE_URL) return "";
		if (value == null) {
			value = fci.getContentProvider().getValue(getKey(), getType());
		}
		return value;
	}

	public void setValue(String v) {
		if (value != null && value.equals(v)) return;
		if (validator!=null && !validator.validate(v)) {
			setValueQuiet(fci.getContentProvider().getDefaultValue(getKey(), getType()));
			return;
		}
		fci.getContentProvider().setValue(getKey(), v, getType());
		fci.callEntryUpdateListener(getKey(), v);
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

	public void init() {
		if (getType() == TYPE_LABEL) return;
		getValue();
		if (displayAdapter != null) {
			displayAdapter.updateValue();
		}
		triggerDependencyCheck();
	}

	/**
	 * @return the option
	 */
	public boolean isOption() {
		return option;
	}

	/**
	 * @return the options
	 */
	public Option[] getOptions() {
		return options.toArray(Option.EMPTY_OPT_ARRAY);
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param option the option to set
	 */
	public void setOption(boolean option) {
		this.option = option;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(SortedSet<Option> options) {
		this.options = options;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(String rule) throws InvalidRuleException, InvalidTypeException {
		this.rule = rule;
		validator = Validator.getValidator(rule, type);
	}

	/**
	 * @param section the section to set
	 */
	protected void setSection(Section section) {
		this.section = section;
	}

	/**
	 * @param dependsOn the dependsOn to set
	 */
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
		if (dependsOn!=null && dependsOn.indexOf('^') ==0) {
			negativeDepend = true;
			this.dependsOn = dependsOn.substring(1);
		}
	}


}
