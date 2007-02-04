package lbms.azsmrc.remote.client.plugins.download;

public interface DownloadFile {

	/**
	 * Returns the Filename
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns the Filelength
	 * 
	 * @return
	 */
	public long getLength();
	
	/**
	 * Returns the Number of Pieces for this File
	 * 
	 * @return
	 */
	public int getNumPieces();
	
	/**
	 * Returns alreasy Downloaded Bytes
	 * 
	 * @return
	 */
	public long getDownloaded();
	
	/**
	 * Returns whether this File has priority
	 * 
	 * @return
	 */
	public boolean getPriority();
	
	/**
	 * Returns whether this File is skipped
	 * @return
	 */
	public boolean getSkipped();
	
	/**
	 * Returns if this File is set to deleted
	 * 
	 * @return
	 */
	public boolean getDeleted();
	
	/**
	 * Returns the Index of the File
	 * 
	 * @return
	 */
	public int getIndex();
	
	/**
	 * Set this file priority option
	 * 
	 * @param priority
	 */
	public void setPriority(boolean priority);
	
	/**
	 * Set this file skipped option
	 * 
	 * @param skipped
	 */
	public void setSkipped(boolean skipped);
	
	/**
	 * Set this file deleted option
	 * 
	 * @param skipped
	 */
	public void setDeleted(boolean skipped);
}
