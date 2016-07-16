package lbms.azsmrc.remote.client.history;

import org.jdom.Element;

/**
 * @author Damokles
 * 
 */
public class DownloadHistoryEntry implements Comparable<DownloadHistoryEntry> {
	private long	timestamp;
	private String	dlName;
	private String	category;

	public DownloadHistoryEntry(Element e) {
		this.category = e.getAttributeValue("category");
		this.dlName = e.getAttributeValue("name");
		this.timestamp = Long.parseLong(e.getAttributeValue("time"));
	}

	public DownloadHistoryEntry(long timestamp, String dlName, String category) {
		super();
		this.timestamp = timestamp;
		this.dlName = dlName;
		this.category = category == null ? "" : category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DownloadHistoryEntry o) {
		int result = 0;
		long t = timestamp - o.timestamp;
		if (t > 0) {
			result = 1;
		} else if (t < 0) {
			result = -1;
		}
		return result;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the dlName
	 */
	public String getDlName() {
		return dlName;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
}
