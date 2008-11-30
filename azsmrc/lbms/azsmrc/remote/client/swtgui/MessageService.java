/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

/**
 * @author Leonard
 * 
 */
public class MessageService {

	private static MessageDialogFactory	dialogFactory;

	public static void displayInformation (String title, String message) {
		displayMessageDialog(MessageType.Information, title, message, null);
	}

	public static void displayInformation (String title, String message,
			String details) {
		displayMessageDialog(MessageType.Information, title, message, details);
	}

	public static void displayWarning (String title, String message) {
		displayMessageDialog(MessageType.Warning, title, message, null);
	}

	public static void displayWarning (String title, String message,
			String details) {
		displayMessageDialog(MessageType.Warning, title, message, details);
	}

	public static void displayError (String title, String message) {
		displayMessageDialog(MessageType.Error, title, message, null);
	}

	public static void displayError (String title, String message,
			String details) {
		displayMessageDialog(MessageType.Error, title, message, details);
	}

	private static void displayMessageDialog (MessageType type, String title,
			String text, String details) {
		if (checkPopupsEnabled()) {
			dialogFactory.displayMessageDialog(type, title, text, details);
		}
	}

	private static boolean checkPopupsEnabled () {
		return RCMain.getRCMain().getProperties().getPropertyAsBoolean(
				"popups_enabled", true);
	}

	public static void setMessageDialogFactory (MessageDialogFactory factory) {
		dialogFactory = factory;
	}

	public static enum MessageType {
		Information, Warning, Error
	}
}
