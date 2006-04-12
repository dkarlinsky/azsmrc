package lbms.azsmrc.remote.client;

public interface RemoteUpdateManager {
	public RemoteUpdate[] getUpdates();
	public boolean updatesAvailable();
	public void load();
	public void applyUpdates();
}
