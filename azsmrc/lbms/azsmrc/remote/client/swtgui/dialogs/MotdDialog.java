package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.net.URL;

import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.HTTPDownload;

/**
 * @author Damokles
 *
 */
public class MotdDialog {

	private String motd = "No Data";

	private MotdDialog (String msg) {
		motd = msg;
		//TODO: Marc open MOTD dialog window here
	}

	public static void open() {
		Thread t = new Thread () {
			public void run() {
				try {
					HTTPDownload dl = new HTTPDownload (new URL(RemoteConstants.MOTD_URL));
					dl.run();
					if (!dl.hasFailed()) {
						new MotdDialog(dl.getBuffer().toString("UTF-8"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
	}
}
