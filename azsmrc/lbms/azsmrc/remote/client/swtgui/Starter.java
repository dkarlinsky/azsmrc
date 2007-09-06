package lbms.azsmrc.remote.client.swtgui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import lbms.azsmrc.remote.client.swing.CrashHandler;
import lbms.tools.launcher.Launchable;
import lbms.tools.updater.Version;

/**
 * @author Damokles
 *
 */
public class Starter implements Launchable {

	/* (non-Javadoc)
	 * @see lbms.tools.launcher.Launchable#launch(java.lang.String[])
	 */
	public boolean launch(String[] args) {
		try {
			return RCMain.start(args);
		} catch (Throwable e) {
			e.printStackTrace();
			File error = new File(System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "error.log");
			PrintStream fout = null;
			try {
				fout = new PrintStream(error);
				e.printStackTrace(fout);
				Throwable cause = e.getCause();
				while (cause != null) {
					cause.printStackTrace(fout);
					cause = cause.getCause();
				}
			} catch (FileNotFoundException e1) {
			} finally {
				if (fout != null) {
					fout.close();
				}
			}
			if (new Version(System.getProperty("java.version"))
					.compareTo("1.6.0") >= 0) {
				System.out.println("Starting CrashHandler...");
				new CrashHandler(e);
			} else {
				System.out.println("CrashHandler couldn't be loaded.\n"+System.getProperty("java.version"));
			}
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RCMain.start(args);
	}

}
