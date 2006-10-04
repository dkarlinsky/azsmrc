package lbms.azsmrc.remote.client.plugins.download;

public interface DownloadAdvancedStats {


	public boolean _isLoaded();
	public void load();

	public String getComment();
	public String getTrackerUrl();
	public String getCreatedOn();
	public String getSaveDir();
	public long getPieceCount();
	public long getPieceSize();
}
