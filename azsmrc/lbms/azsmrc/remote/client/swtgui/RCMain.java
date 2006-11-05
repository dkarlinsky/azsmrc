package lbms.azsmrc.remote.client.swtgui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.LoginData;
import lbms.azsmrc.remote.client.SESecurityManager;
import lbms.azsmrc.remote.client.SESecurityManagerListener;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientEventListener;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.events.ConnectionListener;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;
import lbms.azsmrc.remote.client.events.ExceptionListener;
import lbms.azsmrc.remote.client.events.GlobalStatsListener;
import lbms.azsmrc.remote.client.events.HTTPErrorListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.pluginsimpl.PluginLoader;
import lbms.azsmrc.remote.client.pluginsimpl.PluginManagerImpl;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.remote.client.swtgui.dialogs.ErrorDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.InputShell;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageSlideShell;
import lbms.azsmrc.remote.client.swtgui.dialogs.MotdDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByFileDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByURLDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.ServerUpdateDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateProgressDialog;
import lbms.azsmrc.remote.client.swtgui.sound.Sound;
import lbms.azsmrc.remote.client.swtgui.sound.SoundException;
import lbms.azsmrc.remote.client.swtgui.sound.SoundManager;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.Timer;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.remote.client.util.TimerEventPeriodic;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.CryptoTools;
import lbms.tools.ExtendedProperties;
import lbms.tools.launcher.Launchable;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.jdom.Element;

public class RCMain implements Launchable {

	public static final String LOGGER_ROOT = "lbms.azsmrc";
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String FSEP = System.getProperty("file.separator");

	protected Shell shell;
	protected Display display;
	private static RCMain rcMain;
	private Client client;
	private ExtendedProperties properties;
	private Properties  azsmrcProperties;
	private File confFile;
	private Timer timer;
	private TimerEventPeriodic updateTimer;
	private boolean connect;
	private Logger logger;
	private Updater updater;
	private Proxy proxy;
	private PluginManagerImpl pluginManager;

	private TrayItem systrayItem;
	private DownloadManagerShell mainWindow;
	private long runTime;
	private boolean manifestInUse;
	private StartServer startServer;


	//  I18N prefix
	public static final String PFX = "rcmain.";

	private Transferable emptyTransfer = new Transferable() {
		private DataFlavor[] emptyArray = new DataFlavor[0];
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			throw new UnsupportedFlavorException(flavor);
		}

