package lbms.azsmrc.plugin.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import lbms.azsmrc.plugin.main.MultiUser;
import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.Timers;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
import lbms.azsmrc.plugin.main.history.DownloadHistory;
import lbms.azsmrc.plugin.main.history.DownloadHistoryEntry;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.Serializer;
import lbms.azsmrc.shared.UserNotFoundException;
import lbms.tools.TorrentDownload;

import org.apache.commons.io.FileSystemUtils;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.security.SESecurityManager;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginManager;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.download.DownloadRemovalVetoException;
import org.gudy.azureus2.plugins.download.DownloadStats;
import org.gudy.azureus2.plugins.download.DownloadWillBeAddedListener;
import org.gudy.azureus2.plugins.ipc.IPCException;
import org.gudy.azureus2.plugins.ipc.IPCInterface;
import org.gudy.azureus2.plugins.peers.PeerManagerStats;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.torrent.TorrentEncodingException;
import org.gudy.azureus2.plugins.torrent.TorrentException;
import org.gudy.azureus2.plugins.torrent.TorrentManager;
import org.gudy.azureus2.plugins.tracker.TrackerException;
import org.gudy.azureus2.plugins.tracker.TrackerTorrent;
import org.gudy.azureus2.plugins.tracker.TrackerTorrentRemovalVetoException;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;
import org.gudy.azureus2.plugins.ui.config.Parameter;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.ui.model.PluginConfigModel;
import org.gudy.azureus2.plugins.update.Update;
import org.gudy.azureus2.plugins.update.UpdateCheckInstance;
import org.gudy.azureus2.plugins.update.UpdateException;
import org.gudy.azureus2.plugins.utils.UTTimerEvent;
import org.gudy.azureus2.plugins.utils.UTTimerEventPerformer;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
import org.gudy.azureus2.pluginsimpl.local.ui.config.BooleanParameterImpl;
import org.gudy.azureus2.pluginsimpl.local.ui.config.DirectoryParameterImpl;
import org.gudy.azureus2.pluginsimpl.local.ui.config.FileParameter;
import org.gudy.azureus2.pluginsimpl.local.ui.config.IntParameterImpl;
import org.gudy.azureus2.pluginsimpl.local.ui.config.ParameterImpl;
import org.gudy.azureus2.pluginsimpl.local.ui.config.StringParameterImpl;
import org.gudy.azureus2.ui.swt.views.ConfigView;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class RequestManager {
	private final Map<String, RequestHandler>	handlerList			= new HashMap<String, RequestHandler>();
	private static RequestManager				instance			= new RequestManager();
	private final Map<String, Integer[]>		downloadControlList	= new HashMap<String, Integer[]>();
	private UTTimerEvent						resumeTask;
	private TorrentAttribute					taCateory;

	private boolean								restart				= false;

	/**
	 * @return Returns the instance.
	 */
	public static RequestManager getInstance() {
		return instance;
	}

	public void initialize(PluginInterface pi) {
		taCateory = pi.getTorrentManager().getAttribute(
				TorrentAttribute.TA_CATEGORY);
		pi.getDownloadManager().addDownloadWillBeAddedListener(
				new DownloadWillBeAddedListener() {
					public void initialised(Download dl) {
						String hash = EncodingUtil.encode(dl.getTorrent().getHash());
						System.out.println("DownloadWillBeAddedListener checking: "
								+ hash);
						if (downloadControlList.containsKey(hash)) {
							System.out.println("DownloadWillBeAddedListener found: "
									+ hash);
							Integer[] options = downloadControlList.get(hash);
							DiskManagerFileInfo[] dmfi = dl.getDiskManagerFileInfo();
							if (dmfi.length != options.length) {
								System.out.println("DownloadWillBeAddedListener Array Sizes don't match.");
								System.out.println("localFiles: " + dmfi.length
										+ ", remote: " + options.length);
								return;
							}
							for (int i = 0; i < dmfi.length; i++) {
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

	public void addHandler(String request, RequestHandler handler) {
		handlerList.put(request, handler);
	}

	public void removeHandler(String request) {
		handlerList.remove(request);
	}

	public void invalidXMLError(TrackerWebPageResponse response) {

	}

	public void handleRequest(Document xmlRequest,
			TrackerWebPageResponse response, User user, boolean useCompression)
			throws IOException {
		Element requestRoot = xmlRequest.getRootElement();
		List<Element> queries = requestRoot.getChildren("Query");
		double protocolVersion = 1;

		response.setContentType("text/xml");

		try {
			protocolVersion = requestRoot.getAttribute("version").getDoubleValue();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}

		user.updateLogin();

		Document xmlResponse = new Document();
		Element responseRoot = new Element("Response");
		for (Element query : queries) {
			Element newResponse = new Element("Result");
			String id = query.getAttributeValue("id");
			if (id != null) {
				newResponse.setAttribute("id", id);
			}
			String request = query.getAttributeValue("switch");
			responseRoot.setAttribute("version",
					Double.toString(RemoteConstants.CURRENT_VERSION));

			if (protocolVersion != RemoteConstants.CURRENT_VERSION) {
				if (handlerList.get("_InvalidProtocolVersion_").handleRequest(
						query, newResponse, user)) {
					responseRoot.addContent(newResponse);
				}
				continue;
			}

			if (handlerList.containsKey(request)) {
				newResponse.setAttribute("switch", request);
				try {
					if (handlerList.get(request).handleRequest(query,
							newResponse, user)) {
						responseRoot.addContent(newResponse);
					}
				} catch (Exception e) {
					Element debug = new Element("Debug");
					debug.setAttribute("debugMsg", e.getMessage());
					Element stackTrace = new Element("StackTrace");
					String sTrace = "";
					for (StackTraceElement trace : e.getStackTrace()) {
						sTrace += trace + "\n";
					}
					stackTrace.setText(sTrace);
					debug.addContent((Element) query.clone());
					debug.addContent(sTrace);
					responseRoot.addContent(debug);
				}
				continue;
			} else {
				if (handlerList.get("_UnhandledRequest_").handleRequest(query,
						newResponse, user)) {
					responseRoot.addContent(newResponse);
				}
			}
		}

		xmlResponse.addContent(responseRoot);

		if (!requestRoot.getAttributeValue("noResponse", "false").equalsIgnoreCase(
				"true")) {
			if (user.hasEvents()
					&& !requestRoot.getAttributeValue("noEvents", "false").equalsIgnoreCase(
							"true")) {
				responseRoot.addContent(sendEvents(user));
			}
			XMLOutputter out = new XMLOutputter();
			if (useCompression) {
				GZIPOutputStream gos = new GZIPOutputStream(
						response.getOutputStream());
				response.setHeader("Content-Encoding", "gzip");
				out.output(xmlResponse, gos);
				gos.finish();
			} else {
				out.output(xmlResponse, response.getOutputStream());
			}
		}
		System.out.println("\nResponse:");

		new XMLOutputter(Format.getPrettyFormat()).output(xmlResponse,
				System.out); // Response

		if (restart) {
			System.out.println("Restarting Azureus");
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Plugin.getPluginInterface().getUpdateManager().applyUpdates(
								true);
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

	private Torrent getTorrentFromXML(Element e) throws TorrentException {
		String torrentData = e.getText();
		return Plugin.getPluginInterface().getTorrentManager().createFromBEncodedData(
				EncodingUtil.decode(torrentData),
				TorrentManager.PRESERVE_ENCODING);
	}

	private Download getDownloadByHash(String hash) throws DownloadException {
		return Plugin.getPluginInterface().getDownloadManager().getDownload(
				EncodingUtil.decode(hash));
	}

	/**
	 * This will convert all Parameters in Entries of FlexyConf
	 * 
	 * @param pluginSection parent to containt the Elements
	 * @param parameters Parameters to be converted
	 */
	private void parameterArrayToEntry(Element pluginSection,
			Parameter[] parameters) {
		Map<Parameter, Element> parameterToElement = new HashMap<Parameter, Element>();
		// Add all parameters
		for (int j = 0; j < parameters.length; j++) {
			Parameter parameter = parameters[j];
			Element entry = new Element("Entry");
			if (parameter instanceof StringParameterImpl) {
				entry.setAttribute("type", "string");
			} else if (parameter instanceof IntParameterImpl) {
				entry.setAttribute("type", "integer");
			} else if (parameter instanceof BooleanParameterImpl) {
				entry.setAttribute("type", "boolean");
			} else if (parameter instanceof FileParameter) {
				entry.setAttribute("type", "string");
			} else if (parameter instanceof DirectoryParameterImpl) {
				entry.setAttribute("type", "string");
			} else {
				continue;
			}
			entry.setAttribute("key", ((ParameterImpl) parameter).getKey());
			entry.setAttribute("label", parameter.getLabelText());
			entry.setAttribute("index", Integer.toString(j));
			parameterToElement.put(parameter, entry);
			pluginSection.addContent(entry);
		}
		// Check for dependencies
		for (int j = 0; j < parameters.length; j++) {
			Parameter parameter = parameters[j];
			if (parameter instanceof BooleanParameterImpl) {
				List<Parameter> parametersToEnable = ((BooleanParameterImpl) parameter).getEnabledOnSelectionParameters();
				Iterator<Parameter> iter = parametersToEnable.iterator();
				while (iter.hasNext()) {
					Parameter parameterToEnable = iter.next();
					Element entry = parameterToElement.get(parameterToEnable);
					if (entry != null) {
						entry.setAttribute("dependsOn",
								((ParameterImpl) parameter).getKey());
					}
				}

				List<Parameter> parametersToDisable = ((BooleanParameterImpl) parameter).getDisabledOnSelectionParameters();
				iter = parametersToDisable.iterator();
				while (iter.hasNext()) {
					Parameter parameterToDisable = iter.next();
					Element entry = parameterToElement.get(parameterToDisable);
					entry.setAttribute("dependsOn",
							'^' + ((ParameterImpl) parameter).getKey());
				}
			}
		}
	}

	private void setDlStats(Element dle, Download dl, int switches) {
		DownloadStats ds = dl.getStats();
		dle.setAttribute("name", dl.getName());
		dle.setAttribute("hash", EncodingUtil.encode(dl.getTorrent().getHash()));
		dle.setAttribute("forceStart", Boolean.toString(dl.isForceStart()));
		dle.setAttribute("position", Integer.toString(dl.getPosition()));
		if ((switches & RemoteConstants.ST_DOWNLOADED) != 0) {
			dle.setAttribute("downloaded", Long.toString(ds.getDownloaded()));
		}
		if ((switches & RemoteConstants.ST_UPLOADED) != 0) {
			dle.setAttribute("uploaded", Long.toString(ds.getUploaded()));
		}
		if ((switches & RemoteConstants.ST_DOWNLOAD_AVG) != 0) {
			dle.setAttribute("downloadAVG",
					Long.toString(ds.getDownloadAverage()));
		}
		if ((switches & RemoteConstants.ST_UPLOAD_AVG) != 0) {
			dle.setAttribute("uploadAVG", Long.toString(ds.getUploadAverage()));
		}
		if ((switches & RemoteConstants.ST_TOTAL_AVG) != 0) {
			dle.setAttribute("totalAVG", Long.toString(ds.getTotalAverage()));
		}
		if ((switches & RemoteConstants.ST_STATE) != 0) {
			dle.setAttribute("state", Integer.toString(dl.getState()));
		}
		if ((switches & RemoteConstants.ST_STATUS) != 0) {
			dle.setAttribute("status", ds.getStatus());
		}
		if ((switches & RemoteConstants.ST_ELAPSED_TIME) != 0) {
			dle.setAttribute("elapsedTime", ds.getElapsedTime());
		}
		if ((switches & RemoteConstants.ST_ETA) != 0) {
			dle.setAttribute("eta", ds.getETA());
		}
		if ((switches & RemoteConstants.ST_AVAILABILITY) != 0) {
			dle.setAttribute("availability",
					Float.toString(ds.getAvailability()));
		}
		if ((switches & RemoteConstants.ST_COMPLETITION) != 0) {
			dle.setAttribute("completition",
					Integer.toString(ds.getDownloadCompleted(false)));
		}
		if ((switches & RemoteConstants.ST_HEALTH) != 0) {
			dle.setAttribute("health", Integer.toString(ds.getHealth()));
		}
		if ((switches & RemoteConstants.ST_SHARE) != 0) {
			dle.setAttribute("shareRatio", Integer.toString(ds.getShareRatio()));
		}
		if ((switches & RemoteConstants.ST_TRACKER) != 0) {
			dle.setAttribute("tracker", ds.getTrackerStatus());
		}
		if ((switches & RemoteConstants.ST_LIMIT_DOWN) != 0) {
			dle.setAttribute("downloadLimit",
					Integer.toString(dl.getMaximumDownloadKBPerSecond()));
		}
		if ((switches & RemoteConstants.ST_LIMIT_UP) != 0) {
			dle.setAttribute("uploadLimit",
					Integer.toString(dl.getUploadRateLimitBytesPerSecond()));
		}

		if (dl.getState() == Download.ST_DOWNLOADING
				|| dl.getState() == Download.ST_SEEDING) { // Stopped DLs don't
			// have a
			// PeerManager
			PeerManagerStats pms = dl.getPeerManager().getStats();
			if ((switches & RemoteConstants.ST_SEEDS) != 0) {
				dle.setAttribute("seeds",
						Integer.toString(pms.getConnectedSeeds()));
			}
			if ((switches & RemoteConstants.ST_LEECHER) != 0) {
				dle.setAttribute("leecher",
						Integer.toString(pms.getConnectedLeechers()));
			}
			if ((switches & RemoteConstants.ST_DISCARDED) != 0) {
				dle.setAttribute("discarded", Long.toString(pms.getDiscarded()));
			}
		}
		if ((switches & RemoteConstants.ST_TOTAL_SEEDS) != 0) {
			dle.setAttribute("total_seeds",
					Integer.toString(dl.getLastScrapeResult().getSeedCount()));
		}
		if ((switches & RemoteConstants.ST_TOTAL_LEECHER) != 0) {
			dle.setAttribute(
					"total_leecher",
					Integer.toString(dl.getLastScrapeResult().getNonSeedCount()));
		}
		if ((switches & RemoteConstants.ST_SCRAPE_TIMES) != 0) {
			dle.setAttribute(
					"last_scrape",
					Long.toString(dl.getLastScrapeResult().getScrapeStartTime()));
			dle.setAttribute(
					"next_scrape",
					Long.toString(dl.getLastScrapeResult().getNextScrapeStartTime()));
		}
		if ((switches & RemoteConstants.ST_SIZE) != 0) {
			dle.setAttribute("size", Long.toString(dl.getTorrent().getSize()));
		}

		if ((switches & RemoteConstants.ST_CATEGORY) != 0) {
			String cat_String = dl.getAttribute(taCateory);
			if (cat_String != null) {
				dle.setAttribute("category", cat_String);
			}
		}
	}

	private RequestManager() {
		addHandler("_InvalidProtocolVersion_", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				response.setAttribute("switch", "_InvalidProtocolVersion_");
				Element error = new Element("Error");
				error.setText("Invalid Protocol Version.");
				error.setAttribute("code",
						Integer.toString(RemoteConstants.E_INVALID_PROTOCOL));
				response.addContent(error);
				return true;
			}
		});
		addHandler("_UnhandledRequest_", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				Plugin.addToLog("_UnhandledRequest_: "
						+ xmlRequest.getAttributeValue("switch"));
				response.setAttribute("switch", "_UnhandledRequest_");
				Element error = new Element("Error");
				Element xmlErroneousRequest = (Element) xmlRequest.clone();
				error.addContent(xmlErroneousRequest);
				error.setAttribute("code",
						Integer.toString(RemoteConstants.E_INVALID_REQUEST));
				response.addContent(error);
				return true;
			}
		});
		addHandler("Ping", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				response.setAttribute("switch", "Ping");
				response.setText("Pong");
				return true;
			}
		});
		addHandler("Echo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				response.setAttribute("switch",
						xmlRequest.getAttributeValue("_swtich"));
				// remove not needed attribs
				xmlRequest.removeAttribute("switch");
				xmlRequest.removeAttribute("_switch");
				// copy all remaining attributes
				response.setAttributes(xmlRequest.getAttributes());
				// copy all children
				List<Element> children = response.getChildren();
				for (Element e : children) {
					response.addContent(e.detach());
				}
				return true;
			}
		});
		addHandler("listTransfers", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				// response.setAttribute("switch", "listTransfers");
				int switches = 0;
				try {
					switches = xmlRequest.getAttribute("options").getIntValue();
				} catch (Exception e) {
				}
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				Element transferList = new Element("Transfers");
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(
						true);
				for (Download dl : dlList) {
					if (singleUser
							|| user.hasDownload(dl)
							|| (user.checkAccess(RemoteConstants.RIGHTS_SEE_PUBLICDL) && MultiUser.isPublicDownload(dl))) {
						if (dl.getTorrent() == null) {
							continue;
						}
						Element dle = new Element("Transfer");
						setDlStats(dle, dl, switches);
						transferList.addContent(dle);
					}
				}
				response.addContent(transferList);
				return true;
			}
		});
		addHandler("updateDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				// response.setAttribute("switch", "listTransfers");
				boolean fullUpdate = Boolean.parseBoolean(xmlRequest.getAttributeValue("fullUpdate"));

				response.addContent(user.getDownloadContainerManager().updateDownload(
						fullUpdate));

				return true;
			}
		});
		addHandler("addDownload", new RequestHandler() {
			public boolean handleRequest(final Element xmlRequest,
					Element response, final User user) throws IOException {
				String location = xmlRequest.getAttributeValue("location");
				File file_location = null;
				final boolean addAsPublicDownload = (user.checkAccess(RemoteConstants.RIGHTS_ADD_PUBLICDL) && Boolean.parseBoolean(xmlRequest.getAttributeValue("addAsPublic")));

				if (user.checkAccess(RemoteConstants.RIGHTS_SET_DL_DIR)) {
					if (xmlRequest.getAttributeValue("fileLocation") != null) {
						file_location = new File(
								xmlRequest.getAttributeValue("fileLocation"));
						if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
								"restrictSaveDir", false)) {
							String restrictSaveDir = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter(
									"restrictedSaveDir");
							if (!file_location.getCanonicalPath().startsWith(
									restrictSaveDir)) {
								file_location = null;
								user.eventErrorMessage("Desired download location is outside of the restricted download directory: "
										+ restrictSaveDir);
								return false;
							}
						}
					}
					if (file_location != null && !file_location.exists()) {
						file_location = null;
					}
				}
				if (location.equalsIgnoreCase("URL")) {
					final String url = xmlRequest.getAttributeValue("url");
					final File file = file_location;
					new Thread(new Runnable() {
						public void run() {
							try {
								TorrentManager torrentManager = Plugin.getPluginInterface().getTorrentManager();

								String username = xmlRequest.getAttributeValue("username");
								String password = xmlRequest.getAttributeValue("password");
								Torrent newTorrent = null;
								if (url.startsWith("magnet")) {
									newTorrent = torrentManager.getURLDownloader(
											new URL(url)).download();
								} else {
									TorrentDownload tdl = new TorrentDownload(
											new URL(url));
									if (username != null && password != null) {
										tdl.setLogin(username, password);
									}
									tdl.run();
									if (tdl.hasFailed()) {
										user.eventErrorMessage("Torrent Download failed. Reason: "
												+ tdl.getFailureReason());
										return;
									}
									newTorrent = torrentManager.createFromBEncodedData(tdl.getBuffer().toByteArray());
									if (newTorrent.getAdditionalProperty("encoding") == null) {
										newTorrent.setEncoding("UTF8");
									}
								}

								Download dl = Plugin.getPluginInterface().getDownloadManager().addDownload(
										newTorrent, null, file);
								if (!user.hasDownload(dl)) {

									try {
										Plugin.getXMLConfig().saveConfigFile();
									} catch (IOException e) {
									}

									if (dl.isComplete()) {
										DiskManagerFileInfo[] fileInfo = dl.getDiskManagerFileInfo();
										final File[] files = new File[fileInfo.length];
										for (int i = 0; i < files.length; i++) {
											files[i] = fileInfo[i].getFile();
										}
										for (File file : files) {
											try {
												Utilities.copy(file, new File(
														user.getOutputDir()),
														false);
											} catch (IOException e) {
												Plugin.addToLog(e.getMessage());
												e.printStackTrace();
											}
										}
										user.eventDownloadFinished(dl);
									} else if (!addAsPublicDownload) {
										MultiUser.addUserToDownload(user, dl);
									}
								}
							} catch (MalformedURLException e) {
								user.eventException(e);
								e.printStackTrace();
							} catch (TorrentException e) {
								user.eventException(e, url);
								e.printStackTrace();
							} catch (DownloadException e) {
								user.eventException(e);
							}
						};
					}).start();
				} else if (location.equalsIgnoreCase("XML")) {
					try {
						Torrent newTorrent = getTorrentFromXML(xmlRequest.getChild("Torrent"));

						String fileOptions = xmlRequest.getAttributeValue("fileOptions");
						if (fileOptions != null) {
							int[] opt = EncodingUtil.StringToIntArray(fileOptions);
							System.out.println("AzSMRC addDL: found FileOptions: "
									+ EncodingUtil.IntArrayToString(opt));
							Integer[] options = new Integer[opt.length];
							for (int i = 0; i < opt.length; i++) {
								options[i] = opt[i];
							}
							downloadControlList.put(
									EncodingUtil.encode(newTorrent.getHash()),
									options);
							System.out.println("AzSMRC addDL downloadControlList.size:"
									+ downloadControlList.size());
						}

						if (xmlRequest.getAttributeValue("encoding") != null) {
							try {
								newTorrent.setEncoding(xmlRequest.getAttributeValue("encoding"));
							} catch (TorrentEncodingException e1) {
								e1.printStackTrace();
							}
						} else if (newTorrent.getAdditionalProperty("encoding") == null) {
							newTorrent.setEncoding("UTF8");
						}

						Download dl = Plugin.getPluginInterface().getDownloadManager().addDownload(
								newTorrent, null, file_location);

						user.addDownload(dl);
						Plugin.getXMLConfig().saveConfigFile();

						if (dl.isComplete()) {
							DiskManagerFileInfo[] fileInfo = dl.getDiskManagerFileInfo();
							final File[] files = new File[fileInfo.length];
							for (int i = 0; i < files.length; i++) {
								files[i] = fileInfo[i].getFile();
							}
							for (File file : files) {
								try {
									Utilities.copy(file, new File(
											user.getOutputDir()), false);
								} catch (IOException e) {
									Plugin.addToLog(e.getMessage());
									e.printStackTrace();
								}
							}
							user.eventDownloadFinished(dl);
						} else {
							if (!addAsPublicDownload) {
								MultiUser.addUserToDownload(user, dl);
							}
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						User[] users = Plugin.getXMLConfig().getUsersOfDownload(
								dl);
						if (users.length == 1 || users.length == 0) {
							if (dl.getState() != Download.ST_STOPPED) {
								dl.stop();
							}
							if (xmlRequest.getAttributeValue("delTorrent") != null) {
								try {
									dl.remove(
											Boolean.parseBoolean(xmlRequest.getAttributeValue("delTorrent")),
											Boolean.parseBoolean(xmlRequest.getAttributeValue("delData")));
								} catch (NullPointerException e) {
								}
							} else {
								dl.remove();
							}
						} else {
							MultiUser.removeUserFromDownload(user, dl);
						}
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					} catch (DownloadRemovalVetoException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("stopDownload", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						dl.setMaximumDownloadKBPerSecond(xmlRequest.getAttribute(
								"limit").getIntValue());
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						dl.setUploadRateLimitBytesPerSecond(xmlRequest.getAttribute(
								"limit").getIntValue());
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						int newpos = xmlRequest.getAttribute("position").getIntValue();
						Download dl = getDownloadByHash(hash);
						dl.setPosition(newpos);
					} catch (DataConversionException e) {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if ((singleUser || user.hasDownload(hash) || MultiUser.isPublicDownload(hash))
						&& user.checkAccess(RemoteConstants.RIGHTS_FORCESTART)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						int newpos = xmlRequest.getAttribute("position").getIntValue();
						Download dl = getDownloadByHash(hash);
						dl.moveTo(newpos);
					} catch (DataConversionException e) {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						dl.moveDown();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("setTorrentAttribute", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						TorrentAttribute ta = Plugin.getPluginInterface().getTorrentManager().getAttribute(
								xmlRequest.getAttributeValue("attribute"));
						Download dl = getDownloadByHash(hash);
						dl.setAttribute(ta,
								xmlRequest.getAttributeValue("value"));
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("requestDownloadScrape", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						dl.requestTrackerScrape(false);
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("requestDownloadAnnounce", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						dl.requestTrackerAnnounce(false);
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("renameDownload", new RequestHandler() {
			public boolean handleRequest(final Element xmlRequest,
					Element response, final User user) throws IOException {
				if (!user.checkAccess(RemoteConstants.RIGHTS_SET_DL_DIR)) {
					return false;
				}
				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						File target = new File(
								xmlRequest.getAttributeValue("target"));
						if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
								"restrictSaveDir", false)) {
							String restrictSaveDir = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter(
									"restrictedSaveDir");
							if (!target.getCanonicalPath().startsWith(
									restrictSaveDir)) {
								user.eventErrorMessage("Desired download location is outside of the restricted download directory: "
										+ restrictSaveDir);
								return false;
							}
						}
						final Download dl = getDownloadByHash(hash);
						new Thread(new Runnable() {
							public void run() {
								try {
									if (!dl.isPaused()) {
										dl.stop();
									}
									dl.renameDownload(xmlRequest.getAttributeValue("target"));
									dl.restart();
								} catch (DownloadException e) {
									user.eventException(e);
									e.printStackTrace();
								}
							};
						}).start();
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveDataFiles", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (!user.checkAccess(RemoteConstants.RIGHTS_SET_DL_DIR)) {
					return false;
				}
				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						final Download dl = getDownloadByHash(hash);
						final File target = new File(
								xmlRequest.getAttributeValue("target"));
						if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
								"restrictSaveDir", false)) {
							String restrictSaveDir = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter(
									"restrictedSaveDir");
							if (!target.getCanonicalPath().startsWith(
									restrictSaveDir)) {
								user.eventErrorMessage("Desired download location is outside of the restricted download directory: "
										+ restrictSaveDir);
								return false;
							}
						}
						if (target.exists() && target.isDirectory()) {
							new Thread(new Runnable() {
								public void run() {
									try {
										if (!dl.isPaused()) {
											dl.stop();
										}
										dl.moveDataFiles(target);
										dl.restart();
									} catch (DownloadException e) {
										user.eventException(e);
										e.printStackTrace();
									}
								};
							}).start();
						} else {
							user.eventException(new Exception(
									"Error Moving Data Files: Directory not found"));
						}
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("moveTorrentFile", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						final Download dl = getDownloadByHash(hash);
						final File target = new File(
								xmlRequest.getAttributeValue("target"));
						if (target.exists() && target.isDirectory()) {
							new Thread(new Runnable() {
								public void run() {
									try {
										if (!dl.isPaused()) {
											dl.stop();
										}
										dl.moveTorrentFile(target);
										dl.restart();
									} catch (DownloadException e) {
										user.eventException(e);
										e.printStackTrace();
									}
								};
							}).start();
						} else {
							user.eventException(new Exception(
									"Error Moving Torrent File: Directory not found"));
						}
					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		addHandler("getDownloadStats", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				response.setAttribute("switch", "listTransfers");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Element transferList = new Element("Transfers");
						int options = xmlRequest.getAttribute("options").getIntValue();
						Download dl = getDownloadByHash(hash);
						Element dle = new Element("Transfer");
						setDlStats(dle, dl, options);
						transferList.addContent(dle);
						response.addContent(transferList);
					} catch (DataConversionException e) {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(
						true);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser) {
					Plugin.getPluginInterface().getDownloadManager().startAllDownloads();
				} else {
					for (Download dl : dlList) {
						if (user.hasDownload(dl)) {
							try {
								dl.restart();
							} catch (DownloadException e) {
								user.eventException(e);
								e.printStackTrace();
							}
						}
					}
				}
				return false;
			}
		});
		addHandler("stopAllDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				Download[] dlList = Plugin.getPluginInterface().getDownloadManager().getDownloads(
						true);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser) {
					Plugin.getPluginInterface().getDownloadManager().stopAllDownloads();
				} else {
					for (Download dl : dlList) {
						if (user.hasDownload(dl)) {
							try {
								dl.stop();
							} catch (DownloadException e) {
								user.eventException(e);
								e.printStackTrace();
							}
						}
					}
				}
				return false;
			}
		});
		addHandler("pauseDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser
						|| user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Plugin.getPluginInterface().getDownloadManager().pauseDownloads();
					if (xmlRequest.getAttribute("timeout") != null) {
						if (resumeTask != null) {
							resumeTask.cancel();
						}
						long timeout = System.currentTimeMillis()
								+ Long.parseLong(xmlRequest.getAttributeValue("timeout"))
								* 1000;
						resumeTask = Timers.getTimer().addEvent(timeout,
								new UTTimerEventPerformer() {
									public void perform(UTTimerEvent event) {
										Plugin.getPluginInterface().getDownloadManager().resumeDownloads();
									}
								});
					}
				}
				return false;
			}
		});
		addHandler("resumeDownloads", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser
						|| user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					if (resumeTask != null) {
						resumeTask.cancel();
					}
					Plugin.getPluginInterface().getDownloadManager().resumeDownloads();
				}
				return false;
			}
		});
		addHandler("getFiles", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				response.setAttribute("hash", hash);
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						DiskManagerFileInfo[] dmfi = dl.getDiskManagerFileInfo();
						Element files = new Element("Files");
						for (int i = 0; i < dmfi.length; i++) {
							Element file = new Element("File");
							file.setAttribute("name",
									dmfi[i].getFile().getPath());
							file.setAttribute("length",
									Long.toString(dmfi[i].getLength()));
							file.setAttribute("numPieces",
									Long.toString(dmfi[i].getNumPieces()));
							file.setAttribute("downloaded",
									Long.toString(dmfi[i].getDownloaded()));
							file.setAttribute("priority",
									Boolean.toString(dmfi[i].isPriority()));
							file.setAttribute("skipped",
									Boolean.toString(dmfi[i].isSkipped()));
							file.setAttribute("deleted",
									Boolean.toString(dmfi[i].isDeleted()));
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
		addHandler("getDownloadHistory", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				long startDate = Long.parseLong(xmlRequest.getAttributeValue("startDate"));
				long endDate = Long.parseLong(xmlRequest.getAttributeValue("endDate"));
				DownloadHistoryEntry[] entries = DownloadHistory.getInstance().getEntries(
						user, startDate, endDate);
				for (DownloadHistoryEntry entry : entries) {
					response.addContent(entry.toElement());
				}
				return true;
			}
		});
		addHandler("setFileOptions", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {

				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						DiskManagerFileInfo[] dmfi = dl.getDiskManagerFileInfo();
						if (xmlRequest.getAttributeValue("index") != null) {
							try {
								boolean priority = Boolean.parseBoolean(xmlRequest.getAttributeValue("priority"));
								boolean skipped = Boolean.parseBoolean(xmlRequest.getAttributeValue("skipped"));
								boolean deleted = Boolean.parseBoolean(xmlRequest.getAttributeValue("deleted"));
								int index = Integer.parseInt(xmlRequest.getAttributeValue("index"));
								if (dmfi[index].isPriority() != priority) {
									dmfi[index].setPriority(priority);
								}
								if (dmfi[index].isSkipped() != skipped) {
									dmfi[index].setSkipped(skipped);
								}
								if (dmfi[index].isDeleted() != deleted) {
									dmfi[index].setDeleted(deleted);
								}
							} catch (NullPointerException e) {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				String hash = xmlRequest.getAttributeValue("hash");
				boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
						"singleUserMode", false);
				if (singleUser || user.hasDownload(hash)
						|| MultiUser.isPublicDownload(hash)) {
					try {
						Download dl = getDownloadByHash(hash);
						Torrent tor = dl.getTorrent();
						response.setAttribute("hash", hash);
						Element as = new Element("AdvancedStats");
						as.setAttribute("trackerUrl",
								tor.getAnnounceURL().toExternalForm());
						as.setAttribute("comment", tor.getComment());
						as.setAttribute(
								"createdOn",
								Plugin.getPluginInterface().getUtilities().getFormatters().formatDate(
										tor.getCreationDate() * 1000));
						as.setAttribute("pieceCount",
								Long.toString(tor.getPieceCount()));
						as.setAttribute("pieceSize",
								Long.toString(tor.getPieceSize()));
						File saveDir = new File(dl.getSavePath());
						if (saveDir.isDirectory()) {
							as.setAttribute("saveDir",
									saveDir.getAbsolutePath());
						} else {
							as.setAttribute("saveDir", saveDir.getParent());
						}
						response.addContent(as);

					} catch (DownloadException e) {
						user.eventException(e);
						e.printStackTrace();
					}
					return true;
				} else {
					return false;
				}
			}
		});
		addHandler("globalStats", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				PluginInterface pluginInterface = Plugin.getPluginInterface();

				int downloading = 0;
				int downloadqueue = 0;
				int seeding = 0;
				int seedqueue = 0;
				Download[] dlList = pluginInterface.getDownloadManager().getDownloads();
				for (int i = 0; i < dlList.length; i++) {
					switch (dlList[i].getState()) {
					case Download.ST_SEEDING:
						seeding++;
						break;
					case Download.ST_DOWNLOADING:
						downloading++;
						break;
					default:
						if (dlList[i].isComplete()) {
							seedqueue++;
						} else {
							downloadqueue++;
						}
						break;
					}
				}

				response.setAttribute(
						"receiveRate",
						Integer.toString(pluginInterface.getDownloadManager().getStats().getDataReceiveRate()));
				response.setAttribute(
						"sendRate",
						Integer.toString(pluginInterface.getDownloadManager().getStats().getDataSendRate()));

				response.setAttribute("seeding", Integer.toString(seeding));
				response.setAttribute("seedqueue", Integer.toString(seedqueue));
				response.setAttribute("downloading",
						Integer.toString(downloading));
				response.setAttribute("downloadqueue",
						Integer.toString(downloadqueue));
				return true;
			}
		});
		addHandler("getUsers", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				Element users = new Element("Users");
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					User[] userList = Plugin.getXMLConfig().getUsers();
					for (int i = 0; i < userList.length; i++) {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Element userE = xmlRequest.getChild("User");
					try {
						User userC = Plugin.getXMLConfig().getUser(
								userE.getAttributeValue("uName"));
						if (userE.getAttribute("username") != null) {
							Plugin.getXMLConfig().renameUser(
									userE.getAttributeValue("uName"),
									userE.getAttributeValue("username"));
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
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
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					Plugin.getXMLConfig().removeUser(
							xmlRequest.getAttributeValue("username"));
					Plugin.getXMLConfig().saveConfigFile();
				}
				return false;
			}
		});
		addHandler("setAzParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							pc.setBooleanParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							// TODO
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							// TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							pc.setIntParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
					response.setAttribute("switch", "getAzParameter");
					return handlerList.get("getAzParameter").handleRequest(
							xmlRequest, response, user); // TODO Hack
				}
				return false;
			}
		});
		addHandler("getAzParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",
								xmlRequest.getAttributeValue("type"));
						response.setAttribute("key",
								xmlRequest.getAttributeValue("key"));
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute(
									"value",
									Boolean.toString(pc.getBooleanParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_STRING:
							response.setAttribute(
									"value",
									pc.getStringParameter(xmlRequest.getAttributeValue("key")));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							response.setAttribute(
									"value",
									Float.toString(pc.getFloatParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute(
									"value",
									Integer.toString(pc.getIntParameter(xmlRequest.getAttributeValue("key"))));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
					return true;
				}
				return false;
			}
		});
		addHandler("setPluginParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							pc.setPluginParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							pc.setPluginParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttributeValue("value"));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							// TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							pc.setPluginParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
					response.setAttribute("switch", "getPluginParameter");
					return handlerList.get("getPluginParameter").handleRequest(
							xmlRequest, response, user); // TODO Hack
				}
				return false;
			}
		});
		addHandler("getPluginParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",
								xmlRequest.getAttributeValue("type"));
						response.setAttribute("key",
								xmlRequest.getAttributeValue("key"));
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute(
									"value",
									Boolean.toString(pc.getPluginBooleanParameter(xmlRequest.getAttributeValue("key"))));
							break;
						case RemoteConstants.PARAMETER_STRING:

							if (pc.getPluginStringParameter(
									xmlRequest.getAttributeValue("key"), null) == null) {
								response.setAttribute("value", "");
								response.setAttribute(
										"type",
										Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
							} else {
								response.setAttribute(
										"value",
										pc.getPluginStringParameter(xmlRequest.getAttributeValue("key")));
							}
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							// TODO
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute(
									"value",
									Integer.toString(pc.getPluginIntParameter(xmlRequest.getAttributeValue("key"))));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
					return true;
				}
				return false;
			}
		});
		addHandler("setCoreParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						int type = xmlRequest.getAttribute("type").getIntValue();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							Plugin.getPluginInterface().getPluginconfig().setUnsafeBooleanParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getBooleanValue());
							break;
						case RemoteConstants.PARAMETER_STRING:
							Plugin.getPluginInterface().getPluginconfig().setUnsafeStringParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttributeValue("value"));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							Plugin.getPluginInterface().getPluginconfig().setUnsafeFloatParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getFloatValue());
							break;
						case RemoteConstants.PARAMETER_INT:
							Plugin.getPluginInterface().getPluginconfig().setUnsafeIntParameter(
									xmlRequest.getAttributeValue("key"),
									xmlRequest.getAttribute("value").getIntValue());
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
					}
					response.setAttribute("switch", "getCoreParameter");
					return handlerList.get("getCoreParameter").handleRequest(
							xmlRequest, response, user); // TODO Hack
				}
				return false;

			}
		});
		addHandler("getCoreParameter", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					try {
						int type = xmlRequest.getAttribute("type").getIntValue();
						response.setAttribute("type",
								xmlRequest.getAttributeValue("type"));
						response.setAttribute("key",
								xmlRequest.getAttributeValue("key"));
						PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						switch (type) {
						case RemoteConstants.PARAMETER_BOOLEAN:
							response.setAttribute(
									"value",
									Boolean.toString(pc.getUnsafeBooleanParameter(
											xmlRequest.getAttributeValue("key"),
											false)));
							break;
						case RemoteConstants.PARAMETER_STRING:
							response.setAttribute(
									"value",
									pc.getUnsafeStringParameter(
											xmlRequest.getAttributeValue("key"),
											""));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							response.setAttribute(
									"value",
									Float.toString(pc.getUnsafeFloatParameter(
											xmlRequest.getAttributeValue("key"),
											0)));
							break;
						case RemoteConstants.PARAMETER_INT:
							response.setAttribute(
									"value",
									Integer.toString(pc.getUnsafeIntParameter(
											xmlRequest.getAttributeValue("key"),
											0)));
							break;
						}
					} catch (DataConversionException e) {
						e.printStackTrace();
						response.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					} catch (ClassCastException e) {
						e.printStackTrace();
						response.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}
					return true;
				}
				return false;
			}
		});
		addHandler("restartAzureus", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					restart = true;
				}
				return false;
			}
		});
		addHandler("getRemoteInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				PluginInterface pi = Plugin.getPluginInterface();
				response.setAttribute("azureusVersion", pi.getAzureusVersion());
				response.setAttribute("pluginVersion", pi.getPluginVersion());
				Element fc = Plugin.getPSFlexyConf().toDocument().getRootElement();
				fc.detach();
				response.addContent(fc);

				return true;
			}
		});
		addHandler("getSubDirs", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				if (user.checkAccess(RemoteConstants.RIGHTS_SET_DL_DIR)) {
					String restrictSaveDir = null;
					File parent_dir = null;

					if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
							"restrictSaveDir", false)) {
						restrictSaveDir = Plugin.getPluginInterface().getPluginconfig().getPluginStringParameter(
								"restrictedSaveDir");
					}
					if (xmlRequest.getAttribute("dir") != null) {
						parent_dir = new File(
								xmlRequest.getAttributeValue("dir"));
						if (restrictSaveDir != null
								&& !parent_dir.getCanonicalPath().startsWith(
										restrictSaveDir)) {
							parent_dir = null;
							user.eventErrorMessage("Desired location is outside of the restricted download directory: "
									+ restrictSaveDir);
							return false;
						} else {

						}
					} else {
						if (restrictSaveDir != null) {
							parent_dir = new File(restrictSaveDir);
						} else {
							parent_dir = new File(".");
						}
					}
					if (parent_dir != null) {
						File[] subdirs = parent_dir.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								return pathname.isDirectory();
							};
						});
						for (int i = 0; i < subdirs.length; i++) {
							Element dir = new Element("Dir");
							dir.setAttribute("path",
									subdirs[i].getCanonicalPath());
							response.addContent(dir);
						}
					}
					return true;

				} else {
					return false;
				}
			}
		});
		addHandler("getDriveInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				PluginInterface pi = Plugin.getPluginInterface();
				if (Constants.isLinux || Constants.isWindows) {
					PluginConfig pc = pi.getPluginconfig();
					Element dir;
					if (!user.getOutputDir().equals("")
							&& new File(user.getOutputDir()).exists()) {
						dir = new Element("Directory");
						dir.setAttribute("name", "destination.dir");
						dir.setAttribute("path", user.getOutputDir());
						dir.setAttribute(
								"free",
								Long.toString(FileSystemUtils.freeSpaceKb(user.getOutputDir())));
						response.addContent(dir);
					}
					String defSDir = pc.getStringParameter("Default save path");
					if (new File(defSDir).exists()) {
						dir = new Element("Directory");
						dir.setAttribute("name", "save.dir");
						dir.setAttribute("path", defSDir);
						dir.setAttribute(
								"free",
								Long.toString(FileSystemUtils.freeSpaceKb(defSDir)));
						response.addContent(dir);
					}
				}
				return true;
			}
		});
		addHandler("getUpdateInfo", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				UpdateCheckInstance uci = Plugin.getLatestUpdate();
				if (uci == null) {
					response.setAttribute("updateAvailable",
							Boolean.toString(false));
				} else {

					Update[] updates = uci.getUpdates();

					if (updates.length == 0) {
						response.setAttribute("updateAvailable",
								Boolean.toString(false));
					} else {
						response.setAttribute("updateAvailable",
								Boolean.toString(true));
					}

					for (Update update : updates) {
						Element uElement = new Element("Update");
						uElement.setAttribute("name", update.getName());
						uElement.setAttribute("newVersion",
								update.getNewVersion());
						uElement.setAttribute("isMandatory",
								Boolean.toString(update.isMandatory()));
						response.addContent(uElement);
					}
				}
				return true;
			}
		});
		addHandler("applyUpdates", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					final UpdateCheckInstance uci = Plugin.getLatestUpdate();
					if (uci != null) {
						final Set<String> apply = new HashSet<String>();
						List<Element> aElem = xmlRequest.getChildren("Apply");
						for (Element e : aElem) {
							apply.add(e.getAttributeValue("name"));
						}
						if (apply.size() > 0) {
							Thread t = new Thread(new Runnable() {
								public void run() {
									Update[] updates = uci.getUpdates();
									for (Update update : updates) {
										if (!apply.contains(update.getName())) {
											continue;
										}
										ResourceDownloader[] downloader = update.getDownloaders();
										for (ResourceDownloader dl : downloader) {
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
				}
				return false;
			}
		});
		addHandler("createSSLCertificate", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					String alias = xmlRequest.getAttributeValue("alias");
					String dn = xmlRequest.getAttributeValue("dn");
					int strength = Integer.parseInt(xmlRequest.getAttributeValue("strength"));
					if (strength < 512) {
						strength = 1024;
					}
					if (alias == null || dn == null || alias.equals("")
							|| dn.equals("")) {
						user.eventException("CreateSSLCert: Parameter incorrect");
					} else {
						try {
							SESecurityManager.createSelfSignedCertificate(
									alias, dn, strength);
							user.eventMessage("SSL Certificate successfully created.");
						} catch (Exception e) {
							user.eventException(e, "CreateSSLCert");
						}
					}
				}
				return false;
			}
		});
		addHandler("listPlugins", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					PluginInterface pi = Plugin.getPluginInterface();
					PluginInterface[] piList = pi.getPluginManager().getPluginInterfaces();
					for (PluginInterface plug : piList) {
						Element pe = new Element("Plugin");
						pe.setAttribute("id", plug.getPluginID());
						pe.setAttribute("name", plug.getPluginName());
						pe.setAttribute("version", plug.getPluginVersion());
						pe.setAttribute("dir", plug.getPluginDirectoryName());
						pe.setAttribute("disabled",
								Boolean.toString(plug.isDisabled()));
						pe.setAttribute("builtin",
								Boolean.toString(plug.isBuiltIn()));
						pe.setAttribute("operational",
								Boolean.toString(plug.isBuiltIn()));
						pe.setAttribute("mandatory",
								Boolean.toString(plug.isMandatory()));
						pe.setAttribute("unloadable",
								Boolean.toString(plug.isUnloadable()));
						response.addContent(pe);
					}
				}
				return true;
			}
		});
		addHandler("setPluginDisable", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					PluginInterface pi = Plugin.getPluginInterface();
					PluginInterface piTarget = pi.getPluginManager().getPluginInterfaceByID(
							xmlRequest.getAttributeValue("pluginID"));
					if (piTarget != null) {
						pi.setDisabled(Boolean.parseBoolean(xmlRequest.getAttributeValue("disable")));
					}
				}
				return false;
			}
		});
		addHandler("getPluginsFlexyConfig", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				try {
					System.out.println("Creating PluginFlexyConf");
					Element fc = new Element("FlexyConfiguration");
					Element rootSection = new Element("Section");
					rootSection.setAttribute("label", "PluginConfig");
					fc.addContent(rootSection);

					int index = 0;

					PluginConfigModel[] pluginSections = Plugin.getPluginInterface().getUIManager().getPluginConfigModels();
					System.out.println("PluginFlexyConf Size: "
							+ pluginSections.length);

					if (pluginSections.length == 0) {
						return false; // don't reply if we don't have any
						// ConfigModels
					}

					for (PluginConfigModel o : pluginSections) {
						System.out.println(o.getClass());
						if (o instanceof BasicPluginConfigModel) {
							BasicPluginConfigModel pluginModel = (BasicPluginConfigModel) o;
							Parameter[] parameters = pluginModel.getParameters();
							Element pluginSection = new Element("Section");
							pluginSection.setAttribute("index",
									Integer.toString(index));
							String section_key = pluginModel.getSection();

							// if resource exists without prefix then use it as
							// plugins don't
							// need to start with the prefix

							if (!MessageText.keyExists(section_key)) {
								section_key = ConfigView.sSectionPrefix
										+ section_key;
							}
							String name = MessageText.getString(section_key);

							System.out.println("PluginFlexyConf Adding: "
									+ name);

							pluginSection.setAttribute("label", name);
							index++;
							parameterArrayToEntry(pluginSection, parameters);
							rootSection.addContent(pluginSection);
						}
					}

					/*
					 * ParameterRepository repository =
					 * ParameterRepository.getInstance(); String[] names =
					 * repository.getNames(); Arrays.sort(names);
					 * System.out.println("PluginFlexyConf Size2:
					 * "+names.length); for (int i = 0; i < names.length; i++) {
					 * String pluginName = names[i];
					 * System.out.println("PluginFlexyConf Adding:
					 * "+pluginName); Parameter[] parameters =
					 * repository.getParameterBlock(pluginName); Element
					 * pluginSection = new Element ("Section");
					 * pluginSection.setAttribute("index",
					 * Integer.toString(i+index)); fc.addContent(pluginSection); //
					 * Note: 2070's plugin documentation for
					 * PluginInterface.addConfigUIParameters // said to pass
					 * <"ConfigView.plugins." + displayName>. This was // never
					 * implemented in 2070. 2070 read the key <displayName>
					 * without // the prefix. // // 2071+ uses <sSectionPrefix
					 * ("ConfigView.section.plugins.") + pluginName> // and
					 * falls back to <displayName>. Since //
					 * <"ConfigView.plugins." + displayName> was never
					 * implemented in the // first place, a check for it has not
					 * been created boolean bUsePrefix =
					 * MessageText.keyExists(ConfigView.sSectionPrefix +
					 * "plugins." + pluginName); if (bUsePrefix) {
					 * pluginSection.setAttribute("label",
					 * MessageText.getString(ConfigView.sSectionPrefix +
					 * "plugins." + pluginName)); } else {
					 * pluginSection.setAttribute("label",MessageText.getString(pluginName)); }
					 * parameterArrayToEntry(pluginSection, parameters); }
					 */
					response.addContent(fc);
					return true;
				} catch (Throwable e) {
					e.printStackTrace();
					return false;
				}
			}
		});

		addHandler("addDownloadToUser", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)
						|| (user.getUsername().equalsIgnoreCase(xmlRequest.getAttributeValue("userName")))) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(
								EncodingUtil.decode(xmlRequest.getAttributeValue("hash")));
						if (dl != null) {
							user.addDownload(dl);
							MultiUser.addUserToDownload(user, dl);
						}
					} catch (DownloadException e) {
						e.printStackTrace();
						return false;
					}
				}
				return true;
			}
		});
		addHandler("removeDownloadFromUser", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)
						|| (user.getUsername().equalsIgnoreCase(xmlRequest.getAttributeValue("userName")))) {
					try {
						Download dl = Plugin.getPluginInterface().getDownloadManager().getDownload(
								EncodingUtil.decode(xmlRequest.getAttributeValue("hash")));
						if (dl != null) {
							user.removeDownload(dl);
							MultiUser.removeUserFromDownload(user, dl);
						}
					} catch (DownloadException e) {
						e.printStackTrace();
						return false;
					}
				}
				return true;
			}
		});

		addHandler("hostTorrent", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					return false;
				}

				String location = xmlRequest.getAttributeValue("location");
				Torrent torrent = null;
				if (location.equalsIgnoreCase("XML")) {
					try {
						torrent = getTorrentFromXML(xmlRequest.getChild("Torrent"));
					} catch (TorrentException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				} else if (location.equalsIgnoreCase("Download")) {
					try {
						String hash = xmlRequest.getAttributeValue("hash");
						torrent = getDownloadByHash(hash).getTorrent();
					} catch (DownloadException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				}
				if (torrent != null) {
					try {
						Plugin.getPluginInterface().getTracker().host(
								torrent,
								Boolean.parseBoolean(xmlRequest.getAttributeValue("persistent")),
								Boolean.parseBoolean(xmlRequest.getAttributeValue("passive")));
					} catch (TrackerException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				}
				return false;
			}
		});
		addHandler("publishTorrent", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					User user) throws IOException {
				if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					return false;
				}

				String location = xmlRequest.getAttributeValue("location");
				Torrent torrent = null;
				if (location.equalsIgnoreCase("XML")) {
					try {
						torrent = getTorrentFromXML(xmlRequest.getChild("Torrent"));
					} catch (TorrentException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				} else if (location.equalsIgnoreCase("Download")) {
					try {
						String hash = xmlRequest.getAttributeValue("hash");
						torrent = getDownloadByHash(hash).getTorrent();
					} catch (DownloadException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				}
				if (torrent != null) {
					try {
						Plugin.getPluginInterface().getTracker().publish(
								torrent);
					} catch (TrackerException e) {
						e.printStackTrace();
						user.eventException(e);
					}
				}
				return false;
			}
		});
		addHandler("getTrackerTorrents", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {

				TrackerTorrent[] ttorrents = Plugin.getPluginInterface().getTracker().getTorrents();
				Element torrentsE = new Element("TrackerTorrents");
				for (TrackerTorrent tt : ttorrents) {
					Element tte = new Element("TrackerTorrent");
					tte.setAttribute("hash",
							EncodingUtil.encode(tt.getTorrent().getHash()));
					tte.setAttribute("name", tt.getTorrent().getName());
					tte.setAttribute("announceCount",
							Long.toString(tt.getAnnounceCount()));
					tte.setAttribute("avgAnnounceCount",
							Long.toString(tt.getAverageAnnounceCount()));
					tte.setAttribute("avgBytesIn",
							Long.toString(tt.getAverageBytesIn()));
					tte.setAttribute("avgBytesOut",
							Long.toString(tt.getAverageBytesOut()));
					tte.setAttribute("avgDownloaded",
							Long.toString(tt.getAverageDownloaded()));
					tte.setAttribute("avgUploaded",
							Long.toString(tt.getAverageUploaded()));
					tte.setAttribute("avgScrapeCount",
							Long.toString(tt.getAverageScrapeCount()));
					tte.setAttribute("completedCount",
							Long.toString(tt.getCompletedCount()));
					tte.setAttribute("totalLeft",
							Long.toString(tt.getTotalLeft()));
					tte.setAttribute("dateAdded",
							Long.toString(tt.getDateAdded()));
					tte.setAttribute("scrapeCount",
							Long.toString(tt.getScrapeCount()));
					tte.setAttribute("totalBytesOut",
							Long.toString(tt.getTotalBytesOut()));
					tte.setAttribute("totalBytesIn",
							Long.toString(tt.getTotalBytesIn()));
					tte.setAttribute("totalDownloaded",
							Long.toString(tt.getTotalDownloaded()));
					tte.setAttribute("totalUploaded",
							Long.toString(tt.getTotalUploaded()));
					tte.setAttribute("seedCount",
							Integer.toString(tt.getSeedCount()));
					tte.setAttribute("leecherCount",
							Integer.toString(tt.getLeecherCount()));
					tte.setAttribute("status", Integer.toString(tt.getStatus()));
					tte.setAttribute("badNATCount",
							Integer.toString(tt.getBadNATCount()));
					tte.setAttribute("isPassive",
							Boolean.toString(tt.isPassive()));
					try {
						tte.setAttribute("canBeRemoved",
								Boolean.toString(tt.canBeRemoved()));
					} catch (TrackerTorrentRemovalVetoException e) {
						tte.setAttribute("canBeRemoved", "false");
					}
					torrentsE.addContent(tte);
				}
				response.addContent(torrentsE);
				return true;
			}
		});
		addHandler("trackerTorrentRemove", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					TrackerTorrent[] torrents = Plugin.getPluginInterface().getTracker().getTorrents();
					for (TrackerTorrent tt : torrents) {
						String hash = EncodingUtil.encode(tt.getTorrent().getHash());
						if (hash.equalsIgnoreCase(xmlRequest.getAttributeValue("hash"))) {
							try {
								tt.remove();
							} catch (TrackerTorrentRemovalVetoException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				}
				return false;
			}
		});
		addHandler("trackerTorrentStop", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					TrackerTorrent[] torrents = Plugin.getPluginInterface().getTracker().getTorrents();
					for (TrackerTorrent tt : torrents) {
						String hash = EncodingUtil.encode(tt.getTorrent().getHash());
						if (hash.equalsIgnoreCase(xmlRequest.getAttributeValue("hash"))) {
							try {
								tt.stop();
							} catch (TrackerException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				}
				return false;
			}
		});
		addHandler("trackerTorrentStart", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					TrackerTorrent[] torrents = Plugin.getPluginInterface().getTracker().getTorrents();
					for (TrackerTorrent tt : torrents) {
						String hash = EncodingUtil.encode(tt.getTorrent().getHash());
						if (hash.equalsIgnoreCase(xmlRequest.getAttributeValue("hash"))) {
							try {
								tt.start();
							} catch (TrackerException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				}
				return false;
			}
		});
		addHandler("ipcCall", new RequestHandler() {
			public boolean handleRequest(Element xmlRequest, Element response,
					final User user) throws IOException {
				response.setAttribute("senderID",
						xmlRequest.getAttributeValue("senderID"));
				response.setAttribute("pluginID",
						xmlRequest.getAttributeValue("pluginID"));
				response.setAttribute("method",
						xmlRequest.getAttributeValue("method"));
				if (xmlRequest.getAttribute("pluginID") == null) {
					return false;
				} else {
					PluginManager pm = Plugin.getPluginInterface().getPluginManager();
					PluginInterface tPi = pm.getPluginInterfaceByID(xmlRequest.getAttributeValue("pluginID"));
					if (tPi == null) {
						response.setAttribute(
								"status",
								Integer.toString(RemoteConstants.IPC_ERROR_PLUGIN_NOT_FOUND));
						return true;
					}
					List<Element> paramList = xmlRequest.getChildren("Parameter");
					List<Object> params = new Vector<Object>();
					for (Element e : paramList) {
						switch (Integer.parseInt(e.getAttributeValue("type"))) {
						case RemoteConstants.PARAMETER_NULL:
							params.add(null);
							break;
						case RemoteConstants.PARAMETER_BOOLEAN:
							params.add(Boolean.parseBoolean(e.getText()));
							break;
						case RemoteConstants.PARAMETER_FLOAT:
							params.add(Float.parseFloat(e.getText()));
							break;
						case RemoteConstants.PARAMETER_INT:
							params.add(Integer.parseInt(e.getText()));
							break;
						case RemoteConstants.PARAMETER_STRING:
							params.add(e.getText());
							break;
						case RemoteConstants.PARAMETER_LONG:
							params.add(Long.parseLong(e.getText()));
							break;
						case RemoteConstants.PARAMETER_DOUBLE:
							params.add(Double.parseDouble(e.getText()));
							break;
						case RemoteConstants.PARAMETER_XML_ELEMENT:
							params.add(e.getChildren().get(0));
							break;
						}
					}

					IPCInterface ipcI = tPi.getIPC();
					Object result = null;
					try {
						result = ipcI.invoke(
								xmlRequest.getAttributeValue("method"),
								params.toArray());
					} catch (IPCException e) {
						e.printStackTrace();
						response.setAttribute("status",
								Integer.toString(RemoteConstants.IPC_EXCEPTION));
						response.setText(e.getMessage());
						return true;
					}
					if (result == null) {
						return false;
					}
					Element resultElement = new Element("Result");
					if (result instanceof Boolean) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_BOOLEAN));
						resultElement.setText(result.toString());
					} else if (result instanceof Integer) {
						resultElement.setAttribute("type",
								Integer.toString(RemoteConstants.PARAMETER_INT));
						resultElement.setText(result.toString());
					} else if (result instanceof Float) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_FLOAT));
						resultElement.setText(result.toString());
					} else if (result instanceof String) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_STRING));
						resultElement.setText(result.toString());
					} else if (result instanceof Long) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_LONG));
						resultElement.setText(result.toString());
					} else if (result instanceof Double) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_DOUBLE));
						resultElement.setText(result.toString());
					} else if (result instanceof Element) {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_XML_ELEMENT));
						resultElement.addContent((Element) result);
					} else if (result instanceof Serializable) {
						try {
							resultElement.setAttribute(
									"type",
									Integer.toString(RemoteConstants.PARAMETER_SERIALZED_OBJECT));
							resultElement.addContent(Serializer.serializeObjectToElement((Serializable) result));
						} catch (Exception e1) {
							resultElement.setAttribute(
									"type",
									Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
							e1.printStackTrace();
						}
					} else {
						resultElement.setAttribute(
								"type",
								Integer.toString(RemoteConstants.PARAMETER_NOT_FOUND));
					}

					response.addContent(resultElement);
					response.setAttribute("status",
							Integer.toString(RemoteConstants.IPC_OK));
				}
				return true;
			}
		});
	}
}
