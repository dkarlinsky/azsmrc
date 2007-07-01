/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;

import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.CryptoTools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;

/**
 * @author Damokles
 *
 */
public class ErrorReporter {

	public final static String REPORT_URL = "http://azsmrc.sourceforge.net/reportError2.php";

	private String stackTrace = "";
	private String email = "";
	private String additionalInfo = "";
	private String systemInfo = "";
	private String os;
	private String jvm;
	private String swtVer;
	private String azsmrcVersion;
	private String hash = "";
	private boolean init;

	private List<ErrorReporterListener> listener = new Vector<ErrorReporterListener>();

	public ErrorReporter () {
		init();
		readErrorLog();
	}

	public ErrorReporter (Throwable cause) {
		init();
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		while (cause != null) {
			cause.printStackTrace(writer);
			cause = cause.getCause();
		}
		stackTrace = buffer.toString();
		try {
			hash = CryptoTools.formatByte(CryptoTools.messageDigest(stackTrace.getBytes(), "SHA-1"),true);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void init() {
		if (init) return;
		gatherSystemInfo();
		init = true;
	}

	private void gatherSystemInfo() {
		os = System.getProperty("os.name");
		jvm = System.getProperty( "java.version" ) +" "+ System.getProperty( "java.vendor" );
		swtVer = SWT.getVersion()+" "+SWT.getPlatform();
		try {
			azsmrcVersion = RCMain.getRCMain().getAzsmrcProperties().getProperty("version");
		} catch (RuntimeException e) {
			azsmrcVersion = "unkown please add this in the additional info field.";
		}
		systemInfo  = "OS: "+os+"\n"
					+ "JVM: "+jvm +"\n"
		 			+ "SWT Version: "+swtVer+"\n"
		 			+ "AzSMRC Version: "+azsmrcVersion;
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
				stackTrace = sb.toString();
				hash = CryptoTools.formatByte(CryptoTools.messageDigest(stackTrace.getBytes(), "SHA-1"),true);
				System.out.println("StackTrace: "+stackTrace);
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
				boolean submitted = false;
				for (int i=0;i<3;i++) {
					OutputStream os = null;
					InputStream is = null;
					try {
						URL url = new URL (REPORT_URL);
						URLConnection conn = url.openConnection();
						conn.setDoOutput(true);
						conn.setDoInput(true);
						os = conn.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os);
						String send = "stacktrace="+stackTrace+"&hash="+hash+"&email="+email+"&additional_info="+additionalInfo+"&os="+os+"&jvm="+jvm+"&swt="+swtVer+"&version="+azsmrcVersion;
						System.out.println("Error Report: "+send);
						osw.write(send);
						osw.close();
						conn.connect();
						is = conn.getInputStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(is));

						if ("OK".equalsIgnoreCase(br.readLine())) {
							submitted = true;
							String line = br.readLine();
							if (line.startsWith("REDIRECT:")) {
								String redirectUrl = line.substring(10);
								for (ErrorReporterListener l:listener) {
									l.redirectTo(redirectUrl);
								}
							} else if (line.startsWith("TEXT:")) {
								for (ErrorReporterListener l:listener) {
									l.showText("");
								}
							}

							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						if (os!=null) {
							try {os.close();} catch (IOException e) {}
						}
						if (is!=null) {
							try {is.close();} catch (IOException e) {}
						}
					}
				}
				for (ErrorReporterListener l:listener) {
					l.errorSubmitted(submitted);
				}
			}
		});
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

		for (ErrorReporterListener l:listener) {
			l.redirectTo("http://damo.ath.cx");
			l.errorSubmitted(false);
		}
	}

	public void sendPerEMail () {
		try {
			Program.launch(("mailto:azsmrc-devs@list.sourceforge.net?subject=AzSMRC+ErrorReport&body="+URLEncoder.encode("System Info:\n"+systemInfo+"\n\nAdditional Info:\n"+additionalInfo+"\n\nStackTrace ("+hash+"):\n"+stackTrace,RemoteConstants.DEFAULT_ENCODING)).replace('+', ' '));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String getFormattedReport() {
		return "System Info:\n"+systemInfo+"\n\nAdditional Info:\n"+additionalInfo+"\n\nStackTrace ("+hash+"):\n"+stackTrace;
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
	public String getStackTrace() {
		return stackTrace;
	}

	/**
	 * @return the systemInfo
	 */
	public String getSystemInfo() {
		return systemInfo;
	}

	/**
	 * @return the hash
	 */
	public String getErrorID() {
		return hash;
	}

	public void addListener (ErrorReporterListener l) {
		listener.add(l);
	}

	public void removeListener (ErrorReporterListener l) {
		listener.remove(l);
	}
}
