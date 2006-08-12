package lbms.azsmrc.remote.client.plugins;


/**
 * @author Damokles
 *
 */
public interface AzSMRCInterface {

	/**
	 * Opens a Message Popup.
	 * 
	 * @param title
	 * @param message
	 */
	public void popupMessage(String title, String message);

	/**
	 * Opens a Warning Popup.
	 * 
	 * @param title
	 * @param message
	 */
	public void popupWarning(String title, String message);

	/**
	 * Opens an Error Popup.
	 * 
	 * @param title
	 * @param message
	 */
	public void popupError(String title, String message);

	/**
	 * sets the status bar text alert area given String text and with the color black
	 * @param text
	 */
	public void setStatusBarText (String text);

	/**
	 * sets the status bar text alert area given String text and SWT.COLOR_*
	 * @param text
	 * @param color
	 */
	public void setStatusBarText (String text, int color);

	/**
	 * @return username or null if no user is active
	 */
	public String getCurrentUsername();

}
