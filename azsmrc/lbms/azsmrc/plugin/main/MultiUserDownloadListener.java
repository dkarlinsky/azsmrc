package lbms.azsmrc.plugin.main;

import java.io.File;
import java.io.IOException;

import lbms.azsmrc.shared.UserNotFoundException;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;

public class MultiUserDownloadListener implements org.gudy.azureus2.plugins.download.DownloadListener {

	static private MultiUserDownloadListener instance = new MultiUserDownloadListener();
	//static private TorrentAttribute taCategory = Plugin.getPluginInterface().getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
	static private TorrentAttribute taUser = Plugin.getPluginInterface().getTorrentManager().getPluginAttribute("User");

	private MultiUserDownloadListener() {}

	public static MultiUserDownloadListener getInstance() {
		return instance;
	}

	public void positionChanged(Download download, int oldPosition, int newPosition) {
	}

	public void stateChanged(final Download download, int old_state, int new_state) {
		if (old_state == Download.ST_DOWNLOADING && download.isComplete()) {
			String user_attrib = download.getAttribute(taUser);
			download.removeListener(this); //remove after trigger
			final boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);if (user_attrib != null) {
				try {
					if (!download.getSavePath().contains(COConfigurationManager.getStringParameter("Default save path"))) return;//not in standart save path
					if (user_attrib.equalsIgnoreCase(MultiUser.SHARED_USER_NAME) ) { //Multiple owner
						DiskManagerFileInfo[] fileInfo = download.getDiskManagerFileInfo();
						final File[] files = new File[fileInfo.length];
						for (int i=0;i<files.length;i++) {
							files[i] = fileInfo[i].getFile();
						}
						final User[] users = Plugin.getXMLConfig().getUsersOfDownload(download);
						if (users != null) {
							if (users.length == 1) {
								download.moveDataFiles(new File(users[0].getOutputDir()));
								download.moveTorrentFile(new File(users[0].getOutputDir()));
							} else {
								new Thread(new Runnable (){
									public void run () {
										for (int i=0;i<users.length-1;i++) {
											for (File file:files) {
												try {
													Utilities.copy(file, new File(users[i].getOutputDir()), false);
												} catch (IOException e) {
													Plugin.addToLog(e.getMessage());
													e.printStackTrace();
												}
											}
											if (!singleUser)users[i].eventDownloadFinished(download);
										}
										try {
											download.moveDataFiles(new File(users[users.length-1].getOutputDir()));
											download.moveTorrentFile(new File(users[users.length-1].getOutputDir()));
											if (!singleUser)users[users.length-1].eventDownloadFinished(download);

										} catch (DownloadException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}).start();
							}
						}
					} else { //only a Single Download owner
						User user = Plugin.getXMLConfig().getUser(user_attrib);
						download.moveDataFiles(new File(user.getOutputDir()));
						download.moveTorrentFile(new File(user.getOutputDir()));
						if (!singleUser)user.eventDownloadFinished(download);
					}
				} catch (UserNotFoundException e) {
					Plugin.addToLog("Cannot find a User associated with this Download "+download.getName()+"; Category: "+user_attrib);
				} catch (DownloadException e) {
					Plugin.addToLog(e.getMessage());
				}
			}
			if (singleUser) {
				User[] users = Plugin.getXMLConfig().getUsers();
				for (User u:users) {
					u.eventDownloadFinished(download);
				}
			}
		}
	}
}
