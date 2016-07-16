package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.DownloadAdvancedStats;

public class DownloadAdvancedStatsImpl implements DownloadAdvancedStats {

	private boolean isLoaded;
	private boolean loading;
	private String comment, trackerUrl, createdOn, saveDir;
	private long pieceCount, pieceSize;
	private String hash;
	private Client client;

	public DownloadAdvancedStatsImpl(String hash, Client client) {
		comment = trackerUrl = createdOn = saveDir = "loading...";
		isLoaded = loading = false;
		this.hash = hash;
		this.client = client;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @param createdOn The createdOn to set.
	 */
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @param isLoaded The isLoaded to set.
	 */
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	/**
	 * @param loading The loading to set.
	 */
	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	/**
	 * @param pieceCount The pieceCount to set.
	 */
	public void setPieceCount(long pieceCount) {
		this.pieceCount = pieceCount;
	}

	/**
	 * @param pieceSize The pieceSize to set.
	 */
	public void setPieceSize(long pieceSize) {
		this.pieceSize = pieceSize;
	}

	/**
	 * @param saveDir The saveDir to set.
	 */
	public void setSaveDir(String saveDir) {
		this.saveDir = saveDir;
	}

	/**
	 * @param trackerUrl The trackerUrl to set.
	 */
	public void setTrackerUrl(String trackerUrl) {
		this.trackerUrl = trackerUrl;
	}

	public boolean _isLoaded() {
		return isLoaded;
	}

	public String getComment() {
		return comment;
	}

	public String getTrackerUrl() {
		return trackerUrl;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public String getSaveDir() {
		return saveDir;
	}

	public long getPieceCount() {
		return pieceCount;
	}

	public long getPieceSize() {
		return pieceSize;
	}

	public void load() {
		if (isLoaded || loading) return;
		loading = true;
		client.sendGetAdvancedStats(hash);
	}

}