		public DataFlavor[] getTransferDataFlavors() {
			return emptyArray;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return false;
		}
	};

	protected boolean terminated, failedConnection;

	public static void main(String[] args) {
		start(args);
	}

	public boolean launch(String[] args) {
		start(args);
		return false;
	}

	public static void start(String[] args) {
		try {
			if (rcMain == null) { //if it is launched by launchable then rcMain should already exist
				new RCMain();
			}
			rcMain.initConfig();
			if (!rcMain.checkInstance(args)) return;
			rcMain.init();
			rcMain.open();
		} catch (Throwable e) {
			e.printStackTrace();
			File error = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+"error.log");
			PrintStream fout = null;
			try {
				fout = new PrintStream(error);
				e.printStackTrace(fout);
				Throwable cause = e.getCause();
				while (cause != null) {
					cause.printStackTrace(fout);
					cause = cause.getCause();
				}
			} catch (FileNotFoundException e1) {
			} finally {
				if (fout != null)fout.close();
			}
		}
	}

	public boolean checkInstance (String[] args) {
		boolean closedown = false;

		startServer = new StartServer();

		if  (args.length > 0 && args[0].equalsIgnoreCase( "--closedown" )){
			closedown	= true;
		}

		boolean another_instance = startServer.getState() != StartServer.STATE_LISTENING;

		if( another_instance && !properties.getPropertyAsBoolean("multiInstance") ) {
			StartSocket ss = new StartSocket(args);
			closedown = true;
			if( !ss.sendArgs() ) {  //arg passing attempt failed, so start core anyway
				closedown = false;
				String msg = "There appears to be another program process already listening on socket [127.0.0.1: 6880].\nLoading of torrents via command line parameter will fail until this is fixed.";
				System.out.println( msg );
			}
		} else {
			String[] args2 = new String[args.length+1];
			args2[0] = "args";
			System.arraycopy(args, 0, args2, 1, args.length);
			startServer.processArgs(args2);
		}

		if (closedown) return false;

		//let the server wait for connections
		startServer.pollForConnections();
		return true;
	}

	public void open() {
		DownloadContainer.loadColumns();
		SeedContainer.loadColumns();
		terminated = false;

		display.asyncExec(new SWTSafeRunnable() {
			public void runSafe() {
				createContents();
			}
		});

		connect = properties.getPropertyAsBoolean("auto_connect");
		if (connect) {
			try {
				LoginData login = new LoginData(properties.getProperty("lastConnection",""));
				if (login.isComplete()) {
					connect(false);
					updateTimer(properties.getPropertyAsBoolean("auto_open"));
					client.getDownloadManager().update(true);
				} else {
					connect = false;
				}
			} catch (MalformedURLException e) {
				connect = false;
			}
		}
		File error = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+"error.log");
		if (error.exists()) {
			System.out.println("Crash Detected");
			ErrorDialog.createAndOpen();
		}
		System.out.println("setCoreStarted");
		startServer.setCoreStarted(true);
		System.out.println("openQueuedTorrents");
		startServer.openQueuedTorrents();
		while (!terminated) { //runnig
			if (!display.readAndDispatch ()) display.sleep ();
		}
		if (mainWindow != null) { //cleanup
			mainWindow.close_shell();
		}
		shutdown();
		display.dispose();
	}

	protected void createContents() {
		shell = new Shell();
		SplashScreen.setProgressAndText("Loading Images", 80);
		ImageRepository.loadImages(display);
		SplashScreen.setProgressAndText("Creating GUI", 90);

/*		//Show Splash
		if(properties.getPropertyAsBoolean("show_splash",true)){
			SplashScreen.open(getDisplay(), 20);
		}*/

		final Tray systray = display.getSystemTray ();
		if (systray != null) {
			systrayItem = new TrayItem (systray, SWT.NONE);

			//Listener for the system tray

			systrayItem.addSelectionListener(new SelectionListener()
			{
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// this is a double click .. if we ever want to do something with this
					// do it here

				}

				public void widgetSelected(SelectionEvent arg0) {
					//normal selection is a single click
					try{
						Shell dms_shell = getMainWindow().getShell();
						if(dms_shell.getMinimized()){
							dms_shell.setMinimized(false);
							dms_shell.setVisible(true);

						} else{
							dms_shell.setMinimized(true);
							dms_shell.setVisible(false);
						}
					}catch(Exception e1){
						if (mainWindow == null) {
							mainWindow = new DownloadManagerShell();
						}
						mainWindow.open();
						return;
					}

				}
			});
		} else {
			properties.setProperty("tray.minimize", false);
			properties.setProperty("tray.exit", false);
		}

		setTrayIcon(0);
		//Open mainWindow if AutoOpen is true

		if(properties.getPropertyAsBoolean("auto_open")){
			if (mainWindow == null) {
				mainWindow = new DownloadManagerShell();
			}
			mainWindow.open();
		}


		final Menu menu = new Menu(shell, SWT.POP_UP);

		final MenuItem openMainWindowMenuItem = new MenuItem(menu, SWT.PUSH);
		openMainWindowMenuItem.setText(I18N.translate(PFX + "traymenu.openMainWindow"));

		final MenuItem addMenuItem = new MenuItem(menu, SWT.CASCADE);
		addMenuItem.setText(I18N.translate(PFX + "traymenu.add"));

		final Menu addMenu = new Menu(addMenuItem);
		addMenuItem.setMenu(addMenu);

		final MenuItem addByFile = new MenuItem(addMenu,SWT.PUSH);
		addByFile.setText(I18N.translate(PFX + "traymenu.add.byFile"));

		final MenuItem addByUrl = new MenuItem(addMenu,SWT.PUSH);
		addByUrl.setText(I18N.translate(PFX + "traymenu.add.byURL"));

		final MenuItem quickMenuItem = new MenuItem(menu, SWT.CASCADE);
		quickMenuItem.setText(I18N.translate(PFX + "traymenu.quickmenu"));

		final Menu quickMenu = new Menu(quickMenuItem);
		quickMenuItem.setMenu(quickMenu);

		final MenuItem pauseDownloads_1min = new MenuItem(quickMenu,SWT.PUSH);
		pauseDownloads_1min.setText(I18N.translate(PFX + "traymenu.quickmenu.pause1min"));

		final MenuItem pauseDownloads_5min = new MenuItem(quickMenu,SWT.PUSH);
		pauseDownloads_5min.setText(I18N.translate(PFX + "traymenu.quickmenu.pause5min"));

		final MenuItem pauseDownloads_UserSpecified = new MenuItem(quickMenu, SWT.PUSH);
		pauseDownloads_UserSpecified.setText(I18N.translate(PFX + "traymenu.quickmenu.pauseUserSpecified"));

		new MenuItem (menu, SWT.SEPARATOR);

		final MenuItem connectDisconnectMenuItem = new MenuItem(menu, SWT.PUSH);
		if (!connect) connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.connect"));
		else connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.disconnect"));

		final MenuItem silentItem = new MenuItem(menu, SWT.CHECK);
		silentItem.setText(I18N.translate(PFX + "traymenu.silentMode"));

		final MenuItem disablePopupItem = new MenuItem(menu, SWT.CHECK);
		disablePopupItem.setText(I18N.translate(PFX + "traymenu.disablePopups"));

		final MenuItem closeMenuItem = new MenuItem(menu, SWT.PUSH);
		closeMenuItem.setText(I18N.translate(PFX + "traymenu.close"));

		//###########################################################

		openMainWindowMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mainWindow == null) {
					mainWindow = new DownloadManagerShell();
					mainWindow.open();
				}
			}
		});

		closeMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				terminated = true;
			}
		});

		connectDisconnectMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (connect) {
					connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.connect"));
					disconnect();
					setTrayIcon(0);
					if(mainWindow != null){
						mainWindow.setConnectionStatusBar(0);
						mainWindow.setLogInOutButtons(false);
						mainWindow.clearMapsAndChildred();
					}
				} else {
					connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.disconnect"));
					client.sendGetGlobalStats();
					if (mainWindow != null) {
						connect(true);
						mainWindow.setConnectionStatusBar(2);
					} else
						connect(false);
				}
			}
		});

		//----------listener for the menu before it opens------\\
		menu.addMenuListener(new MenuListener(){

			public void menuHidden(MenuEvent arg0) {
			}

			public void menuShown(MenuEvent arg0) {
				if (!connect){
					connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.connect"));
					addMenuItem.setEnabled(false);
					quickMenuItem.setEnabled(false);
				}else{
					connectDisconnectMenuItem.setText(I18N.translate(PFX + "traymenu.disconnect"));
					addMenuItem.setEnabled(true);
					quickMenuItem.setEnabled(true);
				}
				silentItem.setSelection(SoundManager.isSilent());
				disablePopupItem.setSelection(!properties.getPropertyAsBoolean("popups_enabled"));
			}
		});

		addByUrl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OpenByURLDialog.open();
			}
		});

		addByFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OpenByFileDialog.open();
			}
		});

		pauseDownloads_1min.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				client.getDownloadManager().pauseDownloads(60);
			}
		});

		pauseDownloads_5min.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				client.getDownloadManager().pauseDownloads(300);
			}
		});

		pauseDownloads_UserSpecified.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try{
					InputShell is = new InputShell(I18N.translate(PFX + "traymenu.quickmenu.pauseUserSpecified.inputShell.title"),
							I18N.translate(PFX + "traymenu.quickmenu.pauseUserSpecified.inputShell.message"));
					String str_minutes = is.open();
					int mins = Integer.parseInt(str_minutes);
					client.getDownloadManager().pauseDownloads(mins*60);
					logger.info("Pausing all downloads for " + mins*60  + " seconds (" + mins + " minutes)");
				}catch(Exception ex) {}


			}
		});

		silentItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SoundManager.setSilentMode(silentItem.getSelection());
			}
		});

		disablePopupItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				properties.setProperty("popups_enabled", silentItem.getSelection());
			}
		});
		if (systrayItem != null) {
			systrayItem.addListener (SWT.MenuDetect, new Listener () {
				public void handleEvent (Event event) {
					menu.setVisible (true);
				}
			});
		}

		//Add in the clipboard monitor
		addAWTClipboardMonitor();
		SplashScreen.setProgressAndText("All Done", 100);
		checkUpadteAndMotd();
	}

	private void initConfig() {
		confFile = new File(USER_DIR+FSEP+"config.cfg");
		properties = null;
		{
			Properties defaultProps = new Properties();
			InputStream is = null;
			try {
				is = RCMain.class.getClassLoader().getResourceAsStream("default.cfg");
				defaultProps.loadFromXML(is);
				is.close();
				properties = new ExtendedProperties(defaultProps); //read in default values
			} catch (IOException e1) {
				properties = new ExtendedProperties(); //if something happens create empty properties
				e1.printStackTrace();
			} finally {
				if (is!=null) try { is.close(); } catch (IOException e) {}
			}
		}

		System.out.println("Loading Properties.");
		if (confFile.exists() && confFile.canRead()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(confFile);
				properties.loadFromXML(fin);
			} catch (IOException e) {
				e.printStackTrace();
			}  finally {
				if (fin!=null) try { fin.close(); } catch (IOException e) {}
			}
		}
		azsmrcProperties = new Properties();
		{
			InputStream is = null;
			try {
				is = RCMain.class.getClassLoader().getResourceAsStream("azsmrc.properties");
				azsmrcProperties.load(is);
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				if (is!=null) try { is.close(); } catch (IOException e) {}
			}
		}
		generateUID();
	}

	private void init () {
		display = Display.getDefault();
		runTime = System.currentTimeMillis();
		System.out.println("Starting up RCMain.");
		System.out.println("Checking javaw.exe.manifest");
		javawExeManifest();

		if(properties.getPropertyAsBoolean("show_splash")){
			SplashScreen.open(display, 20);
		}
		SplashScreen.setProgressAndText("Creating Logger.",10);
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
		logger = Logger.getLogger(LOGGER_ROOT);

		SplashScreen.setProgressAndText("Loading I18N.",15);
		try {
			I18N.setDefault("lbms/azsmrc/remote/client/internat/default.lang");
			if (properties.getProperty("language") != null) {
				I18N.setLocalized("lbms/azsmrc/remote/client/internat/"+properties.getProperty("language")+".lang");
			}
			I18N.reload();
			if (I18N.isInitialized()) {
				System.out.println("I18N initialized.");
			}
			if (I18N.isLocalized()) {
				System.out.println("I18N localized.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		SplashScreen.setProgressAndText("Loading Plugins.",20);
		pluginManager = new PluginManagerImpl();

		PluginLoader.findAndLoadPlugins(pluginManager, getProperties());

		SESecurityManager.getSingleton().addSecurityListner(new SESecurityManagerListener() {
			public boolean trustCertificate(String ressource, X509Certificate x509_cert) {
				// TODO Change it
				logger.debug("Trusting Certificate: "+ressource);
				return true;
			}
		});
		SplashScreen.setProgressAndText("Creating Client.",25);
		try {
			LoginData login = new LoginData(properties.getProperty("lastConnection",""));
			client = new Client(login);
		} catch (MalformedURLException e1) {
			client = new Client();
		}
		if (properties.getPropertyAsBoolean("connection.proxy.use")) {
			Proxy.Type type = Proxy.Type.valueOf(properties.getProperty("connection.proxy.type"));
			InetSocketAddress inetAddress = new InetSocketAddress(properties.getProperty("connection.proxy.url"),properties.getPropertyAsInt("connection.proxy.port"));
			proxy = new Proxy(type,inetAddress);
			if (properties.getProperty("connection.proxy.username") != null && properties.getProperty("connection.proxy.password") != null) {
				Authenticator.setDefault(new Authenticator() {
					PasswordAuthentication pw = new PasswordAuthentication(properties.getProperty("connection.proxy.username"),properties.getProperty("connection.proxy.password").toCharArray());

					protected PasswordAuthentication getPasswordAuthentication() {
						return pw;
					}
				});
			}
			client.setProxy(proxy);
		}
		client.setFastMode(properties.getPropertyAsBoolean("client.fastmode"));
		client.addGlobalStatsListener(new GlobalStatsListener() {
			public void updateStats(final int d, final int u, final int seeding, final int seedqueue, final int downloading, final int downloadqueue) {
				if(display != null)
					display.syncExec(new SWTSafeRunnable() {
						public void runSafe() {
							//setTrayToolTip("AzSMRC: D:"+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(d)+" - U:"+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(u));

							setTrayToolTip(seeding + " ("+ seedqueue+") "
									+ I18N.translate(PFX + "tray.tooltip.part1") + " "
									+ downloading + " ("+ downloadqueue+") "
									+ I18N.translate(PFX + "tray.tooltip.part2") + "\n"
									+ I18N.translate(PFX + "tray.tooltip.part3")
									+ " "+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(d)
									+ I18N.translate(PFX + "tray.tooltip.part4")
									+ " "+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(u));
						}
					});
			}
		});

		client.addHTTPErrorListener(new HTTPErrorListener() {
			public void httpError(int statusCode) {
				if (statusCode == UNAUTHORIZED) {
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.failed") + " " + statusCode
								+ I18N.translate(PFX + "mainwindow.statusbar.badUsername_badPassword"), SWT.COLOR_RED);
					}
					logger.warn("Connection failed: " + statusCode + " Bad Username or Password");
					new MessageSlideShell(display,SWT.ICON_ERROR,I18N.translate(PFX + "mainwindow.statusbar.failed"),statusCode + " " + I18N.translate(PFX + "mainwindow.statusbar.badUsername_badPassword"),(String)null);
					/*MessageDialog.error(display,I18N.translate(PFX + "mainwindow.statusbar.failed")
							,statusCode + " " + I18N.translate(PFX + "mainwindow.statusbar.badUsername_badPassword"));*/
					disconnect();
				} else {
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.failed") + " "+statusCode, SWT.COLOR_RED);
					}
					logger.warn("Connection failed: "+statusCode);
				}
			}
		});
		client.addConnectionListener(new ConnectionListener() {
			public void connectionState(final int state) {
				logger.debug("Connection State: "+state);
				if(display != null)
					display.syncExec(new SWTSafeRunnable() {
						public void runSafe() {
							setTrayIcon(state);
						}
					});
				if (state == ST_CONNECTION_ERROR) {
					int delay = client.getFailedConnections()*30000;
					delay = (delay>120000)?120000:delay;
					logger.warn("Connection failed "+client.getFailedConnections()+" times, delay: "+(delay/1000)+"sec");
					if (mainWindow != null)
						updateTimer(true,delay);
					else
						updateTimer(false,delay);
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.failed") + " "
								+ client.getFailedConnections()
								+ " " + I18N.translate(PFX + "mainwindow.statusbar.numTimes"), SWT.COLOR_RED);
					}
					failedConnection = true;
					 if(client.getFailedConnections() > 0 && client.getFailedConnections()%3 == 0){
						 new MessageSlideShell(RCMain.getRCMain().getDisplay(),SWT.ICON_ERROR,
								 I18N.translate(PFX + "mainwindow.statusbar.connectionError")
								 ,I18N.translate(PFX + "mainwindow.statusbar.connectionError.failed")
									+ " " + RCMain.getRCMain().getClient().getFailedConnections() + " "
									+ I18N.translate(PFX + "mainwindow.statusbar.connectionError.failed_part2"),
									(String)null);

						 /*MessageDialog.error(RCMain.getRCMain().getDisplay(),
									I18N.translate(PFX + "mainwindow.statusbar.connectionError")
									, I18N.translate(PFX + "mainwindow.statusbar.connectionError.failed")
									+ " " + RCMain.getRCMain().getClient().getFailedConnections() + " "
									+ I18N.translate(PFX + "mainwindow.statusbar.connectionError.failed_part2"));*/
							return;
						}
				} else if (failedConnection && (state == ST_CONNECTED)) {
					if (mainWindow != null) {
						updateTimer(true,0);
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.connectionSuccessful")
								, SWT.COLOR_DARK_GREEN);
					} else
						updateTimer(false,0);
				}

			}
		});
		client.getDownloadManager().addListener(new DownloadManagerListener() {
			public void downloadAdded(Download download) {
				 logger.info("Download Added: "+download.getName());

			}
			public void downloadRemoved(Download download) {
				logger.info("Download Removed: "+download.getName());
			};
		});
		client.addClientEventListener(new ClientEventListener() {
			public void handleEvent(int type, long time, Element event) {
				String msg = event.getAttributeValue("message");
				switch (type) {
				case RemoteConstants.EV_DL_FINISHED:
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished")
								+ " " + event.getAttributeValue("name"), SWT.COLOR_DARK_GREEN);
					}
					SoundManager.playSound(Sound.DOWNLOADING_FINISHED);

					if (event.getAttributeValue("duration") != null) {
						long duration = Long.parseLong(event.getAttributeValue("duration"));
						String avgDl = (event.getAttributeValue("avgDownload") != null)?event.getAttributeValue("avgDownload") : "";
						logger.info(I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished")+" "+event.getAttributeValue("name")
											+" "+I18N.translate(PFX  + "mainwindow.statusbar.downloadFinishedIn")+DisplayFormatters.formatTime(duration*1000)+" "+avgDl);
						new MessageSlideShell(
								display,
								SWT.ICON_INFORMATION,
								I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished"),
								event.getAttributeValue("name")+"\n"+I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished")+DisplayFormatters.formatTime(duration*1000)+" "+avgDl,
								(String)null);
						/*MessageDialog.message(display,I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished"),
								event.getAttributeValue("name")+"\n"+I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished")+DisplayFormatters.formatTime(duration*1000)+" "+avgDl);*/
					}
					else {
						logger.info(I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished")+" "+event.getAttributeValue("name"));
						MessageDialog.message(display,I18N.translate(PFX  + "mainwindow.statusbar.downloadFinished"),event.getAttributeValue("name"));
					}
					break;

				case RemoteConstants.EV_DL_EXCEPTION:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Download Exception: "+msg, SWT.COLOR_RED);
					}
					logger.error("Download Exception: "+msg);
					break;

				case RemoteConstants.EV_EXCEPTION:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Remote Exception: "+msg, SWT.COLOR_RED);
					}
					logger.error(I18N.translate(PFX + "mainwindow.statusbar.remoteException") + " " + msg);
					break;
				case RemoteConstants.EV_UPDATE_AVAILABLE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.remoteUpdateAvailable"), SWT.COLOR_DARK_GREEN);
					}
					logger.info("Remote Update Available");
					client.getRemoteUpdateManager().load();
					break;
				case RemoteConstants.EV_MESSAGE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.serverMessage") + ": " + msg);
					}
					new MessageSlideShell(
							display,
							SWT.ICON_INFORMATION,
							I18N.translate(PFX + "mainwindow.statusbar.serverMessage"),
							msg,
							(String)null);
					/*MessageDialog.message(getDisplay(), I18N.translate(PFX + "mainwindow.statusbar.serverMessage"), msg);*/
					logger.debug("Server Message: "+msg);
					break;
				case RemoteConstants.EV_ERROR_MESSAGE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.serverErrorMessage") + ": " + msg, SWT.COLOR_RED);
					}
					logger.error("Server ErrorMessage: "+msg);
					new MessageSlideShell(
							display,
							SWT.ICON_ERROR,
							I18N.translate(PFX + "mainwindow.statusbar.serverErrorMessage"),
							msg,
							(String)null);
					//MessageDialog.error(getDisplay(), I18N.translate(PFX + "mainwindow.statusbar.serverErrorMessage"), msg);
					break;
				}
			}
		});
		client.addExceptionListener(new ExceptionListener() {
			public void exceptionOccured(Exception e, boolean serious) {
				if (e instanceof SSLHandshakeException) {
					new MessageSlideShell(
							display,
							SWT.ICON_ERROR,
							"Connection Error",
							"Server doesn't support SSL.",
							(String)null);
					//MessageDialog.message(display,true,5000,"Connection Error","Server doesn't support SSL.");
					disconnect();
					logger.error("Connection Error: Server doesn't support SSL.");
				}
			}
		});
		client.addClientUpdateListener(new ClientUpdateListener() {
			public void update(long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_UPDATE_INFO) != 0) {
					if (client.getRemoteUpdateManager().updatesAvailable()) {
						ServerUpdateDialog.open();
					}
				}
			}
		});


		SplashScreen.setProgressAndText("Creating Updater.",40);
		try {
			updater = new Updater(new URL(RemoteConstants.UPDATE_URL),new File("AzSMRCupdate.xml.gz"),new File(USER_DIR));
			updater.setProxy(getProxy());
		} catch (MalformedURLException e2) {
		}
		updater.addListener(new UpdateListener() {
			public void exception(Exception e) {
				System.out.println("Update Exception: "+e.getLocalizedMessage());
				System.out.println("Update Exception: "+e.getMessage());
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.updateException") + ": "+e.getLocalizedMessage(),SWT.COLOR_RED);
				}
				logger.error("Update Exception: "+e.getLocalizedMessage());

			}
			public void noUpdate() {
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.noUpdateAvailable"));
				}
				logger.info("No Update Available");
			}
			public void updateAvailable(final Update update) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.updateAvailable")
							+ " " + update.getVersion());
				}
				logger.info("Update Available: Version "+update.getVersion());
				if (properties.getPropertyAsBoolean("update.autoupdate")) {
					updater.doUpdate();
				}else{
					display.asyncExec(new SWTSafeRunnable() {
						public void runSafe() {
							UpdateDialog.open(display,update,updater);
						}
					});

				}
			}
			public void updateFailed(String reason) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.updateFailed")
							+ ": " + reason,SWT.COLOR_RED);
				}
				logger.info("Update Failed: "+reason);
			}
			public void updateFinished() {
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.updateFinished"));
				}
				logger.info("Update Finished");
			}

			public void updateError(String error) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.updateError")
							+ ": " + error,SWT.COLOR_RED);
				}
				logger.info("Update Error: "+error);
			}
			public void initializeUpdate(lbms.tools.Download[] dls) {
				UpdateProgressDialog.initialize(dls);
			}
		});

		SplashScreen.setProgressAndText("Loading Sounds.",50);
		loadSounds();
		SplashScreen.setProgressAndText("Creating Timer.",55);
		timer = new Timer("Main Timer",5);
		SplashScreen.setProgressAndText("Initializing Plugins.",60);
		pluginManager.initialize(this);
		SplashScreen.setProgressAndText("Finished Startup.",70);
	}

	public RCMain () {
		rcMain = this;
	}

	private void shutdown() {
		System.out.println("Shutting down");
		timer.destroy();
		DownloadContainer.saveColumns();
		SeedContainer.saveColumns();
		getRunTime();
		pluginManager.shutdown();
		saveConfig();
		ImageRepository.unLoadImages();
		System.out.println("Shutdown completed");
	}

	public void checkUpadteAndMotd () {
		Thread t = new Thread () {
			public void run() {
				long lastcheck = properties.getPropertyAsLong("update.lastcheck");
				if (System.currentTimeMillis()-lastcheck > 1000*60*60*24)
					if (properties.getPropertyAsBoolean("update.autocheck") || !properties.getPropertyAsBoolean("motd.disable")) {
						if (mainWindow != null) {
							mainWindow.setStatusBarText(I18N.translate(PFX + "mainwindow.statusbar.checking"));
						}
						URL url = null;
						try {
							if (properties.getPropertyAsBoolean("statistics.allow")) {
								url = new URL (RemoteConstants.INFO_URL+"?app=AzSMRC_client&version="+azsmrcProperties.getProperty("version","0")+"&uid="+properties.getProperty("azsmrc.uid"));
							} else	{
								url = new URL (RemoteConstants.INFO_URL);
							}
							HttpURLConnection conn = (HttpURLConnection)url.openConnection();
							conn.connect();
							if (conn.getResponseCode() == 200) { //won't work unless something is requested
								BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
								String info = br.readLine();
								if (properties.getPropertyAsBoolean("update.autocheck") && !info.equalsIgnoreCase(properties.getProperty("update.Last-Modified", ""))) {
									properties.setProperty("update.Last-Modified", info);
									updater.checkForUpdates(properties.getPropertyAsBoolean("update.beta"));
								}
								info = br.readLine();
								if (!properties.getPropertyAsBoolean("motd.disable") && !info.equalsIgnoreCase(properties.getProperty("motd.Last-Modified", ""))) {
									properties.setProperty("motd.Last-Modified", info);
									MotdDialog.open();
								}
								br.close();
							}
							conn.disconnect();
							properties.setProperty("update.lastcheck", System.currentTimeMillis());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			}
		};
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public long getRunTime() {
		long now = System.currentTimeMillis();
		long rTime = (now-runTime)
			+properties.getPropertyAsLong("runTime");
		runTime = now;
		properties.setProperty("runTime", rTime);
		return rTime;
	}

	public void close() {
		terminated = true;
	}

	public void setTrayToolTip (String toolTip) {
		if (systrayItem != null) systrayItem.setToolTipText(toolTip);
	}

	public boolean connected () {
		return connect;
	}

	public void connect(boolean open) {
		logger.debug("Connect!");
		connect = true;
		client.connect();
		/*client.sendGetPluginsFlexyConf();*/
		client.getDownloadManager().update(true);
		updateTimer(open);
	}

	public void disconnect() {
		logger.debug("Disconnect!");
		connect = false;
		client.disconnect();
		stopUpdateTimer();
	}

	public void stopUpdateTimer () {
		if (updateTimer != null) updateTimer.cancel();
	}

	public void updateTimer(boolean open) {
		if (connect)
			updateTimer(open,0);
	}

	public void updateTimer(boolean open,final int delay) {
		if (!connect) return;

		if (updateTimer != null) updateTimer.cancel();
		logger.debug("Changing Timer: "+(open?"GUI mode":"Tray mode"));
		if (open) {
			updateTimer = timer.addPeriodicEvent(properties.getPropertyAsLong("connection_interval_open")+delay,
				new TimerEventPerformer() {
				public void perform(TimerEvent event) {
					logger.debug("Timer: GUI mode");
					client.getDownloadManager().update(false);
				}
			});
		}
		else {
			updateTimer = timer.addPeriodicEvent(properties.getPropertyAsLong("connection_interval_closed")+delay,
				new TimerEventPerformer() {
				public void perform(TimerEvent event) {
					logger.debug("Timer: Tray mode");
					client.sendGetGlobalStats();
				}
			});
		}
	}

	public static RCMain getRCMain() {
		return rcMain;
	}

	public Client getClient() {
		return client;
	}

	public Display getDisplay(){
		return display;
	}

	public DownloadManagerShell getMainWindow(){
		return mainWindow;
	}

	public void closeMainWindow(){
		if (mainWindow != null) {
			mainWindow.close_shell();
			mainWindow = null;
		}
	}

	public void openMainWindow(){
		try{
			Shell dms_shell = getMainWindow().getShell();
			if(dms_shell.getMinimized()){
				dms_shell.setMinimized(false);
				dms_shell.setVisible(true);

			} else{
				dms_shell.setMinimized(true);
				dms_shell.setVisible(false);
			}
		}catch(Exception e1){
			if (mainWindow == null) {
				mainWindow = new DownloadManagerShell();
			}
			mainWindow.open();
			return;
		}

		/*mainWindow = new DownloadManagerShell();
			mainWindow.open();*/
	}

	public Timer getMainTimer () {
		return timer;
	}

	public ExtendedProperties getProperties() {
		return properties;
	}


	/**
	 * @return Returns the azsmrcProperties.
	 */
	public Properties getAzsmrcProperties() {
		return azsmrcProperties;
	}


	/**
	 * @return Returns the updater.
	 */
	public Updater getUpdater() {
		return updater;
	}

	/**
	 * Sets the tray Icon
	 *
	 * @param connection -- int -- @see ConnectionListener.ST_*
	 */
	public void setTrayIcon (int connection) {
		if(systrayItem == null || systrayItem.isDisposed()) return;
		switch (connection) {
		case ConnectionListener.ST_CONNECTION_ERROR:
			systrayItem.setToolTipText(I18N.translate(PFX + "tray.tooltip.connectionError"));
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Red"));
		break;

		case ConnectionListener.ST_DISCONNECTED:
			systrayItem.setToolTipText(I18N.translate(PFX + "tray.tooltip.notConnected"));
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Disconnected"));
		break;

		case ConnectionListener.ST_CONNECTING:
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Connecting"));
		break;

		case ConnectionListener.ST_CONNECTED:
			//systrayItem.setToolTipText("AzMultiUser RC");  //Let the listener do this!
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Blue"));
		}
	}

	public void javawExeManifest() {
		if (!(RemoteConstants.isWindowsXP || RemoteConstants.isWindows2003) ) {
			manifestInUse = false;
		} else {
			//due to the new exe we don't need the check anymore
			manifestInUse = true;
		}
	}

	/**
	 * @return Returns the manifestInUse.
	 */
	public boolean isManifestInUse() {
		return manifestInUse;
	}

	public void saveConfig() {
		if (properties!=null) {
			FileOutputStream fos = null;
			try {
				if (!confFile.exists()) confFile.createNewFile();
				fos = new FileOutputStream(confFile);
				properties.storeToXML(fos, null);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void loadSounds() {
		if (!properties.getPropertyAsBoolean("soundManager.active", true)) return;
		loadSound(Sound.ERROR, 					properties.getProperty("sound.Error"));
		loadSound(Sound.DOWNLOAD_ADDED, 		properties.getProperty("sound.DownloadAdded"));
		loadSound(Sound.DOWNLOADING_FINISHED,	properties.getProperty("sound.DownloadingFinished"));
		loadSound(Sound.SEEDING_FINISHED,		properties.getProperty("sound.SeedingFinished"));
	}

	public void unLoadSounds() {
		SoundManager.unLoad(Sound.ERROR);
		SoundManager.unLoad(Sound.DOWNLOAD_ADDED);
		SoundManager.unLoad(Sound.DOWNLOADING_FINISHED);
		SoundManager.unLoad(Sound.SEEDING_FINISHED);
	}

	/**
	 * Sets the Proxy for the current session.
	 *
	 * The data will not be saved.
	 *
	 * @param stype Proxy Type {@link http://java.sun.com/j2se/1.5.0/docs/api/java/net/Proxy.Type.html}
	 * @param url the Proxy url
	 * @param port the Proxy port
	 */
	public void setProxy (String stype, String url, int port) {
		Proxy.Type type = Proxy.Type.valueOf(stype);
		InetSocketAddress inetAddress = new InetSocketAddress(url, port);
		proxy = new Proxy(type,inetAddress);
		client.setProxy(proxy);
	}

	/**
	 * Sets the Proxy for the current session.
	 *
	 * The data will not be saved.
	 * @param proxy the Proxy to use
	 */
	public void setProxy (Proxy proxy) {
		this.proxy = proxy;
		client.setProxy(proxy);
	}

	/**
	 * @return the proxy
	 */
	public Proxy getProxy() {
		return proxy;
	}

	private void loadSound (Sound key, String snd) {
		if (snd == null || snd.equals("")) {
			logger.warn("Couldn't Load "+key+" because location was empty.");
			return;
		}
		File sndFile = new File (snd);
		if (sndFile.exists() && sndFile.canRead()) {
			SoundManager.unLoad(key);
			try {
				SoundManager.load(key, sndFile);
			} catch (SoundException e) {
				e.printStackTrace();
			}
		} else {
			logger.warn("Couldn't Load "+key+" from "+snd);
		}
	}

	/**
	 * Generates a Random UID
	 *
	 * @return true if UID was generated, false if UID already present
	 */
	private boolean generateUID() {
		if (properties.containsKey("azsmrc.uid")) return false;

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
			properties.setProperty("azsmrc.uid", uid);
			saveConfig();
			return true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getAWTClipboardString () {
		try {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable transferData = systemClipboard.getContents( null );
			for ( DataFlavor dataFlavor : transferData.getTransferDataFlavors() )
			{
			  Object content = transferData.getTransferData( dataFlavor );
			  if ( content instanceof String )
			  {
			   return (String)content;
			  }
			}
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setAWTClipboardString (String content) {
		try {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			systemClipboard.setContents(new StringSelection(content), null);
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void addAWTClipboardMonitor(){
		//AWT Listener for the clipboard
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
/*		FlavorListener[] listeners = clipboard.getFlavorListeners();
		for(FlavorListener listen:listeners){
			clipboard.removeFlavorListener(listen);
		}*/

		clipboard.addFlavorListener(new FlavorListener(){
			Pattern magnetPattern = Pattern.compile("^magnet:\\?xt=urn:btih:[A-Za-z2-7]{32}$",Pattern.CASE_INSENSITIVE);

			public void flavorsChanged(FlavorEvent event) {
				if(connect && properties.getPropertyAsBoolean("auto_clipboard",Utilities.isLinux()? false : true)){
					try {
						Transferable contents = clipboard.getContents(this);
						if ( ( contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
							try {
								final String string = ((String) contents.getTransferData(DataFlavor.stringFlavor)).trim();
								if(!string.contains("torrent") && !string.contains("magnet")) return;
								boolean valid = false;

								if (magnetPattern.matcher(string).find()) {
									valid = true;
								} else {
									try {
										new URL(string);
										valid = true;
									} catch (MalformedURLException e) {}
								}
								if (valid) {
									clipboard.setContents(emptyTransfer,null); //clear Transfer
									display.asyncExec(new SWTSafeRunnable(){
										public void runSafe() {
											OpenByURLDialog.openWithURL(string);
										}
									});
								} else {
									System.out.println("Not Valid URL: "+ string);
								}
							} catch(Exception e) {
								//Just a regular string.. so ignore
								e.printStackTrace();
							}

						} else if(( contents != null) && contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
							try{
								List list = (List) contents.getTransferData(DataFlavor.javaFileListFlavor);
								Iterator iter = list.iterator();
								while(iter.hasNext()){
									File file = (File) iter.next();
									logger.debug("*******FILE****" + file.getPath());
								}

							}catch(Exception e){
								e.printStackTrace();
							}

						}
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}

			}

		});
	}


	/**
	 * Returns the intialized PluginManagerImpl used in RCMain
	 * @return PluginManagerImpl
	 */
	public PluginManagerImpl getPluginManagerImpl(){
		return pluginManager;
	}
}//EOF
