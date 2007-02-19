package lbms.azsmrc.remote.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
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
import lbms.azsmrc.remote.client.events.IPCResponseListener;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.events.GlobalStatsListener;
import lbms.azsmrc.remote.client.impl.DownloadManagerImpl;
import lbms.azsmrc.remote.client.impl.RemoteInfoImpl;
import lbms.azsmrc.remote.client.impl.RemoteUpdateManagerImpl;
import lbms.azsmrc.remote.client.impl.TrackerImpl;
import lbms.azsmrc.remote.client.impl.TrackerTorrentImpl;
import lbms.azsmrc.remote.client.impl.UserManagerImpl;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.Timer;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.stats.StatsInputStream;
import lbms.tools.stats.StatsOutputStream;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class is the connection to the Azureus Server.
 * 
 * This class is responsible for all requests send to the Azureus server.
 * The response is handled by @see ResponseManager.
 * 
 * @author Damokles
 *
 */
public class Client {

	private static final long TRANSACTION_TIMEOUT = 1500;

	private Semaphore semaphore = new Semaphore(1);
	private boolean transaction, ssl, fastMode, useProxy;
	private URL server;
	private String username;
	private String password;
	private Proxy proxy;
	private Queue<Element> transactionQueue = new ConcurrentLinkedQueue<Element>();
	private TimerEvent transactionTimeout;
	private Timer timer;
	private int failedConnections;
	private static Logger logger;
	private boolean connect;

	//special send variables
	private boolean updateDownloads;
	private boolean updateDownloadsFull;

	private DownloadManagerImpl downloadManager;
	private UserManagerImpl userManager;
	private ResponseManager responseManager;
	private RemoteInfoImpl remoteInfo;
	private RemoteUpdateManagerImpl remoteUpdateManager;
	private TrackerImpl tracker;


	//use Vector of Collections.synchronizedList here
	private List<ClientUpdateListener> 	clientUpdateListeners	= new ArrayList<ClientUpdateListener>();
	private List<GlobalStatsListener> 	globalStatsListners		= new ArrayList<GlobalStatsListener>();
	private List<ExceptionListener> 	exceptionListeners		= new ArrayList<ExceptionListener>();
	private List<ConnectionListener> 	connectionListeners		= new ArrayList<ConnectionListener>();
	private List<ClientEventListener> 	eventListeners			= new ArrayList<ClientEventListener>();
	private List<HTTPErrorListener> 	httpErrorListeners		= new ArrayList<HTTPErrorListener>();
	private List<ParameterListener>		parameterListeners		= new ArrayList<ParameterListener>();
	private List<IPCResponseListener>	ipcResponseListeners	= new ArrayList<IPCResponseListener>();

	/**
	 * Creates the client
	 */
	public Client () {
		init();
	}


	/**
	 * Creates the Client with an URL
	 * @param server
	 */
	public Client(URL server) {
		init();
		setServer(server);
	}

	public Client (LoginData login) {
		init();
		setLoginData(login);
	}

	public void setLoginData (LoginData login) {
		setServer(login.getURL());
		username = login.getUsername();
		password = login.getPassword();
	}

	/**
	 * Initialises Variables
	 */
	private void init() {
		downloadManager = new DownloadManagerImpl(this);
		responseManager = new ResponseManager(this);
		reset();
		timer 	= new Timer("Client Timer",1);
		ssl 	= false;
		fastMode = false;
		logger = Logger.getLogger("lbms.azsmrc.client");
	}

	/**
	 * Reset Variables that are Server specific.
	 * 
	 * Call this when you disconnect from a Server.
	 */
	private void reset() {
		userManager 		= new UserManagerImpl(this);
		remoteInfo			= new RemoteInfoImpl(this);
		remoteUpdateManager = new RemoteUpdateManagerImpl (this);
		tracker 			= new TrackerImpl (this);
		downloadManager.clear();
		failedConnections 	= 0;
		connect = false;
	}

	/**
	 * If you want to use any send* methods you need to connect first.
	 */
	public void connect() {
		connect = true;
	}

	/**
	 * Disconnect.
	 */
	public void disconnect() {
		connect = false;
		callConnectionListener(ConnectionListener.ST_DISCONNECTED);
	}

	/**
	 * @return whether currently Connected to a Server
	 */
	public boolean isConnected() {
		return connect;
	}

