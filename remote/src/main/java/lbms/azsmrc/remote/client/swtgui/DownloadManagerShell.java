/*
 * Created on Apr 30, 2005 Created by omschaub
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.LoginData;
import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.events.ConnectionListener;
import lbms.azsmrc.remote.client.events.DownloadListener;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;
import lbms.azsmrc.remote.client.events.GlobalStatsListener;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.plugins.ui.swt.ViewID;
import lbms.azsmrc.remote.client.pluginsimpl.PluginManagerImpl;
import lbms.azsmrc.remote.client.pluginsimpl.ui.swt.UIPluginViewImpl;
import lbms.azsmrc.remote.client.swtgui.container.Container;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.remote.client.swtgui.dialogs.ChangeCategoryDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.ConnectionDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.CreateTorrentDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.InputShell;
import lbms.azsmrc.remote.client.swtgui.dialogs.MoveDataDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.NormalUserDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByFileDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByURLDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.ServerUpdateDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.TableColumnEditorDialog;
import lbms.azsmrc.remote.client.swtgui.tabs.ConsoleTab;
import lbms.azsmrc.remote.client.swtgui.tabs.DownloadHistoryTab;
import lbms.azsmrc.remote.client.swtgui.tabs.ManageUsersTab;
import lbms.azsmrc.remote.client.swtgui.tabs.MyTracker;
import lbms.azsmrc.remote.client.swtgui.tabs.PreferencesTab;
import lbms.azsmrc.remote.client.swtgui.tabs.ReadmeTab;
import lbms.azsmrc.remote.client.swtgui.tabs.ServerDetailsTab;
import lbms.azsmrc.remote.client.swtgui.tabs.TorrentDetailsTab;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.ExtendedProperties;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class DownloadManagerShell {

	private static final String							PFX						= "downloadmanagershell.";

	private static final long							DONATION_INTERVAL		= 24 * 60 * 60 * 1000;

	private Shell										DOWNLOAD_MANAGER_SHELL;
	private Table										downloadsTable,
			seedsTable;
	private final Semaphore								semaphore				= new Semaphore(
																						1);
	private SashForm									sash;
	private CTabFolder									tabFolder;
	private CTabItem									myTorrents;
	private MenuItem									moveData;

	// Map pluginTabs = new HashMap();

	private final DownloadListener						dlL						= new DownloadListener() {

																					public void stateChanged (
																							Download download,
																							int old_state,
																							int new_state) {
																						try {
																							semaphore
																									.acquire();
																							// Changing
																							// from
																							// Downloading
																							// to
																							// Seeding
																							if (new_state == Download.ST_SEEDING
																									&& downloadsMap
																											.containsKey(download
																													.getHash())) {
																								downloadsMap
																										.remove(download
																												.getHash());
																								SeedContainer sc = new SeedContainer(
																										download,
																										seedsTable,
																										SWT.NULL);
																								seedsMap
																										.put(
																												download
																														.getHash(),
																												sc);
																								redrawTables = true;
																							}
																						} catch (InterruptedException e) {

																							e
																									.printStackTrace();
																						} finally {
																							semaphore
																									.release();
																						}
																						// Other
																						// changes
																						// go
																						// here
																					}

																					public void positionChanged (
																							Download download,
																							int oldPosition,
																							int newPosition) {
																						logger
																								.debug("Position changed "
																										+ download
																										+ " "
																										+ oldPosition
																										+ " -> "
																										+ newPosition);
																						redrawTables = true;
																					}

																				};

	private boolean										COLLAPSED;
	private boolean										bSingleUserMode;
	private boolean										redrawTables;
	private String										TITLE;

	private final SortedMap<String, DownloadContainer>	downloadsMap			= new TreeMap<String, DownloadContainer>();
	private final SortedMap<String, SeedContainer>		seedsMap				= new TreeMap<String, SeedContainer>();

	private Container[]									downloadsArray			= new Container[0];
	private Container[]									seedsArray				= new Container[0];
	private final Container[]							emptyArray				= new Container[0];

	private ToolItem									top, up, bottom, down,
			login, logout, quickconnect;
	private ToolItem									refresh, manage_users,
			preferences, console;
	private ToolItem									addTorrent_by_file,
			addTorrent_by_url, pauseAll, resumeAll;
	private ToolItem									stopTorrent,
			queueTorrent, removeTorrent;
	private MenuItem									menuLogin, menuLogout,
			menuQuickconnect, menuRestartAzureus;
	private MenuItem									menuServerDetails,
			menuServerUpdate, menuAddByFile, menuAddbyURL;
	private MenuItem									menuMyShares;

	// status bar labels
	private CLabel										statusBarText;
	private Composite									statusbarComp;
	private CLabelPadding								statusDown, statusUp, /*
																			 * statusBarText,
																			 */
														connectionStatusIcon,
			sslStatusIcon;

	private String										azureusDownload			= "N/S";
	private String										azureusUpload			= "N/S";
	private int											upSpeed					= 0;
	private int											downSpeed				= 0;

	private static Logger								logger					= Logger
																						.getLogger("lbms.azsmrc.DownloadManagerShell");	;
	private final Set<String>							openPluginViews			= new HashSet<String>();

	private int											drag_drop_line_start	= -1;

	public void open () {
		if (DOWNLOAD_MANAGER_SHELL != null
				&& !DOWNLOAD_MANAGER_SHELL.isDisposed()) {
			DOWNLOAD_MANAGER_SHELL.setVisible(true);
			DOWNLOAD_MANAGER_SHELL.forceFocus();
			DOWNLOAD_MANAGER_SHELL.forceActive();
			return;
		}

		if (TITLE == null) {
			TITLE = "AzSMRC";
		}

		DOWNLOAD_MANAGER_SHELL = new Shell(RCMain.getRCMain().getDisplay());

		DOWNLOAD_MANAGER_SHELL.setImage(ImageRepository
				.getImage("TrayIcon_Blue"));

		// Grid Layout
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		DOWNLOAD_MANAGER_SHELL.setLayout(gridLayout);
		DOWNLOAD_MANAGER_SHELL.setMinimumSize(400, 250);

		// -----------------------MAIN TOP MENU----------------------------\\
		Menu menuBar = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.BAR);
		DOWNLOAD_MANAGER_SHELL.setMenuBar(menuBar);

		MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText(I18N.translate(PFX + "menu.main"));

		Menu fileSubmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		fileItem.setMenu(fileSubmenu);

		MenuItem remoteItem = new MenuItem(menuBar, SWT.CASCADE);
		remoteItem.setText(I18N.translate(PFX + "menu.remote"));

		Menu remoteSubmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		remoteItem.setMenu(remoteSubmenu);

		MenuItem toolItem = new MenuItem(menuBar, SWT.CASCADE);
		toolItem.setText(I18N.translate(PFX + "menu.tools"));

		Menu toolSubmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		toolItem.setMenu(toolSubmenu);

		MenuItem pluginItem = new MenuItem(menuBar, SWT.CASCADE);
		pluginItem.setText(I18N.translate(PFX + "menu.plugins"));

		Menu pluginSubmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		pluginItem.setMenu(pluginSubmenu);

		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(I18N.translate(PFX + "menu.help"));

		Menu helpSubmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		helpItem.setMenu(helpSubmenu);

		// ----Main submenu

		// Login
		menuLogin = new MenuItem(fileSubmenu, SWT.PUSH);
		menuLogin.setText(I18N.translate(PFX + "menu.main.connect"));
		menuLogin.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ConnectionDialog.open(RCMain.getRCMain().getDisplay());
			}
		});

		// QuickConnect
		menuQuickconnect = new MenuItem(fileSubmenu, SWT.PUSH);
		menuQuickconnect
				.setText(I18N.translate(PFX + "menu.main.quickconnect"));
		menuQuickconnect.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Properties properties = RCMain.getRCMain().getProperties();
				if (properties.containsKey("lastConnection")) {
					try {
						LoginData login = new LoginData(properties.getProperty(
								"lastConnection", ""));
						if (login.isComplete()) {
							RCMain.getRCMain().connect(true);
							initializeConnection();
						} else if (login.hasUsername() && login.hasHost()) {
							InputShell is = new InputShell(
									I18N
											.translate(PFX
													+ "menu.main.quickconnect.inputshell.title"),
									I18N
											.translate(PFX
													+ "menu.main.quickconnect.inputshell.text1")
											+ " "
											+ login.getUsername()
											+ I18N
													.translate(PFX
															+ "menu.main.quickconnect.inputshell.text2")
											+ " " + login.getHost());
							is.setIsPassword(true);
							String pw = is.open();
							if (pw != null) {
								RCMain.getRCMain().getClient().setPassword(pw);
								RCMain.getRCMain().connect(true);
								initializeConnection();
							}
						} else {
							ConnectionDialog.open(RCMain.getRCMain()
									.getDisplay());
						}
					} catch (MalformedURLException e1) {
						ConnectionDialog.open(RCMain.getRCMain().getDisplay());
					}

				}
			}
		});

		// Logout
		menuLogout = new MenuItem(fileSubmenu, SWT.PUSH);
		menuLogout.setText(I18N.translate(PFX + "menu.main.logout"));
		menuLogout.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				RCMain.getRCMain().disconnect();
				setGUItoLoggedOut();
			}
		});

		// Separator
		new MenuItem(fileSubmenu, SWT.SEPARATOR);

		// Exit
		MenuItem exitItem = new MenuItem(fileSubmenu, SWT.PUSH);
		exitItem.setText(I18N.translate(PFX + "menu.main.exit") + "\tCtrl+Q");
		exitItem.setAccelerator(SWT.CTRL + 'Q');
		exitItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				boolean confirmExit = RCMain.getRCMain().getProperties()
						.getPropertyAsBoolean("confirm.exit");
				if (confirmExit) {
					MessageBox messageBox = new MessageBox(
							DOWNLOAD_MANAGER_SHELL, SWT.ICON_QUESTION | SWT.OK
									| SWT.CANCEL);
					messageBox.setText(I18N.translate(PFX
							+ "menu.main.exit.messagebox.title"));
					messageBox.setMessage(I18N.translate(PFX
							+ "menu.main.exit.messagebox.message"));
					int response = messageBox.open();
					switch (response) {
					case SWT.OK:
						RCMain.getRCMain().close();
						break;
					case SWT.CANCEL:
						break;
					}
				} else {
					RCMain.getRCMain().close();
				}
			}
		});

		// -----Remote

		// Add by file
		menuAddByFile = new MenuItem(remoteSubmenu, SWT.PUSH);
		menuAddByFile.setText(I18N.translate(PFX + "menu.remote.addbyfile"));
		menuAddByFile.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				OpenByFileDialog.open();
			}
		});

		// Add by URL
		menuAddbyURL = new MenuItem(remoteSubmenu, SWT.PUSH);
		menuAddbyURL.setText(I18N.translate(PFX + "menu.remote.addbyurl"));
		menuAddbyURL.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				OpenByURLDialog.open();
			}
		});

		// Separator
		new MenuItem(remoteSubmenu, SWT.SEPARATOR);

		// Server Details
		menuServerDetails = new MenuItem(remoteSubmenu, SWT.PUSH);
		menuServerDetails.setText(I18N.translate(PFX
				+ "menu.remote.serverdetails"));
		menuServerDetails.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ServerDetailsTab.open(tabFolder);
			}
		});
		menuServerUpdate = new MenuItem(remoteSubmenu, SWT.PUSH);
		menuServerUpdate.setText(I18N.translate(PFX
				+ "menu.remote.serverupdates"));
		menuServerUpdate.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ServerUpdateDialog.open();
			}
		});

		// Separator
		new MenuItem(remoteSubmenu, SWT.SEPARATOR);

		// Restart Server
		menuRestartAzureus = new MenuItem(remoteSubmenu, SWT.PUSH);
		menuRestartAzureus.setText(I18N.translate(PFX
				+ "menu.remote.restartserver"));
		menuRestartAzureus.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				MessageBox messageBox = new MessageBox(DOWNLOAD_MANAGER_SHELL,
						SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				messageBox.setText(I18N.translate(PFX
						+ "menu.remote.restartserver.messagebox.title"));
				messageBox.setMessage(I18N.translate(PFX
						+ "menu.remote.restartserver.messagebox.message"));
				int response = messageBox.open();
				switch (response) {
				case SWT.OK:
					RCMain.getRCMain().getClient().sendRestartAzureus();
					break;
				case SWT.CANCEL:
					break;
				}
			}
		});

		// -----Tools Submenu

		menuMyShares = new MenuItem(toolSubmenu, SWT.PUSH);
		menuMyShares.setText(I18N.translate(PFX + "menu.tools.mytracker"));
		menuMyShares.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				MyTracker.open(tabFolder);
			}
		});

		MenuItem menuCreateTorrent = new MenuItem(toolSubmenu, SWT.PUSH);
		menuCreateTorrent.setText(I18N.translate(PFX
				+ "menu.tools.createTorrent"));
		menuCreateTorrent.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				CreateTorrentDialog.open();
			}
		});

		MenuItem menuConsole = new MenuItem(toolSubmenu, SWT.PUSH);
		menuConsole.setText(I18N.translate(PFX + "menu.tools.console"));
		menuConsole.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ConsoleTab.open(tabFolder, true);
			}
		});

		MenuItem menuPrefs = new MenuItem(toolSubmenu, SWT.PUSH);
		menuPrefs.setText(I18N.translate(PFX + "menu.tools.preferences"));
		menuPrefs.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				PreferencesTab.open(tabFolder);
			}
		});

		MenuItem menuHistory = new MenuItem(toolSubmenu, SWT.PUSH);
		menuHistory.setText(I18N.translate(PFX + "menu.tools.history"));
		menuHistory.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				DownloadHistoryTab.open(tabFolder);
			}
		});

		new MenuItem(toolSubmenu, SWT.SEPARATOR);

		MenuItem menuUpdate = new MenuItem(toolSubmenu, SWT.PUSH);
		menuUpdate.setText(I18N.translate(PFX + "menu.tools.update"));
		menuUpdate.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				RCMain.getRCMain().getUpdater().checkForUpdates(
						RCMain.getRCMain().getProperties()
								.getPropertyAsBoolean("update.beta"));
				RCMain.getRCMain().getProperties().setProperty(
						"update.lastcheck", System.currentTimeMillis());
				RCMain.getRCMain().saveConfig();
			}
		});

		// -----Plugin Submenu

		String[] pluginIDs = RCMain.getRCMain().getPluginManagerImpl()
				.getUIManager().getViewIDs(ViewID.MAIN);
		for (final String pluginID : pluginIDs) {
			logger.debug("Adding Plugin Item: " + pluginID);
			MenuItem pluginMenuItem = new MenuItem(pluginSubmenu, SWT.PUSH);
			pluginMenuItem.setText(pluginID);
			pluginMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent (Event e) {
					RCMain.getRCMain().getPluginManagerImpl().getUIManager()
							.openMainView(pluginID);
				}
			});
		}

		// -----Help Submenu

		MenuItem menuAbout = new MenuItem(helpSubmenu, SWT.PUSH);
		menuAbout.setText(I18N.translate(PFX + "menu.help.about"));

		menuAbout.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ReadmeTab.open(tabFolder);
			}
		});

		MenuItem ffPlugin = new MenuItem(helpSubmenu, SWT.PUSH);
		ffPlugin.setText(I18N.translate(PFX + "menu.help.ffPlugin"));

		ffPlugin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				Program.launch("http://www.alexisbrunet.com");
			}
		});

		// --------------------------TOOLBAR--------------------------------\\

		// Toolbar for torrent adding and other features
		final ToolBar bar = new ToolBar(DOWNLOAD_MANAGER_SHELL, SWT.HORIZONTAL
				| SWT.FLAT);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		bar.setLayoutData(gridData);

		login = new ToolItem(bar, SWT.PUSH);
		login.setImage(ImageRepository.getImage("connect"));
		login.setToolTipText(I18N.translate(PFX + "toolbar.login.tooltiptext"));
		login.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ConnectionDialog.open(RCMain.getRCMain().getDisplay());
			}
		});

		quickconnect = new ToolItem(bar, SWT.PUSH);
		quickconnect.setImage(ImageRepository.getImage("connect_quick"));
		quickconnect.setToolTipText(I18N.translate(PFX
				+ "toolbar.quickconnect.tooltiptext"));
		quickconnect.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Properties properties = RCMain.getRCMain().getProperties();
				if (properties.containsKey("lastConnection")) {
					try {
						LoginData login = new LoginData(properties.getProperty(
								"lastConnection", ""));
						if (login.isComplete()) {
							RCMain.getRCMain().connect(true);
							initializeConnection();
						} else if (login.hasUsername() && login.hasHost()) {
							InputShell is = new InputShell(
									I18N
											.translate(PFX
													+ "toolbar.quickconnect.inputshell.title"),
									I18N
											.translate(PFX
													+ "toolbar.quickconnect.inputshell.text1")
											+ " "
											+ login.getUsername()
											+ I18N
													.translate(PFX
															+ "toolbar.quickconnect.inputshell.text2")
											+ " " + login.getHost());
							is.setIsPassword(true);
							String pw = is.open();
							if (pw != null) {
								RCMain.getRCMain().getClient().setPassword(pw);
								RCMain.getRCMain().connect(true);
								initializeConnection();
							}
						} else {
							ConnectionDialog.open(RCMain.getRCMain()
									.getDisplay());
						}
					} catch (MalformedURLException e1) {
						ConnectionDialog.open(RCMain.getRCMain().getDisplay());
					}

				}
			}
		});

		logout = new ToolItem(bar, SWT.PUSH);
		logout.setImage(ImageRepository.getImage("logout"));
		logout.setToolTipText(I18N
				.translate(PFX + "toolbar.logout.tooltiptext"));
		logout.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				RCMain.getRCMain().disconnect();
				setGUItoLoggedOut();
			}
		});

		refresh = new ToolItem(bar, SWT.PUSH);
		refresh.setImage(ImageRepository.getImage("refresh"));
		refresh.setToolTipText(I18N.translate(PFX
				+ "toolbar.refresh.tooltiptext"));
		refresh.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (RCMain.getRCMain().connected()) {
					RCMain.getRCMain().getClient().getDownloadManager().update(
							true);
				}
			}
		});

		// separator
		new ToolItem(bar, SWT.SEPARATOR);

		addTorrent_by_file = new ToolItem(bar, SWT.PUSH);
		addTorrent_by_file.setImage(ImageRepository.getImage("open_by_file"));
		addTorrent_by_file.setToolTipText(I18N.translate(PFX
				+ "toolbar.openbyfile.tooltiptext"));
		addTorrent_by_file.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				OpenByFileDialog.open();
			}
		});

		addTorrent_by_url = new ToolItem(bar, SWT.PUSH);
		addTorrent_by_url.setImage(ImageRepository.getImage("open_by_url"));
		addTorrent_by_url.setToolTipText(I18N.translate(PFX
				+ "toolbar.openbyurl.tooltiptext"));
		addTorrent_by_url.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				OpenByURLDialog.open();
			}
		});

		// separator
		new ToolItem(bar, SWT.SEPARATOR);

		top = new ToolItem(bar, SWT.PUSH);
		top.setImage(ImageRepository.getImage("top"));
		top.setToolTipText(I18N.translate(PFX + "toolbar.top.tooltiptext"));
		top.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (downloadsTable.isFocusControl()) {
					TableItem[] items = downloadsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position - 1) > 0) {
						container.getDownload().moveTo(1);
					}
				} else {
					TableItem[] items = seedsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position - 1) > 0) {
						container.getDownload().moveTo(1);
					}
				}
			}
		});

		up = new ToolItem(bar, SWT.PUSH);
		up.setImage(ImageRepository.getImage("up"));
		up.setToolTipText(I18N.translate(PFX + "toolbar.up.tooltiptext"));
		up.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (downloadsTable.isFocusControl()) {
					TableItem[] items = downloadsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position - 1) > 0) {
						container.getDownload().moveUp();
					}
				} else {
					TableItem[] items = seedsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position - 1) > 0) {
						container.getDownload().moveUp();
					}
				}
				redrawTables();
			}
		});

		down = new ToolItem(bar, SWT.PUSH);
		down.setImage(ImageRepository.getImage("down"));
		down.setToolTipText(I18N.translate(PFX + "toolbar.down.tooltiptext"));
		down.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (downloadsTable.isFocusControl()) {
					TableItem[] items = downloadsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					DownloadContainer container = (DownloadContainer) items[0]
							.getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position + 1) != (downloadsMap.size() + 1)) {
						container.getDownload().moveDown();
					}
				} else {
					TableItem[] items = seedsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if ((current_position + 1) != (seedsMap.size() + 1)) {
						container.getDownload().moveDown();
					}
				}

				redrawTables();

			}
		});

		bottom = new ToolItem(bar, SWT.PUSH);
		bottom.setImage(ImageRepository.getImage("bottom"));
		bottom.setToolTipText(I18N
				.translate(PFX + "toolbar.bottom.tooltiptext"));
		bottom.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (downloadsTable.isFocusControl()) {
					TableItem[] items = downloadsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if (current_position != downloadsMap.size()) {
						container.getDownload().moveTo(downloadsMap.size());
					}
				} else {
					TableItem[] items = seedsTable.getSelection();
					if (items.length == 0 || items.length > 1) {
						return;
					}
					Container container = (Container) items[0].getData();
					int current_position = container.getDownload()
							.getPosition();
					if (current_position != seedsMap.size()) {
						container.getDownload().moveTo(seedsMap.size());
					}
				}
			}
		});

		// Set all torrent move buttons disabled
		setTorrentMoveButtons(false, false, false, false);

		// separator
		new ToolItem(bar, SWT.SEPARATOR);

		queueTorrent = new ToolItem(bar, SWT.PUSH);
		queueTorrent.setImage(ImageRepository.getImage("toolbar_queue"));
		queueTorrent.setToolTipText(I18N.translate(PFX
				+ "toolbar.queuetorrent.tooltiptext"));
		queueTorrent.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				TableItem[] items;
				if (downloadsTable.isFocusControl()) {
					items = downloadsTable.getSelection();
				} else {
					items = seedsTable.getSelection();
				}
				if (items.length == 0) {
					return;
				}

				RCMain.getRCMain().getClient().transactionStart();

				for (TableItem item : items) {
					Container container = (Container) item.getData();
					Download dl = container.getDownload();
					if (dl.getState() != Download.ST_QUEUED) {
						dl.stopAndQueue();
					}
				}

				RCMain.getRCMain().getClient().transactionCommit();
				// Reset Buttons
				setToolBarTorrentIcons(false, true, true);
			}
		});

		stopTorrent = new ToolItem(bar, SWT.PUSH);
		stopTorrent.setImage(ImageRepository.getImage("toolbar_stop"));
		stopTorrent.setToolTipText(I18N.translate(PFX
				+ "toolbar.stoptorrents.tooltiptext"));
		stopTorrent.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				TableItem[] items;
				if (downloadsTable.isFocusControl()) {
					items = downloadsTable.getSelection();
				} else {
					items = seedsTable.getSelection();
				}
				if (items.length == 0) {
					return;
				}
				RCMain.getRCMain().getClient().transactionStart();

				for (TableItem item : items) {
					Container container = (Container) item.getData();
					Download dl = container.getDownload();
					if (dl.getState() != Download.ST_STOPPED) {
						dl.stop();
					}
				}

				RCMain.getRCMain().getClient().transactionCommit();
				// Reset the Toobar Buttons
				setToolBarTorrentIcons(true, false, true);
			}
		});

		removeTorrent = new ToolItem(bar, SWT.PUSH);
		removeTorrent.setImage(ImageRepository.getImage("toolbar_remove"));
		removeTorrent.setToolTipText(I18N.translate(PFX
				+ "toolbar.removetorrents.tooltiptext"));
		removeTorrent.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				TableItem[] items;
				if (downloadsTable.isFocusControl()) {
					items = downloadsTable.getSelection();
				} else {
					items = seedsTable.getSelection();
				}
				if (items.length == 0) {
					return;
				}
				RCMain.getRCMain().getClient().transactionStart();

				for (TableItem item : items) {
					Container container = (Container) item.getData();
					container.getDownload().remove();
					if (seedsMap.containsKey(container.getDownload().getHash())) {
						seedsMap.remove(container.getDownload().getHash());
						redrawTables();
					} else if (downloadsMap.containsKey(container.getDownload()
							.getHash())) {
						downloadsMap.remove(container.getDownload().getHash());
						redrawTables();
					}
				}
				RCMain.getRCMain().getClient().transactionCommit();
			}
		});

		setToolBarTorrentIcons(false, false, false);

		// separator
		new ToolItem(bar, SWT.SEPARATOR);

		pauseAll = new ToolItem(bar, SWT.DROP_DOWN);
		pauseAll.setImage(ImageRepository.getImage("pause"));
		pauseAll.setToolTipText(I18N.translate(PFX
				+ "toolbar.pauseall.tooltiptext"));

		final Menu tbmenu = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.POP_UP);

		final MenuItem pauseDownloads_1min = new MenuItem(tbmenu, SWT.PUSH);
		pauseDownloads_1min.setText(I18N.translate("rcmain."
				+ "traymenu.quickmenu.pause1min"));

		final MenuItem pauseDownloads_5min = new MenuItem(tbmenu, SWT.PUSH);
		pauseDownloads_5min.setText(I18N.translate("rcmain."
				+ "traymenu.quickmenu.pause5min"));

		final MenuItem pauseDownloads_UserSpecified = new MenuItem(tbmenu,
				SWT.PUSH);
		pauseDownloads_UserSpecified.setText(I18N.translate("rcmain."
				+ "traymenu.quickmenu.pauseUserSpecified"));

		pauseDownloads_1min.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				RCMain.getRCMain().getClient().getDownloadManager()
						.pauseDownloads(60);
			}
		});

		pauseDownloads_5min.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				RCMain.getRCMain().getClient().getDownloadManager()
						.pauseDownloads(300);
			}
		});

		pauseDownloads_UserSpecified
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected (SelectionEvent e) {
						try {
							InputShell is = new InputShell(
									I18N
											.translate("rcmain."
													+ "traymenu.quickmenu.pauseUserSpecified.inputShell.title"),
									I18N
											.translate("rcmain."
													+ "traymenu.quickmenu.pauseUserSpecified.inputShell.message"));
							String str_minutes = is.open();
							int mins = Integer.parseInt(str_minutes);
							RCMain.getRCMain().getClient().getDownloadManager()
									.pauseDownloads(mins * 60);
							logger.info("Pausing all downloads for " + mins
									* 60 + " seconds (" + mins + " minutes)");
						} catch (Exception ex) {
						}

					}
				});

		// main pause listener
		pauseAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = pauseAll.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = bar.toDisplay(pt);
					tbmenu.setLocation(pt.x, pt.y);
					tbmenu.setVisible(true);
				} else {
					RCMain.getRCMain().getClient().getDownloadManager()
							.pauseDownloads();
				}
			}
		});

		resumeAll = new ToolItem(bar, SWT.PUSH);
		resumeAll.setImage(ImageRepository.getImage("resume"));
		resumeAll.setToolTipText(I18N.translate(PFX
				+ "toolbar.resumeall.tooltiptext"));
		resumeAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				RCMain.getRCMain().getClient().getDownloadManager()
						.resumeDownloads();
			}
		});

		// separator
		new ToolItem(bar, SWT.SEPARATOR);

		preferences = new ToolItem(bar, SWT.PUSH);
		preferences.setImage(ImageRepository.getImage("preferences"));
		preferences.setToolTipText(I18N.translate(PFX
				+ "toolbar.openprefs.tooltiptext"));
		preferences.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				PreferencesTab.open(tabFolder);
			}
		});

		manage_users = new ToolItem(bar, SWT.PUSH);
		manage_users.setImage(ImageRepository.getImage("manager_users"));
		manage_users.setToolTipText(I18N.translate(PFX
				+ "toolbar.manageusers.tooltiptext"));
		manage_users.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				User user = RCMain.getRCMain().getClient().getUserManager()
						.getActiveUser();
				if (user == null) {
					manage_users.setEnabled(false);
					return;
				}
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					ManageUsersTab.open(tabFolder);
				} else {
					NormalUserDialog.open(user.getUsername());
				}
			}
		});

		console = new ToolItem(bar, SWT.PUSH);
		console.setImage(ImageRepository.getImage("console"));
		console.setToolTipText(I18N.translate(PFX
				+ "toolbar.openconsole.tooltiptext"));
		console.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ConsoleTab.open(tabFolder, true);
			}
		});

		ToolItem information = new ToolItem(bar, SWT.PUSH);
		information.setImage(ImageRepository.getImage("information"));
		information.setToolTipText(I18N.translate(PFX
				+ "toolbar.about.tooltiptext"));
		information.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				ReadmeTab.open(tabFolder);
			}
		});

		setLogInOutButtons(false);

		// shell title
		DOWNLOAD_MANAGER_SHELL.setText(TITLE);

		// ------------------- Listeners for the shell ---------------------\\

		final GlobalStatsListener sul = new GlobalStatsListener() {
			public void updateStats (final int d, final int u, int s, int sq,
					int dl, int dlq) {
				upSpeed = u;
				downSpeed = d;
				refreshStatusBar();
			}
		};

		RCMain.getRCMain().getClient().addGlobalStatsListener(sul);

		final ConnectionListener cl = new ConnectionListener() {

			public void connectionState (final int state) {
				setConnectionStatusBar(state);
				if (state > 0) {
					if (RCMain.getRCMain().getClient().isSSLEncrypted()) {
						setSSLStatusBar(true, true);
					} else {
						setSSLStatusBar(true, false);
					}
				}

				// set the toolbar buttons
				if (state > 0) {
					setLogInOutButtons(true);
					// reset the title of MyTorrents once Logged in
					RCMain.getRCMain().getDisplay().asyncExec(
							new SWTSafeRunnable() {

								@Override
								public void runSafe () {
									String userLoggedIn = RCMain.getRCMain()
											.getClient().getUsername();
									if (myTorrents != null
											|| !myTorrents.isDisposed()) {
										if (bSingleUserMode) {

											if (RCMain
													.getRCMain()
													.getProperties()
													.getPropertyAsBoolean(
															"mainwindow.showHost")) {
												myTorrents
														.setText(I18N
																.translate(PFX
																		+ "mytorrents.tabtitle.all.text1")
																+ " "
																+ RCMain
																		.getRCMain()
																		.getClient()
																		.getServer()
																		.getHost());
											} else {
												myTorrents
														.setText(I18N
																.translate(PFX
																		+ "mytorrents.tabtitle.all.text2"));
											}

										} else if (userLoggedIn != null) {
											if (RCMain
													.getRCMain()
													.getProperties()
													.getPropertyAsBoolean(
															"mainwindow.showHost")) {
												myTorrents
														.setText(userLoggedIn
																+ I18N
																		.translate(PFX
																				+ "mytorrents.tabtitle.user")
																+ ": "
																+ RCMain
																		.getRCMain()
																		.getClient()
																		.getServer()
																		.getHost());
											} else {
												myTorrents
														.setText(userLoggedIn
																+ I18N
																		.translate(PFX
																				+ "mytorrents.tabtitle.user"));
											}
										} else {
											myTorrents
													.setText(I18N
															.translate(PFX
																	+ "mytorrents.tabtitle.fallback"));
										}
									}

								}

							});
				} else {
					setLogInOutButtons(false);
				}
			}
		};

		RCMain.getRCMain().getClient().addConnectionListener(cl);

		final DownloadManagerListener dml = new DownloadManagerListener() {

			public void downloadAdded (final Download download) {
				try {
					if (DOWNLOAD_MANAGER_SHELL == null
							|| DOWNLOAD_MANAGER_SHELL.isDisposed()) {
						RCMain.getRCMain().getClient().getDownloadManager()
								.removeListener(this);
						return;
					}

					semaphore.acquire();
					RCMain.getRCMain().getDisplay().syncExec(
							new SWTSafeRunnable() {
								@Override
								public void runSafe () {
									if (download.getState() == Download.ST_SEEDING
											|| download.isComplete()) {
										if (!seedsMap.containsKey(download
												.getHash())) {
											SeedContainer sc = new SeedContainer(
													download);
											seedsMap
													.put(download.getHash(), sc);
										}
									} else {
										if (!downloadsMap.containsKey(download
												.getHash())) {
											DownloadContainer dc = new DownloadContainer(
													download);
											downloadsMap.put(
													download.getHash(), dc);
										}
									}
									redrawTables();
								};
							});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					semaphore.release();
				}
				download.addDownloadListener(dlL);
			}

			public void downloadRemoved (Download download) {
				if (downloadsMap.containsKey(download.getHash())) {
					downloadsMap.remove(download.getHash());
					redrawTables();
				} else if (seedsMap.containsKey(download.getHash())) {
					seedsMap.remove(download.getHash());
					redrawTables();
				}
			}
		};

		RCMain.getRCMain().getClient().getDownloadManager().addListener(dml);

		final ClientUpdateListener cul = new ClientUpdateListener() {

			public void update (long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_LIST_TRANSFERS) != 0) {
					Download[] downloads = RCMain.getRCMain().getClient()
							.getDownloadManager().getSortedDownloads();
					for (int i = 0; i < downloads.length; i++) {
						if (!downloadsMap.containsKey(downloads[i].getHash())
								&& !seedsMap
										.containsKey(downloads[i].getHash())) {
							if (downloads[i].getState() == Download.ST_SEEDING
									|| downloads[i].isComplete()) {
								SeedContainer sc = new SeedContainer(
										downloads[i], seedsTable, SWT.NULL);
								seedsMap.put(downloads[i].getHash(), sc);
								redrawTables = true;
							} else {
								DownloadContainer dc = new DownloadContainer(
										downloads[i], downloadsTable,
										SWT.BORDER);
								downloadsMap.put(downloads[i].getHash(), dc);
								redrawTables = true;
							}
						} else {
							// is present in one of the maps.. so find it and
							// update it
							if (downloadsMap
									.containsKey(downloads[i].getHash())) {
								if (downloads[i].getState() == Download.ST_SEEDING) {
									downloadsMap.get(downloads[i].getHash())
											.removeFromTable();
									downloadsMap.remove(downloads[i].getHash());
									if (!seedsMap.containsKey(downloads[i]
											.getHash())) {
										SeedContainer sc = new SeedContainer(
												downloads[i], seedsTable,
												SWT.NULL);
										seedsMap
												.put(downloads[i].getHash(), sc);
									}
								}
								DownloadContainer dc = downloadsMap
										.get(downloads[i].getHash());
								if (dc != null) {
									dc.update(false);
								}
							} else if (seedsMap.containsKey(downloads[i]
									.getHash())) {
								SeedContainer sc = seedsMap.get(downloads[i]
										.getHash());
								sc.update(false);
							}
							redrawTables = true;
						}
					}
				}

				if ((updateSwitches & Constants.UPDATE_USERS) != 0) {
					setUserButton();
				}
				if (redrawTables) {
					redrawTables();
					redrawTables = false;
				}
			}
		};

		RCMain.getRCMain().getClient().addClientUpdateListener(cul);

		final ParameterListener pl = new ParameterListener() {

			public void azParameter (String key, String value, int type) {
				if (key
						.equalsIgnoreCase(RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC)) {
					azureusUpload = value;
					refreshStatusBar();
				}

				if (key
						.equalsIgnoreCase(RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC)) {
					azureusDownload = value;
					refreshStatusBar();
				}

			}

			public void pluginParameter (String key, String value, int type) {
				if (key.equalsIgnoreCase("singleUserMode")) {
					bSingleUserMode = Boolean.parseBoolean(value);
					if (bSingleUserMode) {
						setStatusBarText(
								I18N
										.translate("rcmain.mainwindow.statusbar.connectedSingleUser"),
								SWT.COLOR_DARK_GREEN);
					}

				}

			}

			public void coreParameter (String key, String value, int type) {

			}
		};

		RCMain.getRCMain().getClient().addParameterListener(pl);

		// ------------CtabFolder initialization here----------\\
		tabFolder = new CTabFolder(DOWNLOAD_MANAGER_SHELL, SWT.BORDER);
		tabFolder.setLayout(new GridLayout(1, false));
		gridData = new GridData(GridData.FILL_BOTH);
		// gridData.heightHint = 300;
		gridData.horizontalSpan = 2;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gridData);
		tabFolder.setSimple(false);

		// Set the tabs to the OS color scheme
		Display display = RCMain.getRCMain().getDisplay();
		tabFolder.setSelectionForeground(display
				.getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		tabFolder.setSelectionBackground(new Color[] {
				display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND),
				display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT) },
				new int[] { 100 }, true);

		// add in the non closable My torrents ctabitem
		myTorrents = new CTabItem(tabFolder, SWT.NONE);
		myTorrents.setToolTipText("Click here to resort the tables");
		tabFolder.addMouseListener(new MouseListener() {

			public void mouseDoubleClick (MouseEvent arg0) {

			}

			public void mouseDown (MouseEvent arg0) {
			}

			public void mouseUp (MouseEvent arg0) {
				CTabItem tab = tabFolder.getSelection();
				if (tab.equals(myTorrents)) {
					redrawTables();
				}
			}

		});

		// ---------------------- Sash------------------------\\
		sash = new SashForm(tabFolder, SWT.VERTICAL);
		sash.setLayout(new GridLayout());

		myTorrents.setControl(sash);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 300;
		gridData.horizontalSpan = 2;
		sash.setLayoutData(gridData);

		// --------------------- Set Up Downloads Table ---------------------

		downloadsTable = new Table(sash, SWT.VIRTUAL | SWT.MULTI
				| SWT.FULL_SELECTION | SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		downloadsTable.setLayoutData(gridData);
		downloadsTable.setHeaderVisible(true);
		downloadsTable.setData("comparator", Container.getComparators().get(
				RemoteConstants.ST_POSITION));
		downloadsTable.setData("sort", Boolean.toString(true));

		createTableColumns(downloadsTable, DownloadContainer.getColumns());

		downloadsTable.addListener(SWT.SetData, new Listener() {
			@SuppressWarnings("unchecked")
			public void handleEvent (Event event) {
				downloadsArray = downloadsMap.values().toArray(emptyArray);
				if (downloadsArray.length == 0) {
					return;
				}
				Comparator<Container> data = (Comparator<Container>) downloadsTable
						.getData("comparator");

				if (!Boolean.parseBoolean((String) downloadsTable
						.getData("sort"))) {
					data = Collections.reverseOrder(data);
				}

				// if(downloadsArray.length > 1)
				Arrays.sort(downloadsArray, data);

				TableItem item = (TableItem) event.item;
				int index = downloadsTable.indexOf(item);
				if (index + 1 > downloadsArray.length) {
					return;
				}
				// System.out.println("Table count: " +
				// downloadsTable.getItemCount() + " Index: " + index + " |
				// downloadsArray: " + downloadsArray.length + " | downloadsMap:
				// " + downloadsMap.size());
				DownloadContainer container = (DownloadContainer) downloadsArray[index];
				container.setTableItem(item);
				if (index % 2 != 0) {
					item.setBackground(ColorUtilities.getBackgroundColor());
				}
				if (container.getTableItem() != null) {
					container.update(true);
				}
			}
		});

		// add menu to downloadsTable for downloads management
		addDownloadManagerMenu(downloadsTable);

		// Selection listener for the table
		downloadsTable.addListener(SWT.Selection, new Listener() {

			public void handleEvent (Event arg0) {
				TableItem[] items = downloadsTable.getSelection();
				if (items == null) {
					return;
				}
				if (items.length == 1) {
					// Single line selection
					Container container = (Container) items[0].getData();
					Download download = container.getDownload();
					setTorrentMoveButtons(false, false, false, false);
					int index = download.getPosition();
					if (index == 1 && downloadsTable.getItemCount() == 1) {
						setTorrentMoveButtons(false, true, true, false);
					} else if (index == 1) {
						setTorrentMoveButtons(false, true, true, true);
					} else if (index == downloadsTable.getItemCount() - 1) {
						setTorrentMoveButtons(true, true, true, false);
					} else {
						setTorrentMoveButtons(true, true, true, true);
					}

					// torrent control
					if (download.getState() == Download.ST_QUEUED
							|| download.getState() == Download.ST_DOWNLOADING) {
						setToolBarTorrentIcons(false, true, true);
					} else if (download.getState() == Download.ST_STOPPED) {
						setToolBarTorrentIcons(true, false, true);
					} else if (download.getState() == Download.ST_ERROR) {
						setToolBarTorrentIcons(true, true, true);
					} else {
						setToolBarTorrentIcons(false, false, false);
					}

				} else if (items.length > 1) {
					// Multiple selection here
					setTorrentMoveButtons(false, false, false, false);
					setToolBarTorrentIcons(true, true, true);

				}

			}

		});

		downloadsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown (MouseEvent e) {
				if (e.button == 1) {
					if (downloadsTable.getItem(new Point(e.x, e.y)) == null) {
						downloadsTable.deselectAll();
						setTorrentMoveButtons(false, false, false, false);
						setToolBarTorrentIcons(false, false, false);
					}

				}
			}
		});

		downloadsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick (MouseEvent e) {
				if (e.button == 1) {
					if (downloadsTable.getItem(new Point(e.x, e.y)) == null) {
						downloadsTable.deselectAll();
						setTorrentMoveButtons(false, false, false, false);
					} else {
						TableItem[] items = downloadsTable.getSelection();
						if (items.length != 1) {
							return;
						}
						Container dc = (Container) items[0].getData();
						// check for dupicate happens IN the tab open now
						TorrentDetailsTab.open(tabFolder, dc.getDownload());
					}

				}
			}
		});

		createDragDrop(downloadsTable);

		// ----------------------Seeds Table --------------------------\\
		seedsTable = new Table(sash, SWT.VIRTUAL | SWT.MULTI
				| SWT.FULL_SELECTION | SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		seedsTable.setLayoutData(gridData);
		seedsTable.setHeaderVisible(true);
		seedsTable.setData("comparator", Container.getComparators().get(
				RemoteConstants.ST_POSITION));
		seedsTable.setData("sort", Boolean.toString(true));

		createTableColumns(seedsTable, SeedContainer.getColumns());

		// Add menu to seedsTable for download management
		addDownloadManagerMenu(seedsTable);

		// Listeners for seedsTable

		seedsTable.addListener(SWT.SetData, new Listener() {

			@SuppressWarnings("unchecked")
			public void handleEvent (Event event) {
				seedsArray = seedsMap.values().toArray(emptyArray);
				if (seedsArray.length == 0) {
					return;
				}

				Comparator<Container> data = (Comparator<Container>) seedsTable
						.getData("comparator");

				if (!Boolean.parseBoolean((String) seedsTable.getData("sort"))) {
					data = Collections.reverseOrder(data);
				}

				// if(seedsArray.length > 1)
				Arrays.sort(seedsArray, data);

				TableItem item = (TableItem) event.item;
				int index = seedsTable.indexOf(item);
				if (index + 1 > seedsArray.length) {
					return;
				}
				SeedContainer container = (SeedContainer) seedsArray[index];
				container.setTableItem(item);

				if (index % 2 != 0) {
					item.setBackground(ColorUtilities.getBackgroundColor());
				}

				if (container.getTableItem() != null) {
					container.update(true);
				}
			}
		});

		seedsTable.addListener(SWT.Selection, new Listener() {

			public void handleEvent (Event arg0) {
				TableItem[] items = seedsTable.getSelection();
				if (items == null) {
					return;
				}
				if (items.length == 1) {
					Container container = (Container) items[0].getData();
					Download download = container.getDownload();
					int index = download.getPosition();
					// Single line selection
					setTorrentMoveButtons(false, false, false, false);
					if (index == 1 && seedsTable.getItemCount() == 1) {
						setTorrentMoveButtons(false, true, true, false);
					} else if (index == 1) {
						setTorrentMoveButtons(false, true, true, true);
					} else if (index == seedsTable.getItemCount() - 1) {
						setTorrentMoveButtons(true, true, true, false);
					} else {
						setTorrentMoveButtons(true, true, true, true);
					}

					// Torrent Control
					if (download.getState() == Download.ST_QUEUED
							|| download.getState() == Download.ST_SEEDING) {
						setToolBarTorrentIcons(false, true, true);
					} else if (download.getState() == Download.ST_STOPPED) {
						setToolBarTorrentIcons(true, false, true);
					}
				} else if (items.length > 1) {
					// Multiple selection here
					setTorrentMoveButtons(false, false, false, false);
					setToolBarTorrentIcons(true, true, true);

				}

			}

		});

		seedsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown (MouseEvent e) {
				if (e.button == 1) {
					if (seedsTable.getItem(new Point(e.x, e.y)) == null) {
						seedsTable.deselectAll();
						setTorrentMoveButtons(false, false, false, false);
						setToolBarTorrentIcons(false, false, false);
					}

				}
			}
		});

		seedsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick (MouseEvent e) {
				if (e.button == 1) {
					if (seedsTable.getItem(new Point(e.x, e.y)) == null) {
						seedsTable.deselectAll();
						setTorrentMoveButtons(false, false, false, false);
					} else {
						TableItem[] items = seedsTable.getSelection();
						if (items.length > 1) {
							return;
						}
						Container sc = (Container) items[0].getData();
						// check for dupicate happens IN the tab open now
						TorrentDetailsTab.open(tabFolder, sc.getDownload());
					}

				}
			}
		});

		createDragDrop(seedsTable);

		ExtendedProperties properties = RCMain.getRCMain().getProperties();

		sash.setWeights(new int[] { properties.getPropertyAsInt("dms.sash.0"),
				properties.getPropertyAsInt("dms.sash.1") });

		String userLoggedIn = RCMain.getRCMain().getClient().getUsername();
		if (bSingleUserMode) {
			myTorrents.setText(I18N.translate(PFX
					+ "mytorrents.tabtitle.all.text2"));
		} else if (userLoggedIn != null) {
			myTorrents.setText(userLoggedIn
					+ I18N.translate(PFX + "mytorrents.tabtitle.user"));
		} else {
			myTorrents.setText(I18N.translate(PFX
					+ "mytorrents.tabtitle.fallback"));
		}

		// ------------bottom status bar---------------\\
		final int borderFlag = (Utilities.isOSX) ? SWT.NONE : SWT.SHADOW_IN;

		statusbarComp = new Composite(DOWNLOAD_MANAGER_SHELL, borderFlag);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 1;
		statusbarComp.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		statusbarComp.setLayoutData(gridData);

		statusBarText = new CLabel(statusbarComp, borderFlag);
		statusBarText.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);
		statusBarText.setLayoutData(gridData);

		statusDown = new CLabelPadding(statusbarComp, borderFlag);
		statusDown.setText("0B/s");
		statusDown.setImage(ImageRepository.getImage("statusbar_down"));
		statusDown.setToolTipText(I18N.translate(PFX
				+ "statusbar.download.tooltiptext"));

		statusUp = new CLabelPadding(statusbarComp, borderFlag);
		statusUp.setText("0B/s");
		statusUp.setImage(ImageRepository.getImage("statusbar_up"));
		statusUp.setToolTipText(I18N.translate(PFX
				+ "statusbar.upload.tooltiptext"));

		connectionStatusIcon = new CLabelPadding(statusbarComp, borderFlag);
		connectionStatusIcon.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent (Event arg0) {
				if (RCMain.getRCMain().connected()) {
					ServerDetailsTab.open(tabFolder);
				}
			}
		});
		setConnectionStatusBar(0);

		sslStatusIcon = new CLabelPadding(statusbarComp, borderFlag);
		sslStatusIcon.setImage(ImageRepository.getImage("ssl_disabled"));
		sslStatusIcon.setEnabled(false);
		sslStatusIcon.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent (Event arg0) {
				if (RCMain.getRCMain().connected()) {
					ServerDetailsTab.open(tabFolder);
				}
			}
		});

		setSSLStatusBar(false, false);

		statusbarComp.pack();

		// Double click listener for down and up
		Listener speedUpdate = new Listener() {
			public void handleEvent (Event arg0) {
				Client client = RCMain.getRCMain().getClient();
				client.transactionStart();
				client
						.sendGetAzParameter(
								RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
								RemoteConstants.PARAMETER_INT);
				client
						.sendGetAzParameter(
								RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
								RemoteConstants.PARAMETER_INT);
				client.transactionCommit();
			}
		};

		statusDown.addListener(SWT.MouseDoubleClick, speedUpdate);
		statusUp.addListener(SWT.MouseDoubleClick, speedUpdate);

		// menus
		final Menu menuUpSpeed = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.POP_UP);
		menuUpSpeed.addListener(SWT.Show, new Listener() {
			public void handleEvent (Event e) {
				MenuItem[] items = menuUpSpeed.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				// final String config_param =
				// TransferSpeedValidator.getActiveUploadParameter(globalManager);

				int upLimit;

				if (azureusUpload.equalsIgnoreCase("N/S")
						|| azureusUpload.equalsIgnoreCase("0")) {
					upLimit = 0;
				} else {
					upLimit = Integer.valueOf(azureusUpload);
				}

				MenuItem item = new MenuItem(menuUpSpeed, SWT.RADIO);
				item.setText("Unlimited");
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						RCMain
								.getRCMain()
								.getClient()
								.sendSetAzParameter(
										RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
										"0", RemoteConstants.PARAMETER_INT);
						azureusUpload = "0";
						refreshStatusBar();
					}
				});
				if (upLimit == 0) {
					item.setSelection(true);
				}

				final SelectionListener speedChangeListener = new SelectionListener() {
					public void widgetSelected (SelectionEvent e) {
						if (((MenuItem) e.widget).getSelection()) {
							String value = String.valueOf(((MenuItem) e.widget)
									.getData("speed"));
							RCMain
									.getRCMain()
									.getClient()
									.sendSetAzParameter(
											RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
											value,
											RemoteConstants.PARAMETER_INT);
							azureusUpload = value;
							refreshStatusBar();
						}
					}

					public void widgetDefaultSelected (SelectionEvent arg0) {
					}
				};

				int iRel = 0;
				for (int i = 0; i < 12; i++) {
					int[] iAboveBelow;
					if (iRel == 0) {
						iAboveBelow = new int[] { upLimit };
					} else {
						iAboveBelow = new int[] { upLimit - iRel,
								upLimit + iRel };
					}

					for (int j = 0; j < iAboveBelow.length; j++) {
						if (iAboveBelow[j] >= 5) {
							item = new MenuItem(menuUpSpeed, SWT.RADIO,
									(j == 0) ? 1 : menuUpSpeed.getItemCount());
							item.setText(DisplayFormatters
									.formatByteCountToKiBEtcPerSec(
											iAboveBelow[j] * 1024, true));
							item.setData("speed", new Long(iAboveBelow[j]));
							item.addSelectionListener(speedChangeListener);

							if (upLimit == iAboveBelow[j]) {
								item.setSelection(true);
							}
						}
					}

					iRel += (iRel >= 50) ? 50 : (iRel >= 10) ? 10
							: (iRel >= 5) ? 5 : (iRel >= 2) ? 3 : 1;
				}

			}
		});
		statusUp.setMenu(menuUpSpeed);

		final Menu menuDownSpeed = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.POP_UP);
		menuDownSpeed.addListener(SWT.Show, new Listener() {
			public void handleEvent (Event e) {
				MenuItem[] items = menuDownSpeed.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				int downLimit;
				if (azureusDownload.equalsIgnoreCase("N/S")
						|| azureusDownload.equalsIgnoreCase("0")) {
					downLimit = 0;
				} else {
					downLimit = Integer.valueOf(azureusDownload);
				}
				final boolean unlim = (downLimit == 0);
				if (unlim) {
					downLimit = 275;
				}

				MenuItem item = new MenuItem(menuDownSpeed, SWT.RADIO);
				item.setText("Unlimited");
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						RCMain
								.getRCMain()
								.getClient()
								.sendSetAzParameter(
										RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
										"0", RemoteConstants.PARAMETER_INT);
						azureusDownload = "0";
						refreshStatusBar();
					}
				});
				if (unlim) {
					item.setSelection(true);
				}
				final SelectionListener speedChangeListener = new SelectionListener() {
					public void widgetSelected (SelectionEvent e) {
						if (((MenuItem) e.widget).getSelection()) {
							String value = String.valueOf(((MenuItem) e.widget)
									.getData("speed"));
							RCMain
									.getRCMain()
									.getClient()
									.sendSetAzParameter(
											RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
											value,
											RemoteConstants.PARAMETER_INT);
							azureusDownload = value;
							refreshStatusBar();
						}

					}

					public void widgetDefaultSelected (SelectionEvent arg0) {
					}
				};

				int iRel = 0;
				for (int i = 0; i < 12; i++) {
					int[] iAboveBelow;
					if (iRel == 0) {
						iAboveBelow = new int[] { downLimit };
					} else {
						iAboveBelow = new int[] { downLimit - iRel,
								downLimit + iRel };
					}
					for (int j = 0; j < iAboveBelow.length; j++) {
						if (iAboveBelow[j] >= 5) {
							item = new MenuItem(menuDownSpeed, SWT.RADIO,
									(j == 0) ? 1 : menuDownSpeed.getItemCount());
							item.setText(DisplayFormatters
									.formatByteCountToKiBEtcPerSec(
											iAboveBelow[j] * 1024, true));
							item.setData("speed", new Long(iAboveBelow[j]));
							item.addSelectionListener(speedChangeListener);
							item.setSelection(!unlim
									&& downLimit == iAboveBelow[j]);
						}
					}

					iRel += (iRel >= 50) ? 50 : (iRel >= 10) ? 10
							: (iRel >= 5) ? 5 : (iRel >= 2) ? 3 : 1;
				}

			}
		});
		statusDown.setMenu(menuDownSpeed);

		// --------------------Center/Open Shell-------------------\\

		DOWNLOAD_MANAGER_SHELL.addShellListener(new ShellListener() {

			public void shellActivated (ShellEvent arg0) {
			}

			public void shellClosed (ShellEvent event) {
				logger.warn("Closing Shell!!!");

				// Check to see the configs that we are coming in with
				boolean sendMainWindowToTray = RCMain.getRCMain()
						.getProperties().getPropertyAsBoolean("tray.exit");
				boolean confirmExit = RCMain.getRCMain().getProperties()
						.getPropertyAsBoolean("confirm.exit");

				// -----First we need to save everything for the user---\\

				// Saving user preferences for the shell and sash
				int size_x = DOWNLOAD_MANAGER_SHELL.getSize().x;
				int size_y = DOWNLOAD_MANAGER_SHELL.getSize().y;

				int position_x = DOWNLOAD_MANAGER_SHELL.getLocation().x;
				int position_y = DOWNLOAD_MANAGER_SHELL.getLocation().y;
				ExtendedProperties properties = RCMain.getRCMain()
						.getProperties();

				if (!sash.isDisposed()) {
					int[] weights = sash.getWeights();
					properties.setProperty("dms.sash.0", weights[0]);
					properties.setProperty("dms.sash.1", weights[1]);
				}
				properties.setProperty("dms.size.x", size_x);
				properties.setProperty("dms.size.y", size_y);
				properties.setProperty("dms.position.x", position_x);
				properties.setProperty("dms.position.y", position_y);

				boolean b_ok = true;

				// save the downloadsTable column widths
				if (!downloadsTable.isDisposed()) {
					b_ok = true;
					TableColumn[] columns = downloadsTable.getColumns();
					List<Integer> dl_column_list = new ArrayList<Integer>();
					for (TableColumn column : columns) {
						int colWidth = column.getWidth();
						// if it is 0, somthing is wrong.. break out and don't
						// save
						if (colWidth == 0) {
							b_ok = false;
							break;
						} else {
							dl_column_list.add(colWidth);
						}
					}
					if (b_ok) {
						properties.setProperty("downloadsTable.columns.widths",
								EncodingUtil.IntListToString(dl_column_list));
					}
				}

				// save the seedsTable column widths
				if (!seedsTable.isDisposed()) {
					b_ok = true;
					TableColumn[] seed_columns = seedsTable.getColumns();
					List<Integer> seed_column_list = new ArrayList<Integer>();
					for (TableColumn column : seed_columns) {
						int colWidth = column.getWidth();
						// if it is 0, somthing is wrong.. break out and don't
						// save
						if (colWidth == 0) {
							b_ok = false;
							break;
						} else {
							seed_column_list.add(colWidth);
						}

					}
					if (b_ok) {
						properties.setProperty("seedsTable.columns.widths",
								EncodingUtil.IntListToString(seed_column_list));
					}
				}

				// Save Everything!
				RCMain.getRCMain().saveConfig();

				if (!sendMainWindowToTray) { // This means a true exit from
					// the program

					// -----Now check if the user wants a confirmation---\\

					if (confirmExit && !RCMain.getRCMain().isTerminated()) {
						MessageBox messageBox = new MessageBox(
								DOWNLOAD_MANAGER_SHELL, SWT.ICON_QUESTION
										| SWT.OK | SWT.CANCEL);
						messageBox.setText("Confirm Exit");
						messageBox
								.setMessage("Are you sure you wish to exit AzSMRC entirely?");
						int response = messageBox.open();
						switch (response) {
						case SWT.OK:
							event.doit = true;
							RCMain.getRCMain().close();
							break;
						case SWT.CANCEL:
							// user wants to cancel everything.. so just break
							// out
							event.doit = false;
							break;
						}
					} else {
						RCMain.getRCMain().close();
					}
					// -----END of real exit -- everything below here keeps the
					// program open---\\
				} else { // We just need to minimize everything here as we
					// are not really exiting
					// event.doit = false;
					RCMain.getRCMain().updateTimer(false);
					RCMain.getRCMain().getClient().removeGlobalStatsListener(
							sul);
					RCMain.getRCMain().getClient().removeConnectionListener(cl);
					RCMain.getRCMain().getClient().getDownloadManager()
							.removeListener(dml);
					RCMain.getRCMain().getClient().removeClientUpdateListener(
							cul);
					RCMain.getRCMain().getClient().removeParameterListener(pl);
					DOWNLOAD_MANAGER_SHELL.dispose();
					DOWNLOAD_MANAGER_SHELL = null;
					RCMain.getRCMain().nullMainWindow();

					// DOWNLOAD_MANAGER_SHELL.setMinimized(true);
					// DOWNLOAD_MANAGER_SHELL.setVisible(false);
				}
			}

			public void shellDeactivated (ShellEvent arg0) {

			}

			public void shellDeiconified (ShellEvent arg0) {
				RCMain.getRCMain().updateTimer(true);
				RCMain.getRCMain().getClient().getDownloadManager()
						.update(true);
			}

			public void shellIconified (ShellEvent arg0) {
				if (RCMain.getRCMain().getProperties().getPropertyAsBoolean(
						"tray.minimize")) {
					DOWNLOAD_MANAGER_SHELL.setVisible(false);
					RCMain.getRCMain().updateTimer(false);
				}

			}

		});

		// check to see if the console is auto open and open it up if it is
		if (properties.getPropertyAsBoolean("auto_console", false)) {
			ConsoleTab.open(tabFolder, false);
		}

		tabFolder.setSelection(myTorrents);

		// check if connected, and if so, send the correct requests
		initializeConnection();

		// open shell
		properties = RCMain.getRCMain().getProperties();
		int size_x = properties.getPropertyAsInt("dms.size.x");
		int size_y = properties.getPropertyAsInt("dms.size.y");
		int position_x = properties.getPropertyAsInt("dms.position.x");
		int position_y = properties.getPropertyAsInt("dms.position.y");
		// System.out.println("Open: " + size_x + " : "+size_y + " : " +
		// position_x + " : " + position_y);
		DOWNLOAD_MANAGER_SHELL.setSize(size_x, size_y);
		DOWNLOAD_MANAGER_SHELL.setLocation(position_x, position_y);
		DOWNLOAD_MANAGER_SHELL.open();

		try {
			// createDropTarget(DOWNLOAD_MANAGER_SHELL);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (RCMain.getRCMain().getRunTime()
				- properties.getPropertyAsLong("lastDonationQuestion") > DONATION_INTERVAL) {
			properties.setProperty("lastDonationQuestion", RCMain.getRCMain()
					.getRunTime());
			// TODO MARC Popup donation tab
		}

		if (RCMain.getRCMain().connected()) {
			RCMain.getRCMain().updateTimer(true);
		}
	}

	public void close_shell () {
		if (DOWNLOAD_MANAGER_SHELL != null
				&& !DOWNLOAD_MANAGER_SHELL.isDisposed()) {
			DOWNLOAD_MANAGER_SHELL.close();
		}

	}

	public Shell getShell () {
		return DOWNLOAD_MANAGER_SHELL;
	}

	public boolean isCollapsed () {
		return COLLAPSED;
	}

	public void setCollapsed (boolean state) {
		COLLAPSED = state;
	}

	/**
	 * 
	 * @param connection -- int -- 0 for no connection, 1 for connecting, 2 for
	 *            connected
	 */
	public void setConnectionStatusBar (final int connection) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null) {
			return;
		}
		display.syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (connectionStatusIcon == null
						|| connectionStatusIcon.isDisposed()) {
					return;
				}
				if (connection == 0) {
					connectionStatusIcon.setImage(ImageRepository
							.getImage("connect_no"));
					connectionStatusIcon
							.setToolTipText("Not Connected to server");
				} else if (connection == 1) {

					connectionStatusIcon.setImage(ImageRepository
							.getImage("connect_creating"));
					connectionStatusIcon
							.setToolTipText("Attempting to connect to server");
				} else if (connection == 2) {
					connectionStatusIcon.setImage(ImageRepository
							.getImage("connect_established"));
					connectionStatusIcon
							.setToolTipText("Connected to server\nDouble-Click for Server Details");
				}
			}
		});
	}

	public void setSSLStatusBar (final boolean benabled, final boolean buse_ssl) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (benabled) {
					if (buse_ssl) {
						sslStatusIcon.setEnabled(true);
						sslStatusIcon.setImage(ImageRepository
								.getImage("ssl_enabled"));
						sslStatusIcon
								.setToolTipText("SSL Enabled\nDouble-Click for Server Details");
					} else {
						sslStatusIcon.setEnabled(true);
						sslStatusIcon.setImage(ImageRepository
								.getImage("ssl_disabled"));
						sslStatusIcon
								.setToolTipText("SSL Disabled\nDouble-Click for Server Details");
					}
				} else {
					sslStatusIcon.setImage(ImageRepository
							.getImage("ssl_disabled"));
					sslStatusIcon.setEnabled(false);
				}
			}
		});
	}

	public void setTorrentMoveButtons (final boolean bTop, final boolean bUp,
			final boolean bDown, final boolean bBottom) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (top.isEnabled() != bTop) {
					top.setEnabled(bTop);
				}
				if (up.isEnabled() != bUp) {
					up.setEnabled(bUp);
				}
				if (down.isEnabled() != bDown) {
					down.setEnabled(bDown);
				}
				if (bottom.isEnabled() != bBottom) {
					bottom.setEnabled(bBottom);
				}
			}
		});
	}

	public void setUserButton () {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				User user = RCMain.getRCMain().getClient().getUserManager()
						.getActiveUser();
				if (user != null) {
					manage_users.setEnabled(true);
				}
			}
		});
	}

	public void setLogInOutButtons (final boolean bLoggedIn) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (bLoggedIn) {
					login.setEnabled(false);
					menuLogin.setEnabled(false);
					menuRestartAzureus.setEnabled(true);
					menuServerDetails.setEnabled(true);
					menuServerUpdate.setEnabled(true);
					menuAddByFile.setEnabled(true);
					menuAddbyURL.setEnabled(true);
					menuLogout.setEnabled(true);
					logout.setEnabled(true);
					quickconnect.setEnabled(false);
					menuQuickconnect.setEnabled(false);
					refresh.setEnabled(true);
					preferences.setEnabled(true);
					addTorrent_by_file.setEnabled(true);
					addTorrent_by_url.setEnabled(true);
					pauseAll.setEnabled(true);
					resumeAll.setEnabled(true);
				} else {
					login.setEnabled(true);
					menuLogin.setEnabled(true);
					menuLogout.setEnabled(false);
					menuRestartAzureus.setEnabled(false);
					menuServerDetails.setEnabled(false);
					menuServerUpdate.setEnabled(false);
					menuAddByFile.setEnabled(false);
					menuAddbyURL.setEnabled(false);
					logout.setEnabled(false);
					quickconnect.setEnabled(true);
					menuQuickconnect.setEnabled(true);
					refresh.setEnabled(false);
					preferences.setEnabled(true);
					addTorrent_by_file.setEnabled(false);
					addTorrent_by_url.setEnabled(false);
					pauseAll.setEnabled(false);
					resumeAll.setEnabled(false);
					manage_users.setEnabled(false);
					setTorrentMoveButtons(false, false, false, false);
					setToolBarTorrentIcons(false, false, false);
					setSSLStatusBar(false, false);
					setConnectionStatusBar(0);
					azureusDownload = "N/S";
					downSpeed = 0;
					azureusUpload = "N/S";
					upSpeed = 0;
					refreshStatusBar();
				}
			}
		});
	}

	public void redrawColumnsonTables () {
		// setRequestedItems();
		TableColumn[] columns = downloadsTable.getColumns();
		for (TableColumn column : columns) {
			column.dispose();
		}
		createTableColumns(downloadsTable, DownloadContainer.getColumns());
		for (Container dc : downloadsMap.values()) {
			dc.deleteTableItem();
		}

		downloadsTable.clearAll();

		TableColumn[] columns_seeds = seedsTable.getColumns();
		for (TableColumn column : columns_seeds) {
			column.dispose();
		}
		createTableColumns(seedsTable, SeedContainer.getColumns());
		for (Container sc : seedsMap.values()) {
			sc.deleteTableItem();
		}
		seedsTable.clearAll();
		// seedsMap.clear();
		// seedsArray = seedsMap.values().toArray(emptyArray);

		// Be sure to save down the column widths
		RCMain.getRCMain().getMainWindow().saveColumnWidthsToPreferencesFile();

		// readd downloads
		redrawTables();
	}

	private void addDownloadManagerMenu (final Table table) {
		final Menu menu = new Menu(table);

		final MenuItem queue = new MenuItem(menu, SWT.PUSH);
		queue.setText("Queue");
		queue.setImage(ImageRepository.getImage("menu_queue"));
		queue.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {
						Container container = (Container) item.getData();
						container.getDownload().restart();
					}
					RCMain.getRCMain().getClient().transactionCommit();
				}
			}
		});

		final MenuItem forceStart = new MenuItem(menu, SWT.CHECK);
		forceStart.setText("Force Start");
		forceStart.setImage(ImageRepository.getImage("menu_force_start"));
		forceStart.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {
						Container container = (Container) item.getData();
						container.getDownload().setForceStart(
								!container.getDownload().isForceStart());
					}
					RCMain.getRCMain().getClient().transactionCommit();
				}
			}
		});

		final MenuItem stop = new MenuItem(menu, SWT.PUSH);
		stop.setText("Stop");
		stop.setImage(ImageRepository.getImage("menu_stop"));
		stop.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {
						Container container = (Container) item.getData();
						container.getDownload().stop();
					}
					RCMain.getRCMain().getClient().transactionCommit();
				}
			}
		});

		final MenuItem remove = new MenuItem(menu, SWT.PUSH);
		remove.setText("Remove");
		remove.setImage(ImageRepository.getImage("menu_remove"));
		remove.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {
						Container container = (Container) item.getData();

						// Close any tabs that have this download name
						CTabItem[] tabs = tabFolder.getItems();
						for (CTabItem tab : tabs) {
							if (tab.getText().equalsIgnoreCase(
									container.getDownload().getName())) {
								tab.dispose();
							}
						}

						container.getDownload().remove();
						if (seedsMap.containsKey(container.getDownload()
								.getHash())) {
							seedsMap.remove(container.getDownload().getHash());
						} else if (downloadsMap.containsKey(container
								.getDownload().getHash())) {
							downloadsMap.remove(container.getDownload()
									.getHash());
						}

						redrawTables();
					}
					RCMain.getRCMain().getClient().transactionCommit();

				}
			}
		});

		final MenuItem removeAnd = new MenuItem(menu, SWT.CASCADE);
		removeAnd.setText("Remove and");
		removeAnd.setImage(ImageRepository.getImage("menu_remove"));
		Menu removeAndMenu = new Menu(removeAnd);

		final MenuItem downloadDeleteTorrent = new MenuItem(removeAndMenu,
				SWT.PUSH);
		downloadDeleteTorrent.setText("Delete Torrent File");
		downloadDeleteTorrent.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {

						Container container = (Container) item.getData();

						// Close any tabs that have this download name
						CTabItem[] tabs = tabFolder.getItems();
						for (CTabItem tab : tabs) {
							if (tab.getText().equalsIgnoreCase(
									container.getDownload().getName())) {
								tab.dispose();
							}
						}

						container.getDownload().remove(true, false);
						if (seedsMap.containsKey(container.getDownload()
								.getHash())) {
							seedsMap.remove(container.getDownload().getHash());
						} else if (downloadsMap.containsKey(container
								.getDownload().getHash())) {
							downloadsMap.remove(container.getDownload()
									.getHash());
						}
						redrawTables();
					}
					RCMain.getRCMain().getClient().transactionCommit();

				}
			}
		});

		final MenuItem downloadDeleteData = new MenuItem(removeAndMenu,
				SWT.PUSH);
		downloadDeleteData.setText("Delete Data");
		downloadDeleteData.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					String names = "";
					// Pull the names for the dialog
					for (TableItem item : items) {
						names += ((Container) item.getData()).getDownload()
								.getName()
								+ "\n";
					}

					MessageBox messageBox = new MessageBox(
							DOWNLOAD_MANAGER_SHELL, SWT.ICON_QUESTION | SWT.OK
									| SWT.CANCEL);
					messageBox.setText("Confirm Delete");
					messageBox
							.setMessage("Remove the data associated with the following:\n\n"
									+ names);
					int response = messageBox.open();
					switch (response) {
					case SWT.OK:
						RCMain.getRCMain().getClient().transactionStart();
						for (TableItem item : items) {
							Container container = (Container) item.getData();
							container.getDownload().remove(false, true);
							if (seedsMap.containsKey(container.getDownload()
									.getHash())) {
								seedsMap.remove(container.getDownload()
										.getHash());
							} else if (downloadsMap.containsKey(container
									.getDownload().getHash())) {
								downloadsMap.remove(container.getDownload()
										.getHash());
							}
							redrawTables();
						}
						RCMain.getRCMain().getClient().transactionCommit();
						break;
					case SWT.CANCEL:
						break;
					}

				}
			}
		});

		final MenuItem downloadDeleteBoth = new MenuItem(removeAndMenu,
				SWT.PUSH);
		downloadDeleteBoth.setText("Delete Both");
		downloadDeleteBoth.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					String names = "";
					// Pull the names for the dialog
					for (TableItem item : items) {
						names += ((Container) item.getData()).getDownload()
								.getName()
								+ "\n";
					}
					MessageBox messageBox = new MessageBox(
							DOWNLOAD_MANAGER_SHELL, SWT.ICON_QUESTION | SWT.OK
									| SWT.CANCEL);
					messageBox.setText("Confirm Delete");
					messageBox
							.setMessage("Remove both the data and the torrents for the following:\n\n"
									+ names);
					int response = messageBox.open();
					switch (response) {
					case SWT.OK:
						RCMain.getRCMain().getClient().transactionStart();
						for (TableItem item : items) {
							Container container = (Container) item.getData();

							// Close any tabs that have this download name
							CTabItem[] tabs = tabFolder.getItems();
							for (CTabItem tab : tabs) {
								if (tab.getText().equalsIgnoreCase(
										container.getDownload().getName())) {
									tab.dispose();
								}
							}

							container.getDownload().remove(true, true);

							// container.removeFromTable();
							if (seedsMap.containsKey(container.getDownload()
									.getHash())) {
								seedsMap.remove(container.getDownload()
										.getHash());
							} else if (downloadsMap.containsKey(container
									.getDownload().getHash())) {
								downloadsMap.remove(container.getDownload()
										.getHash());
							}
							redrawTables();
						}
						RCMain.getRCMain().getClient().transactionCommit();
						break;
					case SWT.CANCEL:
						break;
					}
				}
			}
		});

		final MenuItem forceRecheck = new MenuItem(menu, SWT.PUSH);
		forceRecheck.setText("Force Re-check");
		forceRecheck.setImage(ImageRepository.getImage("menu_recheck"));
		forceRecheck.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (table != null && !table.isDisposed()) {
					TableItem[] items = table.getSelection();
					if (items.length > 1) {
						return;
					}

					RCMain.getRCMain().getClient().transactionStart();
					for (TableItem item : items) {
						Container container = (Container) item.getData();
						container.getDownload().recheckData();
					}
					RCMain.getRCMain().getClient().transactionCommit();

				}
			}
		});

		// --------------------------- Set Custom per Torrent Speeds
		// ---------------\\

		// advanced > Download Speed Menu //
		final MenuItem itemDownSpeed = new MenuItem(menu, SWT.CASCADE);
		itemDownSpeed.setText("Set Down Speed");

		final Menu menuDownSpeed = new Menu(DOWNLOAD_MANAGER_SHELL,
				SWT.DROP_DOWN);
		itemDownSpeed.setMenu(menuDownSpeed);

		final MenuItem itemCurrentDownSpeed = new MenuItem(menuDownSpeed,
				SWT.PUSH);
		itemCurrentDownSpeed.setEnabled(false);

		itemCurrentDownSpeed.setText("Current: " + "Not Polled");

		new MenuItem(menuDownSpeed, SWT.SEPARATOR);

		Listener itemsDownSpeedListener = new Listener() {
			public void handleEvent (Event e) {
				if (e.widget != null && e.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) e.widget;
					int speed = item.getData("maxdl") == null ? 0
							: ((Integer) item.getData("maxdl")).intValue();

					TableItem[] items = table.getSelection();
					if (items.length == 1) {
						Container container = (Container) items[0].getData();
						container.getDownload().setMaximumDownloadKBPerSecond(
								speed);
					}
				}
			}
		};

		final MenuItem itemsDownSpeed = new MenuItem(menuDownSpeed, SWT.PUSH);
		itemsDownSpeed.setText("Unlimited");
		itemsDownSpeed.setData("maxdl", new Integer(0));
		itemsDownSpeed.addListener(SWT.Selection, itemsDownSpeedListener);

		final MenuItem itemsDownSpeedDisable = new MenuItem(menuDownSpeed,
				SWT.PUSH);
		itemsDownSpeedDisable.setText("Disabled");
		itemsDownSpeedDisable.setData("maxdl", new Integer(-1));
		itemsDownSpeedDisable
				.addListener(SWT.Selection, itemsDownSpeedListener);

		// --- Manual set
		new MenuItem(menuDownSpeed, SWT.SEPARATOR);

		final MenuItem itemDownSpeedManual = new MenuItem(menuDownSpeed,
				SWT.PUSH);
		itemDownSpeedManual.setText("Manual");
		itemDownSpeedManual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				InputShell is = new InputShell("Set Download Speed",
						"Enter a number in kb/s to change download to:");

				String sReturn = is.open();
				if (sReturn == null) {
					return;
				}

				int newSpeed;
				try {
					newSpeed = (int) (Double.valueOf(sReturn).doubleValue()/*
																			 * 
																			 * 1024
																			 */);
				} catch (NumberFormatException er) {
					MessageBox mb = new MessageBox(DOWNLOAD_MANAGER_SHELL,
							SWT.ICON_ERROR | SWT.OK);
					mb.setText("Invalid or Unrecognized Number");
					mb
							.setMessage("The number you entered is invalid or unrecognized");
					mb.open();
					return;
				}

				TableItem[] items = table.getSelection();
				if (items.length == 1) {
					Container container = (Container) items[0].getData();
					container.getDownload().setMaximumDownloadKBPerSecond(
							newSpeed);
				}
			}
		});

		// advanced >Upload Speed Menu //
		final MenuItem itemUpSpeed = new MenuItem(menu, SWT.CASCADE);
		itemUpSpeed.setText("Set Up Speed");

		final Menu menuUpSpeed = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		itemUpSpeed.setMenu(menuUpSpeed);

		final MenuItem itemCurrentUpSpeed = new MenuItem(menuUpSpeed, SWT.PUSH);
		itemCurrentUpSpeed.setEnabled(false);

		itemCurrentUpSpeed.setText("Current: " + "Not Polled");

		// ---
		new MenuItem(menuUpSpeed, SWT.SEPARATOR);

		Listener itemsUpSpeedListener = new Listener() {
			public void handleEvent (Event e) {
				if (e.widget != null && e.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) e.widget;
					int speed = item.getData("maxul") == null ? 0
							: ((Integer) item.getData("maxul")).intValue();

					TableItem[] items = table.getSelection();
					if (items.length == 1) {
						Container container = (Container) items[0].getData();
						container.getDownload()
								.setUploadRateLimitBytesPerSecond(speed);
					}
				}
			}
		};

		final MenuItem itemsUpSpeed = new MenuItem(menuUpSpeed, SWT.PUSH);
		itemsUpSpeed.setText("Unlimited");
		itemsUpSpeed.setData("maxul", new Integer(0));
		itemsUpSpeed.addListener(SWT.Selection, itemsUpSpeedListener);

		new MenuItem(menuUpSpeed, SWT.SEPARATOR);

		final MenuItem itemUpSpeedManual = new MenuItem(menuUpSpeed, SWT.PUSH);
		itemUpSpeedManual.setText("Manual");
		itemUpSpeedManual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				InputShell is = new InputShell("Set Upload Speed",
						"Enter a number in kb/s to change upload to:");

				String sReturn = is.open();
				if (sReturn == null) {
					return;
				}

				int newSpeed;
				try {
					newSpeed = (int) (Double.valueOf(sReturn).doubleValue() * 1024);
				} catch (NumberFormatException er) {
					MessageBox mb = new MessageBox(DOWNLOAD_MANAGER_SHELL,
							SWT.ICON_ERROR | SWT.OK);
					mb.setText("Invalid or Unrecognized Number");
					mb
							.setMessage("The number you entered is invalid or unrecognized");
					mb.open();
					return;
				}

				TableItem[] items = table.getSelection();
				if (items.length == 1) {
					Container container = (Container) items[0].getData();
					container.getDownload().setUploadRateLimitBytesPerSecond(
							newSpeed);
				}

			}
		});

		// -------------------------Rename Menus

		final MenuItem itemMenuRename = new MenuItem(menu, SWT.CASCADE);
		itemMenuRename.setText("Rename");
		final Menu menuRename = new Menu(DOWNLOAD_MANAGER_SHELL, SWT.DROP_DOWN);
		itemMenuRename.setMenu(menuRename);

		final MenuItem menuRenameDisplayedName = new MenuItem(menuRename,
				SWT.PUSH);
		menuRenameDisplayedName.setText("Rename Displayed Name");
		menuRenameDisplayedName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				RCMain.getRCMain().getDisplay().asyncExec(
						new SWTSafeRunnable() {
							@Override
							public void runSafe () {
								InputShell is = new InputShell(
										"Rename Displayed Name",
										"Enter a new name to display for this download"
												+ "\nIf no text is entered, the original name will be used");
								if (table != null && !table.isDisposed()) {
									TableItem[] items = table.getSelection();
									Container container = (Container) items[0]
											.getData();
									is.setTextValue(container.getDownload()
											.getName());
									String displayName = is.open();
									if (displayName != null
											&& !displayName.equals("")) {
										container.getDownload()
												.changeDisplayedName(
														displayName);
									}
								}
							}
						});
			}
		});
		final MenuItem menuRenameSavePath = new MenuItem(menuRename, SWT.PUSH);
		menuRenameSavePath.setText("Rename Save Path");
		menuRenameSavePath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				RCMain.getRCMain().getDisplay().asyncExec(
						new SWTSafeRunnable() {
							@Override
							public void runSafe () {
								InputShell is = new InputShell(
										"Rename Save Path",
										"Enter a new save path name for this download"
												+ "\nIf no text is entered, the download's displayed name will be used");
								if (table != null && !table.isDisposed()) {
									TableItem[] items = table.getSelection();
									Container container = (Container) items[0]
											.getData();
									is.setTextValue(container.getDownload()
											.getSavePath());
									String savepathName = is.open();
									if (savepathName != null
											&& !savepathName.equals("")) {
										container.getDownload().renameDownload(
												savepathName);
									}
								}
							}
						});
			}
		});
		final MenuItem menuRenameBoth = new MenuItem(menuRename, SWT.PUSH);
		menuRenameBoth.setText("Rename Both");
		menuRenameBoth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				RCMain.getRCMain().getDisplay().asyncExec(
						new SWTSafeRunnable() {
							@Override
							public void runSafe () {
								InputShell is = new InputShell(
										"Rename Download",
										"Enter a new name for this download"
												+ "\nIf no text is entered, the original values will be used");
								if (table != null && !table.isDisposed()) {
									TableItem[] items = table.getSelection();
									Container container = (Container) items[0]
											.getData();
									is.setTextValue(container.getDownload()
											.getName());
									String newName = is.open();
									if (newName != null && !newName.equals("")) {
										container.getDownload()
												.changeDisplayedName(newName);
										container.getDownload().renameDownload(
												newName);
									}
								}
							}
						});
			}
		});

		// ----------------------------Move Data
		if (table.equals(seedsTable)) {
			moveData = new MenuItem(menu, SWT.PUSH);
			moveData.setText("Move Data");
			moveData.addListener(SWT.Selection, new Listener() {

				public void handleEvent (Event arg0) {
					TableItem[] items = table.getSelection();
					if (items.length > 1) {
						return;
					}

					for (TableItem item : items) {
						Container container = (Container) item.getData();
						MoveDataDialog.open(container);
					}

				}

			});
		}

		final MenuItem changeCategory = new MenuItem(menu, SWT.PUSH);
		changeCategory.setText("Change Category");
		changeCategory.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
			 * .swt.events.SelectionEvent)t)
			 */
			@Override
			public void widgetSelected (SelectionEvent e) {
				TableItem[] items = table.getSelection();
				List<Container> downloads = new ArrayList<Container>();
				for (TableItem item : items) {
					Container container = (Container) item.getData();
					downloads.add(container);
				}
				ChangeCategoryDialog.open(downloads
						.toArray(new Container[downloads.size()]));
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem editColumns = new MenuItem(menu, SWT.PUSH);
		editColumns.setText("Edit Columns");
		editColumns.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				TableColumnEditorDialog
						.open(table.equals(downloadsTable) ? true : false);
			}
		});

		removeAnd.setMenu(removeAndMenu);
		table.setMenu(menu);

		menu.addMenuListener(new MenuListener() {

			public void menuHidden (MenuEvent arg0) {
				// Nothing to do here

			}

			public void menuShown (MenuEvent arg0) {
				TableItem[] items = table.getSelection();
				if (items.length == 0) {
					stop.setEnabled(false);
					queue.setEnabled(false);
					remove.setEnabled(false);
					forceStart.setEnabled(false);
					forceStart.setSelection(false);
					itemUpSpeed.setEnabled(false);
					itemDownSpeed.setEnabled(false);
					removeAnd.setEnabled(false);
					forceRecheck.setEnabled(false);
					itemMenuRename.setEnabled(false);
					try {
						moveData.setEnabled(false);
					} catch (Exception e) {
					}
				} else if (items.length == 1) {
					stop.setEnabled(true);
					queue.setEnabled(true);
					remove.setEnabled(true);
					forceStart.setEnabled(true);
					itemMenuRename.setEnabled(true);
					menuRenameDisplayedName.setEnabled(true);
					itemUpSpeed.setEnabled(true);

					Container container = (Container) items[0].getData();
					int currentDownSpeed = container.getDownload()
							.getMaximumDownloadKBPerSecond();
					int currentUpSpeed = container.getDownload()
							.getUploadRateLimitBytesPerSecond();
					if (currentUpSpeed <= 0) {
						itemCurrentUpSpeed.setText("Current: " + "Unlimited");
					} else {
						itemCurrentUpSpeed
								.setText("Current: "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(currentUpSpeed));
					}

					if (currentDownSpeed < 0) {
						itemCurrentDownSpeed.setText("Current: " + "Disabled");
					} else if (currentDownSpeed == 0) {
						itemCurrentDownSpeed.setText("Current: " + "Unlimited");
					} else {
						itemCurrentDownSpeed
								.setText("Current: "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(currentDownSpeed));
					}

					itemDownSpeed.setEnabled(true);
					try {
						moveData.setEnabled(true);
					} catch (Exception e) {
					}
					// Check to see if download is already in ForceStart

					if (container.getDownload().isForceStart()) {
						forceStart.setSelection(true);
					} else {
						forceStart.setSelection(false);
					}

					int state = container.getDownload().getState();
					if (state == Download.ST_STOPPED
							|| state == Download.ST_QUEUED
							|| state == Download.ST_ERROR) {
						forceRecheck.setEnabled(true);
						menuRenameSavePath.setEnabled(true);
						menuRenameBoth.setEnabled(true);

					} else {
						forceRecheck.setEnabled(false);
						menuRenameSavePath.setEnabled(false);
						menuRenameBoth.setEnabled(false);
					}

					removeAnd.setEnabled(true);

				} else {
					stop.setEnabled(true);
					queue.setEnabled(true);
					remove.setEnabled(true);
					forceStart.setEnabled(true);
					forceStart.setSelection(false);
					itemUpSpeed.setEnabled(false);
					itemDownSpeed.setEnabled(false);
					forceRecheck.setEnabled(false);
					itemMenuRename.setEnabled(false);
					try {
						moveData.setEnabled(false);
					} catch (Exception e) {
					}
					removeAnd.setEnabled(true);
				}

			}

		});
	}

	/**
	 * Sets the GUI components to a completely logged out state
	 */
	public void setGUItoLoggedOut () {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (DOWNLOAD_MANAGER_SHELL != null) {
					setLogInOutButtons(false);
					setSSLStatusBar(false, false);
					setConnectionStatusBar(0);
					setStatusBarText("Disconnected", SWT.COLOR_RED);
					setLogInOutButtons(false);
					DOWNLOAD_MANAGER_SHELL.setText(TITLE);
					clearMapsAndChildred();

					// close all open stray tabs and dialogs
					CTabItem[] items = tabFolder.getItems();
					for (CTabItem item : items) {
						if (item != myTorrents) {
							item.dispose();
						}
					}

					Shell[] shells = display.getShells();
					for (Shell shell : shells) {
						if (shell != DOWNLOAD_MANAGER_SHELL) {
							shell.dispose();
						}
					}

				}
				RCMain.getRCMain().setTrayIcon(0);
			}
		});
	}

	/**
	 * sets the status bar text alert area given String text and SWT.COLOR_*
	 * 
	 * @param text
	 * @param color
	 */
	public void setStatusBarText (final String text, final int color) {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				try {
					if (statusBarText == null || statusBarText.isDisposed()) {
						return;
					}
					if (text == null) {
						statusBarText.setText("");
						return;
					}
					statusBarText.setForeground(display.getSystemColor(color));
					statusBarText.setText(text);
					statusBarText.getParent().layout();
				} catch (SWTException e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * sets the status bar text alert area given String text and with the color
	 * black
	 * 
	 * @param text
	 */
	public void setStatusBarText (String text) {
		setStatusBarText(text, SWT.COLOR_BLACK);
	}

	/**
	 * Sets the Toolbar Icons for Queue Stop and Remove
	 * 
	 * @param bQueue true if Queue enabled
	 * @param bStop true if Stop enabled
	 * @param bRemove true if Remove enabled
	 */
	public void setToolBarTorrentIcons (final boolean bQueue,
			final boolean bStop, final boolean bRemove) {
		RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (queueTorrent != null || !queueTorrent.isDisposed()) {
					queueTorrent.setEnabled(bQueue);
				}

				if (stopTorrent != null || !stopTorrent.isDisposed()) {
					stopTorrent.setEnabled(bStop);
				}

				if (removeTorrent != null || !removeTorrent.isDisposed()) {
					removeTorrent.setEnabled(bRemove);
				}

			}
		});
	}

	public void createTableColumns (final Table table,
			final List<Integer> dlList) {
		ControlListener resizeListener = new ControlListener() {
			public void controlMoved (ControlEvent arg0) {
			}

			public void controlResized (ControlEvent arg0) {
				saveColumnWidthsToPreferencesFile();
			}
		};

		List<Integer> column_width_list = new ArrayList<Integer>();
		Properties properties = RCMain.getRCMain().getProperties();
		if (table.equals(downloadsTable)) {
			if (properties.containsKey("downloadsTable.columns.widths")) {
				column_width_list = EncodingUtil.StringToIntegerList(properties
						.getProperty("downloadsTable.columns.widths"));
			}
		} else {
			if (properties.containsKey("seedsTable.columns.widths")) {
				column_width_list = EncodingUtil.StringToIntegerList(properties
						.getProperty("seedsTable.columns.widths"));
			}
		}

		table.removeAll(); // clear all

		for (int i = 0; i < dlList.size(); i++) {
			switch (dlList.get(i)) {
			case RemoteConstants.ST_POSITION:
				TableColumn position = new TableColumn(table, SWT.CENTER);
				position.setText("#");
				position.setMoveable(true);
				try {
					position.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					position.setWidth(22);
				}
				position.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_POSITION));
				position.addControlListener(resizeListener);
				if (SWT.getVersion() > 3220) {
					table.setSortDirection(SWT.UP);
					table.setSortColumn(position);
				}
				break;

			case RemoteConstants.ST_HEALTH:
				TableColumn health = new TableColumn(table, SWT.CENTER);
				health.setResizable(false);
				health.setMoveable(true);
				health.setWidth(22);
				break;

			case RemoteConstants.ST_NAME:
				TableColumn name = new TableColumn(table, SWT.LEFT);
				name.setText("Name");
				name.addControlListener(resizeListener);
				name.setMoveable(true);
				try {
					name.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					name.setWidth(300);
				}
				name.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_NAME));
				break;

			case RemoteConstants.ST_COMPLETITION:
				TableColumn progress = new TableColumn(table, SWT.CENTER);
				progress.setText("Percent Complete");
				progress.setWidth(120);
				progress.setMoveable(true);
				progress.setResizable(false);
				progress.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_COMPLETITION));
				break;

			case RemoteConstants.ST_AVAILABILITY:
				TableColumn availability = new TableColumn(table, SWT.RIGHT);
				availability.setText("Availability");
				try {
					availability.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					availability.setWidth(120);
				}
				availability.setMoveable(true);
				availability.addControlListener(resizeListener);
				availability.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_AVAILABILITY));
				break;

			case RemoteConstants.ST_DOWNLOAD_AVG:
				TableColumn dlSpeed = new TableColumn(table, SWT.RIGHT);
				dlSpeed.setText("Down Speed");
				dlSpeed.setMoveable(true);
				dlSpeed.addControlListener(resizeListener);
				dlSpeed.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_DOWNLOAD_AVG));
				try {
					dlSpeed.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					dlSpeed.pack();
				}
				break;

			case RemoteConstants.ST_UPLOAD_AVG:
				TableColumn upSpeed = new TableColumn(table, SWT.RIGHT);
				upSpeed.setText("Up Speed");
				upSpeed.setMoveable(true);
				upSpeed.addControlListener(resizeListener);
				upSpeed.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_UPLOAD_AVG));
				try {
					upSpeed.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					upSpeed.pack();
				}

				break;

			case RemoteConstants.ST_ALL_SEEDS:
				TableColumn seeds = new TableColumn(table, SWT.RIGHT);
				seeds.setText("Seeds");
				seeds.setMoveable(true);
				seeds.addControlListener(resizeListener);
				seeds.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_ALL_SEEDS));
				try {
					seeds.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					seeds.setWidth(120);
				}
				break;

			case RemoteConstants.ST_ALL_LEECHER:
				TableColumn leechers = new TableColumn(table, SWT.RIGHT);
				leechers.setText("Leechers");
				leechers.setMoveable(true);
				leechers.addControlListener(resizeListener);
				leechers.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_ALL_LEECHER));
				try {
					leechers.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					leechers.setWidth(120);
				}

				break;

			case RemoteConstants.ST_UPLOADED:
				TableColumn uploaded = new TableColumn(table, SWT.RIGHT);
				uploaded.setText("Uploaded");
				uploaded.setMoveable(true);
				uploaded.addControlListener(resizeListener);
				uploaded.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_UPLOADED));
				try {
					uploaded.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					uploaded.pack();
				}

				break;

			case RemoteConstants.ST_DOWNLOADED:
				TableColumn downloaded = new TableColumn(table, SWT.RIGHT);
				downloaded.setText("Downloaded");
				downloaded.setMoveable(true);
				downloaded.addControlListener(resizeListener);
				downloaded.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_DOWNLOADED));
				try {
					downloaded.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					downloaded.pack();
				}

				break;

			case RemoteConstants.ST_ETA:
				TableColumn eta = new TableColumn(table, SWT.RIGHT);
				eta.setText("ETA");
				eta.setMoveable(true);
				eta.addControlListener(resizeListener);
				try {
					eta.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					eta.setWidth(120);
				}

				break;

			case RemoteConstants.ST_STATUS:
				TableColumn status = new TableColumn(table, SWT.RIGHT);
				status.setText("Status");
				status.setMoveable(true);
				status.addControlListener(resizeListener);
				try {
					status.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					status.setWidth(150);
				}

				break;
			case RemoteConstants.ST_SHARE:
				TableColumn shareRatio = new TableColumn(table, SWT.RIGHT);
				shareRatio.setText("Share Ratio");
				shareRatio.setMoveable(true);
				shareRatio.addControlListener(resizeListener);
				shareRatio.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_SHARE));
				try {
					shareRatio.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					shareRatio.pack();
				}

				break;

			case RemoteConstants.ST_TRACKER:
				TableColumn tracker = new TableColumn(table, SWT.RIGHT);
				tracker.setText("Tracker");
				tracker.setMoveable(true);
				tracker.addControlListener(resizeListener);
				try {
					tracker.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					tracker.setWidth(150);
				}

				break;

			case RemoteConstants.ST_LIMIT_DOWN:
				TableColumn limitDown = new TableColumn(table, SWT.RIGHT);
				limitDown.setText("Download Limit");
				limitDown.setMoveable(true);
				limitDown.addControlListener(resizeListener);
				try {
					limitDown.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					limitDown.pack();
				}

				break;

			case RemoteConstants.ST_LIMIT_UP:
				TableColumn upLimit = new TableColumn(table, SWT.RIGHT);
				upLimit.setText("Upload Limit");
				upLimit.setMoveable(true);
				upLimit.addControlListener(resizeListener);
				try {
					upLimit.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					upLimit.pack();
				}

				break;

			case RemoteConstants.ST_DISCARDED:
				TableColumn discarded = new TableColumn(table, SWT.RIGHT);
				discarded.setText("Discarded");
				discarded.setMoveable(true);
				discarded.addControlListener(resizeListener);
				discarded.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_DISCARDED));
				try {
					discarded.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					discarded.pack();
				}

				break;

			case RemoteConstants.ST_SIZE:
				TableColumn size = new TableColumn(table, SWT.RIGHT);
				size.setText("Size");
				size.setMoveable(true);
				size.addControlListener(resizeListener);
				size.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_SIZE));
				try {
					size.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					size.setWidth(100);
				}

				break;

			case RemoteConstants.ST_ELAPSED_TIME:
				TableColumn elapsedTime = new TableColumn(table, SWT.RIGHT);
				elapsedTime.setText("Elapsed Time");
				elapsedTime.setMoveable(true);
				elapsedTime.addControlListener(resizeListener);
				try {
					elapsedTime.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					elapsedTime.pack();
				}

				break;

			case RemoteConstants.ST_TOTAL_AVG:
				TableColumn swarm = new TableColumn(table, SWT.RIGHT);
				swarm.setText("Swarm Speed");
				swarm.setMoveable(true);
				swarm.addControlListener(resizeListener);
				try {
					swarm.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					swarm.pack();
				}

				break;

			case RemoteConstants.ST_CATEGORY:
				TableColumn category = new TableColumn(table, SWT.RIGHT);
				category.setText("Category");
				category.setMoveable(true);
				category.addControlListener(resizeListener);
				category.addListener(SWT.Selection, getSortListener(table,
						RemoteConstants.ST_CATEGORY));
				try {
					category.setWidth(column_width_list.get(i));
				} catch (Exception e) {
					category.pack();
				}

				break;

			}
		}
	}

	/**
	 * This listener will set the data to the "comparator" on Table for each
	 * give int column from RemoteConstants
	 * 
	 * @param table
	 * @param column (from RemoteConstants)
	 * @return
	 */
	private Listener getSortListener (final Table table, final int column) {
		Listener sortListener = new Listener() {
			public void handleEvent (Event e) {
				table.setData("comparator", Container.getComparators().get(
						column));
				if (Boolean.parseBoolean((String) table.getData("sort"))) {
					table.setData("sort", Boolean.toString(false));
					if (SWT.getVersion() > 3220) {
						TableColumn col = (TableColumn) e.widget;
						table.setSortDirection(SWT.DOWN);
						table.setSortColumn(col);
					}

				} else {
					table.setData("sort", Boolean.toString(true));
					if (SWT.getVersion() > 3220) {
						TableColumn col = (TableColumn) e.widget;
						table.setSortDirection(SWT.UP);
						table.setSortColumn(col);
					}
				}
				redrawTables();
			}
		};

		return sortListener;
	}

	/**
	 * This function sets the Items that are requested. It uses the columns as
	 * datasource.
	 */
	public void setRequestedItems () {
		int options = 0;
		List<Integer> list = DownloadContainer.getColumns();
		for (int i : list) {
			options |= i;
		}
		list = SeedContainer.getColumns();
		for (int i : list) {
			options |= i;
		}
		options |= RemoteConstants.ST_STATE; // set state since we need it
		RCMain.getRCMain().getProperties().setProperty("transfer_states",
				options);
	}

	public void clearMapsAndChildred () {
		downloadsMap.clear();
		downloadsArray = downloadsMap.values().toArray(emptyArray);
		seedsMap.clear();
		seedsArray = seedsMap.values().toArray(emptyArray);
		RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				downloadsTable.removeAll();
				seedsTable.removeAll();
				Control[] children_downloads = downloadsTable.getChildren();
				for (Control child : children_downloads) {
					child.dispose();
				}
				Control[] children_seeds = seedsTable.getChildren();
				for (Control child : children_seeds) {
					child.dispose();
				}
			}
		});
	}

	public void redrawTables () {
		downloadsArray = downloadsMap.values().toArray(emptyArray);
		seedsArray = seedsMap.values().toArray(emptyArray);
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (downloadsTable != null || !downloadsTable.isDisposed()) {
					downloadsTable.clearAll();
					downloadsTable.setItemCount(downloadsArray.length);
				}

				if (seedsTable != null || !seedsTable.isDisposed()) {
					seedsTable.clearAll();
					seedsTable.setItemCount(seedsArray.length);
				}

				if (downloadsTable.getSelectionCount() == 0
						&& seedsTable.getSelectionCount() == 0) { // Nothing
					// is
					// selected
					// anymore
					setTorrentMoveButtons(false, false, false, false);
					setToolBarTorrentIcons(false, false, false);
				} else if (downloadsTable.getSelectionCount() > 0) { // Something
					// is
					// selected
					// in
					// downloadsTable,
					// refresh
					// Toolbars
					TableItem[] items = downloadsTable.getSelection();
					if (items == null) {
						return;
					}
					if (items.length == 1) {
						Container container = (Container) items[0].getData();
						Download download = container.getDownload();
						// Single line selection
						setTorrentMoveButtons(false, false, false, false);
						int index = download.getPosition();
						if (index == 1 && downloadsTable.getItemCount() == 1) {
							setTorrentMoveButtons(false, true, true, false);
						} else if (index == 1) {
							setTorrentMoveButtons(false, true, true, true);
						} else if (index == downloadsTable.getItemCount() - 1) {
							setTorrentMoveButtons(true, true, true, false);
						} else {
							setTorrentMoveButtons(true, true, true, true);
						}

						// torrent control
						if (download.getState() == Download.ST_QUEUED
								|| download.getState() == Download.ST_DOWNLOADING) {
							setToolBarTorrentIcons(false, true, true);
						} else if (download.getState() == Download.ST_STOPPED) {
							setToolBarTorrentIcons(true, false, true);
						} else if (download.getState() == Download.ST_ERROR) {
							setToolBarTorrentIcons(true, true, true);
						} else {
							setToolBarTorrentIcons(false, false, false);
						}

					} else if (items.length > 1) {
						// Multiple selection here
						setTorrentMoveButtons(false, false, false, false);
						setToolBarTorrentIcons(true, true, true);

					}
				} else if (seedsTable.getSelectionCount() > 0) { // SeedsTable
					// has
					// selection,
					// Refresh
					// toolbars
					TableItem[] items = seedsTable.getSelection();
					if (items == null) {
						return;
					}
					if (items.length == 1) {
						Container container = (Container) items[0].getData();
						Download download = container.getDownload();
						// Single line selection
						setTorrentMoveButtons(false, false, false, false);
						int index = download.getPosition();
						if (index == 1 && seedsTable.getItemCount() == 1) {
							setTorrentMoveButtons(false, true, true, false);
						} else if (index == 1) {
							setTorrentMoveButtons(false, true, true, true);
						} else if (index == seedsTable.getItemCount() - 1) {
							setTorrentMoveButtons(true, true, true, false);
						} else {
							setTorrentMoveButtons(true, true, true, true);
						}

						// Torrent Control

						if (download.getState() == Download.ST_QUEUED
								|| download.getState() == Download.ST_SEEDING) {
							setToolBarTorrentIcons(false, true, true);
						} else if (download.getState() == Download.ST_STOPPED) {
							setToolBarTorrentIcons(true, false, true);
						}
					} else if (items.length > 1) {
						// Multiple selection here
						setTorrentMoveButtons(false, false, false, false);
						setToolBarTorrentIcons(true, true, true);

					}
				}
			}
		});
	}

	public void refreshStatusBar () {
		RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe () {
				if (statusDown != null || !statusDown.isDisposed()) {
					if (azureusDownload.equalsIgnoreCase("0")) {
						statusDown.setText(DisplayFormatters
								.formatByteCountToBase10KBEtcPerSec(downSpeed));
					} else if (azureusDownload.equalsIgnoreCase("N/S")) {
						statusDown
								.setText("["
										+ azureusDownload
										+ "] "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(downSpeed));
					} else {
						statusDown
								.setText("["
										+ azureusDownload
										+ "K] "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(downSpeed));
					}

					if (azureusUpload.equalsIgnoreCase("0")) {
						statusUp.setText(DisplayFormatters
								.formatByteCountToBase10KBEtcPerSec(upSpeed));
					} else if (azureusUpload.equalsIgnoreCase("N/S")) {
						statusUp
								.setText("["
										+ azureusUpload
										+ "] "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(upSpeed));
					} else {
						statusUp
								.setText("["
										+ azureusUpload
										+ "K] "
										+ DisplayFormatters
												.formatByteCountToBase10KBEtcPerSec(upSpeed));
					}

					statusbarComp.layout();
				}
			}
		});
	}

	/**
	 * Call when a successfull connection occurs
	 * 
	 */
	public void initializeConnection () {
		if (RCMain.getRCMain().connected()) {
			Client client = RCMain.getRCMain().getClient();
			client.transactionStart();
			client.getDownloadManager().update(true);
			client.getUserManager().update();

			// Pull the remote info
			client.getRemoteInfo().load();

			// pull the upload download settings
			client
					.sendGetAzParameter(
							RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
							RemoteConstants.PARAMETER_INT);
			client
					.sendGetAzParameter(
							RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
							RemoteConstants.PARAMETER_INT);
			client.sendGetPluginParameter("singleUserMode",
					RemoteConstants.PARAMETER_BOOLEAN);
			client.transactionCommit();
		}
	}

	private void createDragDrop (final Table parent) {
		try {

			Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

			DragSource dragSource = new DragSource(parent, DND.DROP_MOVE);
			dragSource.setTransfer(types);
			dragSource.addDragListener(new DragSourceAdapter() {
				@Override
				public void dragStart (DragSourceEvent event) {
					Table table = parent;
					if (table.getSelectionCount() != 0
							&& table.getSelectionCount() != table
									.getItemCount()) {
						event.doit = true;
						// System.out.println("DragStart");
						drag_drop_line_start = table.getSelectionIndex();
					} else {
						event.doit = false;
						drag_drop_line_start = -1;
					}
				}

				@Override
				public void dragSetData (DragSourceEvent event) {
					event.data = "moveRow";
				}
			});

			DropTarget dropTarget = new DropTarget(parent, DND.DROP_DEFAULT
					| DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK
					| DND.DROP_TARGET_MOVE);

			if (SWT.getVersion() >= 3107) {
				dropTarget.setTransfer(new Transfer[] {
						FileTransfer.getInstance(), /*
													 * HTMLTransfer.getInstance()
													 * ,
													 */
						/* URLTransfer.getInstance(), */TextTransfer
								.getInstance() });
			} else {
				dropTarget.setTransfer(new Transfer[] {
						FileTransfer.getInstance(),
						/* URLTransfer.getInstance(), */TextTransfer
								.getInstance() });
			}

			dropTarget.addDropListener(new DropTargetAdapter() {

				@Override
				public void dragEnter (DropTargetEvent event) {
					// no event.data on dragOver, use drag_drop_line_start to
					// determine if
					// ours

					if (FileTransfer.getInstance().isSupportedType(
							event.currentDataType)) {
						event.detail = DND.DROP_COPY;
					} else if (drag_drop_line_start < 0) {
						if (event.detail != DND.DROP_COPY) {
							if ((event.operations & DND.DROP_LINK) > 0) {
								event.detail = DND.DROP_LINK;
							} else if ((event.operations & DND.DROP_COPY) > 0) {
								event.detail = DND.DROP_COPY;
							}
						}
					} else if (TextTransfer.getInstance().isSupportedType(
							event.currentDataType)) {
						event.feedback = DND.FEEDBACK_EXPAND
								| DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT
								| DND.FEEDBACK_INSERT_BEFORE
								| DND.FEEDBACK_INSERT_AFTER;
						event.detail = event.item == null ? DND.DROP_NONE
								: DND.DROP_MOVE;
					}
				}

				@Override
				public void drop (DropTargetEvent event) {
					if (!(event.data instanceof String)
							|| !((String) event.data).equals("moveRow")) {
						openDroppedTorrents(event);
						return;
					}

					if (event.data instanceof String
							|| event.data instanceof String[]) {
						final String[] sourceNames = (event.data instanceof String[]) ? (String[]) event.data
								: new String[] { (String) event.data };
						for (String s : sourceNames) {
							File f = new File(s);
							if (f.exists() && f.isFile()) {
								openDroppedTorrents(event);
								return;
							}
						}
					}

					// Torrent file from shell dropped
					if (drag_drop_line_start >= 0) { // event.data == null
						event.detail = DND.DROP_NONE;
						if (event.item == null) {
							return;
						}
						int drag_drop_line_end = parent
								.indexOf((TableItem) event.item);

						// moveSelectedTorrents(drag_drop_line_start,
						// drag_drop_line_end);
						if (parent.equals(downloadsTable)) {
							Iterator<String> iter = downloadsMap.keySet()
									.iterator();
							while (iter.hasNext()) {
								Container container = downloadsMap.get(iter
										.next());
								if (container.getDownload().getPosition() == drag_drop_line_start + 1) {
									System.out
											.println("Moving "
													+ container.getDownload()
															.getName()
													+ " From position "
													+ (drag_drop_line_start + 1)
													+ " to "
													+ (drag_drop_line_end + 1));
									container.getDownload().moveTo(
											drag_drop_line_end + 1);
									if (RCMain.getRCMain().connected()) {
										RCMain.getRCMain().getClient()
												.getDownloadManager().update(
														false);
									}

									// make sure to redraw the table here so a
									// resort happens
									redrawTables();
								}
							}
						} else {
							Iterator<String> iter = seedsMap.keySet()
									.iterator();
							while (iter.hasNext()) {
								Container container = seedsMap.get(iter.next());
								if (container.getDownload().getPosition() == drag_drop_line_start) {
									System.out
											.println("Moving "
													+ container.getDownload()
															.getName()
													+ " From position "
													+ (drag_drop_line_start + 1)
													+ " to "
													+ (drag_drop_line_end + 1));
									container.getDownload().moveTo(
											drag_drop_line_end + 1);
									if (RCMain.getRCMain().connected()) {
										RCMain.getRCMain().getClient()
												.getDownloadManager().update(
														false);
									}

									// make sure to redraw the table here so a
									// resort happens
									redrawTables();
								}
							}
						}
						drag_drop_line_start = -1;
					}
				}
			});

		} catch (Throwable t) {
			logger.error("failed to init drag-n-drop + \n" + t);
		}
	}

	public static String parseTextForURL (String text) {
		// examples:
		// <A HREF=http://abc.om/moo>test</a>
		// <A style=cow HREF="http://abc.om/moo">test</a>
		// <a href="http://www.gnu.org/licenses/fdl.html" target="_top">moo</a>

		Pattern pat = Pattern.compile("<.*a\\s++.*href=\"?([^\\'\"\\s>]++).*",
				Pattern.CASE_INSENSITIVE);
		Matcher m = pat.matcher(text);
		if (m.find()) {
			return m.group(1);
		}

		return null;
	}

	public static void openDroppedTorrents (DropTargetEvent event) {
		if (event.data == null) {
			return;
		}

		// boolean bOverrideToStopped = event.detail == DND.DROP_COPY;

		if (event.data instanceof String[] || event.data instanceof String) {
			final String[] sourceNames = (event.data instanceof String[]) ? (String[]) event.data
					: new String[] { (String) event.data };
			if (sourceNames == null) {
				event.detail = DND.DROP_NONE;
			}
			if (event.detail == DND.DROP_NONE) {
				return;
			}
			List<String> files = new ArrayList<String>();
			for (int i = 0; (i < sourceNames.length); i++) {
				final File source = new File(sourceNames[i]);

				if (!source.exists()) {
					try {
						new URL(sourceNames[i]);
						OpenByURLDialog.openWithURL(sourceNames[i]);
					} catch (MalformedURLException e) {
					}
				} else if (source.isFile()) {
					String filename = source.getAbsolutePath();
					try {
						if (!isTorrentFile(filename)) {
							logger
									.info("openDroppedTorrents: file not a torrent file");

							// Torrent creation if we ever support that in FF
							// ShareUtils.shareFile(azureus_core, filename);
						} else {
							System.out
									.println("Dropped file IS torrent -- to open: "
											+ filename);
							files.add(filename);
							/*
							 * openTorrentWindow(null, new String[] { filename
							 * }, bOverrideToStopped);
							 */
						}
					} catch (Exception e) {
						logger.info("Torrent open fails for '" + filename
								+ "'\n" + e.toString());
					}
				} else if (source.isDirectory()) {

					// If we ever want to support torrent creation, this is a
					// directory drop
					// torrent create

					/*
					 * String dir_name = source.getAbsolutePath(); if
					 * (!bAllowShareAdd) { openTorrentWindow(dir_name, null,
					 * bOverrideToStopped); } else { String drop_action =
					 * COConfigurationManager.getStringParameter(
					 * "config.style.dropdiraction" ); if
					 * (drop_action.equals("1")) {
					 * ShareUtils.shareDir(azureus_core, dir_name); } else if
					 * (drop_action.equals("2")) {
					 * ShareUtils.shareDirContents(azureus_core, dir_name,
					 * false); } else if (drop_action.equals("3")) {
					 * ShareUtils.shareDirContents(azureus_core, dir_name,
					 * true); } else { openTorrentWindow(dir_name, null,
					 * bOverrideToStopped); } }
					 */
				}
			}
			if (files.size() > 0) {
				OpenByFileDialog.open(files.toArray(new String[0]));
			}
		}
	}

	public static boolean isTorrentFile (String filename)
			throws FileNotFoundException, IOException {
		File check = new File(filename);
		if (!check.exists()) {
			throw new FileNotFoundException("File " + filename + " not found.");
		}
		if (!check.canRead()) {
			throw new IOException("File " + filename + " cannot be read.");
		}
		if (check.isDirectory()) {
			logger.info("File " + filename + " is a directory.");
			return false;
		}

		try {
			if (!check.isFile()) {
				logger.info("Torrent must be a file ('" + check.getName()
						+ "')");
				return false;
			}

			if (check.length() == 0) {
				logger
						.info("Torrent is zero length('" + check.getName()
								+ "')");
			}

			FileInputStream fis = null;

			try {
				fis = new FileInputStream(check);

				// construct( fis );

			} catch (IOException e) {
				logger.info("IO Exception reading torrent ('" + check.getName()
						+ "')");

			} finally {

				if (fis != null) {

					try {

						fis.close();

					} catch (IOException e) {

						logger.error(e.toString());
						return false;
					}
				}
			}
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	public void saveColumnWidthsToPreferencesFile () {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {

			@Override
			public void runSafe () {
				Properties properties = RCMain.getRCMain().getProperties();
				// save the downloadsTable column widths
				if (!downloadsTable.isDisposed()) {
					TableColumn[] columns = downloadsTable.getColumns();
					List<Integer> dl_column_list = new ArrayList<Integer>();
					for (TableColumn column : columns) {
						dl_column_list.add(column.getWidth());
					}
					properties.setProperty("downloadsTable.columns.widths",
							EncodingUtil.IntListToString(dl_column_list));
				}

				// save the seedsTable column widths
				if (!seedsTable.isDisposed()) {
					TableColumn[] seed_columns = seedsTable.getColumns();
					List<Integer> seed_column_list = new ArrayList<Integer>();
					for (TableColumn column : seed_columns) {
						seed_column_list.add(column.getWidth());
					}
					properties.setProperty("seedsTable.columns.widths",
							EncodingUtil.IntListToString(seed_column_list));
				}

				// Save Everything!
				RCMain.getRCMain().saveConfig();

			}

		});

	}

	/**
	 * Returns the number of tabs currently open in the tabFolder
	 * 
	 * @return int count
	 */
	public CTabFolder getTabFolder () {
		return tabFolder;
	}

	public int[] getSeedsDownloadsCount () {
		return new int[] { seedsMap.size(), downloadsMap.size() };
	}

	private class CLabelPadding extends CLabel {
		private int			lastWidth		= 0;
		private long		widthSetOn		= 0;
		private final int	KEEPWIDTHFOR_MS	= 30 * 1000;

		public CLabelPadding(Composite parent, int style) {
			super(parent, style | SWT.CENTER);

			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
					| GridData.VERTICAL_ALIGN_FILL);
			setLayoutData(gridData);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.custom.CLabel#computeSize(int, int, boolean)
		 */
		@Override
		public Point computeSize (int wHint, int hHint, boolean changed) {
			if (!isVisible()) {
				return (new Point(0, 0));
			}
			Point pt = super.computeSize(wHint, hHint, changed);
			pt.x += 4;

			long now = System.currentTimeMillis();
			if (lastWidth > pt.x && now - widthSetOn < KEEPWIDTHFOR_MS) {
				pt.x = lastWidth;
			} else {
				if (lastWidth != pt.x) {
					lastWidth = pt.x;
				}
				widthSetOn = now;
			}

			return pt;
		}
	}

	/**
	 * Open a plugin view
	 * 
	 * @param pluginViewID string
	 * @return Composite for the plugin
	 */
	public void openPluginView (final String pluginViewID) {

		// check if already open and return
		if (!openPluginViews.add(pluginViewID)) {
			return;
		}

		// Pull the pluginManager
		PluginManagerImpl pm = RCMain.getRCMain().getPluginManagerImpl();

		// pull a view instance
		final UIPluginViewImpl vi = pm.getUIManager().getViewInstance(
				ViewID.MAIN, pluginViewID, null);

		// the vi can be null, so check if the vi is null and if so return
		if (vi == null) {
			return;
		}

		// add in the plugin's ctabitem and make it a closable tab
		CTabItem pluginTab = new CTabItem(tabFolder, SWT.CLOSE);
		pluginTab.setText(pluginViewID);

		pluginTab.addDisposeListener(new DisposeListener() {

			public void widgetDisposed (DisposeEvent arg0) {
				// remove from open views
				openPluginViews.remove(pluginViewID);
				vi.delete();
			}
		});

		Composite comp = new Composite(tabFolder, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginTop = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		// assign this comp to the new tab
		pluginTab.setControl(comp);

		// focus the tab
		tabFolder.setSelection(pluginTab);

		// Now we have a comp.. so pass it to the vi
		vi.initialize(comp);
		comp.layout();

	}

}// EOF
