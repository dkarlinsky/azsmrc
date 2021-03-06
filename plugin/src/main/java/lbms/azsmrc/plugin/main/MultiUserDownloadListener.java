package lbms.azsmrc.plugin.main;

import java.io.File;
import java.io.IOException;

import lbms.azsmrc.shared.UserNotFoundException;

import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;

public class MultiUserDownloadListener implements
		org.gudy.azureus2.plugins.download.DownloadListener {

	static private MultiUserDownloadListener	instance	= new MultiUserDownloadListener();

	// static private TorrentAttribute taCategory =
	// Plugin.getPluginInterface().getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);

	private MultiUserDownloadListener() {
	}

	public static MultiUserDownloadListener getInstance() {
		return instance;
	}

	public void positionChanged(Download download, int oldPosition,
			int newPosition) {
	}

	public void stateChanged(final Download download, int old_state,
			int new_state) {
		if (old_state == Download.ST_DOWNLOADING && download.isComplete()) {
			String user_attrib = download.getAttribute(MultiUser.TA_USER);
			download.removeListener(this); // remove after trigger
			final boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter(
					"singleUserMode", false);

			if (user_attrib != null && !MultiUser.isPublicDownload(download)) {
				try {

					// only notify and not move if download is not in def save
					// dir
					if (!download.getSavePath().contains(
							Plugin.getPluginInterface().getPluginconfig().getUnsafeStringParameter(
									"Default save path", null))) {

						if (!singleUser) {
							if (MultiUser.isSharedDownload(download)) {
								User[] users = Plugin.getXMLConfig().getUsersOfDownload(
										download);
								for (int i = 0; i < users.length - 1; i++) {
									users[i].eventDownloadFinished(download);
								}
							} else {
								User user = Plugin.getXMLConfig().getUser(
										user_attrib);
								user.eventDownloadFinished(download);
							}
						} else {
							User[] users = Plugin.getXMLConfig().getUsers();
							for (User u : users) {
								u.eventDownloadFinished(download);
							}
						}
						return;// not in standart save path
					}

					// move download as well
					if (MultiUser.isSharedDownload(download)) { // Multiple
																// owner
						DiskManagerFileInfo[] fileInfo = download.getDiskManagerFileInfo();
						final File[] files = new File[fileInfo.length];
						for (int i = 0; i < files.length; i++) {
							files[i] = fileInfo[i].getFile();
						}
						final User[] users = Plugin.getXMLConfig().getUsersOfDownload(
								download);
						if (users != null) {
							if (users.length == 1) {
								if (users[0].getOutputDir() != null
										&& !users[0].getOutputDir().equals("")) {
									File outDir = new File(
											users[0].getOutputDir());
									if (!outDir.exists()) {
										outDir.mkdirs();
									}
									download.moveDataFiles(new File(
											users[0].getOutputDir()));
									download.moveTorrentFile(new File(
											users[0].getOutputDir()));
								}
								if (!singleUser) {
									users[0].eventDownloadFinished(download);
								}
							} else {
								new Thread(new Runnable() {
									public void run() {
										for (int i = 0; i < users.length - 1; i++) {
											if (users[i].getOutputDir() != null
													&& !users[i].getOutputDir().equals(
															"")) {
												File outDir = new File(
														users[i].getOutputDir());
												if (!outDir.exists()) {
													outDir.mkdirs();
												}
												for (File file : files) {
													try {
														Utilities.copy(
																file,
																new File(
																		users[i].getOutputDir()),
																false);
													} catch (IOException e) {
														Plugin.addToLog(e.getMessage());
														e.printStackTrace();
													}
												}
											}
											if (!singleUser) {
												users[i].eventDownloadFinished(download);
											}
										}
										try {
											if (users[users.length - 1].getOutputDir() != null
													&& !users[users.length - 1].getOutputDir().equals(
															"")) {
												File outDir = new File(
														users[users.length - 1].getOutputDir());
												if (!outDir.exists()) {
													outDir.mkdirs();
												}
												download.moveDataFiles(new File(
														users[users.length - 1].getOutputDir()));
												download.moveTorrentFile(new File(
														users[users.length - 1].getOutputDir()));
											}
											if (!singleUser) {
												users[users.length - 1].eventDownloadFinished(download);
											}

										} catch (DownloadException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}).start();
							}
						}
					} else { // only a Single Download owner
						User user = Plugin.getXMLConfig().getUser(user_attrib);
						if (user.getOutputDir() != null
								&& !user.getOutputDir().equals("")) {
							File outDir = new File(user.getOutputDir());
							if (!outDir.exists()) {
								outDir.mkdirs();
							}
							download.moveDataFiles(new File(user.getOutputDir()));
							download.moveTorrentFile(new File(
									user.getOutputDir()));
						}
						if (!singleUser) {
							user.eventDownloadFinished(download);
						}
					}
				} catch (UserNotFoundException e) {
					Plugin.addToLog("Cannot find a User associated with this Download "
							+ download.getName() + "; Category: " + user_attrib);
				} catch (DownloadException e) {
					Plugin.addToLog(e.getMessage());
				}

			} else if (!singleUser) {
				User[] users = Plugin.getXMLConfig().getUsers();
				for (User u : users) {
					u.eventDownloadFinished(download);
				}
			}

			if (singleUser) {
				User[] users = Plugin.getXMLConfig().getUsers();
				for (User u : users) {
					u.eventDownloadFinished(download);
				}
			}
		}
	}
}
