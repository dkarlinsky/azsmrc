/*
 * Created on Nov 16, 2005
 */
package lbms.azsmrc.plugin.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbms.azsmrc.plugin.gui.View;
import lbms.azsmrc.plugin.pluginsupport.PSupportAzJabber;
import lbms.azsmrc.plugin.pluginsupport.PSupportStatusMailer;
import lbms.azsmrc.plugin.pluginsupport.PluginSupport;
import lbms.azsmrc.plugin.web.RequestManager;
import lbms.azsmrc.plugin.web.WebRequestHandler;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;
import lbms.tools.CryptoTools;
import lbms.tools.flexyconf.FlexyConfiguration;
import lbms.tools.flexyconf.Section;

import org.eclipse.swt.widgets.Display;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginConfigListener;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginListener;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadManager;
import org.gudy.azureus2.plugins.download.DownloadManagerListener;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;
import org.gudy.azureus2.plugins.tracker.Tracker;
import org.gudy.azureus2.plugins.tracker.TrackerException;
import org.gudy.azureus2.plugins.tracker.web.TrackerAuthenticationAdapter;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebContext;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.menus.MenuItem;
import org.gudy.azureus2.plugins.ui.menus.MenuItemFillListener;
import org.gudy.azureus2.plugins.ui.menus.MenuItemListener;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.ui.model.BasicPluginViewModel;
import org.gudy.azureus2.plugins.ui.tables.TableContextMenuItem;
import org.gudy.azureus2.plugins.ui.tables.TableManager;
import org.gudy.azureus2.plugins.ui.tables.TableRow;
import org.gudy.azureus2.plugins.update.Update;
import org.gudy.azureus2.plugins.update.UpdateCheckInstance;
import org.gudy.azureus2.plugins.update.UpdateCheckInstanceListener;
import org.gudy.azureus2.plugins.update.UpdateManager;
import org.gudy.azureus2.plugins.update.UpdateManagerListener;
import org.gudy.azureus2.plugins.utils.LocaleUtilities;
import org.gudy.azureus2.plugins.utils.UTTimerEvent;
import org.gudy.azureus2.plugins.utils.UTTimerEventPerformer;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;




public class Plugin implements org.gudy.azureus2.plugins.Plugin {

	private static LocaleUtilities locale_utils;

	PluginInterface pluginInterface;
	private static PluginInterface pi;
	private static Display display;

	//the XMLConfig file to be used
	private static XMLConfig config;

	//The logger from Azureus
	private static LoggerChannel logger;

	private static UpdateCheckInstance latestUpdate;

	private static Map<String, PluginSupport> pluginSupport = new HashMap<String, PluginSupport>();
	private static FlexyConfiguration psFlexyConfig;
	private static Map<String,Section> psSections = new HashMap<String, Section>();

	private static boolean firstRun;
	private static User currentUser = null;

	//new API startup code
	UISWTInstance swtInstance = null;
	UISWTViewEventListener myView = null;

