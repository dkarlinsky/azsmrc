package lbms.tools.flexyconf;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class FlexyConfiguration {
	private Section rootSection;
	private FCInterface fci;

	public FlexyConfiguration () {

	}

	public FlexyConfiguration (Document doc) {
		Element root = doc.getRootElement();
		fci = new FCInterface (this);
		rootSection = new Section (root.getChild("Section"), fci);
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
	/**
	 * @return the rootSection
	 */
	public Section getRootSection() {
		return rootSection;
	}

	public void saveToFile (File f) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			new XMLOutputter (Format.getPrettyFormat()).output(toDocument(), os);
		} finally {
			if (os!=null)os.close();
		}
	}

	public static FlexyConfiguration readFromFile(File f) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build(is);
			return new FlexyConfiguration(xmlDom);
			//SWTMenu = new SWTMenu(fc,)
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (is!=null)is.close();
		}
	}
}
