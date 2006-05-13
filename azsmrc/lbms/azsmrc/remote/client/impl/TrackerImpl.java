/**
 * 
 */
package lbms.azsmrc.remote.client.impl;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.gudy.azureus2.pluginsimpl.local.tracker.TrackerTorrentImpl;

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

	private static final TrackerTorrentImpl[] emptyArray = new TrackerTorrentImpl[0];
	private Client client;
	private List<TrackerTorrentImpl> torrents = new Vector<TrackerTorrentImpl>();
	private List<TrackerListener> listeners = new Vector<TrackerListener>();

	public TrackerImpl (Client c) {
		this.client = c;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.Tracker#getTorrents()
	 */
	public TrackerTorrent[] getTorrents() {
		return (TrackerTorrent[])torrents.toArray(emptyArray);
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
}
