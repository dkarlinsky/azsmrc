package lbms.azsmrc.remote.client.plugins.download;

public interface DownloadAdvancedStats {


	/**
	 * Returns whether the DownloadAdvancedStats are loaded
	 * @return
	 */
	public boolean _isLoaded();
	
	/**
	 * Load the DownloadAdvancedStats if they aren't already
	 */
	public void load();

	/**
	 * Returns the comment for the Download
	 * 
	 * @return
	 */
	public String getComment();
	
	/**
	 * Returns the URL of the Tracker
	 * 
	 * @return
	 */
	public String getTrackerUrl();
	
	/**
	 * Returns the Date this Torrent was Created
	 * 
	 * @return
	 */
	public String getCreatedOn();
	
	/**
	 * Returns the Remote Save Location
	 * 
	 * @return
	 */
	public String getSaveDir();
	
	/**
	 * Returns the piece count for this Download
	 * 
	 * @return
	 */
	public long getPieceCount();
	
	/**
	 * Returns the piece size 
	 * 
	 * @return
	 */
	public long getPieceSize();
}
