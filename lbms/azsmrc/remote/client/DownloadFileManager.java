package lbms.azsmrc.remote.client;

public interface DownloadFileManager {

	public boolean _isLoaded();

	public DownloadFile[] getFiles();
	public void update();
}
