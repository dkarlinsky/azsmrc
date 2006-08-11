package lbms.azsmrc.remote.client.plugins;


/**
 * @author Damokles
 *
 */
public interface AzSMRCInterface {

	public void popupMessage(String title, String message);

	public void popupWarning(String title, String message);

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

}
