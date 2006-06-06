package lbms.azsmrc.remote.client.impl;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadAdvancedStats;
import lbms.azsmrc.remote.client.DownloadFileManager;
import lbms.azsmrc.remote.client.DownloadStats;
import lbms.azsmrc.remote.client.events.DownloadListener;

public class DownloadImpl implements Download, Comparable<DownloadImpl> {

	private String name;
	private String hash;
	private boolean forceStart = false;
	private int state, position, uploadLimit = -1,downloadLimit = -1, seeds,leecher, totalSeeds,totalLeecher;
	private long discarded, size, lastScrape, nextScrape;
	private DownloadStatsImpl stats;
	private List<DownloadListener> listener = new ArrayList<DownloadListener>();
	private DownloadAdvancedStatsImpl advStats;
	private DownloadFileManagerImpl dlFileMgr;
	private Client client;

	public DownloadImpl (String hash) {
		this.hash = hash;
	}

	public DownloadImpl (String hash,Client client) {
		this.hash = hash;
		this.client = client;
	}

	public int compareTo(DownloadImpl o) {
		if (position == o.position) {
			return name.compareToIgnoreCase(o.name);
		}
		return position-o.position;
	}


	public int getState() {
		return state;
	}

	public void start() {
		client.sendStartDownload(hash);
	}

	public void stop() {
		client.sendStopDownload(hash);
	}

	public void stopAndQueue() {
		client.sendStopAndQueueDownloadDownload(hash);
	}

	public void restart() {
		client.sendRestartDownload(hash);
	}

	public void recheckData() {
		client.sendRecheckDataDownload(hash);
	}

	public boolean isForceStart() {
		return forceStart;
	}

	public void setForceStart(boolean forceStart) {
		client.sendSetForceStart(hash, forceStart);
		this.forceStart = forceStart;

	}

	public String getName() {
		return name;
	}

	public String getHash() {
		return hash;
	}

	public void remove() {
		client.sendRemoveDownload(hash);
	}

	public void remove(boolean delete_torrent, boolean delete_data) {
		client.sendRemoveDownload(hash, delete_torrent, delete_data);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int newPosition) {
		client.sendSetPosition(hash, newPosition);
	}

	public void moveDown() {
		client.sendMoveDown(hash);
	}

	public void moveUp() {
		client.sendMoveUp(hash);
	}

	public void moveTo(int position) {
		client.sendMoveToPosition(hash, position);
	}

	public DownloadStats getStats() {
		return stats;
	}

	public void requestScrape() {
		client.sendRequestDownloadScrape(hash);
	}

	public void requestAnnounce() {
		client.sendRequestDownloadAnnounce(hash);
	}

	public long getLastScrapeTime() {
		return lastScrape;
	}

	public long getNextScrapeTime() {
		return nextScrape;
	}

	/**
	 * @param lastScrape the lastScrape to set
	 */
	public void implSetLastScrapeTime(long lastScrape) {
		this.lastScrape = lastScrape;
	}

	/**
	 * @param nextScrape the nextScrape to set
	 */
	public void implSetNextScrapeTime(long nextScrape) {
		this.nextScrape = nextScrape;
	}

	public void moveDataFiles(String target) {
		client.sendMoveDataFiles(hash, target);
	}

	public DownloadAdvancedStats getAdvancedStats() {
		if (advStats == null) advStats = new DownloadAdvancedStatsImpl(hash, client);
		return advStats;
	}

	public DownloadFileManager getFileManager() {
		if (dlFileMgr == null) dlFileMgr = new DownloadFileManagerImpl(hash, client);
		return dlFileMgr;
	}

	public void setMaximumDownloadKBPerSecond(int kb) {
		client.sendMaximumDownloadKBPerSecond(hash, kb);
	}

	public int getMaximumDownloadKBPerSecond() {
		return downloadLimit;
	}

	public int getUploadRateLimitBytesPerSecond() {
		return uploadLimit;
	}

