package lbms.azsmrc.plugin.main.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lbms.azsmrc.plugin.main.MultiUser;
import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;

import org.gudy.azureus2.plugins.download.Download;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author Damokles
 * 
 */
public class DownloadHistory {

	private static DownloadHistory									instance;
	private SoftReference<Map<String, Set<DownloadHistoryEntry>>>	weakDownloadHistory	= null;
	private Map<String, Set<DownloadHistoryEntry>>					downloadHistory		= null;

	private DownloadHistory() {
	}

	public static DownloadHistory getInstance() {
		if (instance == null) {
			instance = new DownloadHistory();
		}
		return instance;
	}

	public void addEntry(User user, Download dl) {
		loadDownloadHistory();
		if (!downloadHistory.containsKey(user.getUsername())) {
			downloadHistory.put(user.getUsername(),
					new TreeSet<DownloadHistoryEntry>());
		}
		downloadHistory.get(user.getUsername()).add(
				new DownloadHistoryEntry(System.currentTimeMillis() / 1000, dl
						.getName(), dl.getAttribute(MultiUser.TA_CATEGORY)));
		saveDownloadHistory();
	}

	public DownloadHistoryEntry[] getEntries(User user, long startDate,
			long endDate) {
		loadDownloadHistory();

		List<DownloadHistoryEntry> resultList = new ArrayList<DownloadHistoryEntry>();
		Set<DownloadHistoryEntry> entries = downloadHistory.get(user
				.getUsername());
		if (entries != null) {
			for (DownloadHistoryEntry dhe : entries) {
				if (startDate <= dhe.getTimestamp()
						&& endDate >= dhe.getTimestamp()) {
					resultList.add(dhe);
				}
			}
		}

		unloadDownloadHistory();

		return resultList.toArray(new DownloadHistoryEntry[resultList.size()]);
	}

	private synchronized void loadDownloadHistory() {
		if (downloadHistory != null) {
			System.out.println("DownloadHistory was still loaded");
			return; // already loaded
		}
		if (weakDownloadHistory != null) {
			downloadHistory = weakDownloadHistory.get();
			if (downloadHistory != null) {
				System.out
						.println("DownloadHistory was still loaded via WeakRefernce");
				return; // Weak Reference was still there
			}
		}
		System.out.println("DownloadHistory needs to be reloaded from file");

		downloadHistory = new HashMap<String, Set<DownloadHistoryEntry>>();

		File dhFile = new File(Plugin.getPluginInterface()
				.getPluginDirectoryName(), "DownloadHistory.xml.gz");
		if (dhFile.exists()) {
			InputStream is = null;
			try {
				is = new GZIPInputStream(new FileInputStream(dhFile));
				SAXBuilder builder = new SAXBuilder();
				Document xmlDom = builder.build(is);
				Element root = xmlDom.getRootElement();
				List<Element> users = root.getChildren("User");
				for (Element user : users) {
					TreeSet<DownloadHistoryEntry> list = new TreeSet<DownloadHistoryEntry>();
					downloadHistory.put(user.getAttributeValue("name"), list);
					List<Element> elements = user.getChildren("Entry");
					for (Element element : elements) {
						DownloadHistoryEntry entry = new DownloadHistoryEntry(
								element);
						list.add(entry);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	private synchronized void saveDownloadHistory() {
		if (downloadHistory == null) {
			return; // error DH was already unloaded
		}

		Document doc = new Document();
		Element root = new Element("DownloadHistory");
		doc.setRootElement(root);
		Set<String> users = downloadHistory.keySet();
		for (String user : users) {
			System.out.println("DownloadHistory adding user entries for: "
					+ user);
			Element userElement = new Element("User");
			root.addContent(userElement);
			userElement.setAttribute("name", user);
			Set<DownloadHistoryEntry> entries = downloadHistory.get(user);
			if (entries != null) {
				for (DownloadHistoryEntry e : entries) {
					userElement.addContent(e.toElement());
				}
			}
		}
		File dhFile = new File(Plugin.getPluginInterface()
				.getPluginDirectoryName(), "DownloadHistory.xml.gz");
		OutputStream os = null;
		try {
			os = new GZIPOutputStream(new FileOutputStream(dhFile));
			XMLOutputter out = new XMLOutputter();
			out.output(doc, os);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}

		unloadDownloadHistory();
	}

	private synchronized void unloadDownloadHistory() {
		if (downloadHistory == null) {
			return; // error DH was already unloaded
		}
		weakDownloadHistory = new SoftReference<Map<String, Set<DownloadHistoryEntry>>>(
				downloadHistory); // add weak reference for quick loading
		downloadHistory = null; // remove strong reference
	}
}
