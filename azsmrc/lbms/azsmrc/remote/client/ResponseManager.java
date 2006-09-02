package lbms.azsmrc.remote.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import lbms.azsmrc.remote.client.impl.DownloadAdvancedStatsImpl;
import lbms.azsmrc.remote.client.impl.DownloadFileImpl;
import lbms.azsmrc.remote.client.impl.DownloadFileManagerImpl;
import lbms.azsmrc.remote.client.impl.DownloadImpl;
import lbms.azsmrc.remote.client.impl.DownloadManagerImpl;
import lbms.azsmrc.remote.client.impl.DownloadStatsImpl;
import lbms.azsmrc.remote.client.impl.RemoteInfoImpl;
import lbms.azsmrc.remote.client.impl.RemoteUpdateImpl;
import lbms.azsmrc.remote.client.impl.RemoteUpdateManagerImpl;
import lbms.azsmrc.remote.client.impl.TrackerImpl;
import lbms.azsmrc.remote.client.impl.TrackerTorrentImpl;
import lbms.azsmrc.remote.client.impl.UserManagerImpl;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;

public class ResponseManager {
	private Map<String,ResponseHandler> handlerList = new HashMap<String, ResponseHandler>();
	private Client client;
	private DownloadManagerImpl dm;
	private Logger debug;

	public void addHandler (String request, ResponseHandler handler) {
		handlerList.put(request, handler);
	}

	public void removeHandler (String request) {
		handlerList.remove(request);
	}