	/**
	 * Starts a transaction.
	 * 
	 * A transaction will queue all requests while until it is commited
	 * or send by timeout.
	 * @return true if a transaction was active already
	 */
	public boolean transactionStart() {
		boolean old = transaction;
		transaction = true;
		logger.debug("Transaction Started");
		if (transactionTimeout != null) transactionTimeout.cancel();
		transactionTimeout = timer.addEvent(System.currentTimeMillis()+TRANSACTION_TIMEOUT, new TimerEventPerformer() {
			public void perform(TimerEvent event) {
				logger.warn("Transaction Committed by Timeout.");
				transactionCommit();
			}
		});
		return old;
	}

	/**
	 * Commits the transaction.
	 * 
	 * Commits the active transaction.
	 */
	public void transactionCommit() {
		if (transactionTimeout != null) transactionTimeout.cancel();
		transaction = false;
		logger.debug("Transaction Committed ("+transactionQueue.size()+" items)");
		send();
	}

	/**
	 * Sends the queue to the server.
	 * 
	 * This is Threaded, if a transaction is active
	 * it will return immediately.
	 */
	private void send() {
		if (transaction) return;
		if (!connect) return;
		new Thread(new Runnable() {
			public void run() {
				if (!semaphore.tryAcquire()) {
					logger.debug("Client connection already established, postponed transfer.");
					return;
				}
				Document reqDoc = new Document();

				Element request = new Element("Request");
				request.setAttribute("version", Double.toString(RemoteConstants.CURRENT_VERSION));
				reqDoc.addContent(request);

				//special Elements that are used often but only have one return
				//this is used to prevent stacking
				Element statElement = getSendElement("globalStats");
				request.addContent(statElement);

				if (updateDownloads) {
					Element listTransferElement = getSendElement("updateDownloads");
					if (updateDownloadsFull)
						listTransferElement.setAttribute("fullUpdate",Boolean.toString(updateDownloadsFull));
					request.addContent(listTransferElement);
					updateDownloads = false;
					updateDownloadsFull = false;
				}

				Element item = transactionQueue.poll();
				while (item != null) {
					request.addContent(item);
					item = transactionQueue.poll();
				}
				sendHttpRequest(reqDoc);
				semaphore.release();
				//if new elements are in the queue and fastmode is on send again
				if (fastMode && transactionQueue.peek()!=null) {
					logger.debug("FastMode Send");
					send();
				}
			}
		}).start();
	}


