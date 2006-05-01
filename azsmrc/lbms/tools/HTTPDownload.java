package lbms.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import lbms.tools.stats.StatsInputStream;

public class HTTPDownload extends Download  {

	private StringBuffer buffer;
	private String referer;
	private String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2";

	public HTTPDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	public HTTPDownload(URL source) {
		super(source);
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
			HttpURLConnection conn = (HttpURLConnection)source.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			conn.setDoInput(true);

			conn.addRequestProperty("Accept-Encoding","gzip, deflate");
			conn.addRequestProperty("User-Agent", userAgent);
			if (referer != null)
				conn.addRequestProperty("Referer", referer);

			callStateChanged(STATE_CONNECTING);
			conn.connect();

			StatsInputStream sis = new StatsInputStream (conn.getInputStream());
			is = sis;
			String encoding = conn.getHeaderField( "content-encoding");
			int contentLength = conn.getContentLength();

			boolean	gzip = encoding != null && (encoding.equalsIgnoreCase("gzip") || encoding.equalsIgnoreCase("x-gzip"));
			boolean	deflate = encoding != null && (encoding.equalsIgnoreCase("deflate") || encoding.equalsIgnoreCase("x-deflate"));

			if ( gzip ){
				is = new GZIPInputStream( is );
			} else if (deflate) {
				is = new InflaterInputStream(is);
			}
			callStateChanged(STATE_DOWNLOADING);

			if (target != null) {
				target.createNewFile();
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(target);
					int r = is.read();
					while (r!=-1) {
						if (abort) {
							os.close();
							is.close();
							callStateChanged(STATE_ABORTED);
							failed = true;
							failureReason = "Aborted by User";
							return this;
						}
						os.write((char)r);
						r = is.read();
						callProgress(sis.getBytesRead(), contentLength);
					}
				} finally {
					if (os != null)
						os.close();
				}
			} else {
				buffer = new StringBuffer((contentLength>0)?contentLength:4096);
				int r = is.read();
				while (r!=-1) {
					if (abort) {
						is.close();
						callStateChanged(STATE_ABORTED);
						failed = true;
						failureReason = "Aborted by User";
						return this;
					}
					buffer.append((char)r);
					r = is.read();
					callProgress(sis.getBytesRead(), contentLength);
				}
			}
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
	public StringBuffer getBuffer() {
		return buffer;
	}

	/**
	 * @return the referer or null
	 */
	public String getReferer() {
		return referer;
	}

	/**
	 * This needs to be set before the Download is executed
	 * 
	 * @param referer the referer to set
	 */
	public void setReferer(String referer) {
		this.referer = referer;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * This needs to be set before the Download is executed
	 * 
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}
