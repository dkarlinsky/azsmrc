package lbms.azsmrc.plugin.main;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;

public class MultiUser {
	public static final String SHARED_CAT_NAME = "SHARED";
	public static final String SHARED_USER_NAME = "$SHARED$";
	public static final String PUBLIC_USER_NAME = "$PUBLIC$";
	public static final TorrentAttribute TA_USER = Plugin.getPluginInterface().getTorrentManager().getPluginAttribute("User");
	public static final TorrentAttribute TA_CATEGORY = Plugin.getPluginInterface().getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);

	public static boolean isPublicDownload (Download dl) {
		return (dl.getAttribute(MultiUser.TA_USER) == null || dl.getAttribute(MultiUser.TA_USER).equals(PUBLIC_USER_NAME));
	}

	public static boolean isSharedDownload (Download dl) {
		return (dl.getAttribute(MultiUser.TA_USER) == null || dl.getAttribute(MultiUser.TA_USER).equals(SHARED_USER_NAME));
	}

	public static void addUserToDownload (User u, Download dl) {
		if (isPublicDownload(dl)) {
			dl.setAttribute(TA_USER, u.getUsername());
			if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("useUsernamesAsCategory", false)) {
				dl.setAttribute(TA_CATEGORY, u.getUsername());
			}
		} else {
			dl.setAttribute(TA_USER, SHARED_USER_NAME);
			if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("useUsernamesAsCategory", false)) {
				dl.setAttribute(TA_CATEGORY, SHARED_CAT_NAME);
			}
		}
	}

	public static void removeUserFromDownload (User u, Download dl) {
		if (!isPublicDownload(dl)) {//if publis this DL wasn't owned anyway
			if (isSharedDownload(dl)) {
				User[] users = Plugin.getXMLConfig().getUsersOfDownload(dl);

				if (users.length < 3) {
					//if this user is remove the Download isn't shared anymore
					if (users[0].equals(u)) {
						dl.setAttribute(TA_USER, users[1].getUsername());
						if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("useUsernamesAsCategory", false)) {
							dl.setAttribute(TA_CATEGORY, users[1].getUsername());
						}
					} else if (users[1].equals(u)) {
						dl.setAttribute(TA_USER, users[0].getUsername());
						if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("useUsernamesAsCategory", false)) {
							dl.setAttribute(TA_CATEGORY, users[0].getUsername());
						}
					}
				}
			} else {
				//Download is public again
				dl.setAttribute(TA_USER, PUBLIC_USER_NAME);
				if (Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("useUsernamesAsCategory", false)) {
					dl.setAttribute(TA_CATEGORY, null);
				}
			}
		}
	}
}
