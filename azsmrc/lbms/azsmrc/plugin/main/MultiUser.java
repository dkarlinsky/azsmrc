package lbms.azsmrc.plugin.main;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;

public class MultiUser {
	public static final String SHARED_CAT_NAME = "SHARED";
	public static final String SHARED_USER_NAME = "$SHARED$";
	public static final String PUBLIC_DOWNLOAD_NAME = "$PUBLIC$";
	public static final TorrentAttribute TA_USER = Plugin.getPluginInterface().getTorrentManager().getPluginAttribute("User");

	public static boolean isPublicDownload (Download dl) {
		return (dl.getAttribute(MultiUser.TA_USER) == null || dl.getAttribute(MultiUser.TA_USER).equals(SHARED_USER_NAME));
	}
}
