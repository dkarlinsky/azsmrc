package lbms.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import lbms.tools.stats.StatsInputStream;

/**
 * @author Damokles
 *
 */
public class TorrentDownload extends Download {

	public static final int RTC_ERROR = -1;
	public static final int RTC_INIT = 0;
	public static final int RTC_FILE = 1;
	public static final int RTC_BUFFER = 2;
	public static final int RTC_MAGNET = 3;

	private static final int BUFFER_SIZE = 1024;

	private ByteArrayOutputStream buffer;

	private static final String contentType = "application/x-bittorrent";

	private static Pattern hrefPattern = Pattern.compile("href\\s*=\\s*(?:\"|')([\\w:/.?&-=%\\[\\]{}\\(\\) ]+)(?:\"|')",Pattern.CASE_INSENSITIVE);

	private static Pattern torrentHrefPattern = Pattern.compile("href\\s*=\\s*(?:\"|')([\\w:/.?&-=%\\[\\]{}\\(\\) ]+\\.torrent)(?:\"|')",Pattern.CASE_INSENSITIVE);
	private static Pattern magnetPattern = Pattern.compile("magnet:\\?xt=urn:btih:[A-Za-z2-7]{32}", Pattern.CASE_INSENSITIVE);

	private static Pattern torrentDataPattern = Pattern.compile("^d[0-9]+:.*",Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

	private String torrentLinkIdentifier;

	private int returnCode = RTC_INIT;

	private String magnetURL;

	/**
	 *
	 */
	public TorrentDownload() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param source
	 */
	public TorrentDownload(URL source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param source
	 * @param target
	 */
	public TorrentDownload(URL source, File target) {
		super(source, target);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param d
	 */
	public TorrentDownload(Download d) {
		super(d);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			call();
		} catch (Exception e) {
			debugMsg(e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Download call() throws Exception {
		InputStream is = null;
		try {
			debugMsg("TorrentDownloader: starting Download ["+source+"]");
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

			conn.addRequestProperty("Accept-Encoding","gzip, deflate");
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
				failureReason = conn.getResponseMessage() != null ?
						conn.getResponseMessage() : "Connection Failed unknown problem. ["+response+"]";
				callStateChanged(STATE_FAILURE);
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

			buffer = (contentLength > 0 && contentLength < 5242880) ? new ByteArrayOutputStream(contentLength):new ByteArrayOutputStream(524288);
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
			is.close();
			conn.disconnect();
			//finally call again
			callProgress(sis.getBytesRead(), contentLength);
			if (contentLength>0 && !(gzip || deflate) && buffer.size() != contentLength) {
				failed = true;
				failureReason = "Content length doesn't Match.";
				callStateChanged(STATE_FAILURE);
			}
			else {
				if (isTorrent(buffer.toByteArray())) {
					if (target != null) {
						target.createNewFile();
						FileOutputStream os = null;
						try {
							os = new FileOutputStream(target);
							buffer.writeTo(os);
						} finally {
							if (os != null)
								os.close();
						}
						returnCode = RTC_FILE;
					} else {
						returnCode = RTC_BUFFER;
					}
					debugMsg("TorrentDownloader: Download Successful ["+source+"]");
					finished = true;
					callStateChanged(STATE_FINISHED);
				} else {
					//The downloaded Data was not a torrent
					//assume that it was html

					debugMsg("TorrentDownloader: parsing HTML for link ["+source+"]");
					try {
						String html = buffer.toString("UTF-8");
						Matcher tor = torrentHrefPattern.matcher(html);
						if (tor.find()) {
							try {
								String torlink = tor.group(1);
								//set new source and download again
								URL torURL = resolveRelativeURL(source, torlink);

								debugMsg("TorrentDownloader: found Torrent Link ["+torURL+"]");
								if (isHrefTorrent(torURL)) {
									source = torURL;
									callStateChanged(STATE_RESET);
									return call();
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
						Matcher magnet = magnetPattern.matcher(html);
						if (magnet.find()) {
							magnetURL = magnet.group();
							returnCode = RTC_MAGNET;
							finished = true;
							debugMsg("TorrentDownloader: found Magnet Link ["+magnetURL+"] from ["+source+"]");
							callStateChanged(STATE_FINISHED);
							return this;
						}

						if (torrentLinkIdentifier != null) {
							debugMsg("TorrentDownloader: trying to use torrentLinkIdentifier ["+torrentLinkIdentifier+"]");
							Matcher m = Pattern.compile("href\\s*=\\s*(?:\"|')([\\w:/.?&-=%\\[\\]{}\\(\\) ]*"+
									Pattern.quote(torrentLinkIdentifier)+"[\\w:/.?&-=%\\[\\]{}\\(\\) ]*)(?:\"|')", Pattern.CASE_INSENSITIVE).matcher(html);
							if (m.find()) {
								try {
									String torlink = m.group(1);
									//set new source and download again
									URL torURL = resolveRelativeURL(source, torlink);
									debugMsg("TorrentDownloader: found Torrent Link ["+torURL+"]");
									if (isHrefTorrent(torURL)) {
										source = torURL;
										callStateChanged(STATE_RESET);
										return call();
									}
								} catch (MalformedURLException e) {
									e.printStackTrace();
								}
							} else {
								debugMsg("TorrentDownloader: failed to find torrent by torrentLinkIdentifier ["+torrentLinkIdentifier+"]");
							}

						}

						//no direct torrent or magnet link was found
						debugMsg("TorrentDownloader: parsing all links for torrent ["+source+"]");
						Matcher links = hrefPattern.matcher(html);
						while(links.find()) {
							try {
								URL torURL = resolveRelativeURL(source, links.group(1));
								if (isHrefTorrent(torURL)) {
									setTorrentLinkIdentifier(links.group(1));
									source = torURL;
									callStateChanged(STATE_RESET);
									return call();
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
						debugMsg("TorrentDownloader: failed to find torrent Link ["+source+"]");

						failed = true;
						failureReason = "No Torrent Data/Link found";
						callStateChanged(STATE_FAILURE);

					} catch (Exception e) {
						e.printStackTrace();
						failed = true;
						failureReason = e.getMessage();
						callStateChanged(STATE_FAILURE);
					}
				}
			}
		} catch (IOException e) {
			failed = true;
			failureReason = e.getMessage();
			callStateChanged(STATE_FAILURE);
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

	private boolean isHrefTorrent(URL target) {
		try {
			HttpURLConnection conn;
			if ( target.getProtocol().equalsIgnoreCase("https")){

				// see ConfigurationChecker for SSL client defaults
				HttpsURLConnection ssl_con;
				if (proxy != null)
					ssl_con = (HttpsURLConnection)target.openConnection(proxy);
				else
					ssl_con = (HttpsURLConnection)target.openConnection();
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
					conn = (HttpURLConnection)target.openConnection(proxy);
				else
					conn = (HttpURLConnection)target.openConnection();
			}

			conn.setRequestMethod("HEAD");
			conn.connect();
			String ct = conn.getContentType();
			conn.disconnect();
			if(ct != null) {
				return ct.toLowerCase().startsWith(contentType);
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected static URL resolveRelativeURL(URL u, String href) throws MalformedURLException {
		/*String newUrl = u.getProtocol() + "://" + u.getHost();
		if(u.getPort() > 0) newUrl += ":" + u.getPort();
		if(!href.startsWith("/")) { // path relative to current
			String path = u.getPath(); // e.g /dir/file.php
			if(path.indexOf("/") > -1) path = path.substring(0, path.lastIndexOf("/") + 1); // strip file part
			newUrl += path; // append /dir
			if(!newUrl.endsWith("/")) newUrl += "/";
		}*/
		return new URL(u, href);
	}

	/**
	 * @return Returns the buffer.
	 */
	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}

	public boolean isTorrent (byte[] x) {
		//we only need the 10 fist chars
		byte[] y = new byte[10];
		System.arraycopy(x, 0, y, 0, 10);

		try {
			if (torrentDataPattern.matcher(new String (y,"UTF-8")).find()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean isTorrent (File x) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(x);

			//we only need the 10 fist chars
			byte[] y = new byte[10];
			fis.read(y);

			if (torrentDataPattern.matcher(new String (y,"UTF-8")).find()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {}
		}

		return false;
	}

	/**
	 * @return the magnetURL
	 */
	public String getMagnetURL() {
		return magnetURL;
	}

	/**
	 * @return the RTC_* returnCode
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * @return the torrentLinkIdentifier
	 */
	public String getTorrentLinkIdentifier() {
		return torrentLinkIdentifier;
	}

	public void setTorrentLinkIdentifier(String tor) {
		if (torrentLinkIdentifier == null) {
			torrentLinkIdentifier = tor;
		}
		else {
			if (torrentLinkIdentifier.equalsIgnoreCase(tor)) return;
			int i = 0;
			for (;i<tor.length() && i<torrentLinkIdentifier.length() && tor.charAt(i) == torrentLinkIdentifier.charAt(i);i++);
			if (i==0) {
				torrentLinkIdentifier = null;
			} else {
				torrentLinkIdentifier = torrentLinkIdentifier.substring(0, i);
				debugMsg("TorrentDownloader: improved torrentLinkIdentifier: "+torrentLinkIdentifier);
			}
		}
	}

}
