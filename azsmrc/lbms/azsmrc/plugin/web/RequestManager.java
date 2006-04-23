package lbms.azsmrc.plugin.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import lbms.azsmrc.plugin.main.MultiUser;
import lbms.azsmrc.plugin.main.MultiUserDownloadListener;
import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

import org.apache.commons.io.FileSystemUtils;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.download.DownloadRemovalVetoException;
import org.gudy.azureus2.plugins.download.DownloadStats;
import org.gudy.azureus2.plugins.download.DownloadWillBeAddedListener;
import org.gudy.azureus2.plugins.peers.PeerManagerStats;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.gudy.azureus2.plugins.torrent.TorrentManager;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.update.Update;
import org.gudy.azureus2.plugins.update.UpdateCheckInstance;
import org.gudy.azureus2.plugins.update.UpdateException;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class RequestManager {
	private Map<String,RequestHandler> handlerList = new HashMap<String, RequestHandler>();
	private static RequestManager instance = new RequestManager();
	private Map<String, Integer[]> downloadControlList = new HashMap<String, Integer[]>();

	private boolean restart = false;

	/**
	 * @return Returns the instance.
	 */
	public static RequestManager getInstance() {
		return instance;
	}

	public void initialize (PluginInterface pi) {
		System.out.println("AzSMRC: adding DownloadWillBeAddedListener");
		pi.getDownloadManager().addDownloadWillBeAddedListener(new DownloadWillBeAddedListener() {
			public void initialised(Download dl) {
				String hash = EncodingUtil.encode(dl.getTorrent().getHash());
				System.out.println("DownloadWillBeAddedListener checking: "+hash);
				if (downloadControlList.containsKey(hash)) {
					System.out.println("DownloadWillBeAddedListener found: "+hash);
					Integer[] options = downloadControlList.get(hash);
					DiskManagerFileInfo[] dmfi=  dl.getDiskManagerFileInfo();
					if (dmfi.length != options.length) {
						System.out.println("DownloadWillBeAddedListener Array Sizes don't match.");
						System.out.println("localFiles: "+dmfi.length+", remote: "+options.length);
						return;
					}
					for (int i=0; i<dmfi.length;i++) {
						 switch (options[i]) {
						 case 1:
							 break;
						 case 2:
							 dmfi[i].setPriority(true);
							 break;
						 case 0:
							 dmfi[i].setSkipped(true);
							 dmfi[i].setDeleted(true);
							 break;
						 }
					}
				}
			};
		});
	}

	public void addHandler (String request, RequestHandler handler) {
		handlerList.put(request, handler);
	}

	public void removeHandler (String request) {
		handlerList.remove(request);
	}

	public void invalidXMLError (TrackerWebPageResponse response) {

	}

	public void handleRequest (Document xmlRequest, TrackerWebPageResponse response, User user, boolean useCompression) throws IOException {
		Element requestRoot = xmlRequest.getRootElement();
		List<Element> queries = requestRoot.getChildren("Query");
		double protocolVersion = 1;

		response.setContentType("text/xml");

		try {
			protocolVersion = requestRoot.getAttribute("version").getDoubleValue();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
		Document xmlResponse = new Document();
		Element responseRoot = new Element("Response");
		for (Element query:queries) {
			Element newResponse = new Element ("Result");
			String id = query.getAttributeValue("id");
			if (id != null) newResponse.setAttribute("id", id);
			String request = query.getAttributeValue("switch");
			responseRoot.setAttribute("version", Double.toString(RemoteConstants.CURRENT_VERSION));

			if (protocolVersion != RemoteConstants.CURRENT_VERSION) {
				if (handlerList.get("_InvalidProtocolVersion_").handleRequest(query,newResponse,user))
					responseRoot.addContent(newResponse);
				continue;
			}

			if (handlerList.containsKey(request)) {
				newResponse.setAttribute("switch", request);
				try {
					if (handlerList.get(request).handleRequest(query,newResponse,user))
						responseRoot.addContent(newResponse);
				} catch (Exception e) {
					Element debug = new Element ("Debug");
					debug.setAttribute("debugMsg", e.getMessage());
					Element stackTrace = new Element ("StackTrace");
					String sTrace = "";
					for ( StackTraceElement trace:e.getStackTrace()) {
						sTrace += trace+"\n";
					}
					stackTrace.setText(sTrace);
					debug.addContent((Element)query.clone());
					debug.addContent(sTrace);
					responseRoot.addContent(debug);
				}
				continue;
			} else {
				if (handlerList.get("_UnhandledRequest_").handleRequest(query,newResponse,user))
					responseRoot.addContent(newResponse);
			}
		}

		xmlResponse.addContent(responseRoot);

		if (!requestRoot.getAttributeValue("noResponse", "false").equalsIgnoreCase("true")) {
			if (user.hasEvents() && !requestRoot.getAttributeValue("noEvents", "false").equalsIgnoreCase("true"))
				responseRoot.addContent(sendEvents(user));
			XMLOutputter out = new XMLOutputter();
			if (useCompression) {
				GZIPOutputStream gos = new GZIPOutputStream(response.getOutputStream());
				response.setHeader("Content-Encoding", "gzip");
				out.output(xmlResponse,gos);
				gos.finish();
			} else {
				out.output(xmlResponse,response.getOutputStream());
			}
		}
		System.out.println("\nResponse:");
		new XMLOutputter(Format.getPrettyFormat()).output(xmlResponse, System.out);		//Response

		if (restart) {
			System.out.println("Restarting Azureus");
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Plugin.getPluginInterface().getUpdateManager().applyUpdates(true);
					} catch (UpdateException e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}

	private Element sendEvents(User user) {
		Element response = new Element("Result");
		response.setAttribute("switch", "Events");
		user.getEvents(response);
		return response;
	}

	private void setDlStats (Element dle, Download dl, int switches) {
		DownloadStats ds = dl.getStats();
		dle.setAttribute("name", dl.getName());
		dle.setAttribute("hash", EncodingUtil.encode(dl.getTorrent().getHash()));
		dle.setAttribute("forceStart", Boolean.toString(dl.isForceStart()));
		dle.setAttribute("position", Integer.toString(dl.getPosition()));
		if ((switches & RemoteConstants.ST_DOWNLOADED) != 0)
			dle.setAttribute("downloaded", Long.toString(ds.getDownloaded()));
		if ((switches & RemoteConstants.ST_UPLOADED) != 0)
			dle.setAttribute("uploaded", Long.toString(ds.getUploaded()));
		if ((switches & RemoteConstants.ST_DOWNLOAD_AVG) != 0)
			dle.setAttribute("downloadAVG", Long.toString(ds.getDownloadAverage()));
		if ((switches & RemoteConstants.ST_UPLOAD_AVG) != 0)
			dle.setAttribute("uploadAVG", Long.toString(ds.getUploadAverage()));
		if ((switches & RemoteConstants.ST_TOTAL_AVG) != 0)
			dle.setAttribute("totalAVG", Long.toString(ds.getTotalAverage()));
		if ((switches & RemoteConstants.ST_STATE) != 0)
			dle.setAttribute("state", Integer.toString(dl.getState()));
		if ((switches & RemoteConstants.ST_STATUS) != 0)
			dle.setAttribute("status", ds.getStatus());
		if ((switches & RemoteConstants.ST_ELAPSED_TIME) != 0)
			dle.setAttribute("elapsedTime", ds.getElapsedTime());
		if ((switches & RemoteConstants.ST_ETA) != 0)
			dle.setAttribute("eta", ds.getETA());
		if ((switches & RemoteConstants.ST_AVAILABILITY) != 0)
			dle.setAttribute("availability", Float.toString(ds.getAvailability()));
		if ((switches & RemoteConstants.ST_COMPLETITION) != 0)
			dle.setAttribute("completition", Integer.toString(ds.getDownloadCompleted(false)));
		if ((switches & RemoteConstants.ST_HEALTH) != 0)
			dle.setAttribute("health", Integer.toString(ds.getHealth()));
		if ((switches & RemoteConstants.ST_SHARE) != 0)
			dle.setAttribute("shareRatio", Integer.toString(ds.getShareRatio()));
		if ((switches & RemoteConstants.ST_TRACKER) != 0)
			dle.setAttribute("tracker",ds.getTrackerStatus());
		if ((switches & RemoteConstants.ST_LIMIT_DOWN) != 0)
			dle.setAttribute("downloadLimit",Integer.toString(dl.getMaximumDownloadKBPerSecond()));
		if ((switches & RemoteConstants.ST_LIMIT_UP) != 0)
			dle.setAttribute("uploadLimit",Integer.toString(dl.getUploadRateLimitBytesPerSecond()));

		if (dl.getState() == Download.ST_DOWNLOADING || dl.getState() == Download.ST_SEEDING) { //Stopped DLs don't have a PeerManager
			PeerManagerStats pms = dl.getPeerManager().getStats();
			if ((switches & RemoteConstants.ST_SEEDS) != 0)
				dle.setAttribute("seeds",Integer.toString(pms.getConnectedSeeds()));
			if ((switches & RemoteConstants.ST_LEECHER) != 0)
				dle.setAttribute("leecher",Integer.toString(pms.getConnectedLeechers()));
			if ((switches & RemoteConstants.ST_DISCARDED) != 0)
				dle.setAttribute("discarded",Long.toString(pms.getDiscarded()));
		}
		if ((switches & RemoteConstants.ST_TOTAL_SEEDS) != 0)
			dle.setAttribute("total_seeds",Integer.toString(dl.getLastScrapeResult().getSeedCount()));
		if ((switches & RemoteConstants.ST_TOTAL_LEECHER) != 0)
			dle.setAttribute("total_leecher",Integer.toString(dl.getLastScrapeResult().getNonSeedCount()));
		if ((switches & RemoteConstants.ST_SIZE) != 0)
			dle.setAttribute("size",Long.toString(dl.getTorrent().getSize()));
	}


	private RequestManager() {
		addHandler("_InvalidProtocolVersion_", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				response.setAttribute("switch", "_InvalidProtocolVersion_");
				Element error = new Element("Error");
				error.setText("Invalid Protocol Version.");
				error.setAttribute("code", Integer.toString(RemoteConstants.E_INVALID_PROTOCOL));
				response.addContent(error);
				return true;
			}
		});
		addHandler("_UnhandledRequest_", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				Plugin.addToLog("_UnhandledRequest_: "+xmlRequest.getAttributeValue("switch"));
				response.setAttribute("switch", "_UnhandledRequest_");
				Element error = new Element("Error");
				Element xmlErroneousRequest = (Element) xmlRequest.clone();
				error.addContent(xmlErroneousRequest);
				error.setAttribute("code", Integer.toString(RemoteConstants.E_INVALID_REQUEST));
				response.addContent(error);
				return true;
			}
		});
		addHandler("Ping", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				response.setAttribute("switch", "Ping");
				response.setText("Pong");
				return true;
			}
		});
		addHandler("listTransfers", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				//response.setAttribute("switch", "listTransfers");
				int switches = 0;
				try {
					switches = xmlRequest.getAttribute("options").getIntValue();
				} catch (Exception e) {}
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				Element transferList = new Element("Transfers");
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(true);
				for (Download dl:dlList) {
					if(singleUser || user.hasDownload(dl)) {
						Element dle = new Element ("Transfer");
						setDlStats(dle, dl, switches);
						transferList.addContent(dle);
					}
				}
				response.addContent(transferList);
				return true;
			}
		});
		addHandler("addDownload", new RequestHandler() {
			public boolean handleRequest(final Element xmlRequest, Element response,final User user) throws IOException {
				String location = xmlRequest.getAttributeValue("location");
				if (location.equalsIgnoreCase("URL")) {
					final String url = xmlRequest.getAttributeValue("url");
					new Thread(new Runnable (){
						public void run() {
							try {
								TorrentManager torrentManager = Plugin.getPluginInterface().getTorrentManager();
								TorrentAttribute ta = torrentManager.getAttribute(TorrentAttribute.TA_CATEGORY);
								String username = xmlRequest.getAttributeValue("username");
								String password = xmlRequest.getAttributeValue("password");
								Torrent newTorrent = null;
								if (username != null && password != null)
									newTorrent = torrentManager.getURLDownloader(new URL(url),username,password).download();
								else
									newTorrent = torrentManager.getURLDownloader(new URL(url)).download();
								Download dl = Plugin.getPluginInterface().getDownloadManager().addDownload(newTorrent);
								user.addDownload(dl);
								try {
									Plugin.getXMLConfig().saveConfigFile();
								} catch (IOException e) {}

								if (dl.isComplete()) {
									DiskManagerFileInfo[] fileInfo = dl.getDiskManagerFileInfo();
									final File[] files = new File[fileInfo.length];
									for (int i=0;i<files.length;i++) {
										files[i] = fileInfo[i].getFile();
									}
									for (File file:files) {
										try {
											Utilities.copy(file, new File(user.getOutputDir()), false);
										} catch (IOException e) {
											Plugin.addToLog(e.getMessage());
											e.printStackTrace();
										}
									}
									user.eventDownloadFinished(dl);
								} else if (dl.getAttribute(ta) == null) {
									dl.setAttribute(ta, user.getUsername());
									dl.addListener(MultiUserDownloadListener.getInstance());
								} else {
									dl.setAttribute(ta, MultiUser.SHARED_CAT_NAME);
								}
							} catch (MalformedURLException e) {
								user.eventException(e);
								e.printStackTrace();
							} catch (TorrentException e) {
								user.eventException(e,url);
								e.printStackTrace();
							} catch (DownloadException e) {
								user.eventException(e);
							}
						};
					}).start();
				} else if (location.equalsIgnoreCase("XML")) {
					String torrentData = xmlRequest.getChildText("Torrent");
					try {
						TorrentManager torrentManager = Plugin.getPluginInterface().getTorrentManager();
						TorrentAttribute ta = torrentManager.getAttribute(TorrentAttribute.TA_CATEGORY);
						Torrent newTorrent = torrentManager.createFromBEncodedData(EncodingUtil.decode(torrentData));

						String fileOptions = xmlRequest.getChild("Torrent").getAttributeValue("fileOptions");
						if (fileOptions!=null) {
							int[] opt = EncodingUtil.StringToIntArray(fileOptions);
							System.out.println("AzSMRC addDL: found FileOptions: "+EncodingUtil.IntArrayToString(opt));
							Integer[] options = new Integer[opt.length];
							for (int i=0; i<opt.length;i++) {
								options[i] = opt[i];
							}
							downloadControlList.put(EncodingUtil.encode(newTorrent.getHash()), options);
							System.out.println("AzSMRC addDL downloadControlList.size:" +downloadControlList.size());
						}

						Download dl = Plugin.getPluginInterface().getDownloadManager().addDownload(newTorrent);

						user.addDownload(dl);
						Plugin.getXMLConfig().saveConfigFile();

						if (dl.isComplete()) {
							DiskManagerFileInfo[] fileInfo = dl.getDiskManagerFileInfo();
							final File[] files = new File[fileInfo.length];
							for (int i=0;i<files.length;i++) {
								files[i] = fileInfo[i].getFile();
							}
							for (File file:files) {
								try {
									Utilities.copy(file, new File(user.getOutputDir()), false);
								} catch (IOException e) {
									Plugin.addToLog(e.getMessage());
									e.printStackTrace();
								}
							}
							user.eventDownloadFinished(dl);
						} else if (dl.getAttribute(ta) == null) {
							dl.setAttribute(ta, user.getUsername());
							dl.addListener(MultiUserDownloadListener.getInstance());
						} else {
							dl.setAttribute(ta, MultiUser.SHARED_CAT_NAME);
						}
					} catch (TorrentException e) {
						user.eventException(e);
						e.printStackTrace();
					} catch (DownloadException e) {
						user.eventException(e);
					}
				}
				return false;
			}
		});
		addHandler("removeDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						if (dl.getState() != Download.ST_STOPPED)dl.stop();
						if (xmlRequest.getAttributeValue("delTorrent")!=null) {
							try {
								dl.remove(Boolean.parseBoolean(xmlRequest.getAttributeValue("delTorrent")),Boolean.parseBoolean(xmlRequest.getAttributeValue("delData")));
							} catch(NullPointerException e) {
							}
						}
						else
							dl.remove();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					} catch (DownloadRemovalVetoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("stopDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.stop();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("stopAndQueueDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.stopAndQueue();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("restartDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.restart();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("recheckDataDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.recheckData();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("setMaximumDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.setMaximumDownloadKBPerSecond(xmlRequest.getAttribute("limit").getIntValue());
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					} catch (DataConversionException e) {

					}
				}
				return false;
			}
		});
		addHandler("setMaximumUpload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.setUploadRateLimitBytesPerSecond(xmlRequest.getAttribute("limit").getIntValue());
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					} catch (DataConversionException e) {

					}
				}
				return false;
			}
		});
		addHandler("startDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.start();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("setPosition", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						int newpos = xmlRequest.getAttribute("position").getIntValue();
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.setPosition(newpos);
					} catch (DataConversionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("setForceStart", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if ((singleUser || user.hasDownload(hash)) && user.checkAccess(RemoteConstants.RIGHTS_FORCESTART)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.setForceStart(Boolean.parseBoolean(xmlRequest.getAttributeValue("start")));
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveToPosition", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						int newpos = xmlRequest.getAttribute("position").getIntValue();
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.moveTo(newpos);
					} catch (DataConversionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveUp", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.moveUp();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveDown", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.moveDown();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("scrapeDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						dl.getLastScrapeResult().setNextScrapeStartTime(System.currentTimeMillis());
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveDataFiles", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,final User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						final Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						final File target = new File (xmlRequest.getAttributeValue("target"));
						if (target.exists() && target.isDirectory()) {
							new Thread(new Runnable() {
								public void run() {
									try {
										if (!dl.isPaused())
											dl.stop();
										dl.moveDataFiles(target);
										dl.restart();
									} catch (DownloadException e) {
										user.eventException(e);
										e.printStackTrace();
									}
								};
							}).start();
						}
						else
							user.eventException(new Exception("Error Moving Data Files: Directory not found"));
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("getDownloadStats", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				response.setAttribute("switch", "listTransfers");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Element transferList = new Element("Transfers");
						int options = xmlRequest.getAttribute("options").getIntValue();
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						Element dle = new Element ("Transfer");
						setDlStats(dle, dl, options);
						transferList.addContent(dle);
						response.addContent(transferList);
					} catch (DataConversionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		addHandler("startAllDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(true);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				for (Download dl:dlList) {
					if(singleUser || user.hasDownload(dl)) {
						try {
							dl.restart();
						} catch (DownloadException e) {
							user.eventException(e);
							e.printStackTrace();
						}
					}
				}
				return false;
			}
		});
		addHandler("stopAllDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(true);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				for (Download dl:dlList) {
					if(singleUser || user.hasDownload(dl)) {
						try {
							dl.stop();
						} catch (DownloadException e) {
							user.eventException(e);
							e.printStackTrace();
						}
					}
				}
				return false;
			}
		});
		addHandler("getFiles", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				response.setAttribute("hash", hash);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						DiskManagerFileInfo[] dmfi=  dl.getDiskManagerFileInfo();
						Element files = new Element("Files");
						for (int i=0; i<dmfi.length;i++) {
							Element file = new Element("File");
							file.setAttribute("name", dmfi[i].getFile().getPath());
							file.setAttribute("length", Long.toString(dmfi[i].getLength()));
							file.setAttribute("numPieces", Long.toString(dmfi[i].getNumPieces()));
							file.setAttribute("downloaded", Long.toString(dmfi[i].getDownloaded()));
							file.setAttribute("priority",Boolean.toString(dmfi[i].isPriority()));
							file.setAttribute("skipped",Boolean.toString(dmfi[i].isSkipped()));
							file.setAttribute("deleted",Boolean.toString(dmfi[i].isDeleted()));
							files.addContent(file);
						}
						response.addContent(files);
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		addHandler("setFileOptions", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						DiskManagerFileInfo[] dmfi=  dl.getDiskManagerFileInfo();
						if (xmlRequest.getAttributeValue("index")!=null) {
							try {
								boolean priority = Boolean.parseBoolean(xmlRequest.getAttributeValue("priority"));
								boolean skipped = Boolean.parseBoolean(xmlRequest.getAttributeValue("skipped"));
								boolean deleted = Boolean.parseBoolean(xmlRequest.getAttributeValue("deleted"));
								int index = Integer.parseInt(xmlRequest.getAttributeValue("index"));
								if (dmfi[index].isPriority() != priority)
									dmfi[index].setPriority(priority);
								if (dmfi[index].isSkipped() != skipped)
									dmfi[index].setSkipped(skipped);
								if (dmfi[index].isDeleted() != deleted)
									dmfi[index].setDeleted(deleted);
							} catch(NullPointerException e) {
							}
						}
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("getAdvancedStats", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException{
				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(EncodingUtil.decode(hash));
						Torrent tor = dl.getTorrent();
						response.setAttribute("hash",hash);
						Element as = new Element ("AdvancedStats");
						as.setAttribute("trackerUrl", tor.getAnnounceURL().toExternalForm());
						as.setAttribute("comment", tor.getComment());
						as.setAttribute("createdOn", Plugin.getPluginInterface().getUtilities().getFormatters().formatDate(tor.getCreationDate()*1000));
						as.setAttribute("pieceCount", Long.toString(tor.getPieceCount()));
						as.setAttribute("pieceSize",Long.toString(tor.getPieceSize()));
						File saveDir = new File(dl.getSavePath());
						if (saveDir.isDirectory())
							as.setAttribute("saveDir", saveDir.getAbsolutePath());
						else
							as.setAttribute("saveDir", saveDir.getParent());
						response.addContent(as);

					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
					return true;
				} else return false;
			}
		});
		addHandler("globalStats", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				PluginInterface pluginInterface = Plugin.getPluginInterface();

				response.setAttribute("receiveRate",
						Integer.toString(pluginInterface.getDownloadManager().getStats().getDataReceiveRate()));
				response.setAttribute("sendRate",
						Integer.toString(pluginInterface.getDownloadManager().getStats().getDataSendRate()));
				return true;
			}
		});
		addHandler("getUsers", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				Element users = new Element ("Users");
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					User[] userList = Plugin.getXMLConfig().getUsers();
					for (int i=0;i<userList.length; i++) {
						Element userE = userList[i].toElement();
						userE.removeAttribute("password");
						userE.removeChildren("Download");
						users.addContent(userE);
					}
				} else {
					Element userE = user.toElement();
					userE.removeAttribute("password");
					userE.removeChildren("Download");
					users.addContent(userE);
				}
				response.addContent(users);
				return true;
			}
		});
		addHandler("updateUser", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Element userE = xmlRequest.getChild("User");
					try {
						User userC = Plugin.getXMLConfig().getUser(userE.getAttributeValue("uName"));
						if (userE.getAttribute("username") != null) {
							Plugin.getXMLConfig().renameUser(userE.getAttributeValue("uName"), userE.getAttributeValue("username"));
							userE.removeAttribute("username");
						}
						userC.updateUser(userE);
						Plugin.getXMLConfig().saveConfigFile();
					} catch (UserNotFoundException e) {
					} catch (DuplicatedUserException e) {
					}
				}
				return false;
			}
		});
		addHandler("addUser", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Element userE = xmlRequest.getChild("User");

					try {
						Plugin.getXMLConfig().addUser(userE);
					} catch (DuplicatedUserException e) {
					}
					Plugin.getXMLConfig().saveConfigFile();
				}
				return false;
			}
		});
		addHandler("removeUser", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Plugin.getXMLConfig().removeUser(xmlRequest.getAttributeValue("username"));
					Plugin.getXMLConfig().saveConfigFile();
				}
				return false;
			}
		});
		addHandler("setAzParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							pc.setBooleanParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							//TODO
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							//TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							pc.setIntParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
				}
				response.setAttribute("switch", "getAzParameter");
				return handlerList.get("getAzParameter").handleRequest(xmlRequest, response, user); //TODO Hack

			}
		});
		addHandler("getAzParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",xmlRequest.getAttributeValue("type"));
						response.setAttribute("key", xmlRequest.getAttributeValue("key"));
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute("value",Boolean.toString(
							pc.getBooleanParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_STRING:
							response.setAttribute("value",
									pc.getStringParameter(xmlRequest.getAttributeValue("key")));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							response.setAttribute("value",Float.toString(
									pc.getFloatParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute("value",Integer.toString(
							pc.getIntParameter(xmlRequest.getAttributeValue("key"))));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute("type",Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
				}
				return true;
			}
		});
		addHandler("setPluginParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							pc.setPluginParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							pc.setPluginParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttributeValue("value"));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							//TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							pc.setPluginParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
				}
				response.setAttribute("switch", "getPluginParameter");
				return handlerList.get("getPluginParameter").handleRequest(xmlRequest, response, user); //TODO Hack
			}
		});
		addHandler("getPluginParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",xmlRequest.getAttributeValue("type"));
						response.setAttribute("key", xmlRequest.getAttributeValue("key"));
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute("value",Boolean.toString(
							pc.getPluginBooleanParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_STRING:

							if (pc.getPluginStringParameter(xmlRequest.getAttributeValue("key"),null) == null) {
								response.setAttribute("value","");
								response.setAttribute("type",Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
							} else {
								response.setAttribute("value",
										pc.getPluginStringParameter(xmlRequest.getAttributeValue("key")));
							}
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							//TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute("value",Integer.toString(
							pc.getPluginIntParameter(xmlRequest.getAttributeValue("key"))));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute("type",Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
				}
				return true;
			}
		});
		addHandler("setCoreParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							COConfigurationManager.setParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							COConfigurationManager.setParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttributeValue("value"));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							COConfigurationManager.setParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getFloatValue());
							break;
						case RemoteConstants.PARAMETER_INT:
							COConfigurationManager.setParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
				}
				response.setAttribute("switch", "getCoreParameter");
				return handlerList.get("getCoreParameter").handleRequest(xmlRequest, response, user); //TODO Hack

			}
		});
		addHandler("getCoreParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",xmlRequest.getAttributeValue("type"));
						response.setAttribute("key", xmlRequest.getAttributeValue("key"));
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute("value",Boolean.toString(
							COConfigurationManager.getBooleanParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_STRING:
							response.setAttribute("value",
							COConfigurationManager.getStringParameter(xmlRequest.getAttributeValue("key")));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							response.setAttribute("value",Float.toString(
							COConfigurationManager.getFloatParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute("value",Integer.toString(
							COConfigurationManager.getIntParameter(xmlRequest.getAttributeValue("key"))));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute("type",Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
				}
				return true;
			}
		});
		addHandler("restartAzureus", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					restart = true;
				}
				return false;
			}
		});
		addHandler("getRemoteInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, final User user) throws IOException {

				PluginInterface pi = Plugin.getPluginInterface();
				response.setAttribute("azureusVersion", pi.getAzureusVersion());
				response.setAttribute("pluginVersion", pi.getPluginVersion());

				return true;
			}
		});
		addHandler("getDriveInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, final User user) throws IOException {

				PluginInterface pi = Plugin.getPluginInterface();
				if (Constants.isLinux || Constants.isWindows) {
					PluginConfig pc = pi.getPluginconfig();
					Element dir;
					if (!user.getOutputDir().equals("") && new File(user.getOutputDir()).exists()) {
						dir = new Element ("Directory");
						dir.setAttribute("name", "destination.dir");
						dir.setAttribute("path", user.getOutputDir());
						dir.setAttribute("free", Long.toString(FileSystemUtils.freeSpaceKb(user.getOutputDir())));
						response.addContent(dir);
					}
					String defSDir = pc.getStringParameter("Default save path");
					if(new File(defSDir).exists()) {
						dir = new Element ("Directory");
						dir.setAttribute("name", "save.dir");
						dir.setAttribute("path", defSDir);
						dir.setAttribute("free", Long.toString(FileSystemUtils.freeSpaceKb(defSDir)));
						response.addContent(dir);
					}
				}
				return true;
			}
		});
		addHandler("getUpdateInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, final User user) throws IOException {

				UpdateCheckInstance uci = Plugin.getLatestUpdate();
				if (uci == null) {
					response.setAttribute("updateAvailable", Boolean.toString(false));
				} else {
					response.setAttribute("updateAvailable", Boolean.toString(true));
					Update[] updates = uci.getUpdates();
					for (Update update:updates) {
						Element uElement = new Element ("Update");
						uElement.setAttribute("name", update.getName());
						uElement.setAttribute("newVersion", update.getNewVersion());
						uElement.setAttribute("isMandatory",Boolean.toString(update.isMandatory()));
					}
				}
				return true;
			}
		});
		addHandler("applyUpdates", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response, final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					final UpdateCheckInstance uci = Plugin.getLatestUpdate();
					if (uci != null) {
						final Set<String> apply = new HashSet<String>();
						List<Element> aElem = xmlRequest.getChildren("Apply");
						for (Element e:aElem) {
							apply.add(e.getAttributeValue("name"));
						}
						if (apply.size() > 0) {
							Thread t = new Thread( new Runnable() {
								public void run() {
									Update[] updates = uci.getUpdates();
									for (Update update:updates) {
										if (!apply.contains(update.getName()))continue;
										ResourceDownloader[] downloader = update.getDownloaders();
										for (ResourceDownloader dl:downloader) {
											try {
												dl.download();
											} catch (ResourceDownloaderException e) {
												user.eventException(e);
												e.printStackTrace();
											}
										}
									}
								}
							});
							t.setDaemon(true);
							t.start();
						}
					}
					return false;
				}
				return false;
			}
		});
	}
}
