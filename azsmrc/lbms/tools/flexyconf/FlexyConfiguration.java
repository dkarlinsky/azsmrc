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
		rootSection = new Section ("RootSection");
		rootSection.setFCInterface(getFCInterface());
	}

	public FlexyConfiguration (Document doc) {
		Element root = doc.getRootElement();
		fci = new FCInterface (this);
		rootSection = new Section (root.getChild("Section"), fci);
	}

	public FlexyConfiguration (Document doc, String domain) {
		Element root = doc.getRootElement();
		fci = new FCInterface (this,domain);
		rootSection = new Section (root.getChild("Section"), fci);
	}

	public FlexyConfiguration (Element root) {
		fci = new FCInterface (this);
		rootSection = new Section (root.getChild("Section"), fci);
	}

	public FlexyConfiguration (Element root, String domain) {
		fci = new FCInterface (this,domain);
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

	/**
	 * Writes the FlexyConf to a File.
	 * 
	 * @param f File to write to
	 * @throws IOException
	 */
	public void saveToFile (File f) throws IOException {
		saveToStream(new FileOutputStream(f));
	}

	/**
	 * Writes the FlexyConf to the Stream,
	 * 
	 * The Stream is closed after completition.
	 * 
	 * @param os Stream to write to
	 * @throws IOException
	 */
	public void saveToStream (OutputStream os) throws IOException {
		try {
			new XMLOutputter (Format.getPrettyFormat()).output(toDocument(), os);
		} finally {
			if (os!=null)os.close();
		}
	}

	/**
	 * Reads the FlexyConf from a File.
	 * 
	 * @param f File to read from
	 * @return FlexyConf instance
	 * @throws IOException
	 */
	public static FlexyConfiguration readFromFile(File f) throws IOException {
			return readFromStream( new FileInputStream(f) );
	}

	/**
	 * Reads the FlexyConf from a File.
	 * 
	 * @param f File to read from
	 * @param domain Configuration domain
	 * @return FlexyConf instance
	 * @throws IOException
	 */
	public static FlexyConfiguration readFromFile(File f, String domain) throws IOException {
		return readFromStream( new FileInputStream(f), domain );
}

	/**
	 * Reads the FlexyConf from the Stream.
	 * 
	 * The Stream is closed after completition.
	 * 
	 * @param is Stream to read from 
	 * @param domain Configuration domain
	 * @return FlexyConf instance
	 * @throws IOException
	 */
	public static FlexyConfiguration readFromStream(InputStream is, String domain) throws IOException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build(is);
			return new FlexyConfiguration(xmlDom, domain);
			//SWTMenu = new SWTMenu(fc,)
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (is!=null)is.close();
		}
	}

	public static FlexyConfiguration readFromStream(InputStream is) throws IOException {
		return readFromStream(is,"");
	}
}
