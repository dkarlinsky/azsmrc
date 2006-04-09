package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemoteInfo;

public class RemoteInfoImpl implements RemoteInfo {

	private Client client;
	private boolean loaded;
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
		this.azureusVersion = azureusVersion;
	}

	/**
	 * @param pluginVersion The pluginVersion to set.
	 */
	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	/**
	 * @param loaded The loaded to set.
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean load() {
		if (loaded) return true;

		return false;
	}
}
