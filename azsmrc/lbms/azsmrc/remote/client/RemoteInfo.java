package lbms.azsmrc.remote.client;

import java.util.List;
import java.util.Map;

public interface RemoteInfo {

	public String getAzureusVersion();
	public String getPluginVersion();
	public boolean load();
	public boolean refreshDriveInfo();
	public Map<String, String> getDriveInfo();

	public List<RemotePlugin> getRemotePlugins();
}
