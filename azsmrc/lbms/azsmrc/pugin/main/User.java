package lbms.azsmrc.pugin.main;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class User extends lbms.azsmrc.shared.User {

	private Set<String> downloadList = new HashSet<String>();
	private Queue<Element> eventQueue = new LinkedList<Element>();

	/**
	 * Creates a User object and reads the data
	 * from xml document
	 *
	 * @param userElement
	 */
	public User (Element userElement) {
		super(userElement);
		List<Element> downloads = userElement.getChildren("Download");
		for (Element download:downloads) {
			this.addDownload(download.getTextTrim());
		}
	}


	public User(String username, String password, String autoImportDir, String outputDir, int downloadSlots, int userRights) {
		super(username, password, autoImportDir, outputDir, downloadSlots, userRights);
	}

	public User(String username, String password) {
		super(username, password);
	}

	/**
	 * Adds the download to the user.
	 * 
	 * @param download
	 */
	public void addDownload (Download download) {
		downloadList.add(getDlHash(download));
	}

	/**
	 * @param download
	 */
	public void addDownload (String downloadHash) {
		try {
			Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(downloadHash));
			if (dl != null)
				downloadList.add(downloadHash);
		} catch (DownloadException e) {
		}

	}

	/**
	 * This keeps only Downloads that are in both Collections.
	 *
	 * @param dls other collection to intersect with
	 */
	public void retainDownloads (Collection<String> dls) {
		downloadList.retainAll(dls);
	}

	/**
	 * The download will be removed from the user.
	 * 
	 * @param download
	 */
	public void removeDownload (String download) {
		downloadList.remove(download);
	}

	/**
	 * The download will be removed from the user.
	 * 
	 * @param download
	 */
	public void removeDownload (Download download) {
		downloadList.remove(getDlHash(download));
	}

	/**
	 * @return list of Torrentnames
	 */
	public String[] getDownloads () {
		return downloadList.toArray(new String[] {});
	}


	/**
	 * Checks if the user is an owner of the Download
	 * 
	 * @param dlHash
	 * @return
	 */
	public boolean hasDownload(String dlHash) {
		return downloadList.contains(dlHash);
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
	 * This function will return the Base64 String
	 * represantation of the downloadHash
	 * 
	 * @param dl
	 * @return String represantation of the downloadHash
	 */
	public String getDlHash(Download dl) {
		return EncodingUtil.encode(dl.getTorrent().getHash());
	}

	/**
	 * This function will convert the User object
	 * to a xml encoded jdom Element.
	 * 
	 * @return the jdom Element represantation of the object
	 */
	public Element toElement () {
		Element user = super.toElement();
		for (String download:downloadList) {
			Element downloadElement = new Element("Download");
			downloadElement.setText(download);
			user.addContent(downloadElement);
		}
		return user;
	}

	/**
	 * Use this function to check if there are events
	 * in the eventQueue
	 *
	 * @return true if events are in queue
	 */
	public boolean hasEvents() {
		return !eventQueue.isEmpty();
	}

	/**
	 * This function will put all events in the root
	 * Element.
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

	//--------------------------------------------//

	public void eventDownloadFinished(Download dl) {
		Element event = getEventElement();
		event.setAttribute("type", Integer.toString(RemoteConstants.EV_DL_FINISHED));
		event.setAttribute("name", dl.getName());
		event.setAttribute("hash", getDlHash(dl));
		removeDownload(dl);
		eventQueue.offer(event);
	}

	public void eventDownloadRemoved(Download dl) {
		Element event = getEventElement();
		event.setAttribute("type", Integer.toString(RemoteConstants.EV_DL_REMOVED));
		event.setAttribute("name", dl.getName());
		event.setAttribute("hash", getDlHash(dl));

		eventQueue.offer(event);
	}

	public void eventDownloadException (DownloadException e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer.toString(RemoteConstants.EV_DL_EXCEPTION));
		event.setAttribute("message", e.getMessage());
		eventQueue.offer(event);
	}

	public void eventException (Exception e) {
		Element event = getEventElement();
		event.setAttribute("type", Integer.toString(RemoteConstants.EV_EXCEPTION));
		event.setAttribute("message", e.getMessage());
		eventQueue.offer(event);
	}
}
