package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.DownloadFile;
import lbms.azsmrc.remote.client.DownloadFileManager;

public class DownloadFileManagerImpl implements DownloadFileManager {

	private boolean isLoaded;
	private DownloadFileImpl[] dlFiles;
	private String hash;
	private Client client;

	public DownloadFileManagerImpl (String hash, Client client) {
		this.hash = hash;
		this.client = client;
		this.isLoaded = false;
	}

	public boolean _isLoaded() {
		return isLoaded;
	}

	public DownloadFile[] getFiles() {
		return dlFiles;
	}

	public DownloadFileImpl[] getFilesImpl() {
		return dlFiles;
	}

	public void update() {
		client.sendGetFiles(hash);
	}

	/**
	 * @param dlFiles The dlFiles to set.
	 */
	public void setDlFiles(DownloadFileImpl[] dlFiles) {
		this.dlFiles = dlFiles;
	}

	/**
	 * @param isLoaded The isLoaded to set.
	 */
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
}
