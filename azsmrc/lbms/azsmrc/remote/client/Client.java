package lbms.azsmrc.remote.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import lbms.azsmrc.remote.client.events.ClientEventListener;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.events.ConnectionListener;
import lbms.azsmrc.remote.client.events.ExceptionListener;
import lbms.azsmrc.remote.client.events.HTTPErrorListener;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.events.SpeedUpdateListener;
import lbms.azsmrc.remote.client.impl.DownloadManagerImpl;
import lbms.azsmrc.remote.client.impl.RemoteInfoImpl;
import lbms.azsmrc.remote.client.impl.RemoteUpdateManagerImpl;
import lbms.azsmrc.remote.client.impl.UserManagerImpl;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.Timer;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.stats.StatsInputStream;
import lbms.tools.stats.StatsOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Client {

	private static final long TRANSACTION_TIMEOUT = 1500;

	private Semaphore semaphore = new Semaphore(1);
	private boolean transaction, ssl, fastMode;
	private URL server;
	private String username;
	private String password;
	private Queue<Element> transactionQueue = new ConcurrentLinkedQueue<Element>();
	private TimerEvent transactionTimeout;
	private Timer timer;
	private int failedConnections;
	private Logger debug;

	private DownloadManagerImpl downloadManager;
	private UserManagerImpl userManager;
	private ResponseManager responseManager;
	private RemoteInfoImpl remoteInfo;
	private RemoteUpdateManagerImpl remoteUpdateManager;


	//use Vector of Collections.synchronizedList here
	private List<ClientUpdateListener> 	clientUpdateListeners	= new Vector<ClientUpdateListener>();
	private List<SpeedUpdateListener> 	speedUpdateListners		= new Vector<SpeedUpdateListener>();
	private List<ExceptionListener> 	exceptionListeners		= new Vector<ExceptionListener>();
	private List<ConnectionListener> 	connectionListeners		= new Vector<ConnectionListener>();
	private List<ClientEventListener> 	eventListeners			= new Vector<ClientEventListener>();
	private List<HTTPErrorListener> 	httpErrorListeners		= new Vector<HTTPErrorListener>();
	private List<ParameterListener>		parameterListeners		= new Vector<ParameterListener>();

	public Client () {
		init();
	}


	public Client(URL server) {
		this.server = server;
		init();
	}

	private void init() {
		downloadManager = new DownloadManagerImpl();
		responseManager = new ResponseManager(this);
		userManager 	= new UserManagerImpl(this);
		remoteInfo		= new RemoteInfoImpl(this);
		remoteUpdateManager = new RemoteUpdateManagerImpl (this);
		failedConnections 	= 0;
		timer 	= new Timer("Client Timer",1);
		ssl 	= false;
		fastMode = false;
		debug = Logger.getLogger("lbms.azsmrc.client");
		debug.addHandler(new Handler() {
			private Formatter sF = new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage();
				}
			};
			@Override
			public void close() throws SecurityException {}
			@Override
			public void flush() {}
			@Override
			public void publish(LogRecord record) {
				System.out.println(sF.format(record));
			}
		});
	}

	private void reset() {
		userManager 	= new UserManagerImpl(this);
		remoteInfo		= new RemoteInfoImpl(this);
		downloadManager.clear();
		failedConnections 	= 0;
	}

	public void transactionStart() {
		transaction = true;
		debug.fine("Transaction Started");
		transactionTimeout = timer.addEvent(System.currentTimeMillis()+TRANSACTION_TIMEOUT, new TimerEventPerformer() {
			public void perform(TimerEvent event) {
				debug.warning("Transaction Committed by Timeout.");
				transactionCommit();
			}
		});
	}

	public void transactionCommit() {
		if (transactionTimeout != null) transactionTimeout.cancel();
		transaction = false;
		debug.fine("Transaction Committed ("+transactionQueue.size()+" items)");
		send();
	}

	private void send() {
		if (transaction) return;

		new Thread(new Runnable() {
			public void run() {
				if (!semaphore.tryAcquire()) {
					debug.finer("Client connection already established, postponed transfer.");
					return;
				}
				Document reqDoc = new Document();

				Element request = new Element("Request");
				request.setAttribute("version", Double.toString(RemoteConstants.CURRENT_VERSION));
				reqDoc.addContent(request);

				Element statElement = getSendElement();
				statElement.setAttribute("switch", "globalStats");
				request.addContent(statElement);

				Element item = transactionQueue.poll();
				while (item != null) {
					request.addContent(item);
					item = transactionQueue.poll();
				}
				sendHttpRequest(reqDoc);
				semaphore.release();
				//if new elements are in the queue and fastmode is on send again
				if (fastMode && transactionQueue.peek()!=null) {
					debug.fine("FastMode Send");
					send();
				}
			}
		}).start();
	}


	private void sendHttpRequest(Document req) {
		if (server == null) return;
		InputStream is = null;
		GZIPOutputStream gos = null;
		HttpURLConnection connection = null;
		try {
			for (int i=0;i<2;i++)
				try {

					connection = (HttpURLConnection)server.openConnection();
					if ( server.getProtocol().equalsIgnoreCase("https")){
						ssl = true;
						//see ConfigurationChecker for SSL client defaults
						HttpsURLConnection ssl_connection = (HttpsURLConnection)connection;
						// allow for certs that contain IP addresses rather than dns names
						ssl_connection.setHostnameVerifier( new HostnameVerifier() {
							public boolean verify(String arg0, SSLSession arg1) {
								return true; //accept every hostname
							}
						});
					} else ssl = false;
					callConnectionListener(ConnectionListener.ST_CONNECTING);
					connection.setDoOutput(true);
					connection.setDoInput(true);

					connection.setRequestProperty("X-Content-Encoding", "gzip");
					connection.setRequestProperty("Content-type", "text/xml");
					connection.setRequestProperty("Authorization", "Basic: "+(EncodingUtil.encode((username+":"+password).getBytes("8859_1"))));
					connection.setRequestProperty("Accept-encoding", "gzip");
					StatsOutputStream sos = new StatsOutputStream(connection.getOutputStream());
					gos = new GZIPOutputStream(sos);
					new XMLOutputter().output(req, gos);
					gos.close();
					connection.connect();
					String gzip = connection.getHeaderField("Content-encoding");
					StatsInputStream sis = new StatsInputStream(connection.getInputStream());
					if (gzip != null && gzip.toLowerCase().indexOf("gzip") != -1) {
						is = new GZIPInputStream (sis);
					} else {
						is = sis;
					}
					try {
						System.out.println("\nRequest ("+DisplayFormatters.formatByteCountToBase10KBEtc(sos.getBytesWritten())+"):");
						new XMLOutputter(Format.getPrettyFormat()).output(req, System.out);		//Request
						SAXBuilder builder = new SAXBuilder();
						Document xmlDom = builder.build(is);
						System.out.println("\nResponse ("+DisplayFormatters.formatByteCountToBase10KBEtc(sis.getBytesRead())+"):");
						new XMLOutputter(Format.getPrettyFormat()).output(xmlDom, System.out);	//Response
						System.out.println();
						responseManager.handleResponse(xmlDom);
						failedConnections = 0;
						callConnectionListener(ConnectionListener.ST_CONNECTED);
						break;
					} catch (JDOMException e) {
						callExceptionListener(e, false);
						e.printStackTrace();
					}
				} catch (SSLException e) {
					if ( i == 0 ){
						if ( SESecurityManager.getSingleton().installServerCertificates( server ) != null ){
							System.out.println("Installing Certificate");
							continue;	// retry with new certificate
						}
					}
					throw( e );
				}
		}catch (MalformedURLException e) {
			failedConnections++;
			callConnectionListener(ConnectionListener.ST_DISCONNECTED);
			callExceptionListener(e, true);
			e.printStackTrace();
		} catch (IOException e) {
			failedConnections++;
			callConnectionListener(ConnectionListener.ST_DISCONNECTED);
			if (connection != null) {
				try {
					callHTTPErrorListener(connection.getResponseCode());
				} catch (IOException e1) {}
			}
			callExceptionListener(e, true);
			e.printStackTrace();
		} finally {
			try {
				if (is!=null)is.close();
			} catch (IOException e) {
			}
			try {
				if (gos!=null) gos.close();
			} catch (IOException e) {
			}
		}
	}

	private Element getSendElement() {
		Element sendElement = new Element("Query");
		return sendElement;
	}

	private void enqueue(Element e) {
		transactionQueue.offer(e);
		send();
	}

	//--------------------------------------------------------//

	public void sendPing () {
		final Document reqDoc = new Document();

		Element request = new Element("Request");
		request.setAttribute("version", Double.toString(RemoteConstants.CURRENT_VERSION));
		request.setAttribute("noEvents", "true");
		reqDoc.addContent(request);

		Element statElement = getSendElement();
		statElement.setAttribute("switch", "Ping");
		request.addContent(statElement);
		new Thread(new Runnable() {
			public void run() {
				sendHttpRequest(reqDoc);
			}
		}).start();
	}

	public void sendListTransfers(int options) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "listTransfers");
		sendElement.setAttribute("options", Integer.toString(options));
		enqueue(sendElement);
	}

	public void sendAddDownload(String url) {
		sendAddDownload(url, null, null);
	}

	public void sendAddDownload(String url, String username, String password) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "addDownload");
		sendElement.setAttribute("location", "URL");
		sendElement.setAttribute("url", url);
		if (username != null) {
			sendElement.setAttribute("username", username);
			sendElement.setAttribute("password", password);
		}
		enqueue(sendElement);
	}

	public void sendAddDownload(File torrentFile) {
		sendAddDownload(torrentFile, null);
	}

	public void sendAddDownload(File torrentFile, int[] fileOptions) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "addDownload");
		sendElement.setAttribute("location", "XML");
		if (fileOptions != null) {
			sendElement.setAttribute("fileOptions", EncodingUtil.IntArrayToString(fileOptions));
		}
		try {
			sendElement.addContent(loadTorrentToXML(torrentFile));
			enqueue(sendElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRemoveDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "removeDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRemoveDownload (String hash, boolean delete_torrent, boolean delete_data) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "removeDownload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("delTorrent", Boolean.toString(delete_torrent));
		sendElement.setAttribute("delData", Boolean.toString(delete_data));
		enqueue(sendElement);
	}

	public void sendStartDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "startDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStopDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "stopDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRestartDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "restartDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRecheckDataDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "recheckDataDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStopAndQueueDownloadDownload (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "stopAndQueueDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStartAll () {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "startAllDownloads");
		enqueue(sendElement);
	}

	public void sendStopAll () {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "stopAllDownloads");
		enqueue(sendElement);
	}

	public void sendSetForceStart (String hash, boolean start) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setForceStart");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("start", Boolean.toString(start));
		enqueue(sendElement);
	}

	public void sendSetPosition (String hash, int pos) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setPosition");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("position", Integer.toString(pos));
		enqueue(sendElement);
	}

	public void sendMoveToPosition (String hash, int pos) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "moveToPosition");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("position", Integer.toString(pos));
		enqueue(sendElement);
	}

	public void sendMoveUp (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "moveUp");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendMoveDown (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "moveDown");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRequestDownloadScrape (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "requestDownloadScrape");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRequestDownloadAnnounce (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "requestDownloadAnnounce");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendMoveDataFiles (String hash,String target) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "moveDataFiles");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("target", target);
		enqueue(sendElement);
	}

	public void sendMaximumDownloadKBPerSecond (String hash, int limit) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setMaximumDownload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("limit", Integer.toString(limit));
		enqueue(sendElement);
	}

	public void sendUploadRateLimitBytesPerSecond (String hash, int limit) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setMaximumUpload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("limit", Integer.toString(limit));
		enqueue(sendElement);
	}

	public void sendGetDownloadStats (String hash, int options) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getDownloadStats");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("options", Integer.toString(options));
		enqueue(sendElement);
	}

	public void sendGetAdvancedStats (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getAdvancedStats");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendGetFiles (String hash) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getFiles");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendGetUsers () {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getUsers");
		enqueue(sendElement);
	}

	public void sendAddUser (Element user) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "addUser");
		sendElement.addContent(user);
		enqueue(sendElement);
	}

	public void sendRemoveUser (String username) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "removeUser");
		sendElement.setAttribute("username", username);
		enqueue(sendElement);
	}

	public void sendUpdateUser (Element user) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "updateUser");
		sendElement.addContent(user);
		enqueue(sendElement);
	}

	public void sendSetFileOptions (String hash, int index, boolean priority, boolean skipped, boolean deleted) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setFileOptions");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("index", Integer.toString(index));
		sendElement.setAttribute("priority", Boolean.toString(priority));
		sendElement.setAttribute("skipped", Boolean.toString(skipped));
		sendElement.setAttribute("deleted", Boolean.toString(deleted));
		enqueue(sendElement);
	}

	public void sendSetAzParameter(String key, String value, int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setAzParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetAzParameter(String key,int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getAzParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendSetPluginParameter(String key, String value, int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setPluginParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetPluginParameter(String key,int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getPluginParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendSetCoreParameter(String key, String value, int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "setCoreParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetCoreParameter(String key,int type) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getCoreParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendRestartAzureus() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "restartAzureus");
		enqueue(sendElement);
	}

	public void sendGetRemoteInfo() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getRemoteInfo");
		enqueue(sendElement);
	}

	public void sendGetDriveInfo() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getDriveInfo");
		enqueue(sendElement);
	}

	/*public void sendGetPluginsFlexyConf() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getPluginsFlexyConfig");
		enqueue(sendElement);
	}*/

	public void sendApplyUpdates(String[] names) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "applyUpdates");
		for (String n:names) {
			Element a = new Element ("Apply");
			a.setAttribute("name", n);
			sendElement.addContent(a);
		}
		enqueue(sendElement);
	}

	public void sendCreateSLLCertificate(String alias, String dn, int strength) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "createSSLCertificate");
		sendElement.setAttribute("alias", alias);
		sendElement.setAttribute("dn", dn);
		sendElement.setAttribute("strength", Integer.toString(strength));
		enqueue(sendElement);
	}

	public void sendGetGlobalStats() {
		send(); //globalStats are allways requested so just send here
	}

	//********************************************************//

	public void sendGetTrackerTorrents() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getTrackerTorrents");
		enqueue(sendElement);
	}

	public void sendHostTorrent (File torrentFile, boolean persistent, boolean passive) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "hostTorrent");
		sendElement.setAttribute("location", "XML");
		sendElement.setAttribute("persistent", Boolean.toString(persistent));
		sendElement.setAttribute("passive", Boolean.toString(passive));
		try {
			sendElement.addContent(loadTorrentToXML(torrentFile));
			enqueue(sendElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendHostTorrent (Download dl, boolean persistent, boolean passive) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "hostTorrent");
		sendElement.setAttribute("location", "Download");
		sendElement.setAttribute("hash", dl.getHash());
		sendElement.setAttribute("persistent", Boolean.toString(persistent));
		sendElement.setAttribute("passive", Boolean.toString(passive));
		enqueue(sendElement);
	}

	public void sendPublishTorrent (File torrentFile) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "publishTorrent");
		sendElement.setAttribute("location", "XML");
		try {
			sendElement.addContent(loadTorrentToXML(torrentFile));
			enqueue(sendElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPublishTorrent (Download dl) {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "publishTorrent");
		sendElement.setAttribute("location", "Download");
		sendElement.setAttribute("hash", dl.getHash());
		enqueue(sendElement);
	}
	//--------------------------------------------------------//

	public Element loadTorrentToXML (File torrentFile) throws IOException {
		Element torrent = new Element ("Torrent");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(torrentFile);
			byte[] input = new byte[(int)torrentFile.length()];
			for(int i = 0; i < input.length; i++)
			{
				int j = fis.read();
				if(j == -1)
					break;
				input[i] = (byte)j;
			}
			torrent.setText(EncodingUtil.encode(input));
		} catch (IOException e) {
			callExceptionListener(e, true);
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (IOException e) {}
		}
		return torrent;
	}

	//--------------------------------------------------------//

	public void addClientUpdateListener (ClientUpdateListener listener) {
		clientUpdateListeners.add(listener);
	}

	public void removeClientUpdateListener (ClientUpdateListener listener) {
		clientUpdateListeners.remove(listener);
	}

	protected void callClientUpdateListeners (long updateSwitches) {
		for (ClientUpdateListener l:clientUpdateListeners) {
			l.update(updateSwitches);
		}
	}

	public void addSpeedUpdateListener (SpeedUpdateListener listener) {
		speedUpdateListners.add(listener);
	}

	public void removeSpeedUpdateListener (SpeedUpdateListener listener) {
		speedUpdateListners.remove(listener);
	}

	protected void callSpeedUpdateListener(int u, int d) {
		for (SpeedUpdateListener l:speedUpdateListners) {
			l.setSpeed(u, d);
		}
	}

	public void addExceptionListener (ExceptionListener listener) {
		exceptionListeners.add(listener);
	}

	public void removeExceptionListener (ExceptionListener listener) {
		exceptionListeners.remove(listener);
	}

	protected void callExceptionListener(Exception e, boolean serious) {
		for (ExceptionListener l:exceptionListeners) {
			l.exceptionOccured(e,serious);
		}
	}

	public void addConnectionListener (ConnectionListener listener) {
		connectionListeners.add(listener);
	}

	public void removeConnectionListener (ConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	protected void callConnectionListener(int state) {
		for (ConnectionListener l:connectionListeners) {
			l.connectionState(state);
		}
	}

	public void addClientEventListener (ClientEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeClientEventListener (ClientEventListener listener) {
		eventListeners.remove(listener);
	}

	protected void callClientEventListener(int type,long time, Element event) {
		for (ClientEventListener l:eventListeners) {
			l.handleEvent(type, time, event);
		}
	}

	public void addHTTPErrorListener (HTTPErrorListener listener) {
		httpErrorListeners.add(listener);
	}

	public void removeHTTPErrorListener(HTTPErrorListener listener) {
		httpErrorListeners.remove(listener);
	}

	protected void callHTTPErrorListener(int statusCode) {
		for (HTTPErrorListener l:httpErrorListeners) {
			l.httpError(statusCode);
		}
	}

	public void addParameterListener (ParameterListener listener) {
		parameterListeners.add(listener);
	}

	public void removeParameterListener(ParameterListener listener) {
		parameterListeners.remove(listener);
	}

	protected void callAzParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (ParameterListener l:parameterListeners) {
			l.azParameter(key, value, type);
		}
	}

	protected void callPluginParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (ParameterListener l:parameterListeners) {
			l.pluginParameter(key, value, type);
		}
	}

	protected void callCoreParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (ParameterListener l:parameterListeners) {
			l.coreParameter(key, value, type);
		}
	}

	protected boolean verifyParameter (String key, String value, int type) {
		if (key == null || value == null) return false;
		if (type<1 || type>4) return false;
		return true;
	}

	//---------------------------------------------------------//

	/**
	 * @return Returns the server.
	 */
	public URL getServer() {
		return server;
	}


	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}


	/**
	 * @return Returns the downloadManager.
	 */
	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	protected DownloadManagerImpl getDownloadManagerImpl() {
		return downloadManager;
	}

	/**
	 * @return Returns the userManager.
	 */
	public UserManager getUserManager() {
		return userManager;
	}

	protected UserManagerImpl getUserManagerImpl() {
		return userManager;
	}

	/**
	 * @return Returns the remoteInfo.
	 */
	public RemoteInfo getRemoteInfo() {
		return remoteInfo;
	}


	/**
	 * @return Returns the remoteInfo.
	 */
	protected RemoteInfoImpl getRemoteInfoImpl() {
		return remoteInfo;
	}

	/**
	 * @return Returns the remoteUpdateManager.
	 */
	public RemoteUpdateManager getRemoteUpdateManager() {
		return remoteUpdateManager;
	}

	/**
	 * @return Returns the remoteUpdateManager.
	 */
	protected RemoteUpdateManagerImpl getRemoteUpdateManagerImpl() {
		return remoteUpdateManager;
	}


	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		if (this.username != null && !this.username.equalsIgnoreCase(username)) {
			//remove info if user changes
			downloadManager.clear();
			userManager.clear();
		}
		this.username = username;
	}

	/**
	 * @param server The server to set.
	 */
	public void setServer(URL server) {
		setServer(server.toExternalForm());
	}

	public void setServer(String server) {
		reset();
		if (server == null) this.server = null;
		else
		try {
			this.server = new URL (server+"/process.cgi");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/**
	 * @param debug The debug Logger to set.
	 */
	public void setDebugLogger(Logger debug) {
		this.debug = debug;
	}


	/**
	 * @return Returns the failedConnections.
	 */
	public int getFailedConnections() {
		return failedConnections;
	}


	/**
	 * @return Returns the ssl.
	 */
	public boolean isSSLEncrypted() {
		return ssl;
	}


	/**
	 * @return the fastMode
	 */
	public boolean isFastMode() {
		return fastMode;
	}


	/**
	 * @param fastMode the fastMode to set
	 */
	public void setFastMode(boolean fastMode) {
		this.fastMode = fastMode;
	}
}
