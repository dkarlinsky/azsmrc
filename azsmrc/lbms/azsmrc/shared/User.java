package lbms.azsmrc.shared;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class User {
	protected String username;
	protected String password;
	protected String autoImportDir;
	protected String outputDir;
	protected int downloadSlots;
	protected int userRights;
	protected Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Creates a User object and reads the data
	 * from xml document
	 *
	 * @param userElement
	 */
	public User (Element userElement) {
		updateUser(userElement);
	}

	/**
	 * Creates a new User object with
	 * supplied parameters and default
	 * values for any other attributes
	 *
	 * @param username
	 * @param password
	 */
	public User (String username, String password) {
		this.username = username;
		this.password = encryptPassword(password);
		this.autoImportDir = "";
		this.downloadSlots = 1;
		this.outputDir = "";
		this.userRights = 0;
	}



	/**
	 * @param username
	 * @param password
	 * @param autoImportDir
	 * @param outputDir
	 * @param downloadSlots
	 * @param userRights
	 */
	public User(String username, String password, String autoImportDir, String outputDir, int downloadSlots, int userRights) {
		this.username = username;
		this.password = encryptPassword(password);
		this.autoImportDir = autoImportDir;
		this.outputDir = outputDir;
		this.downloadSlots = downloadSlots;
		this.userRights = userRights;
	}

	public void updateUser (Element userElement) {
		try {
			if (userElement.getAttribute("username") != null)
				this.username 		= userElement.getAttribute("username").getValue();
			if (userElement.getAttribute("password") != null)
				this.password 		= userElement.getAttribute("password").getValue();
			if (userElement.getAttribute("autoImportDir") != null)
				this.autoImportDir 	= userElement.getAttribute("autoImportDir").getValue();
			if (userElement.getAttribute("outputDir") != null)
				this.outputDir 		= userElement.getAttribute("outputDir").getValue();
			if (userElement.getAttribute("downloadSlots") != null)
				this.downloadSlots 	= userElement.getAttribute("downloadSlots").getIntValue();
			if (userElement.getAttribute("userRights") != null)
				this.userRights 	= userElement.getAttribute("userRights").getIntValue();
		} catch (DataConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Element> props = userElement.getChildren("Property");
		try {
			for (Element e:props) {
				properties.put(e.getAttributeValue("key"), e.getTextTrim());
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Since passwords are stored encrypted
	 * you need to use this method to verify
	 * a password.
	 *
	 * @param pass password to check
	 * @return Returns true if the password is correct or false
	 */
	public boolean verifyPassword (String pass) {
		return this.password.equalsIgnoreCase(encryptPassword(pass));
	}

	/**
	 * This functions encrypts the password
	 * using a one way hash algorithm SHA-1
	 *
	 * @param pass
	 * @return String length = 40
	 */
	protected static String encryptPassword (String pass) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(pass.getBytes());
			byte[] digest = md.digest();
			for ( byte d : digest )
				result += Integer.toHexString( d & 0xFF);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Don't call this function directly,
	 * call XMLConfig changeUsername instead
	 *
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = encryptPassword(password);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return username;
	}

	/**
	 * This function will convert the User object
	 * to a xml encoded jdom Element.
	 * 
	 * @return the jdom Element represantation of the object
	 */
	public Element toElement () {
		Element user = new Element ("User");
		user.setAttribute("username", username);
		user.setAttribute("password", password);
		user.setAttribute("outputDir",outputDir);
		user.setAttribute("autoImportDir",autoImportDir);
		user.setAttribute("downloadSlots",Integer.toString(downloadSlots));
		user.setAttribute("userRights",Integer.toString(userRights));
		try {
			for (String key:properties.keySet()) {
				Element e = new Element("Property");
				e.setAttribute("key", key);
				e.setText(properties.get(key));
				user.addContent(e);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * @return Returns the autoImportDir.
	 */
	public String getAutoImportDir() {
		return autoImportDir;
	}

	/**
	 * @param autoImportDir The autoImportDir to set.
	 */
	public void setAutoImportDir(String autoImportDir) {
		this.autoImportDir = autoImportDir;
	}

	/**
	 * @return Returns the outputDir.
	 */
	public String getOutputDir() {
		return outputDir;
	}

	/**
	 * @param outputDir The outputDir to set.
	 */
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	/**
	 * This function is similar to checkRight
	 * with the exception that this function will
	 * check admin rights too, admin rights overrides
	 * all, so if one is admin he has access to it.
	 * 
	 * @param right right to check
	 * @return true if set or admin, false if not
	 */
	public boolean checkAccess (int right) {
		return ((right | RemoteConstants.RIGHTS_ADMIN) & userRights) != 0;
	}

	/**
	 * This function will check if a specific right is set.
	 * 
	 * @param right right to check
	 * @return true if set, false if not
	 */
	public boolean checkRight (int right) {
		return (right & userRights) != 0;
	}

	/**
	 * This function will
	 * 
	 * @param rights
	 */
	public void setRights (int rights) {
		this.userRights = rights;
	}

	/**
	 * This will set the right.
	 * Note you cannot unset anything with this
	 * function.
	 * 
	 * @param right
	 */
	public void setRight (int right) {
		this.userRights |= right;
	}

	/**
	 * This function will unset a right.
	 * Actually you can unset more than one right
	 * at a time by combining the rights to uset
	 * using |
	 * 
	 * @param right to unset
	 */
	public void unsetRight (int right) {
		this.userRights &= ~right;
	}

	/**
	 * This function will return the userRights bitfield
	 * 
	 * @return userRights
	 */
	public int getRights() {
		return this.userRights;
	}

	/**
	 * Use this function to display the role of the User
	 *
	 * @return the name of the role
	 */
	public String getRole () {
		if ((userRights & RemoteConstants.RIGHTS_ADMIN) != 0) {
			return "Administrator";
		}
		return "Normal User";
	}

	/**
	 * @return Returns the downloadSlots.
	 */
	public int getDownloadSlots() {
		return downloadSlots;
	}

	/**
	 * @param downloadSlots The downloadSlots to set.
	 */
	public void setDownloadSlots(int downloadSlots) {
		this.downloadSlots = downloadSlots;
	}

	/**
	 * Gets a Property from the user
	 * 
	 * @param key the property key
	 * @return value or null in nonexistent
	 */
	public String getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Sets a Property for the user
	 * 
	 * @param key the property key
	 * @param value the value to set
	 */
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
}
