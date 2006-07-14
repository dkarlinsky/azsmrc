/*
 * File    : DownloadManager.java
 * Created : 06-Jan-2004
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package lbms.azsmrc.remote.client;

import java.io.File;
import java.net.URL;

import lbms.azsmrc.remote.client.events.DownloadManagerListener;

public interface
DownloadManager
{
	/**
	 * add a torrent from a file. 
	 * 
	 * @param torrent_file
	 */
	public void
	addDownload(
		File 	torrent_file );

	/**
	 * add a torrent from a file and select which files should be downloaded.
	 * 
	 * fileSelection is an array consisting of 1 and 0, for every file in the torrent
	 * there has to be either 1 or 0. They have to be in the same order as the torrent
	 * specifies.
	 * 
	 * @param torrent_file local torrent file
	 * @param fileSelection the array selects the downloads 1 is download 0 is DND 
	 */
	public void
	addDownload(
		File 	torrent_file,
		final int[] fileSelection );

	/**
	 * add a torrent from a file and specify save dir
	 * @param torrent_file local torrent file
	 * @param fileLocation file save location; null -> default will be used
	 */
	public void
	addDownload(
		File 	torrent_file,
		final String fileLocation );

	/**
	 * add a torrent from a file and select which files should be downloaded and specify save dir.
	 * 
	 * fileSelection is an array consisting of 1 and 0, for every file in the torrent
	 * there has to be either 1 or 0. They have to be in the same order as the torrent
	 * specifies.
	 * 
	 * @param torrent_file local torrent file
	 * @param fileSelection the array selects the downloads 1 is download 0 is DND 
	 * @param fileLocation file save location; null -> default will be used
	 */
	public void
	addDownload(
		File 	torrent_file,
		final int[] fileSelection,
		final String fileLocation );

	/**
	 * This will add a Download via an URL.
	 * 
	 * @param url TorrentURL
	 */
	public void
	addDownload(
		String		url );

	/**
	 * add a Download via an URL.
	 * 
	 * @param url TorrentURL
	 * @param fileLocation file save location; null -> default will be used
	 */
	public void
	addDownload(
		String		url,
		final String fileLocation );


	/**
	 * add a Download via an URL.
	 * 
	 * You can specify username/password and referer if requiered.
	 * 
	 * @param url TorrentURL
	 * @param username maybe null
	 * @param password maybe null
	 * @param referer maybe null
	 * @param fileLocation file save location; null -> default will be used
	 */
	public void
	addDownload(
		final String url,
		final String username,
		final String password,
		final URL 	referer,
		final String fileLocation );

	/**
	 * Gets the download for a particular torrent, returns null if not found
	 * @param torrent
	 * @return
	 */
	public Download
	getDownload(
		String		hash );

	/**
	 * Gets all the downloads. Returned in Download hash order
	 * @return
	 */
	public Download[]
	getDownloads();

	/**
	 * Gets all the downloads. Returned in Download position order
	 * @return
	 */
	public Download[]
	getSortedDownloads();

	/**
	 * Gets Seeding Downloads only
	 * @return
	 */
	public Download[]
	getSeedingDownloadsOnly();

	/**
	 * Gets only non finished downloads
	 * @return
	 */
	public Download[]
	getDownloadsOnly();

	/**
	 * pause all running downloads
	 *
	 */

	public void
	pauseDownloads();


	/**
	 * pause all running downloads.
	 * 
	 * Will resume after timeout 
	 * 
	 * @param timeout in sec
	 */
	public void
	pauseDownloads(int timeout);

	/**
	 * resume previously paused downloads
	 */

	public void
	resumeDownloads();

	/**
	 * starts all non-running downloads
	 */

	public void
	startAllDownloads();

	/**
	 * stops all running downloads
	 */

	public void
	stopAllDownloads();

		/**
		 * indicates whether or not all active downloads are in a seeding (or effective) seeding state
		 * @return
		 */

	public boolean
	isSeedingOnly();

	/**
	 * Add a listener that will be informed when a download is added to/removed from Azureus
	 * @param l
	 */
	public void
	addListener(
		DownloadManagerListener	l );

	/**
	 * Removes listeners added above
	 * @param l
	 */
	public void
	removeListener(
		DownloadManagerListener	l );

	/**
	 * Will remove all downloads 
	 */
	public void
	clear();

	/**
	 * Will request an update from the Az Server.
	 * 
	 * It uses smart updating, only the differences
	 * since the last call are updated. If you want a
	 * full update set full = true;
	 * @param full whether do a full update or not 
	 */
	public void
	update (boolean full);
}
