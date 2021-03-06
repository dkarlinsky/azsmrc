/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.RemotePlugin;
import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.plugins.PluginInterface;
import lbms.azsmrc.remote.client.pluginsimpl.PluginInterfaceImpl;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
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

import org.apache.log4j.Logger;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom.Element;

public class PreferencesTab {

	private static PreferencesTab instance;
	private static Logger logger = Logger.getLogger(PreferencesTab.class);

	private CTabItem prefsTab;


	private Label pluginLabel;
	private Button singleUser;
	private Tree menuTree;

	private Table rpTable, lpTable;

	private TreeItem tiPlugin, tiSound, tiNotes, tiLocalPlugins, tiRemotePlugins;

	private Composite cOptions;
	private ScrolledComposite sc;
	private Properties properties;
	private Properties defaultProperties;

	private ParameterListener pl;
	private FlexyConfiguration fc;
	private FlexyConfiguration rpFC;
	private SWTMenu fcm;
	private Properties azProps = new Properties();
	private ParameterListener azParam = new ParameterListener() {
		public void azParameter(String key, String value, int type) {}
		public void pluginParameter(String key, String value, int type) {}

		public void coreParameter(final String key,final  String value,final  int type) {
			azProps.setProperty(key, value);
			RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
				public void runSafe() {
					if (fc != null)
						fc.getFCInterface().getEntryUpdateListener().updated(key, value);
					if (rpFC != null)
						rpFC.getFCInterface().getEntryUpdateListener().updated(key, value);
				}
			});
		}
	};

	//Main I18N PFX
	public static final String PFX = "tab.preferencestab.";



	private void loadGUI(final CTabFolder parentTab){
		instance = this;

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




		prefsTab = new CTabItem(parentTab, SWT.CLOSE);
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
				if(instance != null) instance = null;
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
				}else if(event.item.equals(tiRemotePlugins )){
					makeRemotePluginPreferences(cOptions);
				}else if(event.item.equals(tiLocalPlugins)){
					makeLocalPluginPreferences(cOptions);
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

		tiLocalPlugins = new TreeItem(menuTree, SWT.NULL);
		tiLocalPlugins.setText(I18N.translate(PFX + "localplugins"));
		
		addLocalPluginConfig();

		User activeUser = RCMain.getRCMain().getClient().getUserManager().getActiveUser();

		if(RCMain.getRCMain().connected()
				&& activeUser != null
				&& activeUser.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {

			tiRemotePlugins = new TreeItem(menuTree, SWT.NULL);
			tiRemotePlugins.setText(I18N.translate(PFX + "remoteplugins.treeItem.text"));

			Element rpFlexyConfElement = RCMain.getRCMain().getClient().getRemoteInfo().getPluginsFlexyConf();
			RCMain.getRCMain().getClient().transactionStart();
			if (rpFlexyConfElement != null) {
				rpFC = new FlexyConfiguration (rpFlexyConfElement);
				FCInterface fci = rpFC.getFCInterface();
				fci.setI18NProvider(new I18NProvider() {
					/* (non-Javadoc)
					 * @see lbms.tools.flexyconf.I18NProvider#translate(java.lang.String)
					 */
					public String translate(String key) {
						if (key != null)
							return key;
						else return "";
					}
				});
				fci.setContentProvider(new ContentProvider() {

					Client client = RCMain.getRCMain().getClient();

					public String getDefaultValue(String key, int type) {
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
						logger.debug("AzConf Set: "+key+" value: "+value+" type: "+type);
						client.sendSetCoreParameter(key, value, type);
						azProps.setProperty(key, value);
					}
				});
				SWTMenu rpFCM = new SWTMenu(rpFC,menuTree,cOptions);
				rpFCM.addAsRoot(tiRemotePlugins);
				rpFC.getRootSection().initAll();
			}

			initAzFlexyConf();
			RCMain.getRCMain().getClient().transactionCommit();

		}


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
					logger.debug("AzConf Set: "+key+" value: "+value+" type: "+type);
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
					logger.debug("AzSMRC Conf Set: "+key+" value: "+value+" type: "+type);
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

	private void makeRemotePluginPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label pluginsIDed = new Label(composite, SWT.NULL);
		pluginsIDed.setText(I18N.translate(PFX + "remoteplugins.pluginsided.text"));

		//Remote Table Initialization
		rpTable = new Table(composite, SWT.CHECK | SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		rpTable.setHeaderVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		rpTable.setLayoutData(gd);

		//columns
		TableColumn loadatstartup = new TableColumn(rpTable, SWT.CENTER);
		loadatstartup.setText(I18N.translate(PFX + "remoteplugins.columns.loadatstartup"));
		loadatstartup.pack();

		TableColumn type = new TableColumn(rpTable, SWT.CENTER);
		type.setText(I18N.translate(PFX + "remoteplugins.columns.type"));
		type.setWidth(70);

		TableColumn name = new TableColumn(rpTable, SWT.LEFT);
		name.setText(I18N.translate(PFX + "remoteplugins.columns.name"));
		name.setWidth(175);

		TableColumn version = new TableColumn(rpTable, SWT.LEFT);
		version.setText(I18N.translate(PFX + "remoteplugins.columns.version"));
		version.pack();

		TableColumn directory = new TableColumn(rpTable, SWT.LEFT);
		directory.setText(I18N.translate(PFX + "remoteplugins.columns.directory"));
		directory.setWidth(150);

		TableColumn unloadable = new TableColumn(rpTable, SWT.CENTER);
		unloadable.setText(I18N.translate(PFX + "remoteplugins.columns.unloadable"));
		unloadable.pack();


		//Set Data listener
		rpTable.addListener(SWT.SetData, new Listener(){
			public void handleEvent(Event event) {
				//Pull table item
				TableItem item = (TableItem) event.item;
				int index = rpTable.indexOf(item);

				RemotePlugin[] plugins = RCMain.getRCMain().getClient().getRemoteInfo().getRemotePlugins();
				if(plugins == null || plugins.length == 0) return;

				RemotePlugin plugin = plugins[index];


				if(plugin == null) return;

				//Column 0 = loadatstartup (checkbox)
				//Column 1 = type
				//Column 2 = name
				//Column 3 = version
				//Column 4 = directory
				//Column 5 = unloadable

				item.setChecked(!plugin.isDisabled());


				if(plugin.isBuiltIn())
					item.setText(1,I18N.translate(PFX + "remoteplugins.tableitem.builtin"));
				else
					item.setText(1,I18N.translate(PFX + "remoteplugins.tableitem.peruser"));


				item.setText(2, plugin.getPluginName());
				item.setText(3, plugin.getPluginVersion());
				item.setText(4, plugin.getPluginDirectoryName());
				item.setText(5, Boolean.toString(plugin.isUnloadable()));

				
				item.setData(plugin);
				//color every other one
				if(index%2!=0){
					item.setBackground(ColorUtilities.getBackgroundColor());
				}


			}
		});

		rpTable.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				if(event.detail == SWT.CHECK){
					TableItem item = (TableItem) event.item;
					RemotePlugin plugin = (RemotePlugin) item.getData();
					
					plugin.setDisabled(!item.getChecked());
					
				}
			}
			
		});


		Composite buttonComp = new Composite(composite, SWT.NULL);
		buttonComp.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		buttonComp.setLayoutData(gd);



		//redraw the comp
		composite.layout();

		//Clear the rptable
		clearRPTable();
	}

	private void clearRPTable(){
		if(rpTable != null || !rpTable.isDisposed()){
			rpTable.removeAll();
			int count;
			try{
				count = RCMain.getRCMain().getClient().getRemoteInfo().getRemotePlugins().length;
			}catch(Exception e){
				count = 0;
			}
			if(count > 0)
				rpTable.setItemCount(count);
			else
				rpTable.setItemCount(0);
		}
	}
	
	private void makeLocalPluginPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label pluginsIDed = new Label(composite, SWT.NULL);
		pluginsIDed.setText(I18N.translate(PFX + "remoteplugins.pluginsided.text"));

		//Remote Table Initialization
		lpTable = new Table(composite, SWT.CHECK | SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		lpTable.setHeaderVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		lpTable.setLayoutData(gd);

		//columns
		TableColumn loadatstartup = new TableColumn(lpTable, SWT.CENTER);
		loadatstartup.setText(I18N.translate(PFX + "remoteplugins.columns.loadatstartup"));
		loadatstartup.pack();

		TableColumn name = new TableColumn(lpTable, SWT.LEFT);
		name.setText(I18N.translate(PFX + "remoteplugins.columns.name"));
		name.setWidth(175);

		TableColumn version = new TableColumn(lpTable, SWT.LEFT);
		version.setText(I18N.translate(PFX + "remoteplugins.columns.version"));
		version.pack();

		TableColumn directory = new TableColumn(lpTable, SWT.LEFT);
		directory.setText(I18N.translate(PFX + "remoteplugins.columns.directory"));
		directory.setWidth(450);



		//Set Data listener
		lpTable.addListener(SWT.SetData, new Listener(){
			public void handleEvent(Event event) {
				//Pull table item
				TableItem item = (TableItem) event.item;
				int index = lpTable.indexOf(item);

				PluginInterface[] plugins = RCMain.getRCMain().getPluginManagerImpl().getAllPluginInterfaces();
				Arrays.sort(plugins, new Comparator<PluginInterface>() {
					public int compare(PluginInterface o1, PluginInterface o2) {
						return o1.getPluginName().compareToIgnoreCase(o2.getPluginName());
					}
				});
				
				//PluginInterfaceImpl[] deadPlugins = RCMain.getRCMain().getPluginManagerImpl().getDisabledPluginInterfaces();

				
				if(plugins == null || plugins.length == 0) return;

				PluginInterface plugin = plugins[index];


				if(plugin == null) return;

				//Column 0 = loadatstartup (checkbox)
				//Column 1 = name
				//Column 2 = version
				//Column 3 = directory				
				
				item.setChecked(!plugin.isDisabled());
				

				
				item.setText(1, plugin.getPluginName());
				item.setText(2, plugin.getPluginVersion());
				item.setText(3, plugin.getPluginDir());
				
				item.setData(plugin);

				//color every other one
				if(index%2!=0){
					item.setBackground(ColorUtilities.getBackgroundColor());
				}


			}
		});
		
		
		lpTable.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				if(event.detail == SWT.CHECK){
					TableItem item = (TableItem) event.item;
					PluginInterface plugin = (PluginInterface) item.getData();
					
					plugin.setDisabled(!item.getChecked());
					
				}
			}
			
		});


		Composite buttonComp = new Composite(composite, SWT.NULL);
		buttonComp.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		buttonComp.setLayoutData(gd);



		//redraw the comp
		composite.layout();

		//Clear the rptable
		clearLPTable();
	}
	
	private void clearLPTable(){
		if(lpTable != null || !lpTable.isDisposed()){
			lpTable.removeAll();
			int count;
			try{
				count = RCMain.getRCMain().getPluginManagerImpl().getAllPluginInterfaces().length;
			}catch(Exception e){
				count = 0;
			}
			if(count > 0)
				lpTable.setItemCount(count);
			else
				lpTable.setItemCount(0);
		}
	}

	public void addLocalPluginConfig () {
		PluginInterfaceImpl[] plugins = RCMain.getRCMain().getPluginManagerImpl().getPluginInterfacesImpl();
		Arrays.sort(plugins, new Comparator<PluginInterfaceImpl>() {
			public int compare(PluginInterfaceImpl o1, PluginInterfaceImpl o2) {
				return o1.getPluginName().compareToIgnoreCase(o2.getPluginName());
			}
		});
		for (PluginInterfaceImpl p : plugins) {
			FlexyConfiguration fc = p.getPluginFlexyConf();
			if (fc!=null) {
				final PluginInterfaceImpl pi = p;
				final FCInterface fci = fc.getFCInterface();

				fci.setI18NProvider(new I18NProvider() {
					public String translate(String key) {
						return pi.getI18N().translate(key);
					}
				});

				fci.setContentProvider(new ContentProvider() {

					public String getDefaultValue(String key, int type) {
						String v = pi.getPluginConfig().getProperty(key);
						if (v==null) {
							switch (type) {
							case Entry.TYPE_STRING:
								return "";
							case Entry.TYPE_BOOLEAN:
								return "false";
							default:
								return "0";
							}
						}
						else return v;
					}

					public String getValue(String key, int type) {
						String v = pi.getPluginConfig().getProperty(key);
						if (v==null) {
							switch (type) {
							case Entry.TYPE_STRING:
								return "";
							case Entry.TYPE_BOOLEAN:
								return "false";
							default:
								return "0";
							}
						}
						else return v;
					}

					public void setValue(String key, String value, int type) {
						pi.getPluginConfig().setProperty(key, value);
					}
				});
				SWTMenu sm = new SWTMenu(fc,menuTree,cOptions);
				if(tiLocalPlugins != null) sm.addAsSubItem(tiLocalPlugins);
			}
		}
	}

	/**
	 * returns the remoteplugintable from the instance
	 * Caution -- if instance == null then this will be null!
	 * @return rpTable
	 */
	public Table getRemotePluginTable(){
		return instance.rpTable;
	}

	/**
	 * Opens the preference tab
	 * @param CTabFolder parentTab
	 */
	public static void open(final CTabFolder parentTab) {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if (instance == null){
					PreferencesTab pref = new PreferencesTab();
					pref.loadGUI(parentTab);
				}else{
					parentTab.setSelection(instance.prefsTab);
				}
			}
		});
	}
}//EOF
