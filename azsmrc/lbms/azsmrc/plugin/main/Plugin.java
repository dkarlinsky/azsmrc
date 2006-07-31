/*
 * Created on Nov 16, 2005
 */
package lbms.azsmrc.plugin.main;

import java.net.URL;
import java.security.KeyStore;

import lbms.azsmrc.plugin.gui.View;
import lbms.azsmrc.plugin.web.RequestManager;
import lbms.azsmrc.plugin.web.WebRequestHandler;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

import org.eclipse.swt.widgets.Display;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginConfigListener;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.download.DownloadManagerListener;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.tracker.Tracker;
import org.gudy.azureus2.plugins.tracker.TrackerException;
import org.gudy.azureus2.plugins.tracker.web.TrackerAuthenticationAdapter;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebContext;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.update.UpdateCheckInstance;
import org.gudy.azureus2.plugins.update.UpdateCheckInstanceListener;
import org.gudy.azureus2.plugins.update.UpdateManager;
import org.gudy.azureus2.plugins.update.UpdateManagerListener;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;




public class Plugin implements org.gudy.azureus2.plugins.Plugin {

	private static LocaleUtilities locale_utils;

	public static String LOGGED_IN_USER;

	PluginInterface pluginInterface;
	private static PluginInterface pi;
	private static Display display;

	//the XMLConfig file to be used
	private static XMLConfig config;

	//The logger from Azureus
	private static LoggerChannel logger;

	private static UpdateCheckInstance latestUpdate;

	//new API startup code
	UISWTInstance swtInstance = null;
	UISWTViewEventListener myView = null;

	public void initialize(final PluginInterface pluginInterface) {

		this.pluginInterface = pluginInterface;
		locale_utils = pluginInterface.getUtilities().getLocaleUtilities();
		locale_utils.integrateLocalisedMessageBundle("lbms.azsmrc.plugin.internat.Messages");
		pi=pluginInterface;

		UIManager   ui_manager = pluginInterface.getUIManager();
		BasicPluginConfigModel config_model = ui_manager.createBasicPluginConfigModel( "plugins", "plugin.azsmrc");

		//settings on main options panel
		config_model.addBooleanParameter2("azsmrc_military_time","azsmrc.military.time",false);
		config_model.addBooleanParameter2("azsmrc_auto_open","azsmrc.auto.open",true);
		config_model.addBooleanParameter2("singleUserMode", "azsmrc.singleusermode", false);
		config_model.addBooleanParameter2("disableAutoImport", "azsmrc.disable.auto.import", false);
		config_model.addBooleanParameter2("use_ssl","azsmrc.use.ssl",false);
		config_model.addIntParameter2("remote_port", "azsmrc.remote.port", 49009);
		config_model.addLabelParameter2("azsmrc.portchange.alert");

		//Load the config file
		config = XMLConfig.loadConfigFile(pluginInterface.getPluginDirectoryName() + System.getProperty("file.separator") + "MultiUserConfig.xml");

		//initialize the logger
		logger = pluginInterface.getLogger().getTimeStampedChannel("AzSMRC");
		UpdateManager um = pluginInterface.getUpdateManager();
		um.addListener(
				new UpdateManagerListener()
				{
					public void
					checkInstanceCreated(
						UpdateCheckInstance instance )
					{

						instance.addListener( new UpdateCheckInstanceListener() {
							public void cancelled(UpdateCheckInstance instance) {
								if (latestUpdate != null && latestUpdate == instance)
									latestUpdate = null;
							}

							public void complete(UpdateCheckInstance instance) {
								//return if update list is 0
								if (instance.getUpdates().length == 0) return;
								latestUpdate = instance;

								User[] users = config.getUsers();
								for (User user:users) {
									if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN))
										user.eventUpdateAvailable();
								}
							}
						});
					}
				});

		//initialize the timer
		Timers.initTimer();
		if (pluginInterface.getPluginconfig().getPluginBooleanParameter("disableAutoImport",false)) {
			Timers.stopCheckDirsTimer();
		}

		pluginInterface.getPluginconfig().addListener(new PluginConfigListener() {
			public void configSaved() {
				if (pluginInterface.getPluginconfig().getPluginBooleanParameter("disableAutoImport",false)) {
					Timers.stopCheckDirsTimer();
				} else {
					Timers.startCheckDirsTimer();
				}
			};
		});

		//add the download listeners to the downloads
		DownloadManager dm = pluginInterface.getDownloadManager();
		config.removeInvalidDownloadsFromUsers(dm.getDownloads());
		final TorrentAttribute ta = pluginInterface.getTorrentManager().getAttribute(TorrentAttribute.TA_CATEGORY);
		dm.addListener( new DownloadManagerListener()
				{
					public void downloadAdded(Download dl) {
						if (!dl.isComplete() && dl.getAttribute(ta)!= null)	//add only if the download isn't already complete
							dl.addListener(MultiUserDownloadListener.getInstance()); //attach DownloadListener
					}

					public void downloadRemoved(Download dl) {
						boolean singleUser = Plugin.getPluginInterface().getPluginconfig().getPluginBooleanParameter("singleUserMode", false);
						User[] users;
						if (singleUser) {
							users = getXMLConfig().getUsers();
						} else {
							users = getXMLConfig().getUsersOfDownload(dl);
						}
						for (User u:users) {
							u.eventDownloadRemoved(dl);
						}
					}
				});

		pluginInterface.getUIManager().addUIListener(new UIManagerListener() {
			public void UIAttached(UIInstance instance) {
			  if (instance instanceof UISWTInstance) {
				swtInstance = (UISWTInstance)instance;
				display = swtInstance.getDisplay();
				myView = new View(pluginInterface);
				swtInstance.addView(UISWTInstance.VIEW_MAIN, View.VIEWID, myView);
				if(isPluginAutoOpen()){
					swtInstance.openMainView(View.VIEWID,myView,null);
				}
			  }
			}

			public void UIDetached(UIInstance instance) {
			  if (instance instanceof UISWTInstance) {
				swtInstance = null;
			  }
			}
		  });


		//----------------Menu for My Torrents------------------\\
