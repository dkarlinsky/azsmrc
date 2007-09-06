package lbms.azsmrc.remote.client.swing;

import java.io.IOException;

import org.eclipse.swt.SWT;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.shared.RemoteConstants;

/**
 * @author Damokles
 *
 */
public class CrashHandler {

	private static final String PFX = "helper.";
	private ErrorReporter errorReporter;
	private String suggestion;

	public CrashHandler (final Throwable cause) {
		if (!I18N.isInitialized()) {
			I18N.setDefault("lbms/azsmrc/remote/client/internat/default.lang");
			try {
				I18N.reload();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		errorReporter = new ErrorReporter(cause);
		if (analyse(cause)) {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					new Helper(suggestion).setVisible(true);
				}
			});
		} else {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					new ErrorReporterDialog(errorReporter).setVisible(true);
				}
			});
		}
	}

	private boolean analyse (Throwable cause) {
		if (cause instanceof NoClassDefFoundError) {
			if (cause.getMessage().contains("swt")) {
				suggestion = I18N.translate(PFX+"missing.swt");
				return true;
			} else if (cause.getMessage().contains("log4j")) {
				suggestion = I18N.translate(PFX+"missing.log4j");
				return true;
			} else if (cause.getMessage().contains("commons")) {
				suggestion = I18N.translate(PFX+"missing.commons");
				return true;
			} else if (cause.getMessage().contains("jdom")) {
				suggestion = I18N.translate(PFX+"missing.jdom");
				return true;
			}
		} else if (cause instanceof UnsatisfiedLinkError) {
			if (cause.getMessage().contains("swt")) {
				try {
					String swtPlatform = SWT.getPlatform();
					if (RemoteConstants.isWindows && !swtPlatform.equalsIgnoreCase("win")) {
						suggestion = I18N.translate(PFX+"wrong.swt.win");
						return true;
					} else if (RemoteConstants.isLinux && !swtPlatform.equalsIgnoreCase("gtk")) {
						suggestion = I18N.translate(PFX+"wrong.swt.gtk");
						return true;
					} else if (RemoteConstants.isOSX && !swtPlatform.equalsIgnoreCase("carbon")) {
						suggestion = I18N.translate(PFX+"wrong.swt.carbon");
						return true;
					}
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
