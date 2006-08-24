package lbms.azsmrc.plugin.pluginsupport;

import lbms.azsmrc.plugin.main.User;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ipc.IPCException;
import org.gudy.azureus2.plugins.ipc.IPCInterface;
import org.jdom.Element;

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
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#getFlexyConf()
	 */
	public Element getFlexyConf() {
		// TODO Auto-generated method stub
		return null;
	}

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

	/* (non-Javadoc)
	 * @see lbms.azsmrc.plugin.pluginsupport.PluginSupport#isConfigurable()
	 */
	public boolean isConfigurable() {
		return true;
	}

	public void sendMessage(User user,String subject, String msg) {
		if(!active) return;
		String targetMail = user.getProperty("eMailAdress");
		if (targetMail != null)
		try {
			ipc.invoke("sendMessage", new Object[] {subject, targetMail, msg});
		} catch (IPCException e) {
			e.printStackTrace();
		}
	}
}
