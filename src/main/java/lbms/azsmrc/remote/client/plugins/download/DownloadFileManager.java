package lbms.azsmrc.remote.client.plugins.download;

/**
 * @author Damokles
 *
 */
public interface DownloadFileManager {

	/**
	 * Returns whether the DownloadFileManager was loaded
	 * 
	 * @return
	 */
	public boolean _isLoaded();

	/**
	 * Returns the Files for the Download
	 * @return
	 */
	public DownloadFile[] getFiles();
	
	/**
	 * Updates the DownloadFileManager Data
	 */
	public void update();
}
