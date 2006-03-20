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
	 * add a torrent from a file. This will prompt the user for download location etc. if required
	 * This is an async operation so no Download returned
	 * @param torrent_file
	 */
	public void
	addDownload(
		File 	torrent_file );

	/**
	 * add a torrent from a URL. This will prompt the user for download location etc. if required
	 * This is an async operation so no Download returned
	 * @param url
	 */
	public void
	addDownload(
		URL		url );

	/**
	 * add a torrent from a URL. This will prompt the user for download location etc. if required
	 * This is an async operation so no Download returned
	 * @param url
	 * @param referer
	 * @throws DownloadException
	 *
	 */
	public void
	addDownload(
		final URL	url,
		final URL 	referer);

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
	 * pause all running downloads
	 *
	 */

	public void
	pauseDownloads();

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
}
