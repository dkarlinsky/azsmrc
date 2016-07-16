/**
 * 
 */
package lbms.azsmrc.remote.client.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.Tracker;
import lbms.azsmrc.remote.client.TrackerListener;
import lbms.azsmrc.remote.client.TrackerTorrent;

/**
 * @author Damokles
 *
 */
public class TrackerImpl implements Tracker {

	private static final TrackerTorrent[] emptyArray = new TrackerTorrent[0];
	private Client client;
	private Map<String,TrackerTorrentImpl> torrents = Collections.synchronizedMap(new HashMap<String,TrackerTorrentImpl>());
	private List<TrackerListener> listeners = new Vector<TrackerListener>();

	public TrackerImpl (Client c) {
		this.client = c;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#update()
	 */
	public void update() {
		client.sendGetTrackerTorrents();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#getTorrents()
	 */
	public TrackerTorrent[] getTorrents() {
		return torrents.values().toArray(emptyArray);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#host(java.io.File, boolean)
	 */
	public void host(File torrent, boolean persistent) {
		client.sendHostTorrent(torrent, persistent, false);

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#host(java.io.File, boolean, boolean)
	 */
	public void host(File torrent, boolean persistent, boolean passive) {
		client.sendHostTorrent(torrent, persistent, passive);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#host(lbms.azsmrc.remote.client.Download, boolean)
	 */
	public void host(Download dl, boolean persistent) {
		client.sendHostTorrent(dl, persistent, false);

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#host(lbms.azsmrc.remote.client.Download, boolean, boolean)
	 */
	public void host(Download dl, boolean persistent, boolean passive) {
		client.sendHostTorrent(dl, persistent, passive);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#publish(java.io.File)
	 */
	public void publish(File torrent) {
		client.sendPublishTorrent(torrent);

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#publish(lbms.azsmrc.remote.client.Download)
	 */
	public void publish(Download torrent) {
		client.sendPublishTorrent(torrent);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#addListener(lbms.azsmrc.remote.client.TrackerListener)
	 */
	public void addListener(TrackerListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#removeListener(lbms.azsmrc.remote.client.TrackerListener)
	 */
	public void removeListener(TrackerListener listener) {
		listeners.remove(listener);
	}

	public void addTorrent (TrackerTorrentImpl t) {
		torrents.put(t.getHash(), t);
		for (TrackerListener l:listeners)
			l.torrentAdded(t);

	}

	public boolean hasTorrent (String hash) {
		return torrents.containsKey(hash);
	}

	public TrackerTorrentImpl getTorrent (String hash) {
		return torrents.get(hash);
	}
}
