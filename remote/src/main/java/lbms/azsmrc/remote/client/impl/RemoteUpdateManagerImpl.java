package lbms.azsmrc.remote.client.impl;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemoteUpdate;
import lbms.azsmrc.remote.client.RemoteUpdateManager;

public class RemoteUpdateManagerImpl implements RemoteUpdateManager {

	boolean updatesAvailable;
	List<RemoteUpdateImpl> updates = new ArrayList<RemoteUpdateImpl>();
	Client client;

	public RemoteUpdateManagerImpl (Client c) {
		client = c;
	}

	public boolean updatesAvailable() {
		return updatesAvailable;
	}

	public RemoteUpdate[] getUpdates() {
		return updates.toArray(new RemoteUpdate[] {});
	}

	public void load() {
		client.sendGetUpdateInfo();
	}

	public void applyUpdates(String[] u) {
		client.sendApplyUpdates(u);
	}

	/**
	 * @param updatesAvailable The updatesAvailable to set.
	 */
	public void setUpdatesAvailable(boolean updatesAvailable) {
		this.updatesAvailable = updatesAvailable;
	}

	public void clear() {
		updates.clear();
	}

	public void addUpdate (RemoteUpdateImpl u) {
		updates.add(u);
	}
}
