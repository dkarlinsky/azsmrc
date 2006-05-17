package lbms.azsmrc.remote.client.swtgui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.SESecurityManager;
import lbms.azsmrc.remote.client.SESecurityManagerListener;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientEventListener;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.events.ConnectionListener;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;
import lbms.azsmrc.remote.client.events.ExceptionListener;
import lbms.azsmrc.remote.client.events.HTTPErrorListener;
import lbms.azsmrc.remote.client.events.SpeedUpdateListener;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.remote.client.swtgui.dialogs.MessageDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByFileDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByURLDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.ServerUpdateDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.FileUtil;
import lbms.azsmrc.remote.client.util.Timer;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.remote.client.util.TimerEventPeriodic;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.i18n.I18N;
import lbms.tools.launcher.Launchable;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

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

	public static final String LOGGER_NORMAL = "lbms.azsmrc.normal";
	public static final String LOGGER_DEBUG = "lbms.azsmrc.debug";
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String FSEP = System.getProperty("file.separator");

	protected Shell shell;
	protected Display display;
	private static RCMain rcMain;
	private Client client;
	private Properties properties, azsmrcProperties;
	private File confFile;
	private Timer timer;
	private TimerEventPeriodic updateTimer;
	private boolean connect;
	private Logger normalLogger, debugLogger;
	private Updater updater;

	private TrayItem systrayItem;
	private DownloadManagerShell mainWindow;
	private long runTime;
	private boolean manifestInUse;

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

	public void open() {
		DownloadContainer.loadColumns();
		SeedContainer.loadColumns();
		display = Display.getDefault();
		terminated = false;


		connect = (Boolean.parseBoolean(properties.getProperty("auto_connect","false")));
		createContents();
		if (connect) {
			if (properties.getProperty("connection_password_0", "").equals("")
					|| properties.getProperty("connection_username_0", "").equals("")
					|| properties.getProperty("connection_lastURL_0", "").equals("")) {
				connect = false;
			} else if (Boolean.parseBoolean(properties.getProperty("auto_open", "true"))) {
				updateTimer(true);
				client.sendGetGlobalStats();
			} else {
				updateTimer(false);
				client.sendGetGlobalStats();
			}
		}
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
		ImageRepository.loadImages(display);

		//Show Splash
		if(Boolean.parseBoolean(properties.getProperty("show_splash","true"))){
			new SplashScreen(RCMain.getRCMain().getDisplay(),20);
		}

		final Tray systray = display.getSystemTray ();
		systrayItem = new TrayItem (systray, SWT.NONE);
		setTrayIcon(0);

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

		//Open mainWindow if AutoOpen is true

		if(Boolean.parseBoolean(properties.getProperty("auto_open", "true"))){
			if (mainWindow == null) {
				mainWindow = new DownloadManagerShell();
			}
			mainWindow.open();
		}


		final Menu menu = new Menu(shell, SWT.POP_UP);

		final MenuItem openMainWindowMenuItem = new MenuItem(menu, SWT.PUSH);
		openMainWindowMenuItem.setText("Open Main Window");

		final MenuItem addMenuItem = new MenuItem(menu, SWT.CASCADE);
		addMenuItem.setText("Add");
		final Menu addMenu = new Menu(addMenuItem);
		addMenuItem.setMenu(addMenu);

		final MenuItem addByUrl = new MenuItem(addMenu,SWT.PUSH);
		addByUrl.setText("by URL");

		final MenuItem addByFile = new MenuItem(addMenu,SWT.PUSH);
		addByFile.setText("by File");

		new MenuItem (menu, SWT.SEPARATOR);

		final MenuItem connectDisconnectMenuItem = new MenuItem(menu, SWT.PUSH);
		if (!connect) connectDisconnectMenuItem.setText("Connect");
		else connectDisconnectMenuItem.setText("Disconnect");

		final MenuItem closeMenuItem = new MenuItem(menu, SWT.PUSH);
		closeMenuItem.setText("Close");

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
					connectDisconnectMenuItem.setText("Connect");
					disconnect();
					setTrayIcon(0);
					if(mainWindow != null){
						mainWindow.setConnectionStatusBar(0);
						mainWindow.setLogInOutButtons(false);
						mainWindow.clearMapsAndChildred();
					}
				} else {
					connectDisconnectMenuItem.setText("Disconnect");
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
				// TODO Auto-generated method stub
			}

			public void menuShown(MenuEvent arg0) {
				if (!connect){
					connectDisconnectMenuItem.setText("Connect");
					addMenuItem.setEnabled(false);
				}else{
					connectDisconnectMenuItem.setText("Disconnect");
					addMenuItem.setEnabled(true);
				}
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
				new OpenByFileDialog(display);
			}
		});

		systrayItem.addListener (SWT.MenuDetect, new Listener () {
			public void handleEvent (Event event) {
				menu.setVisible (true);
			}
		});

		//Add in the clipboard monitor
		addAWTClipboardMonitor();

	}

	public RCMain () {
		runTime = System.currentTimeMillis();
		System.out.println("Starting up RCMain.");
		System.out.println("Checking javaw.exe.manifest");
		javawExeManifest();
		confFile = new File(USER_DIR+FSEP+"config.cfg");
		properties = new Properties();
		System.out.println("Loading Properties.");
		if (confFile.exists() && confFile.canRead()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(confFile);
				properties.loadFromXML(fin);
			}  catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPropertiesFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (is!=null) try { is.close(); } catch (IOException e) {}
			}
		}
		System.out.println("Creating Logger.");
		normalLogger = Logger.getLogger(LOGGER_NORMAL);
		debugLogger = Logger.getLogger(LOGGER_DEBUG);
		normalLogger.setLevel(Level.FINEST);
		debugLogger.setLevel(Level.FINEST);

		Handler consoleHandler = new Handler() {
			private Formatter sF = new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage();
				}
			};
			@Override
			public void close() throws SecurityException {}
			@Override
			public void flush() {}
			@Override
			public void publish(LogRecord record) {
				System.out.println(sF.format(record));
			}
		};

		normalLogger.addHandler(consoleHandler);
		debugLogger.addHandler(consoleHandler);

		System.out.println("Loading I18N.");
		InputStream langIs = RCMain.class.getClassLoader().getResourceAsStream("lbms/azsmrc/remote/client/internat/default.lang");
		if (langIs != null) {
			try {
				I18N.initialize(langIs);
				if (I18N.isInitialized()) {
					System.out.println("I18N initialized.");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			FileHandler fh = new FileHandler(USER_DIR+FSEP+"debug.log",1024*1024,2,true);
			fh.setLevel(Level.parse(properties.getProperty("debugLevel", "WARNING")));
			debugLogger.addHandler(fh);
		} catch (SecurityException e1) {
			debugLogger.log(Level.WARNING, e1.getMessage(), e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			debugLogger.log(Level.WARNING, e1.getMessage(), e1);
			e1.printStackTrace();
		}

		SESecurityManager.getSingleton().addSecurityListner(new SESecurityManagerListener() {
			public boolean trustCertificate(String ressource, X509Certificate x509_cert) {
				// TODO Change it
				debugLogger.fine("Trusting Certificate: "+ressource);
				return true;
			}
		});
		System.out.println("Creating Client.");
		client = new Client();
		client.setDebugLogger(debugLogger);
		client.setServer(properties.getProperty("connection_lastURL_0",null));
		client.setUsername(properties.getProperty("connection_username_0"));
		client.setPassword(properties.getProperty("connection_password_0"));
		client.setFastMode(Boolean.parseBoolean(properties.getProperty("client.fastmode", "false")));
		client.addSpeedUpdateListener(new SpeedUpdateListener() {
			public void setSpeed(final int d, final int u) {
				if(display != null)
					display.syncExec(new Runnable() {
						public void run() {
							//setTrayToolTip("AzSMRC: D:"+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(d)+" - U:"+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(u));
							setTrayToolTip(client.getDownloadManager().getSeedingDownloadsOnly().length + " seeding, "
									+ client.getDownloadManager().getDownloadsOnly().length + " downloading, "
									+ "D: "+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(d)
									+ ", U: "+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(u));
						}
					});
			}
		});

		client.addHTTPErrorListener(new HTTPErrorListener() {
			public void httpError(int statusCode) {
				if (statusCode == UNAUTHORIZED) {
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Connection failed: "+statusCode+" Bad Username or Password", SWT.COLOR_RED);
					}
					normalLogger.warning("Connection failed: "+statusCode+" Bad Username or Password");
					MessageDialog.error(display,"Connection failed",statusCode+" Bad Username or Password");
					disconnect();
				} else {
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Connection failed: "+statusCode, SWT.COLOR_RED);
					}
					normalLogger.warning("Connection failed: "+statusCode);
				}
			}
		});
		client.addConnectionListener(new ConnectionListener() {
			public void connectionState(final int state) {
				debugLogger.finer("Connection State: "+state);
				if(display != null)
					display.syncExec(new Runnable() {
						public void run() {
							if (state == ST_CONNECTED) setTrayIcon(2);
							else if(state == ST_CONNECTING)setTrayIcon(1);
							else setTrayIcon(0);
						}
					});
				if (state == ST_DISCONNECTED) {
					int delay = client.getFailedConnections()*30000;
					delay = (delay>120000)?120000:delay;
					debugLogger.finer("Connection failed "+client.getFailedConnections()+" times, delay: "+(delay/1000)+"sec");
					if (mainWindow != null)
						updateTimer(true,delay);
					else
						updateTimer(false,delay);
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Connection failed "+client.getFailedConnections()+" time(s).", SWT.COLOR_RED);
					}
					failedConnection = true;
					 if(client.getFailedConnections() > 0 && client.getFailedConnections()%3 == 0){
						 MessageDialog.error(RCMain.getRCMain().getDisplay(),
									"Connection Error", "Failed " + RCMain.getRCMain().getClient().getFailedConnections() + " connection attempts. Please check your settings.");
							return;
						}
				} else if (failedConnection && state == ST_CONNECTED) {
					if (mainWindow != null) {
						updateTimer(true,0);
						mainWindow.setStatusBarText("Connection successful.", SWT.COLOR_DARK_GREEN);
					} else
						updateTimer(false,0);
				}

			}
		});
		client.getDownloadManager().addListener(new DownloadManagerListener() {
			public void downloadAdded(Download download) {
				 normalLogger.fine("Download Added: "+download.getName());

			}
			public void downloadRemoved(Download download) {
				normalLogger.fine("Download Removed: "+download.getName());
			};
		});
		client.addClientEventListener(new ClientEventListener() {
			public void handleEvent(int type, long time, Element event) {
				String msg = event.getAttributeValue("message");
				switch (type) {
				case RemoteConstants.EV_DL_FINISHED:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Download Finished: "+event.getAttributeValue("name"), SWT.COLOR_DARK_GREEN);
					}

					MessageDialog.message(display,"Download Finished",event.getAttributeValue("name"));
					normalLogger.info("Download Finished: "+event.getAttributeValue("name"));
					break;

				case RemoteConstants.EV_DL_EXCEPTION:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Download Exception: "+msg, SWT.COLOR_RED);
					}
					normalLogger.severe("Download Exception: "+msg);
					break;

				case RemoteConstants.EV_EXCEPTION:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Remote Exception: "+msg, SWT.COLOR_RED);
					}
					normalLogger.severe("Remote Exception: "+msg);
					break;
				case RemoteConstants.EV_UPDATE_AVAILABLE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Remote Update Available", SWT.COLOR_DARK_GREEN);
					}
					normalLogger.severe("Remote Update Available");
					client.getRemoteUpdateManager().load();
					break;
				case RemoteConstants.EV_MESSAGE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Server Message: "+msg);
					}
					MessageDialog.message(getDisplay(), "Server Message", msg);
					normalLogger.info("Server Message: "+msg);
					break;
				case RemoteConstants.EV_ERROR_MESSAGE:
					if (mainWindow != null) {
						mainWindow.setStatusBarText("Server ErrorMessage: "+msg, SWT.COLOR_RED);
					}
					normalLogger.severe("Server ErrorMessage: "+msg);
					MessageDialog.error(getDisplay(), "Server ErrorMessage", msg);
					break;
				}
			}
		});
		client.addExceptionListener(new ExceptionListener() {
			public void exceptionOccured(Exception e, boolean serious) {
				if (e instanceof SSLHandshakeException) {
					MessageDialog.message(display,true,5000,"Connection Error","Server doesn't support SSL.");
					disconnect();
					normalLogger.severe("Connection Error: Server doesn't support SSL.");
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


		System.out.println("Creating Updater.");
		try {
			updater = new Updater(new URL(RemoteConstants.UPDATE_URL),new File("AzSMRCupdate.xml.gz"),new File(USER_DIR));
		} catch (MalformedURLException e2) {
		}
		updater.addListener(new UpdateListener() {
			public void exception(Exception e) {
				System.out.println("Update Exception: "+e.getLocalizedMessage());
				System.out.println("Update Exception: "+e.getMessage());
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Update Exception: "+e.getLocalizedMessage(),SWT.COLOR_RED);
				}
				normalLogger.severe("Update Exception: "+e.getLocalizedMessage());

			}
			public void noUpdate() {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("No Update Available");
				}
				normalLogger.info("No Update Available");
			}
			public void updateAvailable(final Update update) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Update Available: Version "+update.getVersion());
				}
				normalLogger.info("Update Available: Version "+update.getVersion());
				if (Boolean.parseBoolean(properties.getProperty("update.autoupdate", "false"))) {
					updater.doUpdate();
				}else{
					display.asyncExec(new Runnable() {
						public void run() {
							new UpdateDialog(display,update,updater);
						}
					});

				}
			}
			public void updateFailed(String reason) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Update Failed: "+reason,SWT.COLOR_RED);
				}
				normalLogger.info("Update Failed: "+reason);
			}
			public void updateFinished() {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Update Finished");
				}
				normalLogger.info("Update Finished");
			}

			public void updateError(String error) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Update Error: "+error,SWT.COLOR_RED);
				}
				normalLogger.info("Update Error: "+error);
			}
			public void initializeUpdate(lbms.tools.Download[] dls) {
				// TODO Add the Progressmonitor stuff

			}
		});
		if (Boolean.parseBoolean(properties.getProperty("update.autocheck", "true"))) {
			long lastcheck = Long.parseLong(properties.getProperty("update.lastcheck", "0"));
			if (System.currentTimeMillis()-lastcheck > 1000*60*60*24) {
				if (mainWindow != null) {
					mainWindow.setStatusBarText("Checking for Updates");
				}
				normalLogger.info("Checking for Updates");
				updater.checkForUpdates(Boolean.parseBoolean(properties.getProperty("update.beta", "false")));
				properties.setProperty("update.lastcheck",Long.toString(System.currentTimeMillis()));
			}
		}
		System.out.println("Creating Timer.");
		timer = new Timer("Main Timer",5);
		System.out.println("Finished Startup.");
		rcMain = this;
	}

	private void shutdown() {
		System.out.println("Shutting down");
		timer.destroy();
		DownloadContainer.saveColumns();
		SeedContainer.saveColumns();
		getRunTime();
		saveConfig();
		ImageRepository.unLoadImages();
		System.out.println("Shutdown completed");
	}

	public long getRunTime() {
		long now = System.currentTimeMillis();
		long rTime = (now-runTime)
			+Long.parseLong(properties.getProperty("runTime", "0"));
		runTime = now;
		properties.setProperty("runTime", Long.toString(rTime));
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
		debugLogger.finer("Connect!");
		connect = true;
		updateTimer(open);
	}

	public void disconnect() {
		debugLogger.finer("Disconnect!");
		connect = false;
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
		debugLogger.finer("Changing Timer: "+(open?"GUI mode":"Tray mode"));
		if (open) {
			updateTimer = timer.addPeriodicEvent(Long.parseLong(properties.getProperty("connection_interval_open","5000"))+delay,
				new TimerEventPerformer() {
				public void perform(TimerEvent event) {
					debugLogger.finest("Timer: GUI mode");
					client.sendListTransfers(Integer.parseInt(properties.getProperty("transfer_states",Integer.toString(RemoteConstants.ST_ALL))));
				}
			});
		}
		else {
			updateTimer = timer.addPeriodicEvent(Long.parseLong(properties.getProperty("connection_interval_closed","15000"))+delay,
				new TimerEventPerformer() {
				public void perform(TimerEvent event) {
					debugLogger.finest("Timer: Tray mode");
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
			mainWindow = new DownloadManagerShell();
			mainWindow.open();
	}

	public Timer getMainTimer () {
		return timer;
	}

	/**
	 * @return Returns the debugLogger.
	 */
	public Logger getDebugLogger() {
		return debugLogger;
	}

	/**
	 * @return Returns the normalLogger.
	 */
	public Logger getNormalLogger() {
		return normalLogger;
	}

	public Properties getProperties() {
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
	 *
	 * @param connection -- int -- 0 for no connection, 1 for connecting, 2 for connected
	 */
	public void setTrayIcon(int connection){
		if(systrayItem == null || systrayItem.isDisposed()) return;
		if(connection==0){
			systrayItem.setToolTipText("AzSMRC -- Not Connected");
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Red"));
		}else if(connection == 1){
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Connecting"));

		}else if(connection == 2){
			//systrayItem.setToolTipText("AzMultiUser RC");  //Let the listener do this!
			systrayItem.setImage(ImageRepository.getImage("TrayIcon_Blue"));
		}
	}

	public void javawExeManifest() {
		if (!(RemoteConstants.isWindowsXP || RemoteConstants.isWindows2003) ) {
			manifestInUse = false;
			return;
		}
		File fDest = new File(System.getProperty("java.home")
				+ "\\bin\\javaw.exe.manifest");
		File fOrigin = new File("javaw.exe.manifest");
		if (!fDest.exists() && fOrigin.exists()) {
			FileUtil.copyFile(fOrigin, fDest);
			manifestInUse = false;
		} else if (fDest.exists())
			manifestInUse = true;
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
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
				if(connect && Boolean.parseBoolean(properties.getProperty("auto_clipboard",Utilities.isLinux()? "false" : "true"))){
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
								display.asyncExec(new Runnable(){
									public void run() {
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
								debugLogger.fine("*******FILE****" + file.getPath());
							}

						}catch(Exception e){
							e.printStackTrace();
						}

					}
				}

			}

		});
	}
}//EOF
