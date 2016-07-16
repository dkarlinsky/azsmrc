package lbms.tools.updater;

import java.util.ArrayList;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Element;

public class UpdateFile {


	public final static String HASH_ALGORITHM = "SHA-1";

	/**
	 * Indicates that this file will not be directly downloaded
	 * used for Files in Archives
	 */
	public final static int TYPE_DND = 0;
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
	private boolean isArchive;
	private boolean isInArchive;
	private List<UpdateFile> archivFiles = new ArrayList<UpdateFile>();

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
			if (file.getAttribute("isArchive") == null) {
				isArchive = false;
			} else {
				isArchive = Boolean.parseBoolean(file.getAttributeValue("isArchive"));
			}
			if (file.getAttribute("isInArchive") == null) {
				isInArchive = false;
			} else {
				isInArchive = Boolean.parseBoolean(file.getAttributeValue("isInArchive"));
			}
			hash = file.getAttributeValue("hash");
			type = file.getAttribute("type").getIntValue();
			size = file.getAttribute("size").getLongValue();
			List<Element> files = file.getChildren("File");
			for (Element f:files)
				archivFiles.add(new UpdateFile(f));
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
		if (isArchive)
			file.setAttribute("isArchive", Boolean.toString(isArchive));
		if (isInArchive)
			file.setAttribute("isInArchive", Boolean.toString(isInArchive));
		file.setAttribute("type",Integer.toString(type));
		file.setAttribute("size", Long.toString(size));
		for (UpdateFile f:archivFiles) {
			file.addContent(f.toElement());
		}
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
	public boolean isArchive() {
		return isArchive;
	}

	/**
	 * @param extract The extract to set.
	 */
	protected void setArchive(boolean isArchiv) {
		this.isArchive = isArchiv;
	}

	/**
	 * @return Returns the isInArchive.
	 */
	public boolean isInArchive() {
		return isInArchive;
	}

	/**
	 * @param isInArchive The isInArchive to set.
	 */
	protected void setInArchive(boolean isInArchiv) {
		this.isInArchive = isInArchiv;
	}

	/**
	 * @return Returns the archivFiles.
	 */
	public List<UpdateFile> getArchivFiles() {
		return archivFiles;
	}

	/**
	 * @param archivFiles The archivFiles to set.
	 */
	protected void setArchivFiles(List<UpdateFile> archivFiles) {
		this.archivFiles = archivFiles;
	}

	protected void addArchivFile (UpdateFile f) {
		this.archivFiles.add(f);
	}

	protected void removeArchivFile (UpdateFile f) {
		this.archivFiles.remove(f);
	}

	protected void clearArcive() {
		this.archivFiles.clear();
	}
}
