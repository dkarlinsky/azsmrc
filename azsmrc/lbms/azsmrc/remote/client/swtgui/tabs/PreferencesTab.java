/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.User;

import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.SSLCertWizard;
import lbms.azsmrc.shared.RemoteConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
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
			RCMain.getRCMain().getDisplay().syncExec(new Runnable() {
				public void run() {
					fc.getFCInterface().getEntryUpdateListener().updated(key, value);
				}
			});
		}
	};

	//Main I18N PFX
	public static final String PFX = "tab.preferencestab.";

	private boolean bModified = false;

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
			// TODO Auto-generated catch block
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
		tiNotes.setText("Notes");

		//add in flexyconfig AzSMRC stuff
		initAzSMRCFlexyConf();

		tiPlugin = new TreeItem(menuTree,SWT.NULL);
		tiPlugin.setText("Plugin Settings");

		tiSound = new TreeItem(menuTree,SWT.NULL);
		tiSound.setText("Sound Alerts");

		User activeUser = RCMain.getRCMain().getClient().getUserManager().getActiveUser();

		if(RCMain.getRCMain().connected()
				&& activeUser != null
				&& activeUser.checkAccess(RemoteConstants.RIGHTS_ADMIN))
			initAzFlexyConf();

		// set the first static notes treeItem and draw the cOptions for it
		menuTree.setSelection(tiNotes);
		makeNotesPreferences(cOptions);
	}

	private void makeNotesPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label sound = new Label(composite, SWT.NULL);
		sound.setText("In order for you to be able to see and alter Azureus settings remotely, you must be connected to the server and the user must have administrator rights.");

		composite.layout();
	}

	private void makeSoundPreferences(final Composite composite){
		Control[] controls = composite.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label sound = new Label(composite, SWT.NULL);
		sound.setText("TODO.. make this section");

		composite.layout();
	}


	private void makePlugPreferences(final Composite composite){
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
										pluginLabel.setText("Status: Properties received from server");
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


			pluginLabel.setText("Status: Sending request to server for settings.. Please wait");

			//Single User
			singleUser = new Button(composite,SWT.CHECK);
			GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
			gridData.horizontalSpan = 2;
			singleUser.setLayoutData(gridData);
			singleUser.setText("Enable Single User Mode");
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

		composite.layout();
	}


	public void addModListener(Control control, int selectionType){
		control.addListener(selectionType, new Listener(){
			public void handleEvent(Event arg0) {
				bModified = true;
			}
		});
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
