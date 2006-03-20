package lbms.tools.updater;

import java.util.ArrayList;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;

public class Update implements Comparable<Update>{

	public static final int LV_SEC_RISK	= 100;
	public static final int LV_BUGFIX	= 50;
	public static final int LV_CHANGE	= 30;
	public static final int LV_FEATURE	= 20;
	public static final int LV_LOW		= 1;

	public static final int TYPE_BETA			= 1;
	public static final int TYPE_STABLE			= 2;
	public static final int TYPE_MAINTENANCE	= 3;

	private List<UpdateFile> fileList = new ArrayList<UpdateFile>();
	private Version version;
	private String url;
	private int importance_level;
	private int type;
	private Changelog changeLog;

	public Update() {

	}

	public Update (Document update) {
		Element root = update.getRootElement();
		readFromElement (root.getChild("Update"));
	}

	public Update (Element update) {
		readFromElement(update);
	}

	public void readFromElement(Element update) {
		try {
			version 			= new Version (update.getAttributeValue("version"));
			url 				= update.getAttributeValue("url");
			type 				= update.getAttribute("type").getIntValue();
			importance_level 	= update.getAttribute("importance").getIntValue();
			changeLog 			= new Changelog (update.getChild("Changelog"));
			readFiles(update.getChild("Files"));
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}

	public Document toDocument() {
		Document doc = new Document();
		Element root = new Element ("Updates");
		root.addContent(toElement());
		doc.addContent(root);
		return doc;
	}

	public Element toElement() {
		Element update = new Element ("Update");
		update.setAttribute("url", url);
		update.setAttribute("version", version.toString());
		update.setAttribute("type", Integer.toString(type));
		update.setAttribute("importance", Integer.toString(importance_level));
		update.addContent(changeLog.toElement());
		Element files = new Element ("Files");
		for (UpdateFile file:fileList) {
			files.addContent(file.toElement());
		}
		update.addContent(files);
		return update;
	}

	private void readFiles (Element filesElement) {
		List<Element> files = filesElement.getChildren("File");
		for (Element file:files) {
			fileList.add(new UpdateFile(file));
		}
	}

	public boolean isComplete() {
		if (changeLog == null
				|| importance_level == 0
				|| version == null
				|| url == null
				|| fileList.size() == 0) return false;
		else return true;
	}

	public boolean isStable() {
		return (type >= TYPE_STABLE);
	}

	public boolean isBeta() {
		return (type == TYPE_BETA);
	}

	public int compareTo(Update o) {
		return version.compareTo(o.version);
	}

	public void addFile (UpdateFile f) {
		if (fileList.contains(f)) return;
		fileList.add(f);
	}

	public void removeFile (UpdateFile f) {
		fileList.remove(f);
	}

	/**
	 * @return Returns the changeLog.
	 */
	public Changelog getChangeLog() {
		return changeLog;
	}

	/**
	 * @return Returns the fileList.
	 */
	public List<UpdateFile> getFileList() {
		return fileList;
	}

	/**
	 * @return Returns the importance_level.
	 */
	public int getImportance_level() {
		return importance_level;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return Returns the version.
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * @param changeLog The changeLog to set.
	 */
	protected void setChangeLog(Changelog changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * @param fileList The fileList to set.
	 */
	protected void setFileList(List<UpdateFile> fileList) {
		this.fileList = fileList;
	}

	/**
	 * @param importance_level The importance_level to set.
	 */
	protected void setImportance_level(int importance_level) {
		this.importance_level = importance_level;
	}

	/**
	 * @param type The type to set.
	 */
	protected void setType(int type) {
		this.type = type;
	}

	/**
	 * @param url The url to set.
	 */
	protected void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param version The version to set.
	 */
	protected void setVersion(Version version) {
		this.version = version;
	}
}
