package lbms.azsmrc.plugin.main;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import lbms.azsmrc.plugin.main.history.DownloadHistory;
import lbms.azsmrc.plugin.pluginsupport.PSupportAzJabber;
import lbms.azsmrc.plugin.pluginsupport.PSupportStatusMailer;
import lbms.azsmrc.plugin.web.DownloadContainerManager;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.utils.Formatters;
import org.jdom.Element;

/**
 * @author Damokles
 * 
 */
public class User extends lbms.azsmrc.shared.User {

	private static final int			SESSION_TIMEOUT	= 5 * 60 * 1000;				// 5min

	private Queue<Element>				eventQueue		= new LinkedList<Element>();
	private DownloadContainerManager	dcm;
	private long						lastLogin;

	/**
	 * Creates a User object and reads the data from xml document
	 * 
	 * @param userElement
	 */
	public User(Element userElement) {
		super(userElement);
	}

	public User(String username, String password, String autoImportDir,
			String outputDir, int downloadSlots, int userRights) {
		super(username, password, autoImportDir, outputDir, downloadSlots,
				userRights);
	}

	public User(String username, String password) {
		super(username, password);
	}

	/**
	 * Adds the download to the user.
	 * 
	 * @param download
	 */
	public void addDownload(Download download) {
		downloadList.add(getDlHash(download));
	}

	/**
	 * @param download
	 */
	@Override
	public void addDownload(String downloadHash) {
		try {
			Download dl = Plugin.getPluginInterface().getDownloadManager()
					.getDownload(EncodingUtil.decode(downloadHash));
			if (dl != null) {
				downloadList.add(downloadHash);
			}
		} catch (DownloadException e) {
		}

	}

	/**
	 * This keeps only Downloads that are in both Collections.
	 * 
	 * @param dls other collection to intersect with
	 */
	public void retainDownloads(Collection<String> dls) {
		downloadList.retainAll(dls);
	}

	/**
	 * The download will be removed from the user.
	 * 
	 * @param download
	 */
	public void removeDownload(Download download) {
		downloadList.remove(getDlHash(download));
	}

	/**
	 * Checks if the user is an owner of the Download
	 * 
	 * @param dl
	 * @return
	 */
	public boolean hasDownload(Download dl) {
		return downloadList.contains(getDlHash(dl));
	}

	/**
	 * This function will return the Base64 String represantation of the
	 * downloadHash
	 * 
	 * @param dl
	 * @return String represantation of the downloadHash
	 */
	public String getDlHash(Download dl) {
		if (dl.getTorrent() != null) {
			return EncodingUtil.encode(dl.getTorrent().getHash());
		}
		return "";
	}

	/**
	 * This function will convert the User object to a xml encoded jdom Element.
	 * 
	 * @return the jdom Element represantation of the object
	 */
	@Override
	public Element toElement() {
		Element user = super.toElement();
		return user;
	}

	/**
	 * Use this function to check if there are events in the eventQueue
	 * 
	 * @return true if events are in queue
	 */
	public boolean hasEvents() {
		return !eventQueue.isEmpty();
	}

	/**
	 * This function will put all events in the root Element.
	 * 
	 * @param root all elements will be added in here
	 */
	public void getEvents(Element root) {
		Element ev = eventQueue.poll();
		while (ev != null) {
			root.addContent(ev);
			ev = eventQueue.poll();
		}
	}

	private Element getEventElement() {
		Element event = new Element("Event");
		event.setAttribute("time", Long.toString(System.currentTimeMillis()));
		return event;
	}

	/**
	 * @return the dcm
	 */
	public DownloadContainerManager getDownloadContainerManager() {
		if (dcm == null) {
			dcm = new DownloadContainerManager(Plugin.getPluginInterface()
					.getDownloadManager(), this);
		}
		return dcm;
	}

	/**
	 * @return the lastLogin
	 */
	public long getLastLogin() {
		return lastLogin;
	}

