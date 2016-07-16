/*
 * Created on Dec 2, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.plugin.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentManager;
import org.gudy.azureus2.plugins.utils.UTTimer;
import org.gudy.azureus2.plugins.utils.UTTimerEvent;
import org.gudy.azureus2.plugins.utils.UTTimerEventPerformer;

public class Timers {
	private static UTTimer timer;
	private static UTTimerEvent checkDirTimer;

	public static synchronized void initTimer () {
		timer = Plugin.getPluginInterface().getUtilities().createTimer("azsmrc");
		startCheckDirsTimer();
	}

	public static synchronized void startCheckDirsTimer () {
		final PluginInterface pluginInterface = Plugin.getPluginInterface();
		if (checkDirTimer != null) checkDirTimer.cancel();

		checkDirTimer = timer.addPeriodicEvent( Plugin.getPluginInterface().getPluginconfig().getPluginIntParameter("user_dir_scan_time", 60) * 1000,

				new UTTimerEventPerformer()

				{

			public void perform(UTTimerEvent ev1 ) {
				try {
					XMLConfig conf = Plugin.getXMLConfig();
					String[] userList = conf.getUserList();
					TorrentManager torrentManager = pluginInterface.getTorrentManager();
					DownloadManager dm = Plugin.getPluginInterface().getDownloadManager();
					Plugin.addToLog("Checking AutoImpdirs...");
					boolean dlAdded = false;
					for (String userName:userList) {
						User user = conf.getUser(userName);
						try {
							String dirName = user.getAutoImportDir();
							String outDirName = user.getOutputDir();
							File dir = new File(dirName);
							File outputDir = new File (outDirName);
							if (dir.exists() && dir.canRead() && outputDir.exists() && outputDir.canRead()) {
								File[] torrents = dir.listFiles(new FilenameFilter() {
									public boolean accept(File dir, String name) {
										if (name.endsWith(".torrent")) return true;
										return false;
									}});
								for (File torrent:torrents) {
									try {
										FileInputStream fin = new FileInputStream(torrent);
										Torrent new_torrent =  torrentManager.createFromBEncodedInputStream(fin);
										fin.close();
										Download dl = dm.addDownload(new_torrent);
										if (dl.isComplete()) continue;

										MultiUser.addUserToDownload(user, dl);

										dlAdded = true;
										user.addDownload(dl);
										Plugin.addToLog("Moving Torrent to: "+Plugin.getPluginInterface().getPluginconfig().getStringParameter("General_sDefaultTorrent_Directory")+"/"+torrent.getName());
										if (!torrent.renameTo(new File(Plugin.getPluginInterface().getPluginconfig().getStringParameter("General_sDefaultTorrent_Directory")+"/"+torrent.getName())))
											torrent.delete();
										Plugin.addToLog("Added Torrent: "+torrent.getAbsolutePath());
										Plugin.addToLog("Added Download: "+dl.getName());

									} catch (DownloadException e) {
										Plugin.addToLog(e.getMessage());
									} catch (Exception e1) {
										Plugin.addToLog(e1.getMessage());
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (dlAdded) conf.saveConfigFile();

				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public static void stopCheckDirsTimer () {
		checkDirTimer.cancel();
	}

	public static UTTimerEvent addPeriodicEvent (long period_millis, UTTimerEventPerformer ep) {
		return timer.addPeriodicEvent(period_millis, ep);
	}

	public static void restartCheckDirsTimer () {
		stopCheckDirsTimer();
		startCheckDirsTimer();
	}

	/**
	 * Destroy the checkDirsTimer
	 * Warning:  This timer can not be used again until the plugin restarts
	 *
	 */
	public static void destroy_checkDirsTimer() {
		timer.destroy();
	}

	public static UTTimer getTimer () {
		return timer;
	}

}