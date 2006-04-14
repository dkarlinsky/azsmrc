package lbms.azsmrc.remote.client.impl;

import java.util.Map;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemoteInfo;

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
		return azureusVersion;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}

	/**
	 * @param azureusVersion The azureusVersion to set.
	 */
	public void setAzureusVersion(String azureusVersion) {
		load();
		this.azureusVersion = azureusVersion;
	}

	/**
	 * @param pluginVersion The pluginVersion to set.
	 */
	public void setPluginVersion(String pluginVersion) {
		load();
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
