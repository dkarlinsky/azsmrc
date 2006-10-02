package lbms.azsmrc.plugin.gui;

import java.io.File;
import java.io.IOException;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;
import lbms.tools.flexyconf.ContentProvider;
import lbms.tools.flexyconf.FCInterface;
import lbms.tools.flexyconf.FlexyConfiguration;
import lbms.tools.flexyconf.I18NProvider;
import lbms.tools.flexyconf.swt.SWTMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class GUIEditUser {

	private Tree tree;

	private User user;

	private ScrolledComposite sc;

	private Composite cOptions;

	private GUIEditUser instance;

	private Shell shell;

	/**
	 * Private method for actually drawing the dialog
	 * @param User _user
	 */
	private void loadGUI(User _user){

		instance = this;
		user = _user;
		System.out.println("USERNAME: "+user);

		//Shell Initialize
		shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setImage(ImageRepository.getImage("plus"));

		//Grid Layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setLayout(layout);

		//composite for shell
		Composite parent = new Composite(shell,SWT.NULL);

		//Grid Layout
		layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		//Gd for comp

		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		//shell title
		shell.setText("Edit User Information");

		//Sash
		final SashForm sash = new SashForm(parent,SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 600;
		gridData.heightHint = 400;
		sash.setLayoutData(gridData);
		sash.setLayout(new GridLayout(1,false));


		//Tree on left side
		tree = new Tree(sash,SWT.BORDER | SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		//SC on right side
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
		sash.setWeights(new int[] {30,70});


		//Button comp for below the sash
		Composite bComp = new Composite(parent, SWT.NULL);
		bComp.setLayout(new GridLayout(2,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		bComp.setLayoutData(gridData);

		//Save button
		Button save = new Button(bComp, SWT.PUSH);
		save.setText("Save");
		save.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				// TODO Feed me!
			}
		});


		//Cancel button
		Button close = new Button(bComp, SWT.PUSH);
		close.setText("Close");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		close.setLayoutData(gridData);

		close.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				shell.dispose();
				instance = null;
			}
		});

		//pack and open shell
		Utilities.centerShellandOpen(shell);

		//Non-Flexy config items
		final TreeItem item1 = new TreeItem(tree, SWT.NULL);
		item1.setText("General Settings");

		tree.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
				TreeItem [] selections = tree.getSelection ();
				if(selections.length != 1) return;
				if(selections[0].equals(item1)){
					//If true then this is the General Settings

					//First remove all from cOptions
					Control[] controls = cOptions.getChildren();
					for(Control control:controls)
						control.dispose();

					//populate cOptions
					addGeneralItems(cOptions);

				}else{
					//Flexyconfig stuff..
				}
			}

		});


		FlexyConfiguration fc = Plugin.getPSFlexyConf();
		FCInterface fci = fc.getFCInterface();
		fci.setContentProvider(new ContentProvider() {
			/* (non-Javadoc)
			 * @see lbms.tools.flexyconf.ContentProvider#getDefaultValue(java.lang.String, int)
			 */
			public String getDefaultValue(String key, int type) {
				return user.getProperty(key);
			}
			/* (non-Javadoc)
			 * @see lbms.tools.flexyconf.ContentProvider#getValue(java.lang.String, int)
			 */
			public String getValue(String key, int type) {
				String v = user.getProperty(key);
				return (v==null)? "" : v;
			}
			/* (non-Javadoc)
			 * @see lbms.tools.flexyconf.ContentProvider#setValue(java.lang.String, java.lang.String, int)
			 */
			public void setValue(String key, String value, int type) {
				user.setProperty(key, value);
			}
		});
		fci.setI18NProvider(new I18NProvider () {
			/* (non-Javadoc)
			 * @see lbms.tools.flexyconf.I18NProvider#translate(java.lang.String)
			 */
			public String translate(String key) {
				return Plugin.getLocaleUtilities().getLocalisedMessageText(key);
			}
		});
		SWTMenu fcm = new SWTMenu (fc,tree,cOptions);
		fcm.addAsRoot();
	}


	private void addGeneralItems(Composite composite){
		//		User Name Label
		Label nameLabel = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		nameLabel.setLayoutData( gridData );
		nameLabel.setText("User Name:");


		//User Name Input field
		final Text userName = new Text(composite,SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 100;
		userName.setLayoutData( gridData);
		userName.setText(user.getUsername());

		//Combo Stuff

		//combo Label
		Label combo_text = new Label(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		combo_text.setLayoutData( gridData );
		combo_text.setText("Select User Type:");

		final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.add("Normal User");
		combo.add("Administrator");



			if(user.checkAccess(RemoteConstants.RIGHTS_ADMIN))
				combo.select(1);
			else
				combo.select(0);


		if(!user.checkRight(RemoteConstants.RIGHTS_ADMIN)){
			combo.setEnabled(false);
		}


		//---------Directory stuff ------------\\


		//output directory
		Label outputDir_text = new Label(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		outputDir_text.setLayoutData( gridData );
		outputDir_text.setText("Output Directory for User:");

		//comp for directory input
		Composite output_comp = new Composite(composite,SWT.NONE);
		output_comp.setLayout(new GridLayout(3,false));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		output_comp.setLayoutData(gridData);



		//output directory input field
		final Text outputDir = new Text(output_comp,SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 250;
		outputDir.setLayoutData(gridData);

		outputDir.setText(user.getOutputDir());

		//icon for output directory
		Label outputDir_icon = new Label(output_comp, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 1;
		outputDir_icon.setLayoutData( gridData );
		outputDir_icon.setToolTipText("Click to choose directory");
		outputDir_icon.setImage(ImageRepository.getImage("folder"));
		outputDir_icon.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				DirectoryDialog dirDialog = new DirectoryDialog(shell);
				dirDialog.setText("Please Choose Output Directory");
				dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
				String selectedDir = dirDialog.open();

				//need to check if selected dir has files and if so, does it have a comments dir
				if(selectedDir == null){
					return;
				}else{
					outputDir.setText(selectedDir);
				}


			}
		});

		//auto import directory
		Label importDir_text = new Label(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		importDir_text.setLayoutData( gridData );
		importDir_text.setText("Automatic Import Directory for User:");

		//comp for directory input
		Composite importDir_comp = new Composite(composite,SWT.NONE);
		importDir_comp.setLayout(new GridLayout(3,false));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		importDir_comp.setLayoutData(gridData);



		//output directory input field
		final Text importDir = new Text(importDir_comp,SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 250;
		importDir.setLayoutData(gridData);

		importDir.setText(user.getAutoImportDir());

		//icon for output directory
		Label importDir_icon = new Label(importDir_comp, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 1;
		importDir_icon.setLayoutData( gridData );
		importDir_icon.setToolTipText("Click to choose directory");
		importDir_icon.setImage(ImageRepository.getImage("folder"));
		importDir_icon.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				DirectoryDialog dirDialog = new DirectoryDialog(shell);
				dirDialog.setText("Please Choose Automatic Import Directory");
				dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
				String selectedDir = dirDialog.open();
				File selectedDir_file = new File(selectedDir);
				//need to check if selected dir has files and if so, does it have a comments dir
				if(selectedDir == null){
					return;
				}else if(!selectedDir_file.exists() || !selectedDir_file.isDirectory()){
					MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
					mb.setText("Error");
					mb.setMessage("Selected Directory does not exist, please choose a valid directory.");
					mb.open();
				}else{
					importDir.setText(selectedDir);
				}


			}
		});

		//Button for Accept
		Button commit = new Button(composite, SWT.PUSH);
		gridData = new GridData(GridData.CENTER);
		gridData.horizontalSpan = 1;
		commit.setLayoutData( gridData);
		commit.setText( "Accept");
		commit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(userName.getText().equalsIgnoreCase("")      ||
						outputDir.getText().equalsIgnoreCase("")||
						importDir.getText().equalsIgnoreCase("")){
					MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
					mb.setText("Error");
					mb.setMessage("Please fill out all of the information.");
					mb.open();
					return;
				}



				//add the user to the XMLConfig file
				try {
					if(!user.getUsername().equalsIgnoreCase(userName.getText())){
						Plugin.getXMLConfig().renameUser(user.getUsername(),userName.getText());
					}

					Plugin.getXMLConfig().saveConfigFile();

					User currentUser = Plugin.getXMLConfig().getUser(userName.getText());


					currentUser.setOutputDir(outputDir.getText());
					currentUser.setAutoImportDir(importDir.getText());

					if(combo.getSelectionIndex() != 0)
						currentUser.setRight(RemoteConstants.RIGHTS_ADMIN);


					Plugin.getXMLConfig().saveConfigFile();
				} catch (IOException e1) {
					Plugin.addToLog(e1.toString());
				} catch (UserNotFoundException e2) {
					MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
					mb.setText("Error");
					mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
					mb.open();
					Plugin.addToLog(e2.getMessage());
					e2.printStackTrace();
				}catch (DuplicatedUserException e2) {
					MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
					mb.setText("Error");
					mb.setMessage("User name already exists.");
					mb.open();
					return;
				}
			}

		});
		
		//Redo the composite so the new stuff appears
		composite.layout();

	}
		/**
		 * Public method to open the dialog
		 * @param User _user
		 */
		public void open(User _user){
			if(instance == null)
				loadGUI(_user);
			else{
				if(shell != null || !shell.isDisposed())
					shell.setFocus();
			}

		}

	}
