package lbms.azsmrc.plugin.gui;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

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
		sash.setWeights(new int[] {80,320});


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
