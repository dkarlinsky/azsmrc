/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

/**
 * @author Leonard
 * 
 */
public interface MessageDialogFactory {

	/**
	 * Displays a Message Dialog, it can eiter be an internal or external
	 * Dialog.
	 * 
	 * @param type Type of the message
	 * @param title the title of the message
	 * @param text the Main text of the message
	 * @param details details of the message, can be null, or not supported by
	 *            message dialog
	 */
	public void displayMessageDialog (MessageService.MessageType type,
			String title, String text, String details);
}