/*		MenuItemFillListener fill_listener =
			new MenuItemFillListener(){

				public void menuWillBeShown(MenuItem menu, Object data) {
					TableRow[] rows = (TableRow[])data;
					if(rows == null) return;

					//Code here to control BEFORE menu is drawn

					//if(!admin)
					//menu.setEnabled(false);
				}

		};





		MenuItemListener    listener =
			new MenuItemListener()
			{
				public void
				selected(
					MenuItem        _menu,
					Object          _target )
				{
					Download download = (Download)((TableRow)_target).getDataSource();
					if ( download == null || download.getTorrent() == null )return;

					//do something with download here


				}
			};



			final TableContextMenuItem menu1 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE,     "azsmrc.mytorrents.menutext1" );
			final TableContextMenuItem menu2 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE,     "azsmrc.mytorrents.menutext1");

			menu1.addFillListener(fill_listener);
			menu2.addFillListener(fill_listener);
			menu1.addListener( listener );
			menu2.addListener( listener );*/

		//-------------------------------------------------------\\

		//Initialize Request Manager
		RequestManager.getInstance().initialize(pi);

		try{
			TrackerWebContext	context;
			boolean sslSupported = false;

			try {
				KeyStore key = pluginInterface.getUtilities().getSecurityManager().getKeyStore();
				if (key.size()>0) sslSupported = true;
			} catch (Exception e1) {}
			pluginInterface.getPluginconfig().setPluginParameter("ssl_supported", sslSupported);

			if (sslSupported) {
				addToLog("SSL is supported!");
			} else {
				addToLog("SSL is not supported!");
			}

			if (sslSupported && pluginInterface.getPluginconfig().getPluginBooleanParameter("use_ssl",false)) {
				context =	pluginInterface.getTracker().createWebContext(
							pluginInterface.getAzureusName() + " - " + pluginInterface.getPluginName(),
							pluginInterface.getPluginconfig().getPluginIntParameter("remote_port",49009), Tracker.PR_HTTPS );
			} else {
				context =	pluginInterface.getTracker().createWebContext(
							pluginInterface.getAzureusName() + " - " + pluginInterface.getPluginName(),
							pluginInterface.getPluginconfig().getPluginIntParameter("remote_port",49009), Tracker.PR_HTTP );
			}
			context.addPageGenerator( new WebRequestHandler(pluginInterface) );

			context.addAuthenticationListener( new TrackerAuthenticationAdapter()
				{
					AEMonitor	this_mon = new AEMonitor( "AZMultiUser:auth" );

					public boolean
					authenticate(
						URL			resource,
						String		user,
						String		pw )
					{
						try{
							this_mon.enter();
							if(user==null||pw==null) return false;
							User userObj = config.getUser(user);
							if (userObj.verifyPassword(pw)) {
								addToLog("User: "+userObj+" has logged in.");
								return true;
							}
							else return false;

						} catch (UserNotFoundException e) {
							return false;
						}
						finally{
							this_mon.exit();
						}
					}
				});

		}catch( TrackerException e ){

			addToLog("Web Initialisation Fails",e);
		}

	}



	/**
	 * Gets the pluginInterface from  Plugin.java
	 * @return pluginInterface
	 */
	public static PluginInterface getPluginInterface(){
		return pi;
	}

	/**
	 * Gets the Display from  Plugin.java from the UISWTInstance
	 * @return display
	 */
	public static Display getDisplay(){
		return display;
	}

	/**
	 * Returns the user set status of whether or not the plugin should autoOpen
	 * @return boolean autoOpen
	 */
	public static boolean isPluginAutoOpen(){
		PluginConfig config_getter = getPluginInterface().getPluginconfig();
		return config_getter.getPluginBooleanParameter("azsmrc_auto_open",true);
	}

	/**
	 * Returns the initialized XMLConfig file
	 * @return config -- initialized XMLConfig file
	 */
	public static XMLConfig getXMLConfig(){
		return config;
	}


	/**
	 * Adds given String to the Azureus log as well as dumping it to the console
	 * @param textToAdd
	 */
	public static void addToLog(String textToAdd){
		//don't spam console if debug is deaktivated
		if(!getPluginInterface().getPluginconfig().getPluginBooleanParameter("debug",true)) return;
		logger.log(textToAdd);
		System.out.println(textToAdd);
	}

	/**
	 * Adds given String to the Azureus log as well as dumping it to the console
	 * It will log the Exception ass well.
	 *
	 * @param textToAdd
	 * @param e
	 */
	public static void addToLog(String textToAdd, Exception e){
		//don't spam console if debug is deaktivated
		if(!getPluginInterface().getPluginconfig().getPluginBooleanParameter("debug",true)) return;
		logger.log(textToAdd);
		logger.log(e.getMessage());
		System.out.println(textToAdd);
		System.out.println(e.getMessage());
	}


	/**
	 * Returns the localeUtilities as defined by the pluginInterface
	 * @return LocaleUtilities from pluginInterface
	 */
	public static LocaleUtilities getLocaleUtilities(){
		return locale_utils;
	}


	/**
	 * @return Returns the latestUpdate.
	 */
	public static UpdateCheckInstance getLatestUpdate() {
		return latestUpdate;
	}

//EOF
}
