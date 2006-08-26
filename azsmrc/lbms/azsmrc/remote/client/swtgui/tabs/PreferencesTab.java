/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.User;

import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.SSLCertWizard;
import lbms.azsmrc.remote.client.swtgui.sound.Sound;
import lbms.azsmrc.remote.client.swtgui.sound.SoundManager;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.flexyconf.ContentProvider;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.FCInterface;
import lbms.tools.flexyconf.FlexyConfiguration;
import lbms.tools.flexyconf.I18NProvider;
import lbms.tools.flexyconf.swt.SWTMenu;
import lbms.tools.i18n.I18NTranslator;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PreferencesTab {

	private Label pluginLabel;
	private Button singleUser;
	private Tree menuTree;

	private TreeItem tiPlugin, tiSound, tiNotes;

	private Composite cOptions;
	private ScrolledComposite sc;
	private Properties properties;
	private Properties defaultProperties;

	private ParameterListener pl;
	private FlexyConfiguration fc;
	private SWTMenu fcm;
	private Properties azProps = new Properties();
	private ParameterListener azParam = new ParameterListener() {
		public void azParameter(String key, String value, int type) {}
		public void pluginParameter(String key, String value, int type) {}

		public void coreParameter(final String key,final  String value,final  int type) {
			azProps.setProperty(key, value);
			RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
				public void runSafe() {
					fc.getFCInterface().getEntryUpdateListener().updated(key, value);
				}
			});
		}
	};

	//Main I18N PFX
	public static final String PFX = "tab.preferencestab.";



	public PreferencesTab(final CTabFolder parentTab){


		//Open properties for reading and saving
		properties = RCMain.getRCMain().getProperties();

		defaultProperties = new Properties();

		InputStream is = null;
		try {
			is = RCMain.class.getClassLoader().getResourceAsStream("default.cfg");
			defaultProperties.loadFromXML(is);
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (is!=null) try { is.close(); } catch (IOException e) {}
		}




		final CTabItem prefsTab = new CTabItem(parentTab, SWT.CLOSE);
		prefsTab.setText(I18N.translate(PFX + "tab.text"));



		final Composite parent = new Composite(parentTab, SWT.NULL);
		parent.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);


		prefsTab.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {
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
		title.setText(I18N.translate(PFX + "grayTitle.text"));
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

		menuTree.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				if(event.item.equals(tiPlugin)){
					makePlugPreferences(cOptions);
				}else if(event.item.equals(tiSound)){
					makeSoundPreferences(cOptions);
				}else if(event.item.equals(tiNotes)){
					makeNotesPreferences(cOptions);
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
				sc.setMinSize(cOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		//set the sash weight
		sash.setWeights(new int[] {80,320});



		prefsTab.setControl(parent);
		parentTab.setSelection(prefsTab);

		//First static notes treeitem so that it can
		//be selected and the composite can be drawn
		tiNotes = new TreeItem(menuTree,SWT.NULL);
		tiNotes.setText(I18N.translate(PFX + "note.treeItem.text"));

		//add in flexyconfig AzSMRC stuff
		initAzSMRCFlexyConf();

		tiPlugin = new TreeItem(menuTree,SWT.NULL);
		tiPlugin.setText(I18N.translate(PFX + "pluginSettings.treeItem.text"));

		tiSound = new TreeItem(menuTree,SWT.NULL);
		tiSound.setText(I18N.translate(PFX + "sound.treeItem.text"));

		User activeUser = RCMain.getRCMain().getClient().getUserManager().getActiveUser();

		if(RCMain.getRCMain().connected()
				&& activeUser != null
				&& activeUser.checkAccess(RemoteConstants.RIGHTS_ADMIN))
			initAzFlexyConf();

		// set the first static notes treeItem and draw the cOptions for it
		try { //it is supported as of SWT3.2
			menuTree.setSelection(tiNotes);
		} catch (NoSuchMethodError e1) {}
		makeNotesPreferences(cOptions);
	}

	private void makeNotesPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label sound = new Label(composite, SWT.NULL);
		sound.setText(I18N.translate(PFX + "note.note1"));

		composite.layout();
	}

	//set up some privates for the make sound pref section
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



	private void makeSoundPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		//button for sound being on or off
		final Button soundActive = new Button(composite, SWT.CHECK);
		soundActive.setText(I18N.translate(PFX + "soundManager.active"));
		soundActive.setSelection(Boolean.parseBoolean(properties.getProperty("soundManager.active", "true")));

		final Button soundDefault = new Button(composite, SWT.CHECK);
		soundDefault.setText(I18N.translate(PFX + "soundManager.default"));
		soundDefault.setSelection(Boolean.parseBoolean(properties.getProperty("soundManager.useDefaults", "true")));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		soundDefault.setLayoutData(gd);

		//Group for user defined sounds
		soundSelectionGroup = new Group(composite, SWT.NULL);
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
				FileDialog dialog = new FileDialog (composite.getShell(), SWT.OPEN);
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
						MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
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
				FileDialog dialog = new FileDialog (composite.getShell(), SWT.OPEN);
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
						MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
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
				FileDialog dialog = new FileDialog (composite.getShell(), SWT.OPEN);
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
						MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
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
				FileDialog dialog = new FileDialog (composite.getShell(), SWT.OPEN);
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
						MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
					}
				}catch (Exception error){
					error.printStackTrace();
					MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sound.soundSelection.playsound.error.message"));
				}
			}
		});

		setUseDefault(soundDefault.getSelection());



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




		composite.layout();
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

	private void makePlugPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}
		pluginLabel = new Label(composite,SWT.NULL);
		if(!RCMain.getRCMain().connected()){
			pluginLabel.setText(I18N.translate(PFX + "pluginSettings.statusLabel.notConnected"));
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
							display.asyncExec(new SWTSafeRunnable(){

								public void runSafe() {
									if(pluginLabel != null && !pluginLabel.isDisposed())
										pluginLabel.setText(I18N.translate(PFX + "pluginSettings.statusLabel.connected"));
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


			pluginLabel.setText(I18N.translate(PFX + "pluginSettings.statusLabel.waiting"));

			//Single User
			singleUser = new Button(composite,SWT.CHECK);
			GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			singleUser.setLayoutData(gridData);
			singleUser.setText(I18N.translate(PFX + "pluginSettings.singleUserButton.text"));
			singleUser.setEnabled(false);

			singleUser.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event arg0) {
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
						System.out.println("Setting singleUserMode plugin parameter on server to " + Boolean.toString(singleUser.getSelection()));
						client.transactionCommit();

					}
				}
			});



			Button bCertWiz = new Button(composite, SWT.PUSH);
			bCertWiz.setText(I18N.translate(PFX + "pluginSettings.openSSLButton.text"));
			bCertWiz.setToolTipText(I18N.translate(PFX + "pluginSettings.openSSLButton.toolTipText"));
			gridData = new GridData(GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			bCertWiz.setLayoutData(gridData);
			bCertWiz.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event arg0) {
					SSLCertWizard.open();
				}
			});

		}

		composite.layout();
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
			fc = FlexyConfiguration.readFromStream(fcIs, "Azureus");
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

	private void initAzSMRCFlexyConf() {
		System.out.println("Trying to initialize AzSMRC Config");
		try {
			InputStream fcIs = this.getClass().getClassLoader().getResourceAsStream("lbms/azsmrc/remote/client/swtgui/flexyconf/AzSMRCPreferences.xml");
			fc = FlexyConfiguration.readFromStream(fcIs, "Azsmrc");

			final FCInterface fci = fc.getFCInterface();

			fci.setI18NProvider(new I18NProvider() {
				public String translate(String key) {
					return I18N.translate(PFX + key);
				}
			});

			fci.setContentProvider(new ContentProvider() {

				public String getDefaultValue(String key, int type) {
					System.out.println("AzSMRC *DEFAULT* Conf Get Def: "+key+" type: "+ type );
					String v = defaultProperties.getProperty(key);
					if (v==null) {

						switch (type) {
						case Entry.TYPE_STRING:
							return "No Default Found";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}
				public String getValue(String key, int type) {
					System.out.println("AzSMRC Conf Get: "+key+" type: "+type );
					String v = properties.getProperty(key);
					if (v==null) {
						switch (type) {
						case Entry.TYPE_STRING:
							return "No Default Found...";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}

				public void setValue(String key, String value, int type) {
					System.out.println("AzSMRC Conf Set: "+key+" value: "+value+" type: "+type);
					properties.setProperty(key, value);
					RCMain.getRCMain().saveConfig();
				}
			});

			fc.getRootSection().initAll();
			fcm = new SWTMenu(fc,menuTree,cOptions);
			fcm.addAsRoot();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}




}//EOF
