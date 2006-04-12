package lbms.azsmrc.remote.client;

public interface RemoteUpdate {
	public String getName();
	public String getNewVersion();
	public boolean isMandatory();
}
