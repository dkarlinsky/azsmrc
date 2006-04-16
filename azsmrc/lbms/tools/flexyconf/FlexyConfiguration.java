package lbms.tools.flexyconf;



import org.jdom.Document;
import org.jdom.Element;

public class FlexyConfiguration {
	private Section rootSection;
	private FCInterface fci;

	public FlexyConfiguration () {

	}

	public FlexyConfiguration (Document doc) {
		Element root = doc.getRootElement();
		rootSection = new Section (root.getChild("Section"));
		fci = new FCInterface (this);
	}

	public Document toDocument () {
		Document doc = new Document();
		Element root = new Element ("FlexyConfiguration");
		root.addContent(rootSection.toElement());
		return doc;
	}

	protected Section getRoot() {
		return rootSection;
	}

	public FCInterface getFCInterface() {
		return fci;
	}
}
