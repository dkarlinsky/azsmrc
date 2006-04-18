package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.DownloadFile;

public class DownloadFileImpl implements DownloadFile {

	private String name;
	public int numPieces, index;
	private long length, downloaded;
	private boolean priority, skipped, deleted;
	private String hash;
	private Client client;

	public DownloadFileImpl(String hash, Client client) {
		name = "";
		this.hash = hash;
		this.client = client;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public long getLength() {
		// TODO Auto-generated method stub
		return length;
	}

	public int getNumPieces() {
		// TODO Auto-generated method stub
		return numPieces;
	}

	public long getDownloaded() {
		// TODO Auto-generated method stub
		return downloaded;
	}

	public boolean getPriority() {
		// TODO Auto-generated method stub
		return priority;
	}

	public boolean getSkipped() {
		// TODO Auto-generated method stub
		return skipped;
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}

	/**
	 * @param downloaded The completition to set.
	 */
	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	/**
	 * @param index The index to set.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @param length The length to set.
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param numPieces The numPieces to set.
	 */
	public void setNumPieces(int numPieces) {
		this.numPieces = numPieces;
	}

	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(boolean priority) {
		if (this.priority != priority) {
			this.priority = priority;
			updateStatus();
		}
	}

	/**
	 * @param skipped The skipped to set.
	 */
	public void setSkipped(boolean skipped) {
		if (this.skipped != skipped) {
			this.skipped = skipped;
			updateStatus();
		}
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		if (this.deleted != deleted) {
			this.deleted = deleted;
			updateStatus();
		}
	}

	public void setPriorityImpl(boolean priority) {
		this.priority = priority;
	}

	/**
	 * @param skipped The skipped to set.
	 */
	public void setSkippedImpl(boolean skipped) {
		this.skipped = skipped;
	}

	private void updateStatus() {
		client.sendSetFileOptions(hash, index, priority, skipped, deleted);
	}
}
