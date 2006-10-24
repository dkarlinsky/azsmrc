package lbms.azsmrc.remote.client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Damokles
 *
 */
public class LoginData implements Comparable<LoginData> {
	private String username = "";
	private String password = "";
	private String host;
	private int port;
	private boolean https;

	public LoginData (String serializedData) throws MalformedURLException{
		deserialize(serializedData);
	}

	public LoginData(String host, int port, String username, String password, boolean https) {
		this.username = username;
		this.password = password;
		this.host = host;
		if (port<1 || port>65535)
			port = 49009;
		else
			this.port = port;
		this.https = https;
	}

	public String getURL () {
		return ((https)?"https":"http")+"://"+host+":"+port;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the https
	 */
	public boolean isHttps() {
		return https;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	public boolean hasUsername () {
		return !username.equals("");
	}

	public boolean hasPassword () {
		return !password.equals("");
	}

	public boolean hasHost () {
		return !host.equals("");
	}

	public boolean isComplete () {
		return !(username.equals("") || password.equals("") || host.equals(""));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(LoginData o) {
		int x = getURL().compareToIgnoreCase(o.getURL());
		if ( x == 0 ) {
			x = username.compareTo(o.username);
		}
		return x;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LoginData) {
			LoginData new_name = (LoginData) obj;
			return compareTo(new_name) == 0;
		} else
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return serialize();
	}

	public String serialize () {
		try {
			return new URL (((https)?"https":"http")+"://"+username+":"+URLEncoder.encode(password,"UTF-8")+"@"+host+":"+port).toExternalForm();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void deserialize (String data) throws MalformedURLException {
		URL x = new URL(data);
		String userinfo = x.getUserInfo();
		if (userinfo != null) {
			username = userinfo.substring(0, userinfo.indexOf(":"));
			try {
				password = URLDecoder.decode(userinfo.substring(userinfo.indexOf(":")+1), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		host = x.getHost();

		port = x.getPort();

		https = x.getProtocol().equalsIgnoreCase("https");
	}
}
