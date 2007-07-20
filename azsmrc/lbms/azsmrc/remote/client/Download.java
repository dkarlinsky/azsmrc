/*
 * File    : Download.java
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

import lbms.azsmrc.remote.client.events.DownloadListener;

import org.gudy.azureus2.plugins.download.DownloadRemovalVetoException;

/**
 * Management of a Torrent's activity.
 *
 * <PRE>
 * A download's lifecycle:
 * torrent gets added
 *    state -&gt; QUEUED
 * slot becomes available, queued torrent is picked, &quot;restart&quot; executed
 *    state -&gt; WAITING
 * state moves through PREPARING to READY
 *    state -&gt; PREPARING
 *    state -&gt; READY
 * execute &quot;start&quot; method
 *    state -&gt; SEEDING -or- DOWNLOADING
 * if torrent is DOWNLOADING, and completes, state changes to SEEDING
 *
 * Path 1                   | Path 2
 * -------------------------+------------------------------------------------
 * execute &quot;stop&quot; method    | startstop rules are met, execute &quot;stopandQueue&quot;
 *    state -&gt; STOPPING     |     state -&gt; STOPPING
 *    state -&gt; STOPPED      |     state -&gt; STOPPED
 *                          |     state -&gt; QUEUED
 * execute &quot;remove&quot; method -&gt; deletes the download
 * a &quot;stop&quot; method call can be made when the download is in all states except STOPPED
 * </PRE>
 *
 * @author parg
 */