	public void setUploadRateLimitBytesPerSecond(int max_rate_bps) {
		client.sendUploadRateLimitBytesPerSecond(hash, max_rate_bps);
	}

	public DownloadStatsImpl getStatsImpl() {
		return stats;
	}

	public void implSetStats(DownloadStatsImpl stats) {
		this.stats = stats;
	}


	/**
	 * @param downloadLimit The downloadLimit to set.
	 */
	public void implSetDownloadLimit(int downloadLimit) {
		this.downloadLimit = downloadLimit;
	}


	/**
	 * @param hash The hash to set.
	 */
	public void implSetHash(String hash) {
		this.hash = hash;
	}


	/**
	 * @param name The name to set.
	 */
	public void implSetName(String name) {
		this.name = name;
	}


	/**
	 * @param state The state to set.
	 */
	public void implSetState(int state) {
		if (this.state != state)
			stateChanged(this.state, state);
		this.state = state;
	}


	/**
	 * @param uploadLimit The uploadLimit to set.
	 */
	public void implSetUploadLimit(int uploadLimit) {
		this.uploadLimit = uploadLimit;
	}

	public void implSetPosition (int pos) {
		if (this.position != pos)
			positionChanged(position, pos);
		this.position = pos;
	}

	public void addDownloadListener(DownloadListener dll) {
		listener.add(dll);
	}

	public void removeDownloadListener(DownloadListener dll) {
		listener.remove(dll);
	}

	private void stateChanged (int oldState,int newState) {
		for (DownloadListener l:listener) {
			l.stateChanged(this, oldState, newState);
		}
	}

	private void positionChanged (int oldPos,int newPos) {
		for (DownloadListener l:listener) {
			l.positionChanged(this, oldPos, newPos);
		}
	}

	/**
	 * @return Returns the leecher.
	 */
	public int getLeecher() {
		return leecher;
	}

	/**
	 * @param leecher The leecher to set.
	 */
	public void implSetLeecher(int leecher) {
		this.leecher = leecher;
	}

	/**
	 * @return Returns the seeds.
	 */
	public int getSeeds() {
		return seeds;
	}

	/**
	 * @param seeds The seeds to set.
	 */
	public void implSetSeeds(int seeds) {
		this.seeds = seeds;
	}

	/**
	 * @return Returns the totalLeecher.
	 */
	public int getTotalLeecher() {
		return totalLeecher;
	}

	/**
	 * @param totalLeecher The totalLeecher to set.
	 */
	public void implSetTotalLeecher(int totalLeecher) {
		this.totalLeecher = totalLeecher;
	}

	/**
	 * @return Returns the totalSeeds.
	 */
	public int getTotalSeeds() {
		return totalSeeds;
	}

	/**
	 * @param totalSeeds The totalSeeds to set.
	 */
	public void implSetTotalSeeds(int totalSeeds) {
		this.totalSeeds = totalSeeds;
	}

	/**
	 * @return Returns the discarded.
	 */
	public long getDiscarded() {
		return discarded;
	}

	public long getSize() {
		return size;
	}

	public void implSetSize (long size) {
		this.size = size;
	}

	public void implSetForceStart (boolean forceStart) {
		this.forceStart = forceStart;
	}

	/**
	 * @param discarded The discarded to set.
	 */
	public void implSetDiscarded(long discarded) {
		this.discarded = discarded;
	}

	/**
	 * @return Returns the advStats.
	 */
	public DownloadAdvancedStatsImpl getAdvancedStatsImpl() {
		if (advStats == null) advStats = new DownloadAdvancedStatsImpl(hash, client);
		return advStats;
	}

	/**
	 * @return Returns the dlFileMgr.
	 */
	public DownloadFileManagerImpl getFileManagerImpl() {
		return dlFileMgr;
	}

	@Override
	public String toString() {
		if (name != null) return name;
		else return super.toString();
	}
}