	/**
	 * Returns whether the client was last seen before the session timeout.
	 * 
	 * @return
	 */
	public boolean isSessionValid() {
		return System.currentTimeMillis() - lastLogin < SESSION_TIMEOUT;
	}

	/**
	 * Update Session Last Login
	 */
	public void updateLogin() {
		lastLogin = System.currentTimeMillis();
	}

	/**
	 * If Session has expired delete Session Data
	 */
	public void checkAndDeleteOldSession() {
		if (!isSessionValid()) {
			if (dcm != null) {
				dcm.destroy();
				dcm = null;
			}
		}
	}

	// --------------------------------------------//

	public void eventDownloadFinished(Download dl) {
		if (Boolean.parseBoolean(getProperty("DownloadHistory"))) {
			DownloadHistory.getInstance().addEntry(this, dl);
		}
		Formatters formatters = Plugin.getPluginInterface().getUtilities()
				.getFormatters();
		String notificationMessage = "Download Finished: "
				+ dl.getName()
				+ "\nTime: "
				+ formatters.formatTimeFromSeconds(dl.getStats()
						.getSecondsDownloading())
				+ "\nAverage Download Speed: "
				+ (dl.getStats().getSecondsDownloading() != 0 ? formatters
						.formatByteCountToKiBEtcPerSec(dl.getTorrent()
								.getSize()
								/ dl.getStats().getSecondsDownloading())
						: "undefined");

		PSupportStatusMailer mailer = (PSupportStatusMailer) Plugin
				.getPluginSupport(PSupportStatusMailer.IDENTIFIER);
		mailer.sendMessage(this, "Download Finished: " + dl.getName(),
				notificationMessage);
		PSupportAzJabber jabber = (PSupportAzJabber) Plugin
				.getPluginSupport(PSupportAzJabber.IDENTIFIER);
		jabber.sendMessage(this, notificationMessage);

		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_DL_FINISHED));
		event.setAttribute("name", dl.getName());
		event.setAttribute("hash", getDlHash(dl));
		event.setAttribute("duration", Long.toString(dl.getStats()
				.getSecondsDownloading()));
		if (dl.getTorrent() != null) {
			event.setAttribute("avgDownload", dl.getStats()
					.getSecondsDownloading() != 0 ? formatters
					.formatByteCountToKiBEtcPerSec(dl.getTorrent().getSize()
							/ dl.getStats().getSecondsDownloading())
					: "undefined");
		}
		eventQueue.offer(event);
	}

	public void eventDownloadRemoved(Download dl) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_DL_REMOVED));
		event.setAttribute("name", dl.getName());
		event.setAttribute("hash", getDlHash(dl));

		eventQueue.offer(event);
	}

	public void eventException(String e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_EXCEPTION));
		event.setAttribute("message", e);
		eventQueue.offer(event);
	}

	public void eventException(Exception e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_EXCEPTION));
		event.setAttribute("message", e.getMessage());
		eventQueue.offer(event);
	}

	public void eventException(Exception e, String add) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_EXCEPTION));
		event.setAttribute("message", e.getMessage() + "; " + add);
		eventQueue.offer(event);
	}

	public void eventUpdateAvailable() {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_UPDATE_AVAILABLE));
		eventQueue.offer(event);
	}

	public void eventMessage(String e) {
		Element event = getEventElement();
		event
				.setAttribute("type", Integer
						.toString(RemoteConstants.EV_MESSAGE));
		event.setAttribute("message", e);
		eventQueue.offer(event);
	}

	public void eventErrorMessage(String e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_ERROR_MESSAGE));
		event.setAttribute("message", e);
		eventQueue.offer(event);
	}

	public void eventPluginMessage(String targetID, Element e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer
				.toString(RemoteConstants.EV_PLUGIN_MESSAGE));
		event.setAttribute("targetID", targetID);
		event.addContent(e);
		eventQueue.offer(event);
	}
}
