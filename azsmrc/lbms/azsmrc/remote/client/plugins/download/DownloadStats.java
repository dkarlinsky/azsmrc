/*
 * File    : DownloadStats.java
 * Created : 08-Jan-2004
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

package lbms.azsmrc.remote.client.plugins.download;

/**
 * @author parg
 * This class gives access to various stats associated with the download
 */

public interface
DownloadStats
{
	public static final int HEALTH_STOPPED    		= 1;
	public static final int HEALTH_NO_TRACKER 		= 2;
	public static final int HEALTH_NO_REMOTE  		= 3;
	public static final int HEALTH_OK  				= 4;
	/** not connected to any peer and downloading */
	public static final int HEALTH_KO 				= 5;
	/** download in STATE_ERROR, see {@link #getStatus()} for error */
	public static final int HEALTH_ERROR 				= 6;

	/**
	 * Returns an overall string representing the state of the download
	 * @return
   *
   * @since 2.0.7.0
	 */
	public String
	getStatus();

	public String
	getTrackerStatus();

	/**
	 * returns a value between 0 and 1000 giving the completion status of the current download
	 * task (e.g. checking, downloading)
	 * @return
   *
   * @since 2.0.7.0
	 */
	public int
	getCompleted();

	public long
	getTotalAverage();


	/**
	 * Gives the number of bytes downloaded
	 * @return
   *
   * @since 2.0.7.0
	 */
	public long
	getDownloaded();

	/**
	 * Gives the number of bytes uploaded
	 * @return
   *
   * @since 2.0.7.0
	 */
	public long
	getUploaded();

	/**
	 * Gives average number of bytes downloaded in last second 
	 * @return
   *
   * @since 2.0.7.0
	 */
	public long
	getDownloadAverage();

	/**
	 * Gives average number of bytes uploaded in last second 
	 * @return
   *
   * @since 2.0.7.0
	 */
	public long
	getUploadAverage();

	public String
	getElapsedTime();

	/**
	 * Gives the estimated time to completion as a string
	 * @return
   *
   * @since 2.0.7.0
	 */
	public String
	getETA();

	/**
	 * Gives the share ratio of the torrent in 1000ths (i.e. 1000 = share ratio of 1)
	 * @return
   *
   * @since 2.0.7.0
	 */
	public int
	getShareRatio();

	/**
	 * Gives the currently seen availability of the torrent
	 * @return
   *
   * @since 2.0.8.2
	 */
	public float
	getAvailability();

	/**
	 * returns an indication of the health of the torrent 
	 * @return	see above HEALTH constants
	 */

	public int
	getHealth();
}
