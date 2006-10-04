package lbms.azsmrc.remote.client.plugins.download;

public interface DownloadFileManager {

	public boolean _isLoaded();

	public DownloadFile[] getFiles();
	public void update();
}
