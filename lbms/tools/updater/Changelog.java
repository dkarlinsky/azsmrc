package lbms.tools.updater;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

public class Changelog {
	private List<String> bugFixes = new ArrayList<String>();
	private List<String> features = new ArrayList<String>();
	private List<String> changes = new ArrayList<String>();

	public Changelog() {

	}

	public Changelog (Element clog) {
		List<Element> tmp = clog.getChildren("BugFix");
		for (Element x:tmp) {
			bugFixes.add(x.getTextTrim());
		}
		tmp = clog.getChildren("Feature");
		for (Element x:tmp) {
			features.add(x.getTextTrim());
		}
		tmp = clog.getChildren("Change");
		for (Element x:tmp) {
			changes.add(x.getTextTrim());
		}
	}

	public Element toElement () {
		Element clog = new Element("Changelog");
		for (String x:bugFixes) {
			Element e = new Element ("BugFix");
			e.setText(x);
			clog.addContent(e);
		}
		for (String x:features) {
			Element e = new Element ("Feature");
			e.setText(x);
			clog.addContent(e);
		}
		for (String x:changes) {
			Element e = new Element ("Change");
			e.setText(x);
			clog.addContent(e);
		}
		return clog;
	}

	protected void addBugFix (String bfix) {
		bugFixes.add(bfix);
	}

	protected void addChange (String change) {
		changes.add(change);
	}

	protected void addFeature (String feature) {
		features.add(feature);
	}

	protected void removeBugFix (String bfix) {
		bugFixes.remove(bfix);
	}

	protected void removeChange (String change) {
		changes.remove(change);
	}

	protected void removeFeature (String feature) {
		features.remove(feature);
	}

	/**
	 * @return Returns the bugFixes.
	 */
	public List<String> getBugFixes() {
		return bugFixes;
	}

	/**
	 * @return Returns the changes.
	 */
	public List<String> getChanges() {
		return changes;
	}

	/**
	 * @return Returns the features.
	 */
	public List<String> getFeatures() {
		return features;
	}


}
