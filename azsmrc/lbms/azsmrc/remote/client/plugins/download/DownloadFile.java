package lbms.azsmrc.remote.client.plugins.download;

public interface DownloadFile {

	public String getName();
	public long getLength();
	public int getNumPieces();
	public long getDownloaded();
	public boolean getPriority();
	public boolean getSkipped();
	public boolean getDeleted();
	public int getIndex();
	public void setPriority(boolean priority);
	public void setSkipped(boolean skipped);
	public void setDeleted(boolean skipped);
}