	public void handleResponse (Document xmlResponse) throws IOException {
		Element requestRoot = xmlResponse.getRootElement();
		List<Element> queries = requestRoot.getChildren("Result");
		double protocolVersion = 1;
		long updates = 0;

		try {
			protocolVersion = requestRoot.getAttribute("version").getDoubleValue();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
		for (Element query:queries) {

			String request = query.getAttributeValue("switch");

			if (protocolVersion != RemoteConstants.CURRENT_VERSION) {
				//TODO call listener
				continue;
			}

			if (handlerList.containsKey(request)) {
				updates |= handlerList.get(request).handleRequest(query);
				continue;
			} else {
				//TODO call listener
			}
		}
		client.callClientUpdateListeners(updates);
	}

	private void updateDownload (Element dle) {
		String hash = dle.getAttributeValue("hash");
		if (hash == null) return;
		DownloadImpl dl;
		DownloadStatsImpl ds;
		boolean exists = dm.manHasDownload(hash);
		if (exists) {
			dl = dm.manGetDownload(hash);
			ds = dl.getStatsImpl();
			exists = true;
		} else {
			dl = new DownloadImpl(hash,client);
			ds = new DownloadStatsImpl();
			dl.implSetStats(ds);
			exists = false;
		}
		// Download Attributes
		if (dle.getAttributeValue("name") != null)
			dl.implSetName(dle.getAttributeValue("name"));
		dl.implSetForceStart(Boolean.parseBoolean(dle.getAttributeValue("forceStart")));
		dl.implSetChecking(Boolean.parseBoolean(dle.getAttributeValue("checking")));
		dl.implSetComplete(Boolean.parseBoolean(dle.getAttributeValue("complete")));
		try {
			dl.implSetPosition(dle.getAttribute("position").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetState(dle.getAttribute("state").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetDownloadLimit(dle.getAttribute("downloadLimit").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetUploadLimit(dle.getAttribute("uploadLimit").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetSeeds(dle.getAttribute("seeds").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetLeecher(dle.getAttribute("leecher").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetTotalSeeds(dle.getAttribute("total_seeds").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetTotalLeecher(dle.getAttribute("total_leecher").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetDiscarded(dle.getAttribute("discarded").getLongValue());
		} catch (Exception e) {}

		// Download Stats

		try {
			ds.setAvailability(dle.getAttribute("availability").getFloatValue());
		} catch (Exception e) {}
		try {
			ds.setDownloaded(dle.getAttribute("downloaded").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setUploaded(dle.getAttribute("uploaded").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setDownAvg(dle.getAttribute("downloadAVG").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setTotalAverage(dle.getAttribute("totalAVG").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setUpAvg(dle.getAttribute("uploadAVG").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setHealth(dle.getAttribute("health").getIntValue());
		} catch (Exception e) {}
		try {
			ds.setCompleted(dle.getAttribute("completition").getIntValue());
		} catch (Exception e) {}
		try {
			dl.implSetSize(dle.getAttribute("size").getLongValue());
		} catch (Exception e) {}
		try {
			ds.setShareRatio(dle.getAttribute("shareRatio").getIntValue());
		} catch (Exception e) {}
		try {
			if (dle.getAttribute("tracker") != null)
			ds.setTrackerStatus(dle.getAttributeValue("tracker"));
		} catch (Exception e) {}

		try {
			dl.implSetLastScrapeTime(Long.parseLong(dle.getAttributeValue("last_scrape")));
			dl.implSetNextScrapeTime(Long.parseLong(dle.getAttributeValue("next_scrape")));
		} catch (Exception e) {}

		try {
			dl.implSetAnnounceTimeToWait(Long.parseLong(dle.getAttributeValue("announceTimeToWait")));
		} catch (Exception e) {}

		if (dle.getAttribute("eta") != null)
			ds.setEta(dle.getAttributeValue("eta"));
		if (dle.getAttribute("elapsedTime") != null)
			ds.setElapsedTime(dle.getAttributeValue("elapsedTime"));
		if (dle.getAttribute("status") != null)
			ds.setStatus(dle.getAttributeValue("status"));
		if (!exists) dm.manAddDownload(dl);

	}

	public ResponseManager(Client caller) {
		this.client = caller;
		this.dm = caller.getDownloadManagerImpl();
		debug = Logger.getLogger("lbms.azsmrc.client");
		addHandler("_InvalidProtocolVersion_", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				return 0;
			}
		});
		addHandler("_UnhandledRequest_", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				try {
					debug.warning("Unhandled Request: "+xmlResponse.getChild("Error").getChild("Query").getAttributeValue("switch"));
					return 0;
				} catch (Exception e) {

					debug.warning("Unhandled Request");
					return 0;
				}
			}
		});
		addHandler("Ping", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				System.out.println("Received Pong.");
				return 0;
			}
		});
		addHandler("listTransfers", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				Element root = xmlResponse.getChild("Transfers");
				List<Element> transfers = root.getChildren("Transfer");
				for (Element t:transfers) {
					updateDownload(t);
				}
				return Constants.UPDATE_LIST_TRANSFERS;
			}
		});
		addHandler("updateDownloads", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				Element root = xmlResponse.getChild("Transfers");
				List<Element> transfers = root.getChildren("Transfer");
				for (Element t:transfers) {
					updateDownload(t);
				}
				return Constants.UPDATE_LIST_TRANSFERS;
			}
		});
		addHandler("getAdvancedStats", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				Element advStats = xmlResponse.getChild("AdvancedStats");
				DownloadImpl dl = dm.manGetDownload(xmlResponse.getAttributeValue("hash"));
				if (dl == null) return 0;
				DownloadAdvancedStatsImpl das = dl.getAdvancedStatsImpl();
				das.setComment(advStats.getAttributeValue("comment"));
				das.setCreatedOn(advStats.getAttributeValue("createdOn"));
				das.setPieceCount(Long.parseLong(advStats.getAttributeValue("pieceCount")));
				das.setPieceSize(Long.parseLong(advStats.getAttributeValue("pieceSize")));
				das.setSaveDir(advStats.getAttributeValue("saveDir"));
				das.setTrackerUrl(advStats.getAttributeValue("trackerUrl"));
				das.setLoaded(true);
				das.setLoading(false);
				return Constants.UPDATE_ADVANCED_STATS;
			}
		});
		addHandler("getFiles", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				Element fileList = xmlResponse.getChild("Files");
				String hash = xmlResponse.getAttributeValue("hash");
				DownloadImpl dl = dm.manGetDownload(hash);
				if (dl == null) return 0;
				DownloadFileManagerImpl dlFm = dl.getFileManagerImpl();
				List<Element> files = fileList.getChildren("File");
				DownloadFileImpl[] fileImpl;
				if (dlFm._isLoaded()) {
					fileImpl = dlFm.getFilesImpl();
				} else {
					fileImpl = new  DownloadFileImpl[files.size()];
				}
				for (int i=0;i<fileImpl.length;i++) {
					if (fileImpl[i] == null) fileImpl[i] = new DownloadFileImpl(hash, client);
					Element e = files.get(i);
					fileImpl[i].setDownloaded(Long.parseLong(e.getAttributeValue("downloaded")));
					fileImpl[i].setIndex(i);
					fileImpl[i].setLength(Long.parseLong(e.getAttributeValue("length")));
					fileImpl[i].setName(e.getAttributeValue("name"));
					fileImpl[i].setNumPieces(Integer.parseInt(e.getAttributeValue("numPieces")));
					fileImpl[i].setPriorityImpl(Boolean.parseBoolean(e.getAttributeValue("priority")));
					fileImpl[i].setSkippedImpl(Boolean.parseBoolean(e.getAttributeValue("skipped")));
				}
				if (!dlFm._isLoaded())
					dlFm.setDlFiles(fileImpl);
				dlFm.setLoaded(true);
				return Constants.UPDATE_DOWNLOAD_FILES;
			}
		});
		addHandler("globalStats", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				int receiveRate = 0,sendRate = 0;
				int s = 0, sq = 0, d = 0, dq = 0;
				try {
					sendRate = xmlResponse.getAttribute("sendRate").getIntValue();
					receiveRate = xmlResponse.getAttribute("receiveRate").getIntValue();
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (xmlResponse.getAttribute("seeding") != null)
				try {
					s = xmlResponse.getAttribute("seeding").getIntValue();
					sq = xmlResponse.getAttribute("seedqueue").getIntValue();
					d = xmlResponse.getAttribute("downloading").getIntValue();
					dq = xmlResponse.getAttribute("downloadqueue").getIntValue();
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				client.callGlobalStatsListener(receiveRate,sendRate,s,sq,d,dq);
				return 0;
			}
		});
		addHandler("getUsers", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				UserManagerImpl um = client.getUserManagerImpl();
				List<Element> userList = xmlResponse.getChild("Users").getChildren("User");
				List<String> userNames = new ArrayList<String>();
				for (Element u:userList) {
					userNames.add(u.getAttributeValue("username"));
					try {
						User user = um.getUser(u.getAttributeValue("username"));
						user.updateUser(u);
					} catch (UserNotFoundException e) {
						try {
							um.addUserImpl(u);
						} catch (DuplicatedUserException e1) {
						}
					}
				}
				um.keepUsers(userNames);
				return Constants.UPDATE_USERS;
			}
		});
		addHandler("Events", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				List<Element> events = xmlResponse.getChildren("Event");
				for (Element event:events) {
					try {
						int type = event.getAttribute("type").getIntValue();
						long time = event.getAttribute("time").getLongValue();
						switch (type) {
						case RemoteConstants.EV_DL_REMOVED:
							dm.manRemoveDownload(event.getAttributeValue("hash"));
							break;
						}
						client.callClientEventListener(type, time, event);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				return 0;
			}
		});
		addHandler("getAzParameter", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				try {
					client.callAzParameterListener(xmlResponse.getAttributeValue("key"),xmlResponse.getAttributeValue("value"),xmlResponse.getAttribute("type").getIntValue());
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
		});
		addHandler("getPluginParameter", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				try {
					client.callPluginParameterListener(xmlResponse.getAttributeValue("key"),xmlResponse.getAttributeValue("value"),xmlResponse.getAttribute("type").getIntValue());
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
		});
		addHandler("getCoreParameter", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				try {
					client.callCoreParameterListener(xmlResponse.getAttributeValue("key"),xmlResponse.getAttributeValue("value"),xmlResponse.getAttribute("type").getIntValue());
				} catch (DataConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
		});
		addHandler("getRemoteInfo", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				RemoteInfoImpl ri = client.getRemoteInfoImpl();
				ri.setAzureusVersion(xmlResponse.getAttributeValue("azureusVersion"));
				ri.setPluginVersion(xmlResponse.getAttributeValue("pluginVersion"));
				ri.setLoaded(true);
				return Constants.UPDATE_REMOTE_INFO;
			}
		});
		addHandler("getDriveInfo", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				RemoteInfoImpl ri = client.getRemoteInfoImpl();
				Map<String, String> map = new HashMap<String, String>();
				List<Element> eList = xmlResponse.getChildren("Directory");
				for (Element e:eList) {
					map.put(e.getAttributeValue("name"), e.getAttributeValue("free"));
					map.put(e.getAttributeValue("name")+".path", e.getAttributeValue("path"));
				}
				ri.setDriveInfo(map);
				ri.setLoading(false);
				return Constants.UPDATE_DRIVE_INFO;
			}
		});
		addHandler("getUpdateInfo", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException{
				RemoteUpdateManagerImpl rum = client.getRemoteUpdateManagerImpl();
				boolean avail = Boolean.parseBoolean(xmlResponse.getAttributeValue("updateAvailable"));
				if (avail) {
					rum.clear();
					rum.setUpdatesAvailable(avail);
					List<Element> updates = xmlResponse.getChildren("Update");
					for (Element u:updates) {
						rum.addUpdate(new RemoteUpdateImpl(u));
					}
				} else {
					rum.setUpdatesAvailable(avail);
				}
				return Constants.UPDATE_UPDATE_INFO;
			}
		});
		addHandler("getTrackerTorrents", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				TrackerImpl tracker = client.getTrackerImpl();
				List<Element> trackerTorrents = xmlResponse.getChild("TrackerTorrents").getChildren("TrackerTorrent");
				for (Element tt:trackerTorrents) {
					TrackerTorrentImpl tti = null;
					boolean newTorrent = false;
					if (tracker.hasTorrent(tt.getAttributeValue("hash"))) {
						tti = tracker.getTorrent(tt.getAttributeValue("hash"));
					} else {
						tti = new TrackerTorrentImpl(client);
						newTorrent = true;
					}
					try {
						tti.setHash(			tt.getAttributeValue("hash"));
						tti.setName(			tt.getAttributeValue("name"));
						tti.setAnnounceCount(	tt.getAttribute("announceCount").getLongValue());
						tti.setAvgAnnounceCount(tt.getAttribute("avgAnnounceCount").getLongValue());
						tti.setAnnounceCount(	tt.getAttribute("avgAnnounceCount").getLongValue());
						tti.setAvgBytesIn(		tt.getAttribute("avgBytesIn").getLongValue());
						tti.setAvgBytesOut(		tt.getAttribute("avgBytesOut").getLongValue());
						tti.setAvgDownloaded(	tt.getAttribute("avgDownloaded").getLongValue());
						tti.setAvgUploaded(		tt.getAttribute("avgUploaded").getLongValue());
						tti.setAvgScrapeCount(	tt.getAttribute("avgScrapeCount").getLongValue());
						tti.setCompletedCount(	tt.getAttribute("completedCount").getLongValue());
						tti.setTotalLeft(		tt.getAttribute("totalLeft").getLongValue());
						tti.setDateAdded(		tt.getAttribute("dateAdded").getLongValue());
						tti.setScrapeCount(		tt.getAttribute("scrapeCount").getLongValue());
						tti.setTotalBytesOut(	tt.getAttribute("totalBytesOut").getLongValue());
						tti.setTotalBytesIn(	tt.getAttribute("totalBytesIn").getLongValue());
						tti.setTotalDownloaded(	tt.getAttribute("totalDownloaded").getLongValue());
						tti.setTotalUploaded(	tt.getAttribute("totalUploaded").getLongValue());
						tti.setSeedCount(		tt.getAttribute("seedCount").getIntValue());
						tti.setLeecherCount(	tt.getAttribute("leecherCount").getIntValue());
						tti.setStatus(			tt.getAttribute("status").getIntValue());
						tti.setBadNATCount(		tt.getAttribute("badNATCount").getIntValue());
						tti.setPassive(			tt.getAttribute("isPassive").getBooleanValue());
						tti.setCanBeRemoved(	tt.getAttribute("canBeRemoved").getBooleanValue());
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
					if (newTorrent) tracker.addTorrent(tti);
				}
				return Constants.UPDATE_TRACKER;
			}
		});
		addHandler("ipcCall", new ResponseHandler() {
			public long handleRequest(Element xmlResponse) throws IOException {
				client.callIPCResponseListeners	(Integer.parseInt(xmlResponse.getAttributeValue("status")),
						xmlResponse.getAttributeValue("senderID"),	xmlResponse.getAttributeValue("pluginID"),
						xmlResponse.getAttributeValue("method"), xmlResponse.getChild("Result"));
				return 0;
			}
		});
	}
}
