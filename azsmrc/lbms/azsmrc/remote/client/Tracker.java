/**
 * 
 */
package lbms.azsmrc.remote.client;

import java.io.File;


/**
 * @author Damokles
 *
 */
public interface Tracker {
	public TrackerTorrent[] 	getTorrents();

	public void 	host(File torrent, boolean persistent);
	public void 	host(File torrent, boolean persistent, boolean passive);

	public void 	host(Download dl, boolean persistent);
	public void 	host(Download dl, boolean persistent, boolean passive);

	public void 	publish(File torrent);
	public void 	publish(Download torrent);

	public void 	addListener(TrackerListener listener);
	public void 	removeListener(TrackerListener listener);

	public void		update();
}
