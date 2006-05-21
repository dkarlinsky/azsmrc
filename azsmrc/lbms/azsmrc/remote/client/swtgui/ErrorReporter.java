/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;

import org.eclipse.swt.SWT;

/**
 * @author Damokles
 *
 */
public class ErrorReporter {

	public final static String REPORT_URL = "http://azsmrc.sourceforge.net/reportError.php";

	public String errorLog = "";
	public String email = "";
	public String additionalInfo = "";
	public String systemInfo = "";
	public boolean sendSystemInfo;

	public ErrorReporter () {
		init();
	}

	public void init() {
		gatherSystemInfo();
		readErrorLog();
	}

	public void gatherSystemInfo() {
		systemInfo  = "OS: "+System.getProperty("os.name")+"\n";
		systemInfo += "SWT Version: "+SWT.getVersion()+" "+SWT.getPlatform();
	}

	public void readErrorLog() {
		FileReader fr = null;
		File error = new File("error.log");
		try {
			if (error.exists() && error.isFile()) {
				CharBuffer cb = CharBuffer.allocate((int)error.length());
				 fr = new FileReader(error);
				fr.read(cb);
				errorLog = cb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {fr.close();} catch (IOException e1) {}
			}
			if (error.exists()) error.delete();
		}
	}

	public void sendToServer () {
		Thread t = new Thread (new Runnable() {
			public void run() {
				OutputStream os = null;
				try {
					URL url = new URL (REPORT_URL);
					URLConnection conn = url.openConnection();
					conn.setDoOutput(true);
					os = conn.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					String send = "error_log="+errorLog+"&email="+email+"&additional_info="+additionalInfo+"&system_info="+systemInfo;
					osw.write(send);
					osw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (os!=null) {
						try {os.close();} catch (IOException e) {}
					}
				}
			}
		});
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * @return the additionalInfo
	 */
	public String getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * @param additionalInfo the additionalInfo to set
	 */
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the errorLog
	 */
	public String getErrorLog() {
		return errorLog;
	}

	/**
	 * @return the systemInfo
	 */
	public String getSystemInfo() {
		return systemInfo;
	}


}
