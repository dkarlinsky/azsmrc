/*
 * Created on Sep 14, 2004
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * @author omschaub
 *
 */
public class ImageRepository {

	private static HashMap<String,Image> images;

	static {
		images = new HashMap<String,Image>();
	}


	public static void loadImages(Display display) {

		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/right_arrow.gif","right_arrow",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/down_arrow.gif","down_arrow",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/background.png","backgroundImage",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/bar.png","barImage",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/gg_connecting.gif","icon",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/edit_add.png","plus",255);

		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/OpenFile.png","openfile",255);

		//Tray Icons
		loadImage(display,"lbms/azsmrc/remote/client/resources/TrayIcon_blue.png","TrayIcon_Blue",255);
		loadImage(display,"lbms/azsmrc/remote/client/resources/TrayIcon_red.png","TrayIcon_Red",255);
		loadImage(display,"lbms/azsmrc/remote/client/resources/TrayIcon_connecting.png","TrayIcon_Connecting",255);
		//Torrent Move Icons
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/bottom.png","bottom",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/down.png","down",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/up.png","up",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/top.png","top",255);

		//Health Icons
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/health/no_tracker.gif","health_blue",255);
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/health/ok.gif","health_green",255);
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/health/ko.gif","health_red",255);
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/health/no_remote.gif","health_yellow",255);
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/health/stopped.gif","health_gray",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/Health_green.png","health_green",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/Health_blue.png","health_blue",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/Health_gray.png","health_gray",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/Health_red.png","health_red",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/Health_yellow.png","health_yellow",255);

		//Statusbar icons
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_creating.png","connect_creating",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_no.png","connect_no",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_established.png","connect_established",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/decrypted_new.png","ssl_disabled",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/encrypted.png","ssl_enabled",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/statusbar_down.png","statusbar_down",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/statusbar/statusbar_up.png","statusbar_up",255);

		//Context menu icons
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/menu/stop.png","menu_stop",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/menu/remove.png","menu_remove",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/menu/recheck.png","menu_recheck",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/menu/queue.png","menu_queue",255);


		//Toolbar icons
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/open_by_file_new.png","open_by_file",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/open_by_url_new.png","open_by_url",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/preferences.png","preferences",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/logout_new.png","logout",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/information.png","information",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/connect_new.png","connect",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/connect_quick_new.png","connect_quick",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/refresh.png","refresh",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/tool_pause.png","pause",255);
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/tool_resume.png","resume",255);
        //loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/server.png","server",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/manage_users.png","manager_users",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/console.png","console",255);

		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/popup.png","popup",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/toolbar/queue_large.png","toolbar_queue",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/toolbar/remove_large.png","toolbar_remove",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/toolbar/stop_large.png","toolbar_stop",255);

        //paypal
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/paypal.gif","paypal",255);

		//if needed
		loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/tool_delete.png","trashcan",255);


        //Server Details toolbar icons
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/serverIcons/user_add.png","add",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/serverIcons/user_delete.png","delete",255);
        loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/serverIcons/folder_new.png","folder",255);

	}


	public static Image loadImage(Display display, String res, String name){
		return loadImage(display,res,name,255);
	}

	public static Image loadImage(Display display, String res, String name,int alpha) {
		Image im = getImage(name);
		if(null == im) {
			InputStream is = ImageRepository.class.getClassLoader().getResourceAsStream(res);
			if(null != is) {
				if(alpha == 255) {
					im = new Image(display, is);
				} else {
					ImageData icone = new ImageData(is);
					icone.alpha = alpha;
					im = new Image(display,icone);
				}
				images.put(name, im);
			} else {
				System.out.println("ImageRepository:loadImage:: Resource not found: " + res);
			}
		}
		return im;
	}

	public static void unLoadImages() {
		Iterator<Image> iter = images.values().iterator();
		while (iter.hasNext()) {
			Image im = iter.next();
			im.dispose();
		}
	}

	public static Image getImage(String name) {
		return images.get(name);
	}

}
