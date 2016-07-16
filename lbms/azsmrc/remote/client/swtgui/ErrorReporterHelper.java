package lbms.azsmrc.remote.client.swtgui;


/**
 * @author Damokles
 *
 */
public class ErrorReporterHelper {

	public static void sendPerEMail (String mail) {
		try {
			org.eclipse.swt.program.Program.launch(mail);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSWTVersion () {
		try {
			return org.eclipse.swt.SWT.getVersion()+" "+org.eclipse.swt.SWT.getPlatform();
		} catch (Exception e) {
			return "SWT not found";
		}
	}

}
