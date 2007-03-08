package lbms.tools.anidb;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import lbms.tools.ExtendedProperties;

/**
 * @author Damokles
 *
 */
public class AniDBService {

	private AniDBClient client;
	private AniDBResponseManager responseManager;
	private AniDBCache cache;
	private ExtendedProperties config;
	private boolean encryption;
	private String sessionKey;
	private Map<String, AniDBResponseHandler> taggedListeners = new HashMap<String, AniDBResponseHandler>();
	private STATE state = STATE.NOT_CONNECTED;

	private String username, password, apipassword;

	DatagramSocket socket;

	private SecretKeySpec encryptionKey;

	public AniDBService (ExtendedProperties cfg) {
		this.config = cfg;

		try {
			socket = new DatagramSocket(cfg.getPropertyAsInt("AniDBService.listenport", 49012));
			/*socket.setReuseAddress(true);
			InetSocketAddress sa = new InetSocketAddress();
			socket.bind(sa);*/
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.cache = new AniDBCache(this);
		this.client = new AniDBClient(this);
		this.responseManager = new AniDBResponseManager(this);
	}

	/**
	 * @param username
	 * @param password
	 * @param apipassword may be null, if it is set encryption will be used
	 */
	public void setLoginData (String username, String password, String apipassword) {
		if (state.equals(STATE.NOT_CONNECTED)) {
			this.username = username;
			this.password = password;
			this.apipassword = apipassword;
			state = STATE.INITIALISATION;
		} else {
			throw new IllegalStateException("You can only change LoginDate while being not connected.");
		}
	}

	public void connect() {
		if (!state.equals(STATE.INITIALISATION)) {
			throw new IllegalStateException("You can only connect when LoginDetails are set and you are not connected.");
		} else {
			if (apipassword != null) {
				client.sendEncrypt();
			} else {
				client.sendAuth();
			}
		}
	}

	public void disconnect() {
		client.sendLogout();
	}

	public void connect (String username, String password, String apipassword) {
		setLoginData(username, password, apipassword);
		connect();
	}

	/**
	 * @return the client
	 */
	public AniDBClient getClient() {
		return client;
	}

	/**
	 * @return the cache
	 */
	public AniDBCache getCache() {
		return cache;
	}

	/**
	 * @return the config
	 */
	public ExtendedProperties getConfig() {
		return config;
	}

	/**
	 * @return the encryptionKey
	 */
	protected SecretKeySpec getEncryptionKey() {
		return encryptionKey;
	}

	/**
	 * @param encryptionKey the encryptionKey to set
	 */
	protected void setEncryptionSalt(String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(apipassword.getBytes());
			md.update(salt.getBytes());
			byte[] k = md.digest();
			this.encryptionKey = new SecretKeySpec(k,"AES");
			encryption = true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the encryption
	 */
	public boolean isEncryptionEnabled() {
		return encryption;
	}

	/**
	 * @return the sessionKey
	 */
	protected String getSessionKey() {
		return sessionKey;
	}

	/**
	 * @param sessionKey the sessionKey to set
	 */
	protected void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * @return the apipassword
	 */
	protected String getApipassword() {
		return apipassword;
	}

	/**
	 * @return the password
	 */
	protected String getPassword() {
		return password;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the socket
	 */
	protected DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Adds a ResponseListener for a Tag
	 * @param tag
	 * @param l
	 */
	protected void addTagResponseListener (String tag, AniDBResponseHandler l) {
		taggedListeners.put(tag, l);
	}

	/**
	 * Gets a ResponseListener for a Tag and removes it from the map.
	 * @param tag
	 * @return ResponseListener or null
	 */
	protected AniDBResponseHandler getTagResponseListener (String tag) {
		AniDBResponseHandler l = taggedListeners.get(tag);
		taggedListeners.remove(tag);
		return l;
	}

	/**
	 * @param state the state to set
	 */
	protected void setState(STATE state) {
		this.state = state;
	}

	/**
	 * @return the state
	 */
	public STATE getState() {
		return state;
	}

	public static enum STATE {
		NOT_CONNECTED, INITIALISATION, INIT_ENCRYPTION, AUTHENTICATING, READY, ERROR
	}
}
