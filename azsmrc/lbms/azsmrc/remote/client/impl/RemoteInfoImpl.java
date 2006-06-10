package lbms.azsmrc.remote.client.impl;

import java.util.Map;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemoteInfo;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageDialog;
import lbms.tools.updater.Version;

public class RemoteInfoImpl implements RemoteInfo {

	private Client client;
	private boolean loaded, loading;
	private String azureusVersion = "";
	private String pluginVersion = "";
	private Map<String, String> driveInfo;

	public RemoteInfoImpl (Client c) {
		client = c;
	}

	public String getAzureusVersion() {
		load();
		return azureusVersion;
	}

	public String getPluginVersion() {
		load();
		return pluginVersion;
	}

	/**
	 * @param azureusVersion The azureusVersion to set.
	 */
	public void setAzureusVersion(String azureusVersion) {
		this.azureusVersion = azureusVersion;
	}

	/**
	 * @param pluginVersion The pluginVersion to set.
	 */
	public void setPluginVersion(String pluginVersion) {
		Version min = new Version(RCMain.getRCMain().getAzsmrcProperties().getProperty("minPluginVersion"));
		Version plugin = new Version (pluginVersion);
		if (min.compareTo(plugin)>0) MessageDialog.warning(RCMain.getRCMain().getDisplay(), I18N.translate("popup.minversion.title"), I18N.translate("popup.minversion.text")+min);
		this.pluginVersion = pluginVersion;
	}

	/**
	 * @param loaded The loaded to set.
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		loading = false;
	}

	/**
	 * @param loading The loading to set.
	 */
	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	/**
	 * @param driveInfo The driveInfo to set.
	 */
	public void setDriveInfo(Map<String, String> driveInfo) {
		this.driveInfo = driveInfo;
	}

	public boolean load() {
		if (loaded) return true;
		if (!loading) {
			refreshDriveInfo();
			loading = true;
			client.sendGetRemoteInfo();
		}
		return false;
	}

	public Map<String, String> getDriveInfo() {
		return driveInfo;
	}

	public boolean refreshDriveInfo() {
		if (loading) return false;
		loading = true;
		client.sendGetDriveInfo();
		return true;
	}
}
