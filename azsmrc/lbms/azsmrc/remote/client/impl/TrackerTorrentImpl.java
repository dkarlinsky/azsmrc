/**
 * 
 */
package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.TrackerTorrent;

/**
 * @author Damokles
 *
 */
public class TrackerTorrentImpl implements TrackerTorrent {

	private Client client;
	private String hash;
	private String name;
	private long announceCount, avgAnnounceCount;
	private long avgBytesIn, avgBytesOut;
	private long avgDownloaded, avgUploaded;
	private long avgScrapeCount;
	private long completedCount, totalLeft;
	private long dateAdded, scrapeCount;
	private long totalBytesOut, totalBytesIn;
	private long totalDownloaded, totalUploaded;
	private int seedCount, leecherCount;
	private int status, badNATCount;
	private boolean canBeRemoved, isPassive;

	public TrackerTorrentImpl (Client client) {
		this.client = client;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return canBeRemoved;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#disableReplyCaching()
	 */
	public void disableReplyCaching() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAnnounceCount()
	 */
	public long getAnnounceCount() {
		return announceCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageAnnounceCount()
	 */
	public long getAverageAnnounceCount() {
		return avgAnnounceCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageBytesIn()
	 */
	public long getAverageBytesIn() {
		return avgBytesIn;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageBytesOut()
	 */
	public long getAverageBytesOut() {
		return avgBytesOut;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageDownloaded()
	 */
	public long getAverageDownloaded() {
		return avgDownloaded;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageScrapeCount()
	 */
	public long getAverageScrapeCount() {
		return avgScrapeCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getAverageUploaded()
	 */
	public long getAverageUploaded() {
		return avgUploaded;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getBadNATCount()
	 */
	public int getBadNATCount() {
		return badNATCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getCompletedCount()
	 */
	public long getCompletedCount() {
		return completedCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getDateAdded()
	 */
	public long getDateAdded() {
		return dateAdded;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getLeecherCount()
	 */
	public int getLeecherCount() {
		return leecherCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getScrapeCount()
	 */
	public long getScrapeCount() {
		return scrapeCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getSeedCount()
	 */
	public int getSeedCount() {
		return seedCount;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getStatus()
	 */
	public int getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getTotalBytesIn()
	 */
	public long getTotalBytesIn() {
		return totalBytesIn;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getTotalBytesOut()
	 */
	public long getTotalBytesOut() {
		return totalBytesOut;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getTotalDownloaded()
	 */
	public long getTotalDownloaded() {
		return totalDownloaded;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getTotalLeft()
	 */
	public long getTotalLeft() {
		// TODO Auto-generated method stub
		return totalLeft;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getTotalUploaded()
	 */
	public long getTotalUploaded() {
		// TODO Auto-generated method stub
		return totalUploaded;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#isPassive()
	 */
	public boolean isPassive() {
		return isPassive;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#remove()
	 */
	public void remove() {
		client.sendTrackerTorrentRemove(this);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#start()
	 */
	public void start() {
		client.sendTrackerTorrentStart(this);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#stop()
	 */
	public void stop() {
		client.sendTrackerTorrentStop(this);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.TrackerTorrent#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @param announceCount the announceCount to set
	 */
	public void setAnnounceCount(long announceCount) {
		this.announceCount = announceCount;
	}

	/**
	 * @param avgAnnounceCount the avgAnnounceCount to set
	 */
	public void setAvgAnnounceCount(long avgAnnounceCount) {
		this.avgAnnounceCount = avgAnnounceCount;
	}

	/**
	 * @param avgBytesIn the avgBytesIn to set
	 */
	public void setAvgBytesIn(long avgBytesIn) {
		this.avgBytesIn = avgBytesIn;
	}

	/**
	 * @param avgBytesOut the avgBytesOut to set
	 */
	public void setAvgBytesOut(long avgBytesOut) {
		this.avgBytesOut = avgBytesOut;
	}

	/**
	 * @param avgDownloaded the avgDownloaded to set
	 */
	public void setAvgDownloaded(long avgDownloaded) {
		this.avgDownloaded = avgDownloaded;
	}

	/**
	 * @param avgScrapeCount the avgScrapeCount to set
	 */
	public void setAvgScrapeCount(long avgScrapeCount) {
		this.avgScrapeCount = avgScrapeCount;
	}

	/**
	 * @param avgUploaded the avgUploaded to set
	 */
	public void setAvgUploaded(long avgUploaded) {
		this.avgUploaded = avgUploaded;
	}

	/**
	 * @param badNATCount the badNATCount to set
	 */
	public void setBadNATCount(int badNATCount) {
		this.badNATCount = badNATCount;
	}

	/**
	 * @param canBeRemoved the canBeRemoved to set
	 */
	public void setCanBeRemoved(boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	/**
	 * @param completedCount the completedCount to set
	 */
	public void setCompletedCount(long completedCount) {
		this.completedCount = completedCount;
	}

	/**
	 * @param dateAdded the dateAdded to set
	 */
	public void setDateAdded(long dateAdded) {
		this.dateAdded = dateAdded;
	}

	/**
	 * @param isPassive the isPassive to set
	 */
	public void setPassive(boolean isPassive) {
		this.isPassive = isPassive;
	}

	/**
	 * @param leecherCount the leecherCount to set
	 */
	public void setLeecherCount(int leecherCount) {
		this.leecherCount = leecherCount;
	}

	/**
	 * @param scrapeCount the scrapeCount to set
	 */
	public void setScrapeCount(long scrapeCount) {
		this.scrapeCount = scrapeCount;
	}

	/**
	 * @param seedCount the seedCount to set
	 */
	public void setSeedCount(int seedCount) {
		this.seedCount = seedCount;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @param totalBytesIn the totalBytesIn to set
	 */
	public void setTotalBytesIn(long totalBytesIn) {
		this.totalBytesIn = totalBytesIn;
	}

	/**
	 * @param totalBytesOut the totalBytesOut to set
	 */
	public void setTotalBytesOut(long totalBytesOut) {
		this.totalBytesOut = totalBytesOut;
	}

	/**
	 * @param totalDownloaded the totalDownloaded to set
	 */
	public void setTotalDownloaded(long totalDownloaded) {
		this.totalDownloaded = totalDownloaded;
	}

	/**
	 * @param totalLeft the totalLeft to set
	 */
	public void setTotalLeft(long totalLeft) {
		this.totalLeft = totalLeft;
	}

	/**
	 * @param totalUploaded the totalUploaded to set
	 */
	public void setTotalUploaded(long totalUploaded) {
		this.totalUploaded = totalUploaded;
	}
}