	/**
	 * Sends the request Document to the Server
	 * @param req the Document to send
	 */
	private void sendHttpRequest(Document req) {
		if (!connect) return;
		if (server == null) return;
		InputStream is = null;
		GZIPOutputStream gos = null;
		HttpURLConnection connection = null;
		try {
			for (int i=0;i<2;i++)
				try {
					if (useProxy)
						connection = (HttpURLConnection)server.openConnection(proxy);
					else
						connection = (HttpURLConnection)server.openConnection();

					if (server.getProtocol().equalsIgnoreCase("https")){
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
					long startTime = System.currentTimeMillis();
					connection.connect();
					String gzip = connection.getHeaderField("Content-encoding");
					StatsInputStream sis = new StatsInputStream(connection.getInputStream());
					if (gzip != null && gzip.toLowerCase().indexOf("gzip") != -1) {
						is = new GZIPInputStream (sis);
					} else {
						is = sis;
					}
					try {
						logger.debug("Request ("+DisplayFormatters.formatByteCountToBase10KBEtc(sos.getBytesWritten())+"):");
						new XMLOutputter(Format.getPrettyFormat()).output(req, System.out);		//Request
						SAXBuilder builder = new SAXBuilder();
						Document xmlDom = builder.build(is);
						logger.debug("Response ("+DisplayFormatters.formatByteCountToBase10KBEtc(sis.getBytesRead())+" "+(System.currentTimeMillis()-startTime) +"msec):");
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
							logger.debug("Installing Certificate");
							continue;	// retry with new certificate
						}
					}
					throw( e );
				}
		}catch (MalformedURLException e) {
			failedConnections++;
			callConnectionListener(ConnectionListener.ST_CONNECTION_ERROR);
			callExceptionListener(e, true);
			e.printStackTrace();
		} catch (IOException e) {
			failedConnections++;
			callConnectionListener(ConnectionListener.ST_CONNECTION_ERROR);
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

	/**
	 * Creates a standard queue Element
	 * @param id Switch
	 * @return the Element
	 */
	public Element getSendElement(String id) {
		Element sendElement = new Element("Query");
		sendElement.setAttribute("switch", id);
		return sendElement;
	}

	/**
	 * Add an Element to the Transaction Queue.
	 * 
	 * NOTE: This is only public because it is used by PluginClientImpl
	 * otherwise it should not be called.
	 * @param e
	 */
	public void enqueue(Element e) {
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

		Element statElement = getSendElement("Ping");
		request.addContent(statElement);
		new Thread(new Runnable() {
			public void run() {
				sendHttpRequest(reqDoc);
			}
		}).start();
	}

	public void sendUpdateDownloads (boolean fullUpdate) {
		updateDownloads = true;
		if (fullUpdate) updateDownloadsFull = true;
		send();
	}

	public void sendAddDownload(String url, String username, String password, String fileLocation) {
		Element sendElement = getSendElement("addDownload");
		sendElement.setAttribute("location", "URL");
		sendElement.setAttribute("url", url);
		if (username != null) {
			sendElement.setAttribute("username", username);
			sendElement.setAttribute("password", password);
		}
		if (fileLocation != null) {
			sendElement.setAttribute("fileLocation", fileLocation);
		}
		enqueue(sendElement);
	}

	public void sendAddDownload(File torrentFile, int[] fileOptions, String fileLocation) {
		Element sendElement = getSendElement("addDownload");
		sendElement.setAttribute("location", "XML");
		if (fileOptions != null) {
			sendElement.setAttribute("fileOptions", EncodingUtil.IntArrayToString(fileOptions));
		}
		if (fileLocation != null) {
			sendElement.setAttribute("fileLocation", fileLocation);
		}
		try {
			sendElement.addContent(loadTorrentToXML(torrentFile));
			enqueue(sendElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRemoveDownload (String hash) {
		Element sendElement = getSendElement("removeDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRemoveDownload (String hash, boolean delete_torrent, boolean delete_data) {
		Element sendElement = getSendElement("removeDownload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("delTorrent", Boolean.toString(delete_torrent));
		sendElement.setAttribute("delData", Boolean.toString(delete_data));
		enqueue(sendElement);
	}

	public void sendStartDownload (String hash) {
		Element sendElement = getSendElement("startDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStopDownload (String hash) {
		Element sendElement = getSendElement("stopDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRestartDownload (String hash) {
		Element sendElement = getSendElement("restartDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRecheckDataDownload (String hash) {
		Element sendElement = getSendElement("recheckDataDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStopAndQueueDownloadDownload (String hash) {
		Element sendElement = getSendElement("stopAndQueueDownload");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendStartAll () {
		Element sendElement = getSendElement("startAllDownloads");
		enqueue(sendElement);
	}

	public void sendStopAll () {
		Element sendElement = getSendElement("stopAllDownloads");
		enqueue(sendElement);
	}

	public void sendPauseDownloads (int timeout) {
		Element sendElement = getSendElement("pauseDownloads");
		if (timeout>0)
			sendElement.setAttribute("timeout", Integer.toString(timeout));
		enqueue(sendElement);
	}

	public void sendResumeDownloads () {
		Element sendElement = getSendElement("resumeDownloads");
		enqueue(sendElement);
	}

	public void sendSetForceStart (String hash, boolean start) {
		Element sendElement = getSendElement("setForceStart");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("start", Boolean.toString(start));
		enqueue(sendElement);
	}

	public void sendSetPosition (String hash, int pos) {
		Element sendElement = getSendElement("setPosition");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("position", Integer.toString(pos));
		enqueue(sendElement);
	}

	public void sendMoveToPosition (String hash, int pos) {
		Element sendElement = getSendElement("moveToPosition");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("position", Integer.toString(pos));
		enqueue(sendElement);
	}

	public void sendMoveUp (String hash) {
		Element sendElement = getSendElement("moveUp");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendMoveDown (String hash) {
		Element sendElement = getSendElement("moveDown");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRequestDownloadScrape (String hash) {
		Element sendElement = getSendElement("requestDownloadScrape");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendRequestDownloadAnnounce (String hash) {
		Element sendElement = getSendElement("requestDownloadAnnounce");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendMoveDataFiles (String hash,String target) {
		Element sendElement = getSendElement("moveDataFiles");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("target", target);
		enqueue(sendElement);
	}

	public void sendMaximumDownloadKBPerSecond (String hash, int limit) {
		Element sendElement = getSendElement("setMaximumDownload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("limit", Integer.toString(limit));
		enqueue(sendElement);
	}

	public void sendUploadRateLimitBytesPerSecond (String hash, int limit) {
		Element sendElement = getSendElement("setMaximumUpload");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("limit", Integer.toString(limit));
		enqueue(sendElement);
	}

	public void sendGetDownloadStats (String hash, int options) {
		Element sendElement = getSendElement("getDownloadStats");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("options", Integer.toString(options));
		enqueue(sendElement);
	}

	public void sendGetAdvancedStats (String hash) {
		Element sendElement = getSendElement("getAdvancedStats");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendGetFiles (String hash) {
		Element sendElement = getSendElement("getFiles");
		sendElement.setAttribute("hash", hash);
		enqueue(sendElement);
	}

	public void sendGetUsers () {
		Element sendElement = getSendElement("getUsers");
		enqueue(sendElement);
	}

	public void sendAddUser (Element user) {
		Element sendElement = getSendElement("addUser");
		sendElement.addContent(user);
		enqueue(sendElement);
	}

	public void sendRemoveUser (String username) {
		Element sendElement = getSendElement("removeUser");
		sendElement.setAttribute("username", username);
		enqueue(sendElement);
	}

	public void sendUpdateUser (Element user) {
		Element sendElement = getSendElement("updateUser");
		sendElement.addContent(user);
		enqueue(sendElement);
	}

	public void sendSetFileOptions (String hash, int index, boolean priority, boolean skipped, boolean deleted) {
		Element sendElement = getSendElement("setFileOptions");
		sendElement.setAttribute("hash", hash);
		sendElement.setAttribute("index", Integer.toString(index));
		sendElement.setAttribute("priority", Boolean.toString(priority));
		sendElement.setAttribute("skipped", Boolean.toString(skipped));
		sendElement.setAttribute("deleted", Boolean.toString(deleted));
		enqueue(sendElement);
	}

	public void sendSetAzParameter(String key, String value, int type) {
		Element sendElement = getSendElement("setAzParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetAzParameter(String key,int type) {
		Element sendElement = getSendElement("getAzParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendSetPluginParameter(String key, String value, int type) {
		Element sendElement = getSendElement("setPluginParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetPluginParameter(String key,int type) {
		Element sendElement = getSendElement("getPluginParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendSetCoreParameter(String key, String value, int type) {
		Element sendElement = getSendElement("setCoreParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("value", value);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendGetCoreParameter(String key,int type) {
		Element sendElement = getSendElement("getCoreParameter");
		sendElement.setAttribute("key", key);
		sendElement.setAttribute("type", Integer.toString(type));
		enqueue(sendElement);
	}

	public void sendRestartAzureus() {
		Element sendElement = getSendElement("restartAzureus");
		enqueue(sendElement);
	}

	public void sendGetRemoteInfo() {
		Element sendElement = getSendElement("getRemoteInfo");
		enqueue(sendElement);
	}

	public void sendListPlugins() {
		Element sendElement = getSendElement("listPlugins");
		enqueue(sendElement);
	}

	public void sendSetPluginDisable(String id, boolean disable) {
		Element sendElement = getSendElement("listPlugins");
		sendElement.setAttribute("pluginID", id);
		sendElement.setAttribute("disable", Boolean.toString(disable));
		enqueue(sendElement);
	}

	public void sendGetPluginsFlexyConfig() {
		Element sendElement = getSendElement("getPluginsFlexyConfig");
		enqueue(sendElement);
	}

	public void sendGetDriveInfo() {
		Element sendElement = getSendElement("getDriveInfo");
		enqueue(sendElement);
	}

	/*public void sendGetPluginsFlexyConf() {
		Element sendElement = getSendElement();
		sendElement.setAttribute("switch", "getPluginsFlexyConfig");
		enqueue(sendElement);
	}*/

	public void sendGetUpdateInfo () {
		Element sendElement = getSendElement("getUpdateInfo");
		enqueue(sendElement);
	}

	public void sendApplyUpdates(String[] names) {
		Element sendElement = getSendElement("applyUpdates");
		for (String n:names) {
			Element a = new Element ("Apply");
			a.setAttribute("name", n);
			sendElement.addContent(a);
		}
		enqueue(sendElement);
	}

	public void sendCreateSLLCertificate(String alias, String dn, int strength) {
		Element sendElement = getSendElement("createSSLCertificate");
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
		Element sendElement = getSendElement("getTrackerTorrents");
		enqueue(sendElement);
	}

	public void sendHostTorrent (File torrentFile, boolean persistent, boolean passive) {
		Element sendElement = getSendElement("hostTorrent");
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
		Element sendElement = getSendElement("hostTorrent");
		sendElement.setAttribute("location", "Download");
		sendElement.setAttribute("hash", dl.getHash());
		sendElement.setAttribute("persistent", Boolean.toString(persistent));
		sendElement.setAttribute("passive", Boolean.toString(passive));
		enqueue(sendElement);
	}

	public void sendPublishTorrent (File torrentFile) {
		Element sendElement = getSendElement("publishTorrent");
		sendElement.setAttribute("location", "XML");
		try {
			sendElement.addContent(loadTorrentToXML(torrentFile));
			enqueue(sendElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPublishTorrent (Download dl) {
		Element sendElement = getSendElement("publishTorrent");
		sendElement.setAttribute("location", "Download");
		sendElement.setAttribute("hash", dl.getHash());
		enqueue(sendElement);
	}

	public void sendTrackerTorrentRemove (TrackerTorrentImpl t) {
		Element sendElement = getSendElement("trackerTorrentRemove");
		sendElement.setAttribute("hash", t.getHash());
		enqueue(sendElement);
	}

	public void sendTrackerTorrentStop (TrackerTorrentImpl t) {
		Element sendElement = getSendElement("trackerTorrentStop");
		sendElement.setAttribute("hash", t.getHash());
		enqueue(sendElement);
	}

	public void sendTrackerTorrentStart (TrackerTorrentImpl t) {
		Element sendElement = getSendElement("trackerTorrentStart");
		sendElement.setAttribute("hash", t.getHash());
		enqueue(sendElement);
	}

	/**
	 * Send an IPC call to Azureus
	 * 
	 * @param pluginID the pluginID of the target plugin
	 * @param senderID the local (plugin)ID of the sender
	 * @param method the remote method to call
	 * @param params array of Parameters supported are: boolean, int, long, float, double, String
	 */
	public void sendIPCCall (String pluginID, String senderID, String method, Object[] params) {
		Element sendElement = getSendElement("ipcCall");
		sendElement.setAttribute("pluginID", pluginID);
		sendElement.setAttribute("senderID", senderID);
		sendElement.setAttribute("method", method);

		if (params != null) {
			for (Object o : params) {
				Element e = new Element ("Parameter");
				if (o instanceof Boolean) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_BOOLEAN));
					e.setText(o.toString());
				} else if (o instanceof Integer) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_INT));
					e.setText(o.toString());
				} else if (o instanceof Float) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_FLOAT));
					e.setText(o.toString());
				} else if (o instanceof String) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_STRING));
					e.setText(o.toString());
				} else if (o instanceof Long) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_LONG));
					e.setText(o.toString());
				} else if (o instanceof Double) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_DOUBLE));
					e.setText(o.toString());
				} else if (o instanceof Element) {
					e.setAttribute("type", Integer.toString(RemoteConstants.PARAMETER_XML_ELEMENT));
					e.addContent((Element)o);
				}else continue; //if nothing matches don't append
				sendElement.addContent(e);
			}
		}
		enqueue(sendElement);
	}
	//--------------------------------------------------------//

	/**
	 * Loads a Torrent file into an XML element.
	 * 
	 * The Torrent Data is Base64 encoded
	 * 
	 * @param torrentFile the Torrent to read
	 * @return the Element containing the Torrent data
	 * @throws IOException
	 */
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
		for (int i=0;i<clientUpdateListeners.size();i++)
			clientUpdateListeners.get(i).update(updateSwitches);

	}

	public void addGlobalStatsListener (GlobalStatsListener listener) {
		globalStatsListners.add(listener);
	}

	public void removeGlobalStatsListener (GlobalStatsListener listener) {
		globalStatsListners.remove(listener);
	}

	protected void callGlobalStatsListener(int d, int u, int s, int sq, int dl, int dlq) {
		for (int i=0;i<globalStatsListners.size();i++)
			globalStatsListners.get(i).updateStats(d, u, s, sq, dl, dlq);
	}

	public void addExceptionListener (ExceptionListener listener) {
		exceptionListeners.add(listener);
	}

	public void removeExceptionListener (ExceptionListener listener) {
		exceptionListeners.remove(listener);
	}

	protected void callExceptionListener(Exception e, boolean serious) {
		for (int i=0;i<exceptionListeners.size();i++)
			exceptionListeners.get(i).exceptionOccured(e, serious);
	}

	public void addConnectionListener (ConnectionListener listener) {
		connectionListeners.add(listener);
	}

	public void removeConnectionListener (ConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	protected void callConnectionListener(int state) {
		for (int i=0;i<connectionListeners.size();i++)
			connectionListeners.get(i).connectionState(state);
	}

	public void addClientEventListener (ClientEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeClientEventListener (ClientEventListener listener) {
		eventListeners.remove(listener);
	}

	protected void callClientEventListener(int type,long time, Element event) {
		for (int i=0;i<eventListeners.size();i++)
			eventListeners.get(i).handleEvent(type, time, event);

	}

	public void addHTTPErrorListener (HTTPErrorListener listener) {
		httpErrorListeners.add(listener);
	}

	public void removeHTTPErrorListener(HTTPErrorListener listener) {
		httpErrorListeners.remove(listener);
	}

	protected void callHTTPErrorListener(int statusCode) {
		for (int i=0;i<httpErrorListeners.size();i++)
			httpErrorListeners.get(i).httpError(statusCode);

	}

	public void addParameterListener (ParameterListener listener) {
		parameterListeners.add(listener);
	}

	public void removeParameterListener(ParameterListener listener) {
		parameterListeners.remove(listener);
	}

	protected void callAzParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (int i=0;i<parameterListeners.size();i++)
			parameterListeners.get(i).azParameter(key, value, type);

	}

	protected void callPluginParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (int i=0;i<parameterListeners.size();i++)
			parameterListeners.get(i).pluginParameter(key, value, type);
	}

	protected void callCoreParameterListener(String key, String value, int type) {
		if (!verifyParameter(key, value, type)) return;
		for (int i=0;i<parameterListeners.size();i++)
			parameterListeners.get(i).coreParameter(key, value, type);
	}

	/**
	 * Verifies Parameters
	 * 
	 * @param key
	 * @param value
	 * @param type
	 * @return true or false
	 */
	protected boolean verifyParameter (String key, String value, int type) {
		if (key == null || value == null) return false;
		if (type<1 || type>4) return false;
		return true;
	}

	public void addIPCResponseListener (IPCResponseListener listener) {
		ipcResponseListeners.add(listener);
	}

	public void removeIPCResponseListener (IPCResponseListener listener) {
		ipcResponseListeners.remove(listener);
	}

	protected void callIPCResponseListeners (int status, String senderID, String pluginID, String method, Element response) {
		for (int i=0;i<ipcResponseListeners.size();i++)
			ipcResponseListeners.get(i).handleIPCResponse(status, senderID, pluginID, method, response);
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

	/**
	 * @return Returns the DownloadManagerImpl
	 */
	protected DownloadManagerImpl getDownloadManagerImpl() {
		return downloadManager;
	}

	/**
	 * @return Returns the userManager.
	 */
	public UserManager getUserManager() {
		return userManager;
	}

	/**
	 * @return Returns the userManagerImpl.
	 */
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
	 * @return returns the Tracker
	 */
	public Tracker getTracker() {
		return tracker;
	}

	/**
	 * @return returns the TrackerImpl
	 */
	protected TrackerImpl getTrackerImpl() {
		return tracker;
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
	 * Set the proxy to use.
	 * 
	 * Set either proxy, or null to deactivate proxy use.
	 * 
	 * @param proxy the proxy to set
	 */
	public void setProxy(Proxy proxy) {
		if (proxy == null) {
			useProxy = false;
		} else {
			this.proxy = proxy;
			useProxy = true;
		}
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
