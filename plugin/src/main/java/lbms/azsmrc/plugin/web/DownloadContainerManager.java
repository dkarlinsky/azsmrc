package lbms.azsmrc.plugin.web;

import java.util.List;
import java.util.Vector;

import lbms.azsmrc.plugin.main.MultiUser;
import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.shared.RemoteConstants;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.download.DownloadManagerListener;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class DownloadContainerManager implements DownloadManagerListener {
	private DownloadManager dm;
	private User user;
	private List<DownloadContainer> dcList = new Vector<DownloadContainer>();


	public DownloadContainerManager (DownloadManager dm, User user) {
		this.dm = dm;
		this.dm.addListener(this);
		this.user = user;
	}

	private Element updateDownloadsFromUser (boolean full) {
		Element transferList = new Element("Transfers");
		for (DownloadContainer dc:dcList) {
			if(user.hasDownload(dc.getDownload()) || (user.checkAccess(RemoteConstants.RIGHTS_SEE_PUBLICDL) && MultiUser.isPublicDownload(dc.getDownload()))) {
				try {
					if (full)
						transferList.addContent(dc.fullUpdate());
					else
						transferList.addContent(dc.updateAndGetDiff());
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		return transferList;
	}

	private Element updateDownloadSingleUser (boolean full) {
		Element transferList = new Element("Transfers");
		for (DownloadContainer dc:dcList) {
			try {
				if (full)
					transferList.addContent(dc.fullUpdate());
				else
					transferList.addContent(dc.updateAndGetDiff());
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return transferList;
	}

	public Element updateDownload (boolean full) {
		boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
		return (singleUser) ? updateDownloadSingleUser(full) : updateDownloadsFromUser(full);
	}

	public void destroy() {
		dm.removeListener(this);
		dcList.clear();
		dcList = null;
		user = null;
	}

	/* DownloadManagerListener Methods */


	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.download.DownloadManagerListener#downloadAdded(org.gudy.azureus2.plugins.download.Download)
	 */
	public void downloadAdded(Download download) {
		try {
			dcList.add(new DownloadContainer(download));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.download.DownloadManagerListener#downloadRemoved(org.gudy.azureus2.plugins.download.Download)
	 */
	public void downloadRemoved(Download download) {
		for (int i=0;i<dcList.size();i++) {
			if (dcList.get(i).getDownload() == download) {
				dcList.remove(i);
				break;
			}
		}

	}
}
