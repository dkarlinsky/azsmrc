/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.swtgui.FireFrogMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PreferencesTab {

	private Text updateIntervalOpen_Text, updateIntervalClosed_Text;
	private Button autoOpen, autoConnect, autoUpdateCheck, autoUpdate;
	private Button trayMinimize, trayExit, popupsEnabled, autoClipboard, autoConsole;

	private Composite cOptions;
	private Properties properties;


	private boolean bModified = false;

	public PreferencesTab(final CTabFolder parentTab){


		//Open properties for reading and saving
		properties = FireFrogMain.getFFM().getProperties();


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
			}

		});



		//top label
		Composite grayLabel = new Composite(parent,SWT.BORDER);
		grayLabel.setBackground(FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_GRAY));
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
		Font newFont = new Font(FireFrogMain.getFFM().getDisplay(), fontData);
		title.setFont(newFont);
		newFont.dispose();

		grayLabel.pack();




		final SashForm sash = new SashForm(parent,SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		sash.setLayoutData(gridData);
		sash.setLayout(new GridLayout(1,false));


		//Tree on left side
		Tree tree = new Tree(sash,SWT.BORDER | SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		final TreeItem tiConnection = new TreeItem(tree,SWT.NULL);
		tiConnection.setText("Connection");

		final TreeItem tiMainWindow = new TreeItem(tree,SWT.NULL);
		tiMainWindow.setText("Main Window");

		final TreeItem tiMisc = new TreeItem(tree,SWT.NULL);
		tiMisc.setText("Miscellaneous");

		final TreeItem tiUpdate = new TreeItem(tree,SWT.NULL);
		tiUpdate.setText("Update");
		tree.addListener(SWT.Selection, new Listener(){

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
				}

			}

		});

		cOptions = new Composite(sash,SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		cOptions.setLayoutData(gridData);
		cOptions.setLayout(new GridLayout(2,false));

		//default selection
		tree.setSelection(new TreeItem[] {tiConnection});
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
				prefsTab.dispose();
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				prefsTab.dispose();
			}
		});


		prefsTab.setControl(parent);
		parentTab.setSelection(prefsTab);
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

		if (Boolean.parseBoolean(properties.getProperty("auto_connect","true"))) {
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
		//Auto Update Check

		autoUpdateCheck = new Button(composite,SWT.CHECK);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
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
							if (FireFrogMain.getFFM().getMainWindow() != null) {
								FireFrogMain.getFFM().getMainWindow().setStatusBarText("No Update Available");
							}
							FireFrogMain.getFFM().getNormalLogger().info("No Update Available");
						}
						public void updateAvailable(Update update) {
							if (FireFrogMain.getFFM().getMainWindow() != null) {
								FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Available: Version "+update.getVersion());
							}
							FireFrogMain.getFFM().getNormalLogger().info("Update Available: Version "+update.getVersion());
							if (Boolean.parseBoolean(properties.getProperty("update.autoupdate", "false"))) {
								updater.doUpdate();
							}else{
								new UpdateDialog(FireFrogMain.getFFM().getDisplay(),update,updater);
							}
						}
						public void updateFailed(String reason) {
							if (FireFrogMain.getFFM().getMainWindow() != null) {
								FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Failed",SWT.COLOR_RED);
							}
							FireFrogMain.getFFM().getNormalLogger().info("Update Failed");
						}
						public void updateFinished() {
							if (FireFrogMain.getFFM().getMainWindow() != null) {
								FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Finished");
							}
							FireFrogMain.getFFM().getNormalLogger().info("Update Finished");
						}
						public void updateError(String error) {
							// TODO Auto-generated method stub

						}
					});

					updater.checkForUpdates(false); //TODO fix the config thing or whatever
				} catch (MalformedURLException e2) {
				}



			}

		});
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

		//Store popupsEnabled
		if(popupsEnabled != null && !popupsEnabled.isDisposed()){
			if(popupsEnabled.getSelection())
				properties.setProperty("popups_enabled", "true");
			else
				properties.setProperty("popups_enabled", "false");
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

		//Reset the boolean for modified
		bModified = false;

		//Save all changes
		FireFrogMain.getFFM().saveConfig();

		//Update Timer
		FireFrogMain.getFFM().updateTimer(true);
	}

}
