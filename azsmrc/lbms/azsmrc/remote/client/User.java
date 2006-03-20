package lbms.azsmrc.remote.client;

import org.jdom.Element;

public class User extends lbms.azsmrc.shared.User {

	private Client client;

	public User(Element userElement) {
		super(userElement);
		password = "";
	}

	public User(String username, String password) {
		super(username, password);
		// TODO Auto-generated constructor stub
	}

	public User(String username, String password, String autoImportDir,
			String outputDir, int downloadSlots, int userRights) {
		super(username, password, autoImportDir, outputDir, downloadSlots,
				userRights);
		// TODO Auto-generated constructor stub
	}

	public void setClient (Client client) {
		this.client = client;
	}

	@Override
	public void setAutoImportDir(String autoImportDir) {
		super.setAutoImportDir(autoImportDir);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("autoImportDir",autoImportDir);
		client.sendUpdateUser(user);
	}

	@Override
	public void setDownloadSlots(int downloadSlots) {
		super.setDownloadSlots(downloadSlots);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("downloadSlots",Integer.toString(downloadSlots));
		client.sendUpdateUser(user);
	}

	@Override
	public void setOutputDir(String outputDir) {
		super.setOutputDir(outputDir);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("outputDir",outputDir);
		client.sendUpdateUser(user);
	}

	@Override
	public void setPassword(String password) {
		super.setPassword(password);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("password", this.password);
		client.sendUpdateUser(user);
	}

	@Override
	public void setRight(int right) {
		super.setRight(right);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("userRights",Integer.toString(userRights));
		client.sendUpdateUser(user);
	}

	@Override
	public void setRights(int rights) {
		super.setRights(rights);
		Element user = new Element ("User");
		user.setAttribute("uName", username);
		user.setAttribute("userRights",Integer.toString(userRights));
		client.sendUpdateUser(user);
	}

	@Override
	public void setUsername(String username) {
		Element user = new Element ("User");
		user.setAttribute("uName", this.username);
		user.setAttribute("username", username);
		super.setUsername(username);
		client.sendUpdateUser(user);
	}
}
