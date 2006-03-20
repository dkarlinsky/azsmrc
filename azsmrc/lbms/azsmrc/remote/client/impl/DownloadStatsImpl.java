package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.DownloadStats;

public class DownloadStatsImpl implements DownloadStats {

	private String status = "", trackerStatus = "", eta = "", elapsedTime = "";
	private int completed = 0, shareRatio = 0, health = 0;
	private long downloaded = 0, uploaded = 0, downAvg = 0, upAvg = 0, totalAverage = 0;
	private float availability = 0;

	/**
	 * @param availability The availability to set.
	 */
	public void setAvailability(float availability) {
		this.availability = availability;
	}

	/**
	 * @param completed The completed to set.
	 */
	public void setCompleted(int completed) {
		this.completed = completed;
	}

	/**
	 * @param downAvg The downAvg to set.
	 */
	public void setDownAvg(long downAvg) {
		this.downAvg = downAvg;
	}

	/**
	 * @param downloaded The downloaded to set.
	 */
	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	public void setElapsedTime (String time) {
		if (time!=null)
			this.elapsedTime = time;
		else
			this.elapsedTime = "";
	}

	/**
	 * @param eta The eta to set.
	 */
	public void setEta(String eta) {
		if (eta!=null)
			this.eta = eta;
		else
			this.eta = "";
	}

	/**
	 * @param health The health to set.
	 */
	public void setHealth(int health) {
		this.health = health;
	}

	/**
	 * @param shareRatio The shareRatio to set.
	 */
	public void setShareRatio(int shareRatio) {
		this.shareRatio = shareRatio;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		if (status != null)
			this.status = status;
		else
			this.status = "";
	}

	/**
	 * @param trackerStatus The trackerStatus to set.
	 */
	public void setTrackerStatus(String trackerStatus) {
		if (trackerStatus != null)
			this.trackerStatus = trackerStatus;
		else
			this.trackerStatus = "";
	}

	/**
	 * @param upAvg The upAvg to set.
	 */
	public void setUpAvg(long upAvg) {
		this.upAvg = upAvg;
	}

	/**
	 * @param uploaded The uploaded to set.
	 */
	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	/**
	 * @param totalAverage The totalAverage to set.
	 */
	public void setTotalAverage(long totalAverage) {
		this.totalAverage = totalAverage;
	}

	public long getTotalAverage() {
		return totalAverage;
	}

	public String getStatus() {
		return status;
	}

	public String getTrackerStatus() {
		return trackerStatus;
	}

	public int getCompleted() {
		return completed;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public long getUploaded() {
		return uploaded;
	}

	public long getDownloadAverage() {
		return downAvg;
	}

	public long getUploadAverage() {
		return upAvg;
	}

	public String getElapsedTime() {
		return elapsedTime;
	}

	public String getETA() {
		return eta;
	}

	public int getShareRatio() {
		return shareRatio;
	}

	public float getAvailability() {
		return availability;
	}

	public int getHealth() {
		return health;
	}

}
