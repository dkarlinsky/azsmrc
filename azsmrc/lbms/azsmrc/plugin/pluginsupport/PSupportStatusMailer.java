package lbms.azsmrc.plugin.pluginsupport;

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
public class PSupportStatusMailer implements PluginSupport {

	public final static String IDENTIFIER = "StatusMailerSupport";
	private boolean active;
	private final static String SUPPORTED_PLUGIN_ID = "azstatusmailer";
	private final static String NAME = "Status Mailer Support";
	private final static String MIN_VERSION = "0.8.0";
	private IPCInterface ipc;

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#getMinPluginVersion()
	 */
	public String getMinPluginVersion() {
		// TODO Auto-generated method stub
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
		Section confSection = Plugin.addPSConfigSection("StatusMailerSupport");
		new Entry("eMailNotification","azsmrc.pluginsupport.config.emailnotify",Entry.TYPE_BOOLEAN,confSection);
		Entry mailE = new Entry("eMail","azsmrc.pluginsupport.config.email",Entry.TYPE_STRING,confSection);
		try {
			//only accept valid email addresses
			mailE.setRule("^[\\w-\\.]+@(?:[\\w-]+\\.)+[\\w-]{2,4}$");
		} catch (InvalidRuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			// TODO Auto-generated catch block
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
			}
		}
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	public void sendMessage(User user,String subject, String msg) {
		if(!active) return;
		if (Boolean.parseBoolean(user.getProperty("eMailNotification"))) {
			String targetMail = user.getProperty("eMail");
			if (targetMail != null)
				try {
					ipc.invoke("sendMessage", new Object[] {subject, targetMail, msg});
				} catch (IPCException e) {
					e.printStackTrace();
				}
		}
	}
}
