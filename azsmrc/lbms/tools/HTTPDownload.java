package lbms.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import lbms.tools.stats.StatsInputStream;

public class HTTPDownload extends Download  {

	private static final int BUFFER_SIZE = 1024;

	private ByteArrayOutputStream buffer;

	public HTTPDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	public HTTPDownload(URL source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public HTTPDownload(Download d) {
		super(d);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		try {
			call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Download call() throws Exception {
		InputStream is = null;
		try {
			HttpURLConnection conn = null;

			if ( source.getProtocol().equalsIgnoreCase("https")){

				// see ConfigurationChecker for SSL client defaults
				HttpsURLConnection ssl_con;
				if (proxy != null)
					ssl_con = (HttpsURLConnection)source.openConnection(proxy);
				else
					ssl_con = (HttpsURLConnection)source.openConnection();
				// allow for certs that contain IP addresses rather than dns names

				ssl_con.setHostnameVerifier(
						new HostnameVerifier()
						{
							public boolean
							verify(
									String		host,
									SSLSession	session )
							{
								return( true );
							}
						});

				conn = ssl_con;
			} else {
				if (proxy != null)
					conn = (HttpURLConnection)source.openConnection(proxy);
				else
					conn = (HttpURLConnection)source.openConnection();
			}
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			conn.setDoInput(true);

			conn.addRequestProperty("Accept-Encoding","gzip, x-gzip, deflate, x-deflate");
			conn.addRequestProperty("User-Agent", userAgent);
			if (referer != null)
				conn.addRequestProperty("Referer", referer);

			if (cookie != null)
				conn.addRequestProperty("Cookie", cookie);

			if (login != null)
				conn.setRequestProperty("Authorization", "Basic: "+login);

			callStateChanged(STATE_CONNECTING);
			conn.connect();

			int response = conn.getResponseCode();

			//connection failed
			if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
				callStateChanged(STATE_FAILURE);
				failed = true;
				failureReason = conn.getResponseMessage();
				return this;
			}


			StatsInputStream sis = new StatsInputStream (conn.getInputStream());
			is = sis;
			String encoding = conn.getHeaderField( "content-encoding");
			int contentLength = conn.getContentLength();
			if (conn.getHeaderField("cookie") != null ) {
				this.cookie = conn.getHeaderField("cookie");
			}

			boolean	gzip = encoding != null && (encoding.equalsIgnoreCase("gzip") || encoding.equalsIgnoreCase("x-gzip"));
			boolean	deflate = encoding != null && (encoding.equalsIgnoreCase("deflate") || encoding.equalsIgnoreCase("x-deflate"));

			if ( gzip ){
				is = new GZIPInputStream( is );
			} else if (deflate) {
				is = new InflaterInputStream(is);
			}
			callStateChanged(STATE_DOWNLOADING);
			long last = System.currentTimeMillis();
			long now;

			byte[] buf = new byte[BUFFER_SIZE];
			if (target != null) {
				target.createNewFile();
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(target);
					for (int read=is.read(buf);read>0;read=is.read(buf)) {
						if (abort) {
							os.close();
							is.close();
							callStateChanged(STATE_ABORTED);
							failed = true;
							failureReason = "Aborted by User";
							return this;
						}
						os.write(buf,0,read);
						now = System.currentTimeMillis();
						if (now-last>=500) {
							callProgress(sis.getBytesRead(), contentLength);
							last = now;
						}
					}
				} finally {
					if (os != null)
						os.close();
				}
			} else {

				buffer = (contentLength > 0 && contentLength < 5242880) ? new ByteArrayOutputStream(contentLength):new ByteArrayOutputStream();
				for (int read=is.read(buf);read>0;read=is.read(buf)) {
					if (abort) {
						is.close();
						callStateChanged(STATE_ABORTED);
						failed = true;
						failureReason = "Aborted by User";
						return this;
					}
					buffer.write(buf, 0, read);
					now = System.currentTimeMillis();
					if (now-last>=500) {
						callProgress(sis.getBytesRead(), contentLength);
						last = now;
					}
				}
			}
			//finally call again
			callProgress(sis.getBytesRead(), contentLength);
			if (contentLength>0 && target != null && !(gzip || deflate) && target.length() != contentLength) {
				failed = true;
				callStateChanged(STATE_FAILURE);
			}
			else {
				finished = true;
				callStateChanged(STATE_FINISHED);
			}
		} catch (IOException e) {
			callStateChanged(STATE_FAILURE);
			failed = true;
			failureReason = e.getMessage();
			e.printStackTrace();
			throw e;
		} finally {
			if (is!= null)
				try {
					is.close();
				} catch (IOException e) {}
		}
		return this;
	}

	/**
	 * @return Returns the buffer.
	 */
	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}
}
