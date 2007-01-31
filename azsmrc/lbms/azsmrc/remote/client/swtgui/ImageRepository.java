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
 * @author omschaub, damokles
 *
 */
public class ImageRepository {

	private static HashMap<String,ImageContainer> images;
	private static Display display;

	static {
		images = new HashMap<String,ImageContainer>();
	}


	public static void preloadImages(Display display) {
		ImageRepository.display = display;
		//Splash Screen Pic
		//loadImage(display,"lbms/azsmrc/remote/client/swtgui/resources/AzSMRC_Splash.png","splash",255);


		registerImage("lbms/azsmrc/remote/client/swtgui/resources/right_arrow.gif","right_arrow",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/down_arrow.gif","down_arrow",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/background.png","backgroundImage",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/bar.png","barImage",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/gg_connecting.gif","icon",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/edit_add.png","plus",255);

		registerImage("lbms/azsmrc/remote/client/swtgui/resources/OpenFile.png","openfile",255);

		//Tray Icons
		registerImage("lbms/azsmrc/remote/client/resources/TrayIcon_blue.png","TrayIcon_Blue",255);
		registerImage("lbms/azsmrc/remote/client/resources/TrayIcon_red.png","TrayIcon_Red",255);
		registerImage("lbms/azsmrc/remote/client/resources/TrayIcon_connecting.png","TrayIcon_Connecting",255);
		registerImage("lbms/azsmrc/remote/client/resources/TrayIcon_disconnected.png","TrayIcon_Disconnected",255);

		//Torrent Move Icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/bottom.png","bottom",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/down.png","down",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/up.png","up",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/top.png","top",255);

		//Health Icons
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/health/no_tracker.gif","health_blue",255);
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/health/ok.gif","health_green",255);
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/health/ko.gif","health_red",255);
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/health/no_remote.gif","health_yellow",255);
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/health/stopped.gif","health_gray",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/Health_green.png","health_green",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/Health_blue.png","health_blue",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/Health_gray.png","health_gray",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/Health_red.png","health_red",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/Health_yellow.png","health_yellow",255);

		//Statusbar icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_creating.png","connect_creating",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_no.png","connect_no",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/connect_established.png","connect_established",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/decrypted_new.png","ssl_disabled",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/encrypted.png","ssl_enabled",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/statusbar_down.png","statusbar_down",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusbar/statusbar_up.png","statusbar_up",255);

		//Context menu icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/menu/stop.png","menu_stop",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/menu/remove.png","menu_remove",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/menu/recheck.png","menu_recheck",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/menu/queue.png","menu_queue",255);


		//Toolbar icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/open_by_file_new.png","open_by_file",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/open_by_url_new.png","open_by_url",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/preferences.png","preferences",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/logout_new.png","logout",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/information.png","information",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/connect_new.png","connect",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/connect_quick_new.png","connect_quick",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/refresh.png","refresh",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/tool_pause.png","pause",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/tool_resume.png","resume",255);
		//registerImage("lbms/azsmrc/remote/client/swtgui/resources/server.png","server",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/manage_users.png","manager_users",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/console.png","console",255);

		registerImage("lbms/azsmrc/remote/client/swtgui/resources/popup.png","popup",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/toolbar/queue_large.png","toolbar_queue",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/toolbar/remove_large.png","toolbar_remove",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/toolbar/stop_large.png","toolbar_stop",255);

		//paypal and amazon
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/paypal.gif","paypal",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/amazon-com.png","amazon.com",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/amazon-de.png","amazon.de",255);
		//if needed
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/tool_delete.png","trashcan",255);


		//Server Details toolbar icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/serverIcons/user_add.png","add",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/serverIcons/user_delete.png","delete",255);
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/serverIcons/folder_new.png","folder",255);

		//UpdateProgressDialog
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/progress_stop.png","progress_stop",255);

		//Status Shell Icons
		registerImage("lbms/azsmrc/remote/client/swtgui/resources/statusShell/close.png","statusShell_close",255);
		for (ImageContainer ic:images.values()) {
			loadImage(ic);
		}
	}

	public static void registerImage(String url, String name, int alpha) {
		images.put(name, new ImageContainer(url,alpha));
	}

	private static Image loadImage (ImageContainer ic) {
		if(ic.getImage() == null || ic.getImage().isDisposed()) {
			InputStream is = ImageRepository.class.getClassLoader().getResourceAsStream(ic.getUrl());
			if(null != is) {
				if(ic.getAlpha() == 255) {
					ic.setImage(new Image(display, is));
				} else {
					ImageData icone = new ImageData(is);
					icone.alpha = ic.getAlpha();
					ic.setImage(new Image(display,icone));
				}
			} else {
				System.out.println("ImageRepository:loadImage:: Resource not found: " + ic.getUrl());
			}
		}
		return ic.getImage();
	}


	public static Image loadImage(Display display, String res, String name){
		return loadImage(display,res,name,255);
	}

	public static Image loadImage(Display display, String res, String name,int alpha) {
		Image im = getImage(name);
		if(null == im || im.isDisposed()) {
			InputStream is = ImageRepository.class.getClassLoader().getResourceAsStream(res);
			if(null != is) {
				if(alpha == 255) {
					im = new Image(display, is);
				} else {
					ImageData icone = new ImageData(is);
					icone.alpha = alpha;
					im = new Image(display,icone);
				}
				images.get(name).setImage(im);
			} else {
				System.out.println("ImageRepository:loadImage:: Resource not found: " + res);
			}
		}
		return im;
	}

	public static void unLoadImages() {
		Iterator<ImageContainer> iter = images.values().iterator();
		while (iter.hasNext()) {
			iter.next().dispose();
		}
	}

	public static Image getImage(String name) {
		if (images.containsKey(name)) {
			Image im = images.get(name).getImage();
			if (im == null || im.isDisposed()) {
				im = loadImage(images.get(name));
			}
			return im;
		} else
			return null;
	}

	protected static class ImageContainer {
		private String url;
		private int alpha;
		private Image img;

		public ImageContainer(String url, int alpha) {
			this.url = url;
			this.alpha = alpha;
		}

		/**
		 * @return the alpha
		 */
		public int getAlpha() {
			return alpha;
		}

		/**
		 * @param alpha the alpha to set
		 */
		public void setAlpha(int alpha) {
			this.alpha = alpha;
		}

		/**
		 * @return the img
		 */
		public Image getImage() {
			return img;
		}

		/**
		 * @param img the img to set
		 */
		public void setImage(Image img) {
			this.img = img;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		public void dispose () {
			if (img != null)
				img.dispose();
		}
	}

}
