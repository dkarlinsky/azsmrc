package lbms.azsmrc.remote.client.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadManager;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;

public class DownloadManagerImpl implements DownloadManager {

	Map<String, DownloadImpl> downloads = Collections.synchronizedMap(new TreeMap<String, DownloadImpl>());
	List<DownloadManagerListener> listeners = new ArrayList<DownloadManagerListener>();

	public void addDownload(File torrent_file) {
		// TODO Auto-generated method stub

	}

	public void addDownload(URL url) {
		// TODO Auto-generated method stub

	}

	public void addDownload(URL url, URL referer) {
		// TODO Auto-generated method stub

	}

	public Download getDownload(String hash) {

		return downloads.get(hash);
	}

	public Download[] getDownloads() {
		return downloads.values().toArray(new Download[] {});
	}

	public Download[] getSortedDownloads() {
		Download[] dls = downloads.values().toArray(new Download[] {});
		Arrays.sort(dls, new Comparator<Download>() {
			public int compare(Download o1, Download o2) {
				if (o1.getPosition() == o2.getPosition()) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}  else	return o1.getPosition() - o2.getPosition();
			}
		});
		return dls;
	}

	public void pauseDownloads() {
		// TODO Auto-generated method stub

	}

	public void resumeDownloads() {
		// TODO Auto-generated method stub

	}

	public void startAllDownloads() {
		// TODO Auto-generated method stub

	}

	public void stopAllDownloads() {
		// TODO Auto-generated method stub

	}

	public boolean isSeedingOnly() {
		// TODO Auto-generated method stub
		return false;
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