public interface Download extends
		lbms.azsmrc.remote.client.plugins.download.Download {
	/** waiting to be told to start preparing */
	public static final int ST_WAITING = 1;
	/** getting files ready (allocating/checking) */
	public static final int ST_PREPARING = 2;
	/** ready to be started if required */
	public static final int ST_READY = 3;
	/** downloading */
	public static final int ST_DOWNLOADING = 4;
	/** seeding */
	public static final int ST_SEEDING = 5;
	/** stopping */
	public static final int ST_STOPPING = 6;
	/** stopped, do not auto-start! */
	public static final int ST_STOPPED = 7;
	/** failed */
	public static final int ST_ERROR = 8;
	/** stopped, but ready for auto-starting */
	public static final int ST_QUEUED = 9;

	public static final String[] ST_NAMES = { "", "Waiting", "Preparing",
			"Ready", "Downloading", "Seeding", "Stopping", "Stopped", "Error",
			"Queued", };

	/**
	 * Use more of the upload bandwidth than low priority downloads don't change
	 * these as they are used by remote clients
	 */

	public static final int PR_HIGH_PRIORITY = 1;
	/** Use less of the upload bandwidth than high priority downloads */
	public static final int PR_LOW_PRIORITY = 2;

	/**
	 * get state from above ST_ set
	 *
	 * @return ST_ constant
	 *
	 * @since 2.0.7.0
	 */
	public int getState();

	/**
	 * See lifecylce description above
	 *
	 * @since 2.0.7.0
	 */
	public void start();

	/**
	 * See lifecylce description above
	 *
	 * @since 2.0.7.0
	 */
	public void stop();

	/**
	 * See lifecylce description above
	 *
	 * @since 2.0.8.0
	 */
	public void stopAndQueue();

	/**
	 * See lifecylce description above
	 *
	 * @since 2.0.7.0
	 */
	public void restart();

	/**
	 * Performs a complete recheck of the downloaded data Download must be in
	 * stopped, queued or error state Action is performed asynchronously and
	 * will progress the download through states PREPARING back to the relevant
	 * state
	 *
	 * @since 2.1.0.3
	 */

	public void recheckData();

	/**
	 * Retrieves whether the download is force started
	 *
	 * @return True if download is force started. False if not.
	 *
	 * @since 2.0.8.0
	 */
	public boolean isForceStart();

	/**
	 * When a download is completed it is rechecked (if the option is enabled).
	 * This method returns true during this phase (at which time the status will
	 * be seeding)
	 *
	 * @return
	 * @since 2.3.0.6
	 */
	public boolean isChecking();

	/**
	 * Indicates if the download has completed or not, exluding any files marked
	 * as Do No Download
	 *
	 * @return Download Complete status
	 * @since 2.1.0.4
	 */
	public boolean isComplete();

	/**
	 * Set the forcestart state of the download
	 *
	 * @param forceStart
	 *            True - Download will start, despite any Start/Stop
	 *            rules/limits<BR>
	 *            False - Turn forcestart state off. Download may or may not
	 *            stop, depending on Start/Stop rules/limits
	 *
	 * @since 2.0.8.0
	 */
	public void setForceStart(boolean forceStart);

	/**
	 * Returns the name of the torrent. Similar to Torrent.getName() and is
	 * usefull if getTorrent() returns null and you still need the name.
	 *
	 * @return name of the torrent
	 *
	 * @since 2.0.8.0
	 */
	public String getName();

	public String getHash();

	/**
	 * Removes a download. The download must be stopped or in error. Removal may
	 * fail if another component does not want the removal to occur - in this
	 * case a "veto" exception is thrown
	 *
	 * @since 2.0.7.0
	 */
	public void remove();

	/**
	 * Same as "remove" but, if successful, deletes the torrent and/or data
	 *
	 * @param delete_torrent
	 * @param delete_data
	 * @since 2.2.0.3
	 */

	public void remove(boolean delete_torrent, boolean delete_data);

	/**
	 * Returns the current position in the queue Completed and Incompleted
	 * downloads have seperate position sets. This means we can have a position
	 * x for Completed, and position x for Incompleted.
	 *
	 * @since 2.0.8.0
	 */
	public int getPosition();

	/**
	 * Sets the position in the queue Completed and Incompleted downloads have
	 * seperate position sets
	 *
	 * @since 2.0.8.0
	 */
	public void setPosition(int newPosition);

	/**
	 * Moves the download position up one
	 *
	 * @since 2.1.0.0
	 */
	public void moveUp();

	/**
	 * Moves the download down one position
	 *
	 * @since 2.1.0.0
	 */
	public void moveDown();

	/**
	 * Moves a download and re-orders the others appropriately. Note that
	 * setPosition does not do this, it merely sets the position thus making it
	 * possible, for example, for two downloads to have the same position
	 *
	 * @param position
	 * @since 2.3.0.7
	 */

	public void moveTo(int position);

	/**
	 * This returns the full save path for the download. If the download is a
	 * simple torrent, this will be the full path of the file being downloaded.
	 * If the download is a multiple file torrent, this will be the path to the
	 * directory containing all the files in the torrent.
	 *
	 * @return Full save path for this download.
	 */
	public String getSavePath();


	/**
	 * Move a download's torrent file to a new location. Download must be
	 * stopped and persistent
	 *
	 * @since 2.3.0.5
	 * @param new_parent_dir
	 */
	public void moveTorrentFile(String new_parent_dir);

	/**
	 * Renames the file (for a single file torrent) or directory (for a multi
	 * file torrent) where the download is being saved to. The download must be
	 * in a state to move the data files to a new location (see
	 * {@link #moveDataFiles(File)}).
	 *
	 * <p>
	 *
	 * This will not rename the displayed name for the torrent - if you wish to
	 * do that, you must do it via the
	 * {@link org.gudy.azureus2.plugins.torrent.TorrentAttribute TorrentAttribute}
	 * class.
	 *
	 * @param name
	 *            New name for the download.
	 * @see #moveDataFiles(File)
	 */
	public void renameDownload(String name);

	/**
	 * Changes the displayed name for the Download
	 *
	 * Note: you could use setTorrentAttribute(RemoteConstants.TA_DISPLAY_NAME, "new name") too
	 *
	 * @param name
	 */
	public void changeDisplayedName(String name);

	/**
	 * Sets a TorrentAttribute.
	 *
	 * Az Default Attributes can be found at RemoteConstants.TA_*
	 *
	 * @param name Attribute name
	 * @param value new value
	 */
	public void setTorrentAttribute (String name, String value);

	/**
	 * Tests whether or not a download can be removed. Due to synchronization
	 * issues it is possible for a download to report OK here but still fail
	 * removal.
	 *
	 * @return
	 * @throws DownloadRemovalVetoException
	 *
	 * @since 2.0.7.0
	 */

	public DownloadStats getStats();

	/**
	 * Sets the maximum download speed in bytes per second. 0 -> unlimited
	 *
	 * @since 2.1.0.2
	 * @param kb
	 */

	public void moveDataFiles(String target);

	public DownloadAdvancedStats getAdvancedStats();

	public DownloadFileManager getFileManager();

	public void setMaximumDownloadKBPerSecond(int kb);

	public int getMaximumDownloadKBPerSecond();

	public int getSeeds();

	public int getLeecher();

	public int getTotalSeeds();

	public int getTotalLeecher();

	public long getDiscarded();

	public long getSize();

	public void requestScrape();

	public void requestAnnounce();

	public long getLastScrapeTime();

	public long getNextScrapeTime();

	public long getAnnounceTimeToWait();

	/**
	 * Get the max upload rate allowed for this download.
	 *
	 * @return upload rate in bytes per second, 0 for unlimited, -1 for upload
	 *         disabled
	 */
	public int getUploadRateLimitBytesPerSecond();

	/**
	 * Set the max upload rate allowed for this download.
	 *
	 * @param max_rate_bps
	 *            limit in bytes per second, 0 for unlimited, -1 for upload
	 *            disabled
	 */
	public void setUploadRateLimitBytesPerSecond(int max_rate_bps);

	public void addDownloadListener(DownloadListener dll);

	public void removeDownloadListener(DownloadListener dll);
}
