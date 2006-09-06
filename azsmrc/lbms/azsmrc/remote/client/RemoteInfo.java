package lbms.azsmrc.remote.client;

import java.util.Map;

import org.jdom.Element;

public interface RemoteInfo {

	public String getAzureusVersion();
	public String getPluginVersion();
	public boolean load();
	public boolean refreshDriveInfo();
	public Map<String, String> getDriveInfo();

	public RemotePlugin[] getRemotePlugins();
	public Element getPluginsFlexyConf();
}
