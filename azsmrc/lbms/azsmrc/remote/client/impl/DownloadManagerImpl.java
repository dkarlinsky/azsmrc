package lbms.azsmrc.remote.client.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadManager;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;

public class DownloadManagerImpl implements DownloadManager {

	private static final Download[] emptyDlArray = new Download[0];

	private Map<String, DownloadImpl> downloads = Collections.synchronizedMap( new TreeMap<String, DownloadImpl>() );
	private List<DownloadManagerListener> listeners =Collections.synchronizedList( new ArrayList<DownloadManagerListener>() );
	private Client client;

	public DownloadManagerImpl (Client client) {
		this.client = client;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.io.File)
	 */
	public void addDownload(File torrent_file) {
		client.sendAddDownload(torrent_file, null, null);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.io.File, int[])
	 */
	public void addDownload(File torrent_file, int[] fileSelection) {
		client.sendAddDownload(torrent_file, fileSelection, null);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.io.File, int[], java.lang.String)
	 */
	public void addDownload(File torrent_file, int[] fileSelection, String fileLocation) {
		client.sendAddDownload(torrent_file, fileSelection, fileLocation);
	}
	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.io.File, java.lang.String)
	 */

	public void addDownload(File torrent_file, String fileLocation) {
		client.sendAddDownload(torrent_file, null, fileLocation);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.net.URL)
	 */
	public void addDownload(String url) {
		client.sendAddDownload(url, null, null, null);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.net.URL, java.lang.String)
	 */
	public void addDownload(String url, String fileLocation) {
		client.sendAddDownload(url, null, null, fileLocation);

	}
	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#addDownload(java.net.URL, java.lang.String, java.lang.String, java.net.URL, java.lang.String)
	 */
	public void addDownload(String url, String username, String password, URL referer, String fileLocation) {
		client.sendAddDownload(url, username, password, fileLocation);
	}

	public Download getDownload(String hash) {

		return downloads.get(hash);
	}

	public Download[] getDownloads() {
		return downloads.values().toArray(emptyDlArray);
	}

	public Download[] getSortedDownloads() {
		Download[] dls = downloads.values().toArray(emptyDlArray);
		Arrays.sort(dls, new Comparator<Download>() {
			public int compare(Download o1, Download o2) {
				if (o1.getPosition() == o2.getPosition()) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}  else	return o1.getPosition() - o2.getPosition();
			}
		});
		return dls;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#getDownloadsOnly()
	 */
	public Download[] getDownloadsOnly() {
		List<Download> dl = new ArrayList<Download>();
		for (Download d:downloads.values()) {
			if (d.getState()!= Download.ST_SEEDING && !d.isComplete() && d.getState() != Download.ST_STOPPED)
				dl.add(d);
		}
		return dl.toArray(emptyDlArray);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#getSeedingDownloadsOnly()
	 */
	public Download[] getSeedingDownloadsOnly() {
		List<Download> dl = new ArrayList<Download>();
		for (Download d:downloads.values()) {
			if ((d.getState()== Download.ST_SEEDING || d.isComplete()) && d.getState() != Download.ST_STOPPED)
				dl.add(d);
		}
		return dl.toArray(emptyDlArray);
	}

	public void pauseDownloads() {
		client.sendPauseDownloads(0);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#pauseDownloads(int)
	 */
	public void pauseDownloads(int timeout) {
		client.sendPauseDownloads(timeout);
	}

	public void resumeDownloads() {
		client.sendResumeDownloads();
	}

	public void startAllDownloads() {
		client.sendStartAll();

	}

	public void stopAllDownloads() {
		client.sendStopAll();
	}

	public boolean isSeedingOnly() {
		return getDownloadsOnly().length == 0;
	}

	public void addListener(DownloadManagerListener l) {
		listeners.add(l);
	}

	public void removeListener(DownloadManagerListener l) {
		listeners.remove(l);
	}

	private void eventDownloadAdded (Download dl) {
		for (DownloadManagerListener l:listeners) {
			l.downloadAdded(dl);
		}
	}

	private void eventDownloadRemoved (Download dl) {
		for (DownloadManagerListener l:listeners) {
			l.downloadRemoved(dl);
		}
	}

	public void clear() {
		downloads.clear();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.DownloadManager#update(boolean)
	 */
	public void update(boolean full) {
		client.sendUpdateDownloads(full);
	}

	//-------------------------------------------//

	public DownloadImpl manGetDownload (String hash) {
		return downloads.get(hash);
	}

	public boolean manHasDownload (String hash) {
		return downloads.containsKey(hash);
	}

	public void manRemoveDownload (String hash) {
		if (downloads.containsKey(hash)) {
			eventDownloadRemoved(downloads.get(hash));
			downloads.remove(hash);
		}
	}

	public void manAddDownload (DownloadImpl dl) {
		downloads.put(dl.getHash(), dl);
		eventDownloadAdded(dl);
	}
}