package lbms.azsmrc.remote.client.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.UserManager;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

public class UserManagerImpl implements UserManager {

	private Map<String,User> userList = Collections.synchronizedMap(new HashMap<String,User>());
	private Client client;

	public UserManagerImpl (Client client) {
		this.client = client;
	}

	public String[] getUserList() {
		String[] list = userList.keySet().toArray(new String[] {});
		Arrays.sort(list);
		return list;
	}

	public User[] getUsers() {
		return userList.values().toArray(new User[] {});
	}

	public User getUser(String userName) throws UserNotFoundException {
		User user = userList.get(userName);
		if (user == null) throw new UserNotFoundException();
		return user;
	}

	public User getActiveUser() {
		return userList.get(client.getUsername());
	}

	public User addUser(Element userElement) throws DuplicatedUserException {
		User user = addUserImpl(userElement);
		client.sendAddUser(user.toElement());
		return user;
	}

	public User addUser(String username, String password) throws DuplicatedUserException {
		if (userList.containsKey(username)) throw new DuplicatedUserException();
		if (!getActiveUser().checkAccess(RemoteConstants.RIGHTS_ADMIN)) return null;
		User user = new User(username,password);
		user.setClient(client);
		userList.put(username, user);
		client.sendAddUser(user.toElement());
		return user;
	}

	public User addUserUser(String username, String password, String autoImportDir,
			String outputDir, int downloadSlots, int userRights)  throws DuplicatedUserException {
		if (userList.containsKey(username)) throw new DuplicatedUserException();
		if (!getActiveUser().checkAccess(RemoteConstants.RIGHTS_ADMIN)) return null;
		User user = new User(username, password, autoImportDir, outputDir, downloadSlots,
		userRights);
		user.setClient(client);
		userList.put(username, user);
		client.sendAddUser(user.toElement());
		return user;
	}

	public User addUserImpl(Element userElement) throws DuplicatedUserException {
		if (userList.containsKey(userElement.getAttributeValue("username"))) throw new DuplicatedUserException();
		User user = new User(userElement);
		user.setClient(client);
		userList.put(user.getUsername(), user);
		return user;
	}

	public void removeUser(String username) {
		if (!getActiveUser().checkAccess(RemoteConstants.RIGHTS_ADMIN)) return;
		client.sendRemoveUser(username);
		userList.remove(username);
	}

	public void removeUser(User user) {
		if (!getActiveUser().checkAccess(RemoteConstants.RIGHTS_ADMIN)) return;
		client.sendRemoveUser(user.getUsername());
		userList.remove(user.getUsername());
	}

	public void keepUsers(List<String> userNames) {
		Set<String> users = new HashSet<String>(userList.keySet());
		users.removeAll(userNames);
		for (String user:users) {
			userList.remove(user);
		}
	}

	public void renameUser(String userName, String newUserName) throws DuplicatedUserException {
		if (userList.containsKey(newUserName)) throw new DuplicatedUserException();

		User user = userList.get(userName);
		if (user == null) return;
		user.setUsername(newUserName);
		userList.remove(userName);
		userList.put(user.getUsername(), user);
	}

	public void update() {
		client.sendGetUsers();
	}

	public void clear() {
		userList.clear();

	}

}
