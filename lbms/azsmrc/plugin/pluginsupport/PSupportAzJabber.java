package lbms.azsmrc.plugin.pluginsupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.InvalidRuleException;
import lbms.tools.flexyconf.InvalidTypeException;
import lbms.tools.flexyconf.Section;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ipc.IPCException;
import org.gudy.azureus2.plugins.ipc.IPCInterface;

/**
 * @author Damokles
 *
 */
public class PSupportAzJabber implements PluginSupport {

	public final static String IDENTIFIER = "AzJabberSupport";
	private boolean active;
	private final static String SUPPORTED_PLUGIN_ID = "azjabber";
	private final static String NAME = "AzJabber Support";
	private final static String MIN_VERSION = "1.1";
	private IPCInterface ipc;

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#getMinPluginVersion()
	 */
	public String getMinPluginVersion() {
		return MIN_VERSION;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#getName()
	 */
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#getSupportedPluginID()
	 */
	public String getSupportedPluginID() {
		return SUPPORTED_PLUGIN_ID;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#initialize(org.gudy.azureus2.plugins.PluginInterface)
	 */
	public void initialize(PluginInterface pi) {
		PluginInterface target = pi.getPluginManager().getPluginInterfaceByID(SUPPORTED_PLUGIN_ID);

		Section confSection = Plugin.addPSConfigSection("AzJabberSupport");
		new Entry("","azsmrc.pluginsupport.AzJabberSupport.info",Entry.TYPE_LABEL,confSection);
		new Entry("JabberNotification","azsmrc.pluginsupport.config.jabbernotify",Entry.TYPE_BOOLEAN,confSection);
		Entry jabberE = new Entry("JabberAddress","azsmrc.pluginsupport.config.jabberAddress",Entry.TYPE_STRING,confSection);
		jabberE.setDependsOn("JabberNotification");

		try {
			//only accept valid email addresses
			jabberE.setRule("^[\\w-\\.]+@(?:[\\w-]+\\.)+[\\w-]{2,4}$");
		} catch (InvalidRuleException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		}


		if (target == null) {
			active = false;
			return;
		} else {
			if (pi.getUtilities().compareVersions(target.getPluginVersion(), MIN_VERSION) < 0) {
				active = false;
				return;
			} else {
				ipc = target.getIPC();
				active = true;
				try {
					String[] usersInRoster = (String[])ipc.invoke("ipcListRecipients", new Object[] {Boolean.FALSE});
					User[] azsmrcUsers =Plugin.getXMLConfig().getUsers();
					List<String> jabberUsers = new ArrayList<String>();
					for (User user:azsmrcUsers) {
						if (Boolean.parseBoolean(user.getProperty("JabberNotification"))) {
							String jabberAddress = user.getProperty("JabberAddress");
							if (jabberAddress != null)
								jabberUsers.add(jabberAddress);
						}
					}
					jabberUsers.removeAll(Arrays.asList(usersInRoster));
					for (String user:jabberUsers) {
						try {
							ipc.invoke("ipcAddUserToRoster", new Object[] {user});
						} catch (IPCException e) {
							e.printStackTrace();
						}
					}
				} catch (IPCException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	public void sendMessage(User user, String msg) {
		if(!active) return;
		if (Boolean.parseBoolean(user.getProperty("JabberNotification"))) {
			String jabberAddress = user.getProperty("JabberAddress");
			if (jabberAddress != null)
				try {
					ipc.invoke("ipcSendMessage", new Object[] {jabberAddress, msg});
				} catch (IPCException e) {
					e.printStackTrace();
				}
		}
	}
}
