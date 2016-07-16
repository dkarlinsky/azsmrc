package lbms.azsmrc.remote.client.impl;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemotePlugin;

/**
 * @author Damokles
 *
 */
public class RemotePluginImpl implements RemotePlugin {

	private String dir, name, id, version;
	private boolean builtIn, mandatory, operational, unloadable, disabled;
	private Client client;

	public RemotePluginImpl (Client c) {
		this.client = c;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#getPluginDirectoryName()
	 */
	public String getPluginDirectoryName() {
		return dir;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#getPluginName()
	 */
	public String getPluginName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#getPluginID()
	 */
	public String getPluginID() {
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#getPluginVersion()
	 */
	public String getPluginVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#isBuiltIn()
	 */
	public boolean isBuiltIn() {
		return builtIn;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#isDisabled()
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#isMandatory()
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#isOperational()
	 */
	public boolean isOperational() {
		return operational;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#isUnloadable()
	 */
	public boolean isUnloadable() {
		return unloadable;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		if (!disabled && id.equalsIgnoreCase("azsmrc")) return; //don't kill yourself ^^
		client.sendSetPluginDisable(id, disabled);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.RemotePlugin#uninstall()
	 */
	public void uninstall() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param builtIn the builtIn to set
	 */
	public void implSetBuiltIn(boolean builtIn) {
		this.builtIn = builtIn;
	}

	/**
	 * @param dir the dir to set
	 */
	public void implSetDir(String dir) {
		this.dir = dir;
	}

	/**
	 * @param id the id to set
	 */
	public void implSetId(String id) {
		this.id = id;
	}

	/**
	 * @param mandatory the mandatory to set
	 */
	public void implSetMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @param name the name to set
	 */
	public void implSetName(String name) {
		this.name = name;
	}

	/**
	 * @param operational the operational to set
	 */
	public void implSetOperational(boolean operational) {
		this.operational = operational;
	}

	/**
	 * @param unloadable the unloadable to set
	 */
	public void implSetUnloadable(boolean unloadable) {
		this.unloadable = unloadable;
	}

	/**
	 * @param version the version to set
	 */
	public void implSetVersion(String version) {
		this.version = version;
	}

	/**
	 * @param disabled
	 */
	public void implSetDisabled (boolean disabled) {
		this.disabled = disabled;
	}

}
