package lbms.azsmrc.remote.client.impl;

import org.jdom.Element;

import lbms.azsmrc.remote.client.RemoteUpdate;

public class RemoteUpdateImpl implements RemoteUpdate {

	private String name;
	private String newVersion;
	private boolean mandatory;

	public RemoteUpdateImpl (Element update) {
		name = update.getAttributeValue("name");
		newVersion = update.getAttributeValue("newVersion");
		mandatory = Boolean.getBoolean(update.getAttributeValue("isMandatory"));
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public String getNewVersion() {
		// TODO Auto-generated method stub
		return newVersion;
	}

	public boolean isMandatory() {
		// TODO Auto-generated method stub
		return mandatory;
	}

	/**
	 * @param mandatory The mandatory to set.
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param newVersion The newVersion to set.
	 */
	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}
}
