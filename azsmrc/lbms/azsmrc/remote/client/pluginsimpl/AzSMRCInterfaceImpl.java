package lbms.azsmrc.remote.client.pluginsimpl;

import java.io.File;

import org.eclipse.swt.SWT;

import lbms.azsmrc.remote.client.plugins.AzSMRCInterface;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageSlideShell;
import lbms.azsmrc.remote.client.swtgui.dialogs.ScrapeDialog;

/**
 * @author Damokles
 *
 */
public class AzSMRCInterfaceImpl implements AzSMRCInterface {

	private RCMain rcMain;

	public AzSMRCInterfaceImpl (RCMain rcMain) {
		this.rcMain = rcMain;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#popupError(java.lang.String, java.lang.String)
	 */
	public void popupError(String title, String message) {
		if (rcMain.getDisplay() != null) {
			//MessageDialog.error(rcMain.getDisplay(), title, message);
			new MessageSlideShell(rcMain.getDisplay(),SWT.ICON_ERROR,title,message,(String)null);
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#popupMessage(java.lang.String, java.lang.String)
	 */
	public void popupMessage(String title, String message) {
		if (rcMain.getDisplay() != null) {
			//MessageDialog.message(rcMain.getDisplay(), title, message);
			new MessageSlideShell(rcMain.getDisplay(),SWT.ICON_INFORMATION,title,message,(String)null);
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#popupWarning(java.lang.String, java.lang.String)
	 */
	public void popupWarning(String title, String message) {
		if (rcMain.getDisplay() != null) {
			//MessageDialog.warning(rcMain.getDisplay(), title, message);
			new MessageSlideShell(rcMain.getDisplay(),SWT.ICON_WARNING,title,message,(String)null);
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#setStatusBarText(java.lang.String)
	 */
	public void setStatusBarText(String text) {
		if (rcMain.getMainWindow() != null) {
			rcMain.getMainWindow().setStatusBarText(text);
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#setStatusBarText(java.lang.String, int)
	 */
	public void setStatusBarText(String text, int color) {
		if (rcMain.getMainWindow() != null) {
			rcMain.getMainWindow().setStatusBarText(text,color);
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.AzSMRCInterface#getCurrentUsername()
	 */
	public String getCurrentUsername() {
		return rcMain.getClient().getUsername();
	}

	public void scrapeTorrent(File file) {
		ScrapeDialog.openFileAndScrape(file);
	}

	public void scrapeTorrent(String url) {
		ScrapeDialog.openURLandScrape(url);
	}
}
