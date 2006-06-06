/**
 * 
 */
package lbms.azsmrc.remote.client;

/**
 * @author Damokles
 *
 */
public interface TrackerListener {

	public void 	torrentAdded(TrackerTorrent torrent);

	public void 	torrentChanged(TrackerTorrent torrent);

	public void 	torrentRemoved(TrackerTorrent torrent);
}
