/**
 * First time startup wizard.. should only run once
 * and allow the user to set up their settings
 *
 *
 */

package lbms.azsmrc.remote.client.swtgui;


import java.io.File;


import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.sound.Sound;
import lbms.azsmrc.remote.client.swtgui.sound.SoundManager;
import lbms.tools.ExtendedProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class StartupWizard {

	//Global Display
	private Display display;

	//Main Shell for wizard
	private Shell shell;

	//Sash for shell
	private SashForm sash;

	//Main composite for Right and Left hand sides
	private Composite lComp;
	private Composite rComp;
	private Composite parent;

	//Labels for steps
	private Label step1, step2, step3, step4, step5;

	//Step we are on
	int step = 1;

	//Buttons for bComp
	private Button btnPrevious, btnContinue;

	//Pull the properties to use for this whole file
	private ExtendedProperties properties = RCMain.getRCMain().getProperties();


	//
	//Stored Info
	//

	//booleans
	private boolean update_autocheck;
	private boolean update_autoupdate;
	private boolean update_beta;
	private boolean useStats;


	//integers
	private int connectionInterval_open;
	private int connectionInterval_closed;

	//Strings
	private String lang;


	//instance to make sure no more than one of these is open
	private static StartupWizard instance;


	//sound stuff
	private Group soundSelectionGroup;
	private Label downloadAddedLabel;
	private Text downloadAddedText;
	private Button downloadAddedButton, downloadAdded_testSound;
	private Label downloadFinishedLabel;
	private Text downloadFinishedText;
	private Button downloadFinishedButton, downloadFinished_testSound;
	private Label seedingFinishedLabel;
	private Text seedingFinishedText;
	private Button seedingFinishedButton, seedingFinished_testSound;
	private Label errorLabel;
	private Text errorText;
	private Button errorButton, error_testSound;



	/**
	 * Main open
	 * @param _display
	 */
	private StartupWizard(){
		//pull the display and check it
		display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;


		instance = this;


		//
		//Stored Info
		//

		//first check to make sure properties is not null.. if so, bail
		if(properties == null) return;

		//booleans
		update_autocheck = Boolean.parseBoolean(properties.getProperty("update.autocheck"));
		update_autoupdate = Boolean.parseBoolean(properties.getProperty("update.autoupdate"));
		update_beta = Boolean.parseBoolean(properties.getProperty("update.beta"));
		useStats = Boolean.parseBoolean(properties.getProperty("statistics.allow"));



		//integers
		connectionInterval_open = Integer.valueOf(properties.getProperty("connection_interval_open"));
		connectionInterval_closed = Integer.valueOf(properties.getProperty("connection_interval_closed"));

		//Strings
		lang = properties.getProperty("language");


		shell = new Shell(display /*SWT.APPLICATION_MODAL*/);
		shell.setLayout(new GridLayout(1,false));
		shell.setText("AzSMRC Startup Wizard");

		//Shell listener to make sure we dispose of everything
		shell.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {

			}

		});


		//icon
		if(!Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));

		//put sash on shell
		sash = new SashForm(shell,SWT.HORIZONTAL);
		sash.setLayout(new GridLayout());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 450;
		gridData.widthHint  = 750;
		sash.setLayoutData(gridData);

		//Left hand list side
		lComp = new Composite(sash, SWT.BORDER);
		lComp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		lComp.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_BOTH);
		lComp.setLayoutData(gridData);

		//Labels for lComp
		step1 = new Label(lComp, SWT.LEFT);
		step1.setText("Step 1");
		step1.setBackground(display.getSystemColor(SWT.COLOR_GRAY));

		step2 = new Label(lComp, SWT.LEFT);
		step2.setText("Step 2");
		step2.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		step3 = new Label(lComp, SWT.LEFT);
		step3.setText("Step 3");
		step3.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		step4 = new Label(lComp, SWT.LEFT);
		step4.setText("Step 4");
		step4.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		step5 = new Label(lComp, SWT.LEFT);
		step5.setText("Step 5");
		step5.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		//Right hand composite
		rComp = new Composite(sash, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		rComp.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_BOTH);
		rComp.setLayoutData(gridData);


		//Main Parent Composite to draw on for right side
		parent = new Composite(rComp, SWT.BORDER);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		//Button Composite
		Composite bComp = new Composite(rComp, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		bComp.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		bComp.setLayoutData(gridData);


		btnPrevious = new Button(bComp, SWT.PUSH);
		btnPrevious.setText("Previous");
		btnPrevious.setEnabled(false);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		btnPrevious.setLayoutData(gridData);
		btnPrevious.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(step!=0)
					loadStep(--step);
			}
		});

		btnContinue = new Button(bComp, SWT.PUSH);
		btnContinue.setText("Continue");
		btnContinue.setEnabled(true);
		btnContinue.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(step!=5)
					loadStep(++step);
				else{
					//This is where we need to commit everything
					try{
						if(properties != null){
							//booleans
							properties.setProperty("update.autocheck",Boolean.toString(update_autocheck));
							properties.setProperty("update.autoupdate",Boolean.toString(update_autoupdate));
							properties.setProperty("update.beta",Boolean.toString(update_beta));
							properties.setProperty("statistics.allow",Boolean.toString(useStats));

							//integers
							properties.setProperty("connection_interval_open",Integer.toString(connectionInterval_open));
							properties.setProperty("connection_interval_closed",Integer.toString(connectionInterval_closed));

							//Strings
							properties.setProperty("language", lang);


							//save the properties
							RCMain.getRCMain().saveConfig();

						}
					}catch(Exception e){
					}
					shell.dispose();
				}

			}
		});



		sash.setWeights(new int[] {10,90});
		//open up the first step
		step = 1;
		loadStep(step);

		//open shell
		GUI_Utilities.centerShellandOpen(shell);

	}

	/**
	 * Step 1
	 */
	private void step1(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		btnPrevious.setEnabled(false);
		btnContinue.setText("Continue");

		//Welcome stuff here
		Label welcome1 = new Label(parent, SWT.CENTER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		welcome1.setLayoutData(gridData);

		welcome1.setText("Welcome to AzSMRC");


		Label welcome2 = new Label(parent, SWT.CENTER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		welcome2.setLayoutData(gridData);
		welcome2.setText("\n\nThis wizard will guide you\n" +
				"through setting up AzSMRC\n" +
				"Please take the time to complete this wizard");

		parent.layout();
	}

	/**
	 * Step 2
	 * Configure the language for azsmrc to use
	 */
	private void step2(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");

		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(2,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		//Label explaining everything
		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("Please choose the default language for AzSMRC to use");


		Label cLabel = new Label(comp, SWT.NULL);
		cLabel.setText("Language: ");



		final Combo language_combo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		language_combo.add("English");
		language_combo.add("Deutsch");


		if(lang.equals("en_EN"))
			language_combo.select(0);
		else if(lang.equals("de_DE"))
			language_combo.select(1);

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		language_combo.setLayoutData(gd);

		language_combo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				int choice = language_combo.getSelectionIndex();
				switch (choice){

				case 0: lang = "en_EN";
						break;
				case 1: lang = "de_DE";
						break;
				}

			}

			public void widgetSelected(SelectionEvent arg0) {}
		});



		parent.layout();
	}


	/**
	 * Step 3
	 * Updates, etc.
	 */
	private void step3(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");


		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(1,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);



		//Update Group
		Group updateGroup = new Group(comp, SWT.NONE);
		updateGroup.setText("AzSMRC Auto Update");
		updateGroup.setLayout(new GridLayout(1, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		updateGroup.setLayoutData(gd);

		final Button btnUpdate_autocheck = new Button(updateGroup, SWT.CHECK);
		btnUpdate_autocheck.setText("Allow AzSMRC to periodically check for updates");
		btnUpdate_autocheck.setSelection(update_autocheck);


		final Button btnUpdate_beta = new Button(updateGroup, SWT.CHECK);
		btnUpdate_beta.setText("Allow updates to include potentially non-stable, beta builds of AzSMRC");
		btnUpdate_beta.setSelection(update_beta);


		final Button btnUpdate_autoupdate = new Button(updateGroup, SWT.CHECK);
		btnUpdate_autoupdate.setText("If an update is found, automatically merge the files without any user interaction");
		btnUpdate_autoupdate.setSelection(update_autoupdate);






		if(!update_autocheck){
			btnUpdate_autoupdate.setSelection(false);
			btnUpdate_beta.setSelection(false);
		}

		//Listeners for buttons
		btnUpdate_autocheck.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				update_autocheck = btnUpdate_autocheck.getSelection();
				if(!update_autocheck){
					btnUpdate_autoupdate.setSelection(false);
					btnUpdate_autoupdate.setEnabled(false);
					update_autoupdate = false;
					update_beta = false;
					btnUpdate_beta.setSelection(false);
					btnUpdate_beta.setEnabled(false);
				}else{
					btnUpdate_autoupdate.setEnabled(true);
					btnUpdate_beta.setEnabled(true);
					btnUpdate_autoupdate.setSelection(update_autoupdate);
					btnUpdate_beta.setSelection(update_beta);
				}
			}

		});

		btnUpdate_autoupdate.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				update_autoupdate = btnUpdate_autoupdate.getSelection();

			}

		});

		btnUpdate_beta.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				update_beta = btnUpdate_beta.getSelection();

			}

		});

		//Stats

		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("Allow AzSMRC to send version and a random ID for anonymouse usage statistics. \nThe data is stored for 24h and will be deleted afterwards");

		final Button btnAllow_stats = new Button(comp, SWT.CHECK);
		btnAllow_stats.setText("Allow Statistics");
		btnAllow_stats.setSelection(useStats);
		btnAllow_stats.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				useStats = btnAllow_stats.getSelection();

			}

		});


		parent.layout();
	}

	/**
	 * Step 4
	 * 	private int connectionInterval_open;
	 * 	private int connectionInterval_closed;
	 */
	private void step4(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");

		Composite comp = new Composite(parent,SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		gl.verticalSpacing = 50;
		comp.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);


		//Open Label
		Label openLabel = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		openLabel.setLayoutData(gd);

		openLabel.setText("Update interval while the main window is open\n(in milliseconds, minimum of 3000 [3 Seconds])");


		//Open Text box
		final Text openText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		openText.setText(String.valueOf(connectionInterval_open));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = 75;
		openText.setLayoutData(gd);
		openText.addListener (SWT.Verify, new Listener () {
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

		openText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				try{
					int num = Integer.valueOf(openText.getText());
					if(num < 3000){
						openText.setText("3000");
						num = 3000;
					}
					connectionInterval_open = num;
				}catch(Exception e){
					openText.setText("5000");
					connectionInterval_open = 5000;
				}

			}
		});

		//Open Label
		Label trayLabel = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		trayLabel.setLayoutData(gd);

		trayLabel.setText("Update interval while the main window is closed\n(in milliseconds, minimum of 3000 [3 Seconds])");


		//Open Text box
		final Text trayText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		trayText.setText(String.valueOf(connectionInterval_closed));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = 75;
		trayText.setLayoutData(gd);
		trayText.addListener (SWT.Verify, new Listener () {
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

		trayText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				try{
					int num = Integer.valueOf(openText.getText());
					if(num < 3000){
						trayText.setText("3000");
						num = 3000;
					}
					connectionInterval_closed = num;
				}catch(Exception e){
					openText.setText("15000");
					connectionInterval_closed = 15000;
				}

			}
		});

		parent.layout();
	}


	/**
	 * Step 5
	 *
	 * Sound
	 */
	private void step5(){
		//since we are using the code from the preferences, we need to spoof the PFX
		final String PFX = "tab.preferencestab.";


		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		btnPrevious.setEnabled(true);
		btnContinue.setText("Finish");

		final Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(2,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		//button for sound being on or off
		final Button soundActive = new Button(comp, SWT.CHECK);
		soundActive.setText(I18N.translate(PFX + "soundManager.active"));
		soundActive.setSelection(Boolean.parseBoolean(properties.getProperty("soundManager.active", "true")));

		final Button soundDefault = new Button(comp, SWT.CHECK);
		soundDefault.setText(I18N.translate(PFX + "soundManager.default"));
		soundDefault.setSelection(Boolean.parseBoolean(properties.getProperty("soundManager.useDefaults", "true")));
		gd = new GridData();
		gd.horizontalSpan = 2;
		soundDefault.setLayoutData(gd);

		//Group for user defined sounds
		soundSelectionGroup = new Group(comp, SWT.NULL);
		soundSelectionGroup.setText(I18N.translate(PFX + "sound.soundSelection.Group.text"));
		soundSelectionGroup.setLayout(new GridLayout(3,false));
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.widthHint = 400;
		soundSelectionGroup.setLayoutData(gd);


		//---- Download Added
		downloadAddedLabel = new Label(soundSelectionGroup, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 3;
		downloadAddedLabel.setLayoutData(gd);
		downloadAddedLabel.setText(I18N.translate(PFX + "sound.soundSelection.downloadAdded.label"));


		downloadAddedText = new Text(soundSelectionGroup, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		downloadAddedText.setLayoutData(gd);
		downloadAddedText.setText(properties.getProperty("sound.DownloadAdded", RCMain.USER_DIR + RCMain.FSEP +"sounds/download_added.wav"));

		downloadAddedButton = new Button(soundSelectionGroup, SWT.PUSH);
		downloadAddedButton.setImage(ImageRepository.getImage("open_by_file"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		downloadAddedButton.setLayoutData(gd);
		downloadAddedButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog (comp.getShell(), SWT.OPEN);
				dialog.setFilterNames (new String [] {"wav Files", "All Files (*.*)"});
				dialog.setFilterExtensions (new String [] {"*.wav", "*.*"}); //Windows wild cards
				dialog.setFilterPath (RCMain.USER_DIR + RCMain.FSEP + "sound"); //Windows path
				dialog.setFileName ("download_added.wav");

				String returnedString = dialog.open();
				if(returnedString != null){
					File file = new File(returnedString);
					if(file.exists() && file.canRead()){
						properties.setProperty("soundManager.active", "true");
						properties.setProperty("soundManager.useDefaults", "false");
						properties.setProperty("sound.DownloadAdded", returnedString);
						RCMain.getRCMain().saveConfig();

						//reload all the sounds
						RCMain.getRCMain().loadSounds();

						soundActive.setSelection(true);
						soundDefault.setSelection(false);
						downloadAddedText.setText(returnedString);
					}
				}
			}
		});

		downloadAdded_testSound = new Button(soundSelectionGroup, SWT.PUSH);
		downloadAdded_testSound.setImage(ImageRepository.getImage("resume"));
		downloadAdded_testSound.setToolTipText(I18N.translate(PFX + "sound.soundSelection.playsound.tooltip"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		downloadAdded_testSound.setLayoutData(gd);
		downloadAdded_testSound.addListener(SWT.Selection, new Listener(){
			public void handleEvent (Event e){
				try{
					if(!SoundManager.playSound(Sound.DOWNLOAD_ADDED)){
						MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
				}
			}
		});

		//----- Download Finished
		downloadFinishedLabel = new Label(soundSelectionGroup, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 3;
		downloadFinishedLabel.setLayoutData(gd);
		downloadFinishedLabel.setText(I18N.translate(PFX + "sound.soundSelection.downloadFinished.label"));


		downloadFinishedText = new Text(soundSelectionGroup, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		downloadFinishedText.setLayoutData(gd);
		downloadFinishedText.setText(properties.getProperty("sound.DownloadingFinished", RCMain.USER_DIR + RCMain.FSEP +"sounds/download_finished.wav"));

		downloadFinishedButton = new Button(soundSelectionGroup, SWT.PUSH);
		downloadFinishedButton.setImage(ImageRepository.getImage("open_by_file"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		downloadFinishedButton.setLayoutData(gd);
		downloadFinishedButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog (comp.getShell(), SWT.OPEN);
				dialog.setFilterNames (new String [] {"wav Files", "All Files (*.*)"});
				dialog.setFilterExtensions (new String [] {"*.wav", "*.*"}); //Windows wild cards
				dialog.setFilterPath (RCMain.USER_DIR + RCMain.FSEP + "sound"); //Windows path
				dialog.setFileName ("download_finished.wav");

				String returnedString = dialog.open();
				if(returnedString != null){
					File file = new File(returnedString);
					if(file.exists() && file.canRead()){
						properties.setProperty("soundManager.active", "true");
						properties.setProperty("soundManager.useDefaults", "false");
						properties.setProperty("sound.DownloadingFinished", returnedString);
						RCMain.getRCMain().saveConfig();

						//reload all the sounds
						RCMain.getRCMain().loadSounds();

						soundActive.setSelection(true);
						soundDefault.setSelection(false);
						downloadFinishedText.setText(returnedString);
					}
				}
			}
		});

		downloadFinished_testSound = new Button(soundSelectionGroup, SWT.PUSH);
		downloadFinished_testSound.setImage(ImageRepository.getImage("resume"));
		downloadFinished_testSound.setToolTipText(I18N.translate(PFX + "sound.soundSelection.playsound.tooltip"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		downloadFinished_testSound.setLayoutData(gd);
		downloadFinished_testSound.addListener(SWT.Selection, new Listener(){
			public void handleEvent (Event e){
				try{
					if(!SoundManager.playSound(Sound.DOWNLOADING_FINISHED)){
						MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
				}
			}
		});


		//----- Seeding Finished
		seedingFinishedLabel = new Label(soundSelectionGroup, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 3;
		seedingFinishedLabel.setLayoutData(gd);
		seedingFinishedLabel.setText(I18N.translate(PFX + "sound.soundSelection.seedingFinished.label"));


		seedingFinishedText = new Text(soundSelectionGroup, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		seedingFinishedText.setLayoutData(gd);
		seedingFinishedText.setText(properties.getProperty("sound.SeedingFinished", RCMain.USER_DIR + RCMain.FSEP +"sounds/seeding_finished.wav"));

		seedingFinishedButton = new Button(soundSelectionGroup, SWT.PUSH);
		seedingFinishedButton.setImage(ImageRepository.getImage("open_by_file"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		seedingFinishedButton.setLayoutData(gd);
		seedingFinishedButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog (comp.getShell(), SWT.OPEN);
				dialog.setFilterNames (new String [] {"wav Files", "All Files (*.*)"});
				dialog.setFilterExtensions (new String [] {"*.wav", "*.*"}); //Windows wild cards
				dialog.setFilterPath (RCMain.USER_DIR + RCMain.FSEP + "sound"); //Windows path
				dialog.setFileName ("seeding_finished.wav");

				String returnedString = dialog.open();
				if(returnedString != null){
					File file = new File(returnedString);
					if(file.exists() && file.canRead()){
						properties.setProperty("soundManager.active", "true");
						properties.setProperty("soundManager.useDefaults", "false");
						properties.setProperty("sound.SeedingFinished", returnedString);
						RCMain.getRCMain().saveConfig();

						//reload all the sounds
						RCMain.getRCMain().loadSounds();

						soundActive.setSelection(true);
						soundDefault.setSelection(false);
						downloadFinishedText.setText(returnedString);
					}
				}
			}
		});

		seedingFinished_testSound = new Button(soundSelectionGroup, SWT.PUSH);
		seedingFinished_testSound.setImage(ImageRepository.getImage("resume"));
		seedingFinished_testSound.setToolTipText(I18N.translate(PFX + "sound.soundSelection.playsound.tooltip"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		seedingFinished_testSound.setLayoutData(gd);
		seedingFinished_testSound.addListener(SWT.Selection, new Listener(){
			public void handleEvent (Event e){
				try{
					if(!SoundManager.playSound(Sound.SEEDING_FINISHED)){
						MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
				}
			}
		});



		//----- Error
		errorLabel = new Label(soundSelectionGroup, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 3;
		errorLabel.setLayoutData(gd);
		errorLabel.setText(I18N.translate(PFX + "sound.soundSelection.error.label"));


		errorText = new Text(soundSelectionGroup, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		errorText.setLayoutData(gd);
		errorText.setText(properties.getProperty("sound.Error", RCMain.USER_DIR + RCMain.FSEP +"sounds/error.wav"));

		errorButton = new Button(soundSelectionGroup, SWT.PUSH);
		errorButton.setImage(ImageRepository.getImage("open_by_file"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		errorButton.setLayoutData(gd);
		errorButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog (comp.getShell(), SWT.OPEN);
				dialog.setFilterNames (new String [] {"wav Files", "All Files (*.*)"});
				dialog.setFilterExtensions (new String [] {"*.wav", "*.*"}); //Windows wild cards
				dialog.setFilterPath (RCMain.USER_DIR + RCMain.FSEP + "sound"); //Windows path
				dialog.setFileName ("error.wav");

				String returnedString = dialog.open();
				if(returnedString != null){
					File file = new File(returnedString);
					if(file.exists() && file.canRead()){
						properties.setProperty("soundManager.active", "true");
						properties.setProperty("soundManager.useDefaults", "false");
						properties.setProperty("sound.Error", returnedString);
						RCMain.getRCMain().saveConfig();

						//reload all the sounds
						RCMain.getRCMain().loadSounds();

						soundActive.setSelection(true);
						soundDefault.setSelection(false);
						errorText.setText(returnedString);
					}
				}
			}
		});

		error_testSound = new Button(soundSelectionGroup, SWT.PUSH);
		error_testSound.setImage(ImageRepository.getImage("resume"));
		error_testSound.setToolTipText(I18N.translate(PFX + "sound.soundSelection.playsound.tooltip"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		error_testSound.setLayoutData(gd);
		error_testSound.addListener(SWT.Selection, new Listener(){
			public void handleEvent (Event e){
				try{
					if(!SoundManager.playSound(Sound.ERROR)){
						MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
				}
			}
		});



		setUseDefault(!soundActive.getSelection());


		soundDefault.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				properties.setProperty("soundManager.useDefaults", Boolean.toString(soundDefault.getSelection()));
				RCMain.getRCMain().saveConfig();
				if(soundDefault.getSelection()){
					soundSelectionGroup.setEnabled(false);

					//reset everthing to defaults
					properties.setProperty("sound.Error", "sounds/error.wav");
					properties.setProperty("sound.DownloadAdded", "sounds/download_added.wav");
					properties.setProperty("sound.DownloadingFinished", "sounds/download_finished.wav");
					properties.setProperty("sound.SeedingFinished", "sounds/seeding_finished.wav");
					RCMain.getRCMain().saveConfig();

					errorText.setText(properties.getProperty("sound.Error", RCMain.USER_DIR + RCMain.FSEP +"sounds/error.wav"));
					seedingFinishedText.setText(properties.getProperty("sound.SeedingFinished", RCMain.USER_DIR + RCMain.FSEP +"sounds/seeding_finished.wav"));
					downloadFinishedText.setText(properties.getProperty("sound.DownloadingFinished", RCMain.USER_DIR + RCMain.FSEP +"sounds/download_finished.wav"));
					downloadAddedText.setText(properties.getProperty("sound.DownloadAdded", RCMain.USER_DIR + RCMain.FSEP +"sounds/download_added.wav"));

					setUseDefault(true);
				}else{
					setUseDefault(false);
				}
			}
		});


		//listener at the end so that we have all the buttons in place
		soundActive.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				properties.setProperty("soundManager.active", Boolean.toString(soundActive.getSelection()));
				RCMain.getRCMain().saveConfig();
				if(soundActive.getSelection()){
					soundDefault.setEnabled(true);
					soundDefault.setSelection(Boolean.parseBoolean(properties.getProperty("soundManager.useDefaults", "true")));
					setUseDefault(soundDefault.getSelection());
					RCMain.getRCMain().loadSounds();
				}else{
					soundDefault.setEnabled(false);
					setUseDefault(true);
					RCMain.getRCMain().unLoadSounds();
				}

			}
		});




		comp.layout();

		parent.layout();
	}

	private void setUseDefault(boolean useDefault){
		soundSelectionGroup.setEnabled(!useDefault);
		downloadAddedLabel.setEnabled(!useDefault);
		downloadAddedText.setEnabled(!useDefault);
		downloadAddedButton.setEnabled(!useDefault);
		downloadAdded_testSound.setEnabled(!useDefault);
		downloadFinishedLabel.setEnabled(!useDefault);
		downloadFinishedText.setEnabled(!useDefault);
		downloadFinishedButton.setEnabled(!useDefault);
		downloadFinished_testSound.setEnabled(!useDefault);
		seedingFinishedLabel.setEnabled(!useDefault);
		seedingFinishedText.setEnabled(!useDefault);
		seedingFinishedButton.setEnabled(!useDefault);
		seedingFinished_testSound.setEnabled(!useDefault);
		errorLabel.setEnabled(!useDefault);
		errorText.setEnabled(!useDefault);
		errorButton.setEnabled(!useDefault);
		error_testSound.setEnabled(!useDefault);
	}


	/**
	 * Loads a step of given integer
	 */
	private void loadStep(int stepToLoad){
		switch (stepToLoad){
		case 1:
			step1.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step2.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step3.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step4.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step5.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step1();
			break;
		case 2:
			step1.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step2.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step3.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step4.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step5.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step2();
			break;
		case 3:
			step1.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step2.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step3.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step4.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step5.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step3();
			break;
		case 4:
			step1.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step2.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step3.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step4.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step5.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step4();
			break;
		case 5:
			step1.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step2.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step3.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step4.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			step5.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step5();
			break;
		}
	}


	/**
	 * Static open method
	 */
	public static void open(){
		if (instance == null || instance.shell == null || instance.shell.isDisposed()){
			new StartupWizard();
		}else
			instance.shell.setActive();
	}
}