/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.SSLCertWizard;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.flexyconf.ContentProvider;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.FCInterface;
import lbms.tools.flexyconf.FlexyConfiguration;
import lbms.tools.flexyconf.I18NProvider;
import lbms.tools.flexyconf.swt.SWTMenu;
import lbms.tools.i18n.I18NTranslator;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PreferencesTab {

	private Label pluginLabel;
	private Text updateIntervalOpen_Text, updateIntervalClosed_Text;
	private Button autoOpen, autoConnect, autoUpdateCheck, autoUpdate;
	private Button trayMinimize, trayExit, showSplash, popupsEnabled;
	private Button autoClipboard, autoConsole, exitConfirm;
	private Button updateBeta, singleUser;
	private Tree menuTree;

	private Composite cOptions;
	private ScrolledComposite sc;
	private Properties properties;

	private ParameterListener pl;
	private FlexyConfiguration fc;
	private SWTMenu fcm;
	private Properties azProps = new Properties();
	private ParameterListener azParam = new ParameterListener() {
		public void azParameter(String key, String value, int type) {}
		public void pluginParameter(String key, String value, int type) {}

		public void coreParameter(final String key,final  String value,final  int type) {
			azProps.setProperty(key, value);
			RCMain.getRCMain().getDisplay().syncExec(new Runnable() {
				public void run() {
					fc.getFCInterface().getEntryUpdateListener().updated(key, value);
				}
			});
		}
	};

	private boolean bModified = false;

	public PreferencesTab(final CTabFolder parentTab){


		//Open properties for reading and saving
		properties = RCMain.getRCMain().getProperties();


		final CTabItem prefsTab = new CTabItem(parentTab, SWT.CLOSE);
		prefsTab.setText("Preferences");



		final Composite parent = new Composite(parentTab, SWT.NULL);
		parent.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);


		prefsTab.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {
				if(bModified){
					MessageBox box = new MessageBox(parent.getShell(),SWT.OK);
					box.setText("Usaved Changes");
					box.setMessage("You have made modifications to the preferences and " +
					"closed the tab before you saved.  All changes have been discarded");
					box.open();
				}
				if(pl != null) RCMain.getRCMain().getClient().removeParameterListener(pl);
				closeAzFlexyConf();
			}

		});



		//top label
		Composite grayLabel = new Composite(parent,SWT.BORDER);
		grayLabel.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		grayLabel.setLayout(new GridLayout(1,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		grayLabel.setLayoutData(gridData);

		Label title = new Label(grayLabel,SWT.NONE);
		title.setText("No changes are actually made until saved");
		title.setBackground(grayLabel.getBackground());

		//Set it bold
		Font initialFont = title.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
		title.setFont(newFont);
		newFont.dispose();

		grayLabel.pack();




		final SashForm sash = new SashForm(parent,SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		sash.setLayoutData(gridData);
		sash.setLayout(new GridLayout(1,false));


		//Tree on left side
		menuTree = new Tree(sash,SWT.BORDER | SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		menuTree.setLayoutData(gridData);

		final TreeItem tiConnection = new TreeItem(menuTree,SWT.NULL);
		tiConnection.setText("Connection");

		final TreeItem tiMainWindow = new TreeItem(menuTree,SWT.NULL);
		tiMainWindow.setText("Main Window");

		final TreeItem tiMisc = new TreeItem(menuTree,SWT.NULL);
		tiMisc.setText("Miscellaneous");

		final TreeItem tiUpdate = new TreeItem(menuTree,SWT.NULL);
		tiUpdate.setText("Update");

		final TreeItem tiPlugin = new TreeItem(menuTree,SWT.NULL);
		tiPlugin.setText("Plugin Settings");

		menuTree.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				if(bModified){
					MessageBox box = new MessageBox(parent.getShell(),SWT.OK | SWT.CANCEL);
					box.setText("Usaved Changes");
					box.setMessage("You have made modifications to these settings\n" +
							"Click OK to apply these changes and continue\n" +
					"Click Cancel to discard these changes and continue");
					int answer = box.open();
					switch(answer){

					case(SWT.OK):
						save(parent);
					break;

					case(SWT.CANCEL):
						bModified = false;
					break;

					}

				}


				if(event.item.equals(tiConnection)){
					makeConnectionPreferences(cOptions);
				}else if(event.item.equals(tiMainWindow)){
					makeMainWindowPreferences(cOptions);
				}else if(event.item.equals(tiMisc)){
					makeMiscPreferences(cOptions);
				}else if(event.item.equals(tiUpdate)){
					makeUpdatePreferences(cOptions);
				}else if(event.item.equals(tiPlugin)){
					makePlugPreferences(cOptions);
				}

			}

		});

		sc = new ScrolledComposite(sash, SWT.V_SCROLL);

		cOptions = new Composite(sc,SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		cOptions.setLayoutData(gridData);
		cOptions.setLayout(new GridLayout(2,false));

		sc.setContent(cOptions);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(cOptions.computeSize(r.width, SWT.DEFAULT));
			}
		});


		//default selection
		menuTree.setSelection(new TreeItem[] {tiConnection});
		makeConnectionPreferences(cOptions);

		//set the sash weight
		sash.setWeights(new int[] {80,320});

		//Buttons
		Composite button_comp = new Composite(parent, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		button_comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);


		Button apply = new Button (button_comp,SWT.PUSH);
		apply.setText("Save Changes");
		apply.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				save(parent);
			}
		});

		Button commit = new Button(button_comp,SWT.PUSH);
		commit.setText("Save Changes and Close");
		commit.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				save(parent);
				if(pl != null) RCMain.getRCMain().getClient().removeParameterListener(pl);
				prefsTab.dispose();
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				if(pl != null) RCMain.getRCMain().getClient().removeParameterListener(pl);
				prefsTab.dispose();
			}
		});


		prefsTab.setControl(parent);
		parentTab.setSelection(prefsTab);

		initAzFlexyConf();
	}



	public void makeConnectionPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		//Auto Connect
		autoConnect = new Button(composite,SWT.CHECK);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoConnect.setLayoutData(gridData);
		autoConnect.setText("AutoConnect: If connection data and password are saved, attempt last connection on startup");

		if (Boolean.parseBoolean(properties.getProperty("auto_connect","false"))) {
			autoConnect.setSelection(true);
		}
		addModListener(autoConnect,SWT.Selection);

		//Update Interval Open
		Label updateIntervalOpen_Label = new Label(composite,SWT.NULL);
		updateIntervalOpen_Label.setText("Update Interval while the main window is open (in seconds, minimum of 3):       ");

		updateIntervalOpen_Text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 30;
		updateIntervalOpen_Text.setLayoutData(gridData);
		updateIntervalOpen_Text.setText(String.valueOf(Long.parseLong((properties.getProperty("connection_interval_open","5000")))/1000));
		updateIntervalOpen_Text.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
		addModListener(updateIntervalOpen_Text,SWT.Modify);


		//Update Interval Closed
		Label updateIntervalClosed_Label = new Label(composite,SWT.NULL);
		updateIntervalClosed_Label.setText("Update Interval while the main window is closed (in seconds, minimum of 3):      ");

		updateIntervalClosed_Text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 30;
		updateIntervalClosed_Text.setLayoutData(gridData);
		updateIntervalClosed_Text.setText(String.valueOf(Long.parseLong((properties.getProperty("connection_interval_closed","15000")))/1000));
		updateIntervalClosed_Text.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
		addModListener(updateIntervalClosed_Text,SWT.Modify);
		composite.layout();		
	}


	public void makeMainWindowPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}


		//Auto Open
		autoOpen = new Button(composite,SWT.CHECK);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoOpen.setLayoutData(gridData);
		autoOpen.setText("AutoOpen: Auto open main window when program is started");

		if (Boolean.parseBoolean(properties.getProperty("auto_open","false"))) {
			autoOpen.setSelection(true);
		}

		addModListener(autoOpen,SWT.Selection);


		//Tray options
		trayMinimize = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		trayMinimize.setLayoutData(gridData);
		trayMinimize.setText("Minimizing the main window will send it to the tray");
		trayMinimize.setSelection(Boolean.parseBoolean(properties.getProperty("tray.minimize","true"))?true:false);
		addModListener(trayMinimize,SWT.Selection);


		//Tray options
		trayExit = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		trayExit.setLayoutData(gridData);
		trayExit.setText("Exiting the main window will send it to the tray");
		trayExit.setSelection(Boolean.parseBoolean(properties.getProperty("tray.exit","true"))?true:false);
		addModListener(trayExit,SWT.Selection);

		//Exit Confirmation
		exitConfirm = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		exitConfirm.setLayoutData(gridData);
		exitConfirm.setText("Show confirmation dialog on exit");
		exitConfirm.setSelection(Boolean.parseBoolean(properties.getProperty("confirm.exit","true"))?true:false);
		addModListener(exitConfirm,SWT.Selection);

		//auto console open
		autoConsole = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoConsole.setLayoutData(gridData);
		autoConsole.setText("Auto open console when opening main window");
		autoConsole.setSelection(Boolean.parseBoolean(properties.getProperty("auto_console","false"))?true:false);
		addModListener(autoConsole,SWT.Selection);


		composite.layout();
	}


	public void makeMiscPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}
		//popupsEnabled
		popupsEnabled = new Button(composite,SWT.CHECK);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		popupsEnabled.setLayoutData(gridData);
		popupsEnabled.setText("Popup Alerts Enabled");

		if (Boolean.parseBoolean(properties.getProperty("popups_enabled","true"))) {
			popupsEnabled.setSelection(true);
		}else
			popupsEnabled.setSelection(false);

		addModListener(popupsEnabled,SWT.Selection);


		//show splash screen
		showSplash = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		showSplash.setLayoutData(gridData);
		showSplash.setText("Show splash screen on startup");

		if(Boolean.parseBoolean(properties.getProperty("show_splash","true"))){
			showSplash.setSelection(true);
		}else
			showSplash.setSelection(false);

		addModListener(showSplash,SWT.Selection);


		//AutoClipboard
		autoClipboard = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoClipboard.setLayoutData(gridData);
		autoClipboard.setText("Clipboard Monitor: Monitor the users clipboard at all times for torrent URLs and files");

		if (Boolean.parseBoolean(properties.getProperty("auto_clipboard",Utilities.isLinux()? "false" : "true"))) {
			autoClipboard.setSelection(true);
		}
		addModListener(autoClipboard,SWT.Selection);


		composite.layout();
	}


	public void makeUpdatePreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		//Update betas
		updateBeta = new Button(composite,SWT.CHECK);
		updateBeta.setText("Use Beta Builds:  Allow updates to include potentially non-stable, beta builds of AzSMRC" );
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		updateBeta.setLayoutData(gridData);

		if(Boolean.parseBoolean(properties.getProperty("update.beta", "false"))){
			updateBeta.setSelection(true);
		}else
			updateBeta.setSelection(false);

		addModListener(updateBeta,SWT.Selection);


		//Auto Update Check

		autoUpdateCheck = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoUpdateCheck.setLayoutData(gridData);
		autoUpdateCheck.setText("Auto Check for Updates: Allow AzSMRC to check for and alert the user to updates");

		if (Boolean.parseBoolean(properties.getProperty("update.autocheck","true"))) {
			autoUpdateCheck.setSelection(true);
		}

		addModListener(autoUpdateCheck,SWT.Selection);

		//Perform Auto Update

		autoUpdate = new Button(composite,SWT.CHECK);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		autoUpdate.setLayoutData(gridData);
		autoUpdate.setText("Auto Update: If an update is found, automatically merge the files without any user interaction");

		if (Boolean.parseBoolean(properties.getProperty("update.autoupdate","false"))) {
			autoUpdate.setSelection(true);
		}

		addModListener(autoUpdate,SWT.Selection);

		//update button
		final Button updateCheck = new Button(composite,SWT.PUSH);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		updateCheck.setLayoutData(gridData);
		updateCheck.setText("Check online for updates");
		updateCheck.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				final Updater updater;
				try {
					updater = new Updater(new URL(RemoteConstants.UPDATE_URL),new File("update.xml.gz"),new File(System.getProperty("user.dir")));
					updater.addListener(new UpdateListener() {
						public void exception(Exception e) {


						}
						public void noUpdate() {
							if (RCMain.getRCMain().getMainWindow() != null) {
								RCMain.getRCMain().getMainWindow().setStatusBarText("No Update Available");
							}
							RCMain.getRCMain().getNormalLogger().info("No Update Available");
						}
						public void updateAvailable(Update update) {
							if (RCMain.getRCMain().getMainWindow() != null) {
								RCMain.getRCMain().getMainWindow().setStatusBarText("Update Available: Version "+update.getVersion());
							}
							RCMain.getRCMain().getNormalLogger().info("Update Available: Version "+update.getVersion());
							if (Boolean.parseBoolean(properties.getProperty("update.autoupdate", "false"))) {
								updater.doUpdate();
							}else{
								new UpdateDialog(RCMain.getRCMain().getDisplay(),update,updater);
							}
						}
						public void updateFailed(String reason) {
							if (RCMain.getRCMain().getMainWindow() != null) {
								RCMain.getRCMain().getMainWindow().setStatusBarText("Update Failed",SWT.COLOR_RED);
							}
							RCMain.getRCMain().getNormalLogger().info("Update Failed");
						}
						public void updateFinished() {
							if (RCMain.getRCMain().getMainWindow() != null) {
								RCMain.getRCMain().getMainWindow().setStatusBarText("Update Finished");
							}
							RCMain.getRCMain().getNormalLogger().info("Update Finished");
						}
						public void updateError(String error) {
							// TODO Auto-generated method stub

						}
					});

					updater.checkForUpdates(Boolean.parseBoolean(properties.getProperty("update.beta", "false")));
					properties.setProperty("update.lastcheck",Long.toString(System.currentTimeMillis()));
					RCMain.getRCMain().saveConfig();
				} catch (MalformedURLException e2) {
				}

			}

		});


		composite.layout();
	}


	public void makePlugPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}
		pluginLabel = new Label(composite,SWT.NULL);
		if(!RCMain.getRCMain().connected()){
			pluginLabel.setText("You are not currently connected to the server, therefore no settings are available under this option");
		}else{
			Client client = RCMain.getRCMain().getClient();
			client.transactionStart();

			client.sendGetPluginParameter("singleUserMode", RemoteConstants.PARAMETER_BOOLEAN);

			client.transactionCommit();


			//Add listener for the preferences
			pl = new ParameterListener(){

				public void azParameter(String key, String value, int type) {

				}

				public void pluginParameter(String key, final String value, int type) {
					if(key.equalsIgnoreCase("singleUserMode")){
						Display display = RCMain.getRCMain().getDisplay();
						if(display != null && !display.isDisposed()){
							display.asyncExec(new Runnable(){

								public void run() {
									if(pluginLabel != null && !pluginLabel.isDisposed())
										pluginLabel.setText("Properties received from server");
									if(singleUser != null && !singleUser.isDisposed()){
										singleUser.setEnabled(true);
										if (Boolean.parseBoolean(value)) {
											singleUser.setSelection(true);
										}else
											singleUser.setSelection(false);
									}

								}

							});
						}


					}

				}

				public void coreParameter(String key, String value, int type) {
					// TODO Auto-generated method stub

				}
			};

			RCMain.getRCMain().getClient().addParameterListener(pl);


			pluginLabel.setText("Sending request to server for settings.. Please wait");

			//Single User
			singleUser = new Button(composite,SWT.CHECK);
			GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			singleUser.setLayoutData(gridData);
			singleUser.setText("Enable Single User Mode");
			singleUser.setEnabled(false);


			addModListener(singleUser,SWT.Selection);


			Button bCertWiz = new Button(composite, SWT.PUSH);
			bCertWiz.setText("Open SSL Wizard");
			bCertWiz.setToolTipText("Opens a wizard to help with creating a certificate and " +
			"enabling secure connection between the remote and server");
			gridData = new GridData(GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			bCertWiz.setLayoutData(gridData);
			bCertWiz.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event arg0) {
					SSLCertWizard.open();

				}

			});

		}

		RCMain.getRCMain().getClient().sendListTransfers(RemoteConstants.ST_ALL);
		composite.layout();
	}


	public void addModListener(Control control, int selectionType){
		control.addListener(selectionType, new Listener(){
			public void handleEvent(Event arg0) {
				bModified = true;
			}
		});
	}

	public void save(Composite parent){
		if((updateIntervalOpen_Text != null && !updateIntervalOpen_Text.isDisposed())||
				(updateIntervalClosed_Text != null && !updateIntervalClosed_Text.isDisposed())){
			String open = updateIntervalOpen_Text.getText();
			String closed = updateIntervalClosed_Text.getText();
			if(open.equalsIgnoreCase("") || closed.equalsIgnoreCase("") ){
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Error");
				messageBox.setMessage("None of the intervals can be blank.");
				messageBox.open();
				return;
			}else if(Long.parseLong(open) < 3){
				updateIntervalOpen_Text.setText("3");
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Error");
				messageBox.setMessage("The interval for when the main window is open is too low.  Please set to 3 seconds or above.");
				messageBox.open();
				return;
			}else if(Long.parseLong(closed) < 3){
				updateIntervalClosed_Text.setText("3");
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Error");
				messageBox.setMessage("The interval for when the main window is closed is too low.  Please set to 3 seconds or above.");
				messageBox.open();
				return;
			}else{
				properties.setProperty("connection_interval_open", String.valueOf(Long.parseLong(open)*1000));
				properties.setProperty("connection_interval_closed", String.valueOf(Long.parseLong(closed)*1000));
			}
		}


		//Store AutoOpen
		if(autoOpen != null && !autoOpen.isDisposed()){
			if(autoOpen.getSelection())
				properties.setProperty("auto_open", "true");
			else
				properties.setProperty("auto_open", "false");
		}

		//Store AutoSave
		if(autoConnect != null && !autoConnect.isDisposed()){
			if(autoConnect.getSelection())
				properties.setProperty("auto_connect", "true");
			else
				properties.setProperty("auto_connect", "false");
		}

		//update Beta
		if(updateBeta != null && !updateBeta.isDisposed()){
			if(updateBeta.getSelection())
				properties.setProperty("update.beta", "true");
			else
				properties.setProperty("update.beta", "false");
		}




		//Store AutoUpdateCheck
		if(autoUpdateCheck != null && !autoUpdateCheck.isDisposed()){
			if(autoUpdateCheck.getSelection())
				properties.setProperty("update.autocheck", "true");
			else
				properties.setProperty("update.autocheck", "false");
		}



		//Store AutoUpdate
		if(autoUpdate != null && !autoUpdate.isDisposed()){
			if(autoUpdate.getSelection())
				properties.setProperty("update.autoupdate", "true");
			else
				properties.setProperty("update.autoupdate", "false");
		}


		//Store tray options
		if(trayMinimize != null && !trayMinimize.isDisposed()){
			if(trayMinimize.getSelection())
				properties.setProperty("tray.minimize","true");
			else
				properties.setProperty("tray.minimize","false");
		}

		if(trayExit != null && !trayExit.isDisposed()){
			if(trayExit.getSelection())
				properties.setProperty("tray.exit","true");
			else
				properties.setProperty("tray.exit","false");
		}


		//Store Confirm exit
		if(exitConfirm != null && !exitConfirm.isDisposed()){
			if(exitConfirm.getSelection())
				properties.setProperty("confirm.exit", "true");
			else
				properties.setProperty("confirm.exit", "false");
		}

		//Store popupsEnabled
		if(popupsEnabled != null && !popupsEnabled.isDisposed()){
			if(popupsEnabled.getSelection())
				properties.setProperty("popups_enabled", "true");
			else
				properties.setProperty("popups_enabled", "false");
		}


		//Store Splash Screen Setting
		if(showSplash != null && !showSplash.isDisposed()){
			if(showSplash.getSelection())
				properties.setProperty("show_splash","true");
			else
				properties.setProperty("show_splash","false");
		}

		//Store the autoClipboard setting
		if(autoClipboard != null && !autoClipboard.isDisposed()){
			if(autoClipboard.getSelection())
				properties.setProperty("auto_clipboard", "true");
			else
				properties.setProperty("auto_clipboard", "false");
		}

		//Store auto console setting
		if(autoConsole != null && !autoConsole.isDisposed()){
			if(autoConsole.getSelection())
				properties.setProperty("auto_console", "true");
			else
				properties.setProperty("auto_console", "false");
		}

		//Plugin Setting to core
		if(singleUser != null && !singleUser.isDisposed()){
			Client client = RCMain.getRCMain().getClient();
			client.transactionStart();

			if(singleUser.getSelection())
				client.sendSetPluginParameter("singleUserMode", "true", RemoteConstants.PARAMETER_BOOLEAN);
			else{
				client.sendSetPluginParameter("singleUserMode", "false", RemoteConstants.PARAMETER_BOOLEAN);
				RCMain.getRCMain().getMainWindow().setStatusBarText("Connected in Multi User Mode", SWT.COLOR_DARK_GREEN);
			}

			client.transactionCommit();

		}





		//Reset the boolean for modified
		bModified = false;

		//Save all changes
		RCMain.getRCMain().saveConfig();

		//Update Timer
		RCMain.getRCMain().updateTimer(true);
	}

	private void initAzFlexyConf() {
		System.out.println("Trying to initialize AzRemoteConf");
		try {
			final I18NTranslator i18n = new I18NTranslator();
			InputStream i18nDIs = this.getClass().getClassLoader().getResourceAsStream("lbms/azsmrc/remote/client/swtgui/flexyconf/MessagesBundle.properties");
			if (i18nDIs!=null) {
				i18n.initialize(i18nDIs);
				System.out.println("I18N: initialized");
				String lang = RCMain.getRCMain().getProperties().getProperty("language");
				if (lang!=null && !lang.equals("")) {
					InputStream i18nLIs = this.getClass().getClassLoader().getResourceAsStream("lbms/azsmrc/remote/client/swtgui/flexyconf/MessagesBundle_"+lang+".properties");
					if (i18nLIs!=null)
						i18n.load(i18nLIs);
					System.out.println("I18N: language loaded");
				}
			}

			//I18N i18n = I18N.
			InputStream fcIs = this.getClass().getClassLoader().getResourceAsStream("lbms/azsmrc/remote/client/swtgui/flexyconf/AzureusPreferences.xml");
			fc = FlexyConfiguration.readFromStream(fcIs);
			Client client = RCMain.getRCMain().getClient();
			final FCInterface fci = fc.getFCInterface();
			fci.setI18NProvider(new I18NProvider() {
				public String translate(String key) {
					System.out.println("I18N: "+key);
					return i18n.translate(key);
				}
			});
			fci.setContentProvider(new ContentProvider() {

				Client client = RCMain.getRCMain().getClient();

				public String getDefaultValue(String key, int type) {
					System.out.println("AzConf Get Def: "+key+" type: "+type );
					String v = azProps.getProperty(key);
					if (v==null) {
						client.sendGetCoreParameter(key, type);
						switch (type) {
						case Entry.TYPE_STRING:
							return "Loading Preferences...";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}
				public String getValue(String key, int type) {
					System.out.println("AzConf Get: "+key+" type: "+type );
					String v = azProps.getProperty(key);
					if (v==null) {
						client.sendGetCoreParameter(key, type);
						switch (type) {
						case Entry.TYPE_STRING:
							return "Loading Preferences...";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}
				public void setValue(String key, String value, int type) {
					System.out.println("AzConf Set: "+key+" value: "+value+" type: "+type);
					client.sendSetCoreParameter(key, value, type);
					azProps.setProperty(key, value);
				}
			});

			client.addParameterListener(azParam);

			//this will query the whole AzConfiguration at once
			client.transactionStart();
			fc.getRootSection().initAll();
			client.transactionCommit();

			fcm = new SWTMenu(fc,menuTree,cOptions);
			fcm.addAsSubItem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeAzFlexyConf() {
		RCMain.getRCMain().getClient().removeParameterListener(azParam);
	}

}
