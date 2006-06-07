/**
 * 
 */
package lbms.azsmrc.plugin.web;

import java.util.List;
import java.util.Vector;

import lbms.azsmrc.plugin.main.User;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.download.DownloadManagerListener;
import org.jdom.Element;

/**
 * @author Damokles
 *
 */
public class DownloadContainerManager {
	private List<DownloadContainer> dcList = new Vector<DownloadContainer>();
	private DownloadManagerListener dml = new DownloadManagerListener() {
		/* (non-Javadoc)
		 * @see org.gudy.azureus2.plugins.download.DownloadManagerListener#downloadAdded(org.gudy.azureus2.plugins.download.Download)
		 */
		public void downloadAdded(Download download) {
			dcList.add(new DownloadContainer(download));
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
	};

	public DownloadContainerManager (DownloadManager dm) {
		dm.addListener(dml);
	}

	public Element updateDownloadsFromUser (User user, boolean full) {
		Element transferList = new Element("Transfers");
		for (DownloadContainer dc:dcList) {
			if(user.hasDownload(dc.getDownload())) {
				if (full)
					transferList.addContent(dc.fullUpdate());
				else
					transferList.addContent(dc.updateAndGetDiff());
			}
		}
		return transferList;
	}

	public Element updateDownload (boolean full) {
		Element transferList = new Element("Transfers");
		for (DownloadContainer dc:dcList) {
			if (full)
				transferList.addContent(dc.fullUpdate());
			else
				transferList.addContent(dc.updateAndGetDiff());
		}
		return transferList;
	}
}