	public void initialize(final PluginInterface pluginInterface) {

		this.pluginInterface = pluginInterface;
		locale_utils = pluginInterface.getUtilities().getLocaleUtilities();
		locale_utils.integrateLocalisedMessageBundle("lbms.azsmrc.plugin.internat.Messages");
		pi=pluginInterface;

		firstRun = generateUID();

		UIManager   ui_manager = pluginInterface.getUIManager();
		BasicPluginConfigModel config_model = ui_manager.createBasicPluginConfigModel( "plugins", "plugin.azsmrc");

		//settings on main options panel
		config_model.addBooleanParameter2("azsmrc_military_time","azsmrc.military.time",false);
		config_model.addBooleanParameter2("azsmrc_auto_open","azsmrc.auto.open",true);
		config_model.addBooleanParameter2("singleUserMode", "azsmrc.singleusermode", false);
		config_model.addBooleanParameter2("useUsernamesAsCategory", "azsmrc.usernameCategory", false);
		config_model.addBooleanParameter2("disableAutoImport", "azsmrc.disable.auto.import", false);
		config_model.addBooleanParameter2("use_ssl","azsmrc.use.ssl",false);
		config_model.addIntParameter2("remote_port", "azsmrc.remote.port", 49009);
		config_model.addLabelParameter2("azsmrc.portchange.alert");
		config_model.addLabelParameter2("azsmrc.statistics.label");
		config_model.addBooleanParameter2("statistics.allow", "azsmrc.statistics.allow", false);

		//Load the config file
		config = XMLConfig.loadConfigFile(pluginInterface.getPluginDirectoryName() + System.getProperty("file.separator") + "MultiUserConfig.xml");

		//initialize the logger
		logger = pluginInterface.getLogger().getTimeStampedChannel("AzSMRC");
		final BasicPluginViewModel	view_model =
			ui_manager.createBasicPluginViewModel( "AzSMRC Log" );
		view_model.getActivity().setVisible(false);
		view_model.getProgress().setVisible(false);
		view_model.getStatus().setVisible(false);

		logger.addListener(
				new LoggerChannelListener()
				{
					public void
					messageLogged(
						int		type,
						String	content )
					{
						view_model.getLogArea().appendText( content + "\n" );
					}

					public void
					messageLogged(
						String		str,
						Throwable	error )
					{
						if ( str.length() > 0 ){
							view_model.getLogArea().appendText( str + "\n" );
						}

						StringWriter sw = new StringWriter();

						PrintWriter	pw = new PrintWriter( sw );

						error.printStackTrace( pw );

						pw.flush();

						view_model.getLogArea().appendText( sw.toString() + "\n" );
					}
				});
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

								//ignore updates with no downloaders
								Update[] upd = instance.getUpdates();
								int c = 0;
								for (int i=0;i<upd.length;i++) {
									if (upd[i].getDownloaders().length == 0) c++;
								}
								if (c == upd.length) return;

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
		logStat();
		Timers.addPeriodicEvent(1000*60*60, new UTTimerEventPerformer() {
			public void perform(UTTimerEvent event) {
				logStat();
			}
		});

		Timers.addPeriodicEvent(1000*60, new UTTimerEventPerformer() {
			public void perform(UTTimerEvent event) {
				config.checkAndDeleteOldSessions();
			}
		});

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
		dm.addListener( new DownloadManagerListener()
				{
					public void downloadAdded(Download dl) {
						if (!dl.isComplete()) {	//add only if the download isn't already complete
							dl.addListener(MultiUserDownloadListener.getInstance()); //attach DownloadListener
							if (dl.getAttribute(MultiUser.TA_USER) == null)
								dl.setAttribute(MultiUser.TA_USER, MultiUser.PUBLIC_USER_NAME);
						}
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
				config_model.addLabelParameter2("azsmrc.ssl.impossible");
			}

			if (sslSupported && pluginInterface.getPluginconfig().getPluginBooleanParameter("use_ssl",false)) {
				config_model.addLabelParameter2("azsmrc.ssl.enabled");
				context =	pluginInterface.getTracker().createWebContext(
							pluginInterface.getAzureusName() + " - " + pluginInterface.getPluginName(),
							pluginInterface.getPluginconfig().getPluginIntParameter("remote_port",49009), Tracker.PR_HTTPS );
			} else {
				if (sslSupported) config_model.addLabelParameter2("azsmrc.ssl.possible");
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

		//Add Plugin Support
		pluginSupport.put(PSupportStatusMailer.IDENTIFIER, new PSupportStatusMailer());
		pluginSupport.put(PSupportAzJabber.IDENTIFIER, new PSupportAzJabber());

		pluginInterface.addListener(new PluginListener() {
			/* (non-Javadoc)
			 * @see org.gudy.azureus2.plugins.PluginListener#closedownComplete()
			 */
			public void closedownComplete() {
				// TODO Auto-generated method stub

			}
			/* (non-Javadoc)
			 * @see org.gudy.azureus2.plugins.PluginListener#closedownInitiated()
			 */
			public void closedownInitiated() {
				try {
					config.saveConfigFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			/* (non-Javadoc)
			 * @see org.gudy.azureus2.plugins.PluginListener#initializationComplete()
			 */
			public void initializationComplete() {
				addToLog("Initializing Plugin Support...");
				for (PluginSupport ps:pluginSupport.values()) {
					try {
						addToLog("Loading Plugin Support for: ["+ps.getName()+"]...");
						ps.initialize(pluginInterface);
						addToLog("Plugin Support for: ["+ps.getName()+"] is active: "+ps.isActive());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				addToLog("Finished loading Plugin Support.");
			}
		});

		TableContextMenuItem incompleteMenuItem = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, "azsmrc.tablemenu.useritem");
		TableContextMenuItem completeMenuItem = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE, "azsmrc.tablemenu.useritem");

		incompleteMenuItem.setStyle(MenuItem.STYLE_MENU);
		completeMenuItem.setStyle(MenuItem.STYLE_MENU);


		MenuItemFillListener fillListener = new MenuItemFillListener () {
			/* (non-Javadoc)
			 * @see org.gudy.azureus2.plugins.ui.menus.MenuItemFillListener#menuWillBeShown(org.gudy.azureus2.plugins.ui.menus.MenuItem, java.lang.Object)
			 */
			public void menuWillBeShown(MenuItem menu, Object data) {
				menu.removeAllChildItems();
				TableManager tm = pluginInterface.getUIManager().getTableManager();
				if (getCurrentUser() != null && getCurrentUser().checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					TableRow[] rows = (TableRow[])data;
					if (rows.length == 0) return;
					if (rows.length == 1) {


						Download dl = (Download)rows[0].getDataSource();
						User[] ownerList = config.getUsersOfDownload(dl);

						if (MultiUser.isPublicDownload(dl)) {
							TableContextMenuItem owner = tm.addContextMenuItem((TableContextMenuItem)menu, "");
							//displayState.setEnabled(false);
							owner.setText("PUBLIC");

							TableContextMenuItem separator = tm.addContextMenuItem((TableContextMenuItem)menu, "");
							separator.setStyle(MenuItem.STYLE_SEPARATOR);


						} else {

							if (MultiUser.isSharedDownload(dl)) {
								for (final User o:ownerList) {
									TableContextMenuItem owner = tm.addContextMenuItem((TableContextMenuItem)menu, "");
									owner.setText(o.getUsername());
									owner.addListener(new MenuItemListener() {
										/* (non-Javadoc)
										 * @see org.gudy.azureus2.plugins.ui.menus.MenuItemListener#selected(org.gudy.azureus2.plugins.ui.menus.MenuItem, java.lang.Object)
										 */
										public void selected(MenuItem menu,	Object target) {
											TableRow row = (TableRow) target;
											Download dl = (Download)row.getDataSource();
											MultiUser.removeUserFromDownload(o, dl);
											System.out.println("Removing User "+o.getUsername()+" from "+dl.getName());
										}
									});
								}

							} else {
								TableContextMenuItem owner = tm.addContextMenuItem((TableContextMenuItem)menu, "");
								//displayState.setEnabled(false);
								owner.setText(ownerList[0].getUsername());
								final User o = ownerList[0];
								owner.addListener(new MenuItemListener() {
									/* (non-Javadoc)
									 * @see org.gudy.azureus2.plugins.ui.menus.MenuItemListener#selected(org.gudy.azureus2.plugins.ui.menus.MenuItem, java.lang.Object)
									 */
									public void selected(MenuItem menu,	Object target) {
										TableRow row = (TableRow) target;
										Download dl = (Download)row.getDataSource();
										MultiUser.removeUserFromDownload(o, dl);
										System.out.println("Removing User "+o.getUsername()+" from "+dl.getName());
									}
								});
							}

							TableContextMenuItem separator = tm.addContextMenuItem((TableContextMenuItem)menu, "");
							separator.setStyle(MenuItem.STYLE_SEPARATOR);

							TableContextMenuItem removeAllUsers = tm.addContextMenuItem((TableContextMenuItem)menu, "azsmrc.tablemenu.removeallusers");
							removeAllUsers.addMultiListener(new MenuItemListener() {
								/* (non-Javadoc)
								 * @see org.gudy.azureus2.plugins.ui.menus.MenuItemListener#selected(org.gudy.azureus2.plugins.ui.menus.MenuItem, java.lang.Object)
								 */
								public void selected(MenuItem menu, Object target) {
									TableRow[] rows = (TableRow[])target;
									for (int i=0;i<rows.length;i++) {
										Download dl = (Download)rows[i].getDataSource();
										MultiUser.removeUsersFromDownload(dl);
										System.out.println("Removing Users from "+dl.getName());
									}
								}
							});
						}

						List<User> users = new ArrayList<User>(Arrays.asList(config.getUsers()));
						users.removeAll(Arrays.asList(ownerList));
						if (users.size()>10) {
							menu = tm.addContextMenuItem((TableContextMenuItem)menu, "azsmrc.tablemenu.adduser");
						}
						for (final User u:users) {
							TableContextMenuItem addToUser = tm.addContextMenuItem((TableContextMenuItem)menu, "");
							addToUser.setText("Add to: "+u.getUsername());
							addToUser.addMultiListener(new MenuItemListener() {
								public void selected(MenuItem menu, Object target) {
									TableRow[] rows = (TableRow[])target;
									for (int i=0;i<rows.length;i++) {
										Download dl = (Download)rows[i].getDataSource();
										MultiUser.addUserToDownload(u, dl);
										System.out.println("Adding "+dl.getName()+" to "+u.getUsername());
									}
								};
							});
						}

					} else {
						TableContextMenuItem removeAllUsers = tm.addContextMenuItem((TableContextMenuItem)menu, "azsmrc.tablemenu.removeallusers");
						removeAllUsers.addMultiListener(new MenuItemListener() {
							/* (non-Javadoc)
							 * @see org.gudy.azureus2.plugins.ui.menus.MenuItemListener#selected(org.gudy.azureus2.plugins.ui.menus.MenuItem, java.lang.Object)
							 */
							public void selected(MenuItem menu, Object target) {
								TableRow[] rows = (TableRow[])target;
								for (int i=0;i<rows.length;i++) {
									Download dl = (Download)rows[i].getDataSource();
									MultiUser.removeUsersFromDownload(dl);
									System.out.println("Removing Users from "+dl.getName());
								}
							}
						});

						User[] users = config.getUsers();
						for (final User u:users) {
							TableContextMenuItem addToUser = tm.addContextMenuItem((TableContextMenuItem)menu, "");
							addToUser.setText("Add to: "+u.getUsername());
							addToUser.addMultiListener(new MenuItemListener() {
								public void selected(MenuItem menu, Object target) {
									TableRow[] rows = (TableRow[])target;
									for (int i=0;i<rows.length;i++) {
										Download dl = (Download)rows[i].getDataSource();
										MultiUser.addUserToDownload(u, dl);
										System.out.println("Adding "+dl.getName()+" to "+u.getUsername());
									}
								};
							});
						}
					}
				} else {
					TableContextMenuItem displayState = tm.addContextMenuItem((TableContextMenuItem)menu, "");
					displayState.setEnabled(false);
					displayState.setText("You are not logged in or don't have Admin access.");
				}
			}
		};
		incompleteMenuItem.addFillListener(fillListener);
		completeMenuItem.addFillListener(fillListener);

	}

	/**
	 * Generates a Random UID
	 *
	 * @return true if UID was generated, false if UID already present
	 */
	private boolean generateUID() {
		if (!pi.getPluginconfig().getPluginStringParameter("azsmrc.uid").equals("")) return false;

		long n = System.currentTimeMillis();
		byte[] b = new byte[8];
		b[7] = (byte) (n);
		n >>>= 8;
		b[6] = (byte) (n);
		n >>>= 8;
		b[5] = (byte) (n);
		n >>>= 8;
		b[4] = (byte) (n);
		n >>>= 8;
		b[3] = (byte) (n);
		n >>>= 8;
		b[2] = (byte) (n);
		n >>>= 8;
		b[1] = (byte) (n);
		n >>>= 8;
		b[0] = (byte) (n);
		try {
			String uid = CryptoTools.formatByte(CryptoTools.messageDigest(b, "SHA-1"),true);
			pi.getPluginconfig().setPluginParameter("azsmrc.uid", uid);
			return true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void logStat() {
		if (pi.getPluginconfig().getPluginBooleanParameter("statistics.allow")) {
			long lastcheck = Long.parseLong(pi.getPluginconfig().getPluginStringParameter("stats.lastcheck","0"));
			if (System.currentTimeMillis()-lastcheck > 1000*60*60*24) {
				Thread t = new Thread() {
					public void run() {
						try {
							URL url = new URL (RemoteConstants.INFO_URL+"?app=AzSMRC_server&version="+pi.getPluginVersion()+"&uid="+pi.getPluginconfig().getPluginStringParameter("azsmrc.uid"));

							System.out.println(url.toExternalForm());
							HttpURLConnection conn = (HttpURLConnection)url.openConnection();
							conn.connect();
							conn.getResponseCode();
							conn.disconnect();
							pi.getPluginconfig().setPluginParameter("stats.lastcheck",Long.toString(System.currentTimeMillis()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t.setDaemon(true);
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			}
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
		System.out.println("AzSMRC: "+textToAdd);
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
		logger.log(textToAdd,e);
		System.out.println("AzSMRC: "+textToAdd);
		System.out.println(e.getMessage());
	}

	public static LoggerChannel getLoggerChannel() {
		return logger;
	}

	/**
	 * @return the currentUser
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * @param currentUser the currentUser to set
	 */
	public static void setCurrentUser(User currentUser) {
		Plugin.currentUser = currentUser;
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

	public static PluginSupport getPluginSupport (String key) {
		return pluginSupport.get(key);
	}

	public static Section addPSConfigSection (String label) {
		label = "azsmrc.pluginsupport."+label;
		if (psSections.containsKey(label))
			return psSections.get(label);
		else {
			Section s = new Section(label,getPSFlexyConf().getRootSection());
			psSections.put(label, s);
			return s;
		}
	}

	public static FlexyConfiguration getPSFlexyConf() {
		if (psFlexyConfig == null) psFlexyConfig = new FlexyConfiguration();
		return psFlexyConfig;
	}

	/**
	 * @return the firstRun
	 */
	public static boolean isFirstRun() {
		if (firstRun) {
			firstRun = false;
			return true;
		}
		return firstRun;
	}

	/**
	 * Returns an Array of users.
	 *
	 * This Method is intended to be used via IPC.
	 *
	 * @return array of User
	 */
	public String[] ipcGetUsers () {
		return config.getUserList();
	}

	/**
	 * Adds a Download to a User if he exists.
	 *
	 * This Method is intended to be used via IPC.
	 *
	 * @param uName username to add Download to
	 * @param dl Download to add
	 * @return true on success, false on fail
	 */
	public boolean ipcAddDownloadToUser (String uName, Download dl) {
		try {
			User u = config.getUser(uName);
			u.addDownload(dl);
			MultiUser.addUserToDownload(u, dl);
			return true;
		} catch (UserNotFoundException e) {
			return false;
		}
	}

//EOF
}
