/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;

import lbms.azsmrc.remote.client.RemoteInfo;

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
	public boolean init;

	public ErrorReporter () {
		init();
	}

	public void init() {
		if (init) return;
		gatherSystemInfo();
		readErrorLog();
		init = true;
	}

	private void gatherSystemInfo() {
		RemoteInfo rInfo = RCMain.getRCMain().getClient().getRemoteInfo();
		systemInfo  = "OS: "+System.getProperty("os.name")+"\n"
					+ "JVM: "+System.getProperty( "java.version" ) +" "+ System.getProperty( "java.vendor" ) +"\n"
		 			+ "SWT Version: "+SWT.getVersion()+" "+SWT.getPlatform()+"\n"
		 			+ "AzSMRC Version: "+RCMain.getRCMain().getAzsmrcProperties().getProperty("version")+"\n"
		 			+ "AzSMRC Plugin: "+rInfo.getPluginVersion()+"\n"
		 			+ "Azureus Version: "+rInfo.getAzureusVersion();
	}

	private void readErrorLog() {
		FileInputStream fis = null;
		File error = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+"error.log");
		try {
			if (error.exists() && error.isFile()) {
				StringBuffer sb = new StringBuffer((int)error.length());
				fis = new FileInputStream(error);
				for (int i=0;-1!=(i=fis.read());) {
					sb.append((char)i);
				}
				errorLog = sb.toString();
				System.out.println("Errorlog: "+errorLog);
			} else {
				System.out.println(error+" did not exist");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {fis.close();} catch (IOException e1) {}
			}
			if (error.exists()) error.delete();
		}
	}

	/**
	 * This will transmit the Error report to the sf.net site
	 */
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
					System.out.println("Error Report: "+send);
					osw.write(send);
					osw.close();
					conn.connect();
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
