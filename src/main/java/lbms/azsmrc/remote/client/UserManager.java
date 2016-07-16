package lbms.azsmrc.remote.client;

import org.jdom.Element;

import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.UserNotFoundException;

public interface UserManager {
	public String[] getUserList();
	public User[] getUsers();
	public User getActiveUser();
	public User getUser(String user) throws UserNotFoundException;
	public User addUser(Element userElement) throws DuplicatedUserException;
	public User addUser(String username, String password) throws DuplicatedUserException;
	public User addUserUser(String username, String password, String autoImportDir,
			String outputDir, int downloadSlots, int userRights) throws DuplicatedUserException;
	public void renameUser (String userName, String newUserName)  throws DuplicatedUserException;
	public void removeUser( String username );
	public void removeUser( User user );
	public void clear();
	public void update();
}
