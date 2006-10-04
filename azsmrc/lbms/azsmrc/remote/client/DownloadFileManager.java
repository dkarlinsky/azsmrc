package lbms.azsmrc.remote.client;

public interface DownloadFileManager extends lbms.azsmrc.remote.client.plugins.download.DownloadFileManager {

	public boolean _isLoaded();

	public DownloadFile[] getFiles();
	public void update();
}
