package lbms.azsmrc.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.download.Download;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 *
 * @author Damokles
 *
 */
public class XMLConfig {

	private File configFile;
	private HashMap<String,User> userList = new HashMap<String,User>();

	private double configVersion = 0.6;

	/**
	 * Prevent direct instantation
	 */
	private XMLConfig() {};

	/**
	 * This is a static factory Method.
	 * It loads the config file or creates
	 * it if needed.
	 *
	 * @param fname Location of the config file
	 */
	public static XMLConfig loadConfigFile (String fname) {
		XMLConfig conf = new XMLConfig();
		conf.configFile = new File(fname);

		try {
			if (!conf.configFile.exists()) {
				conf.createNewConfigFile();
			}
			String key = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter("secKey");
			SecretKey secKey = new SecretKeySpec(  EncodingUtil.decode(key) , "DES" );

			Cipher c = Cipher.getInstance( "DES" );
			c.init( Cipher.DECRYPT_MODE, secKey );

			CipherInputStream cis = new CipherInputStream(new FileInputStream(conf.configFile),c);


			SAXBuilder builder = new SAXBuilder();
			Document xmlDom = builder.build( cis );
			new XMLOutputter(Format.getPrettyFormat()).output(xmlDom, System.out);
			conf.readConfig(xmlDom);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conf;
	}

	/**
	 * Reads the xml Document into java objects
	 *
	 * @param xmlDom Document object to read from
	 */
	private void readConfig (Document xmlDom) {
		Element root = xmlDom.getRootElement();

		List<Element> users = root.getChild("UserList").getChildren();
		for (Element userElement :users) {
			User user = new User(userElement);
			userList.put(user.getUsername(), user);
		}
		if (userList.isEmpty()) {
			User admin = new User ("admin" , "azmultiuser");
			admin.setRights(RemoteConstants.RIGHTS_ADMIN);
			userList.put(admin.getUsername(), admin);
		}
	}

	/**
	 * Creates a new config file
	 * @param file  Location of the config file
	 */
	private void createNewConfigFile () throws IOException {

			try {
				KeyGenerator kg = KeyGenerator.getInstance( "DES" );
				kg.init(56);
				SecretKey secKey = kg.generateKey();
				Plugin.getPluginInterface().getPluginconfig().setPluginParameter("secKey", EncodingUtil.encode( secKey.getEncoded()));
				Plugin.getPluginInterface().getPluginconfig().save();

				configFile.createNewFile();
				Element root = new Element ("MultiUserConfig");
				root.setAttribute("configVersion", Double.toString(configVersion));
				Element users = new Element ("UserList");

				User admin = new User ("admin" , "azmultiuser");
				admin.setRights(RemoteConstants.RIGHTS_ADMIN);
				users.addContent(admin.toElement());
				root.addContent(users);

				Document xmlDoc = new Document(root);
				XMLOutputter out = new XMLOutputter();
				Cipher c = Cipher.getInstance( "DES" );
				c.init( Cipher.ENCRYPT_MODE, secKey );

				CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(configFile),c);
				out.output( xmlDoc,  cos);
				cos.close();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PluginException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Saves the current config file.
	 */
	public void saveConfigFile () throws IOException {
		Element root = new Element ("MultiUserConfig");
		root.setAttribute("configVersion", Double.toString(configVersion));
		Element users = new Element ("UserList");
		Set<String> keys = userList.keySet();
		for (String key:keys) {
			users.addContent(userList.get(key).toElement());
		}
		root.addContent(users);
		Document xmlDoc = new Document(root);
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat() );

		try {
			String key = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter("secKey");
			SecretKey secKey = new SecretKeySpec( EncodingUtil.decode(key), "DES" );
			Cipher c = Cipher.getInstance( "DES" );
			c.init( Cipher.ENCRYPT_MODE, secKey );

			CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(configFile),c);
			out.output( xmlDoc,  cos);
			cos.close();
			out.output( xmlDoc, System.out);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This returns a list of all Usernames
	 * if you want details about a User
	 * use getUser(Username).
	 *
	 * @return A list of Usernames
	 */
	public String[] getUserList() {
		String[] list = userList.keySet().toArray(new String[] {});
		Arrays.sort(list);
		return list;
	}

	/**
	 * This will return all user Objects
	 *
	 * @return Array with all Users
	 */
	public User[] getUsers () {
		return userList.values().toArray(new User[] {});
	}

	/**
	 * Use this function to get the User-object
	 *
	 * @param userName
	 * @return A User-object for the User.
	 * @throws UserNotFoundException
	 */
	public User getUser(String userName) throws UserNotFoundException {
		User user = userList.get(userName);
		if (user == null) throw new UserNotFoundException();
		return user;
	}

	/**
	 * This function adds a new User.
	 *
	 * @param userName
	 * @param pass
	 * @throws DuplicatedUserException
	 */
	public void addUser (String userName, String pass) throws DuplicatedUserException {
		if (userList.containsKey(userName)) throw new DuplicatedUserException();
		userList.put(userName, new User(userName,pass));
	}

	public void addUser (Element userElement) throws DuplicatedUserException {
		if (userList.containsKey(userElement.getAttributeValue("username"))) throw new DuplicatedUserException();
		User user = new User(userElement);
		userList.put(user.getUsername(), user);
	}

	/**
	 * This function will change a username
	 * to the new username.
	 *
	 * @param userName Old Name
	 * @param newUserName New Name
	 */
	public void renameUser(String userName, String newUserName) throws DuplicatedUserException {
		if (userList.containsKey(newUserName)) throw new DuplicatedUserException();

		User user = userList.get(userName);
		if (user == null) return;
		user.setUsername(newUserName);
		userList.remove(userName);
		userList.put(user.getUsername(), user);
	}

	/**
	 * This function deletes a User.
	 *
	 * @param userName
	 */
	public void removeUser (String userName) {
		userList.remove(userName);
	}

	/**
	 * Returns the Number of assigned download slots.
	 *
	 * @return
	 */
	public int getAssignedDownloadSlots () {
		int slots = 0;
		Set<String> keys = userList.keySet();
		for (String key:keys) {
			slots+= userList.get(key).getDownloadSlots();
		}
		return slots;
	}

	/**
	 * Returns all Users of a Download
	 *
	 * @param dlName
	 * @return array of Users or null
	 */
	public User[] getUsersOfDownload (Download dl) {
		List<User> temp = new Vector<User>();
		Set<String> keys = userList.keySet();
		for (String key:keys) {
			if (userList.get(key).hasDownload(dl))
				temp.add(userList.get(key));
		}
		return temp.toArray(new User[] {});
	}

	public void removeInvalidDownloadsFromUsers (Download[] dls) {
		List<String> dlh = new ArrayList<String>();
		for (int i=0;i<dls.length;i++) {
			dlh.add(EncodingUtil.encode(dls[i].getTorrent().getHash()));
		}
		Set<String> keys = userList.keySet();
		for (String key:keys) {
			userList.get(key).retainDownloads(dlh);
		}
		try {
			saveConfigFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
