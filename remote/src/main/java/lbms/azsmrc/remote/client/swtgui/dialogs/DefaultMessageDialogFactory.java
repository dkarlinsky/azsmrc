/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.swtgui.MessageDialogFactory;
import lbms.azsmrc.remote.client.swtgui.MessageService.MessageType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * @author Leonard
 * 
 */
public class DefaultMessageDialogFactory implements MessageDialogFactory {

	private Display	display;

	public DefaultMessageDialogFactory(Display display) {
		super();
		this.display = display;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lbms.azsmrc.remote.client.swtgui.MessageDialogFactory#displayMessageDialog
	 * (lbms.azsmrc.remote.client.swtgui.MessageService.MessageType,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void displayMessageDialog (MessageType type, String title,
			String text, String details) {
		int iconID;
		switch (type) {

		case Information:
			iconID = SWT.ICON_INFORMATION;
			break;

		case Warning:
			iconID = SWT.ICON_WARNING;
			break;

		case Error:
			iconID = SWT.ICON_ERROR;
			break;

		default:
			iconID = SWT.ICON_INFORMATION;
			break;
		}
		new MessageSlideShell(display, iconID, title, text, details);

	}

}
