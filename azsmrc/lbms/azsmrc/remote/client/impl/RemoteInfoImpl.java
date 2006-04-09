package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemoteInfo;

public class RemoteInfoImpl implements RemoteInfo {

	private Client client;
	private boolean loaded, loading;
	private String azureusVersion = "";
	private String pluginVersion = "";

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

	public boolean load() {
		if (loaded) return true;
		if (!loading) {
			loading = true;
			client.sendGetRemoteInfo();
		}
		return false;
	}
}
