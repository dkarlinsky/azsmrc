package lbms.tools.updater;

import lbms.tools.Download;

public interface UpdateListener {

	/**
	 * This will be called to pass Exceptions to the Listener
	 * 
	 * @param e
	 */
	public void exception(Exception e);

	/**
	 * This will be called if an Update is available
	 * 
	 * @param update The new Update
	 */
	public void updateAvailable(Update update);

	/**
	 * This will be called if the check completes and
	 * no Update is available.
	 */
	public void noUpdate();

	/**
	 * This will be called if an Update finishes successfully.
	 */
	public void updateFinished();

	/**
	 * @param error
	 */
	public void updateError (String error);

	/**
	 * This will be called if the Update Fails
	 * 
	 * @param reason The reason of Failure
	 */
	public void updateFailed(String reason);

	/**
	 * This will be called after doUpdate() is called
	 * 
	 * It will provide access to the Downloads so you can
	 * attach Listener to them.
	 * 
	 * @param dls Downloads of the Update
	 */
	public void initializeUpdate (Download[] dls);

}
