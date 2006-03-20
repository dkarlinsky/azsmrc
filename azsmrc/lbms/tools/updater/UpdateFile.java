package lbms.tools.updater;

import org.jdom.DataConversionException;
import org.jdom.Element;

public class UpdateFile {

	/**
	 * Indicates that the URL is a normal URL and will just
	 * try to download.
	 */
	public final static int TYPE_NORMAL = 1;
	/**
	 * This indicates that the URL is hosted on Sf.net downloadsite
	 * it will try to identify mirrors and will use, every mirror if
	 * needed.
	 */
	public final static int TYPE_SF_NET = 2;

	private String name;
	private String path;
	private Version version;
	private String url;
	private String hash;
	private int type;
	private long size;
	private boolean extract;

	public UpdateFile() {

	}

	public UpdateFile (Element file) {
		try {
			name = file.getAttributeValue("name");
			if (file.getAttribute("path") == null) {
				path = "";
			} else {
				path = file.getAttributeValue("path");			}

			version = new Version(file.getAttributeValue("version"));
			if (file.getAttribute("url") == null) {
				url = "";
			} else {
				url = file.getAttributeValue("url");
			}
			if (file.getAttribute("extract") == null) {
				extract = false;
			} else {
				extract = Boolean.parseBoolean(file.getAttributeValue("extract"));
			}
			hash = file.getAttributeValue("hash");
			type = file.getAttribute("type").getIntValue();
			size = file.getAttribute("size").getLongValue();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}

	Element toElement () {
		Element file = new Element("File");
		file.setAttribute("name", name);
		if (path!="")
			file.setAttribute("path",path);
		file.setAttribute("version", version.toString());
		if (url!="")
			file.setAttribute("url", url);
		file.setAttribute("hash", hash);
		if (extract)
			file.setAttribute("extract", Boolean.toString(extract));
		file.setAttribute("type",Integer.toString(type));
		file.setAttribute("size", Long.toString(size));
		return file;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return Returns the version.
	 */
	public Version getVersion() {
		return version;
	}
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return Returns the hash.
	 */
	public String getHash() {
		return hash;
	}
	/**
	 * @param hash The hash to set.
	 */
	protected void setHash(String hash) {
		this.hash = hash;
	}
	/**
	 * @param name The name to set.
	 */
	protected void setName(String name) {
		this.name = name;
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

	/**
	 * @return Returns the size.
	 */
	public long getSize() {
		return size;
	}
	/**
	 * @param size The size to set.
	 */
	protected void setSize(long size) {
		this.size = size;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	protected void setType(int type) {
		this.type = type;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path The path to set.
	 */
	protected void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return Returns the extract.
	 */
	public boolean isExtract() {
		return extract;
	}

	/**
	 * @param extract The extract to set.
	 */
	protected void setExtract(boolean extract) {
		this.extract = extract;
	}




}
