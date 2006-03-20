package lbms.azsmrc.remote.client;

public interface DownloadFile {

	public String getName();
	public long getLength();
	public int getNumPieces();
	public long getDownloaded();
	public boolean getPriority();
	public boolean getSkipped();
	public int getIndex();
	public void setPriority(boolean priority);
	public void setSkipped(boolean skipped);
}
