/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
//import org.eclipse.swt.dnd.Clipboard;
//import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class CreateTorrentDialog {

	//I18N prefix
	public static final String PFX = "dialog.createtorrent.";

	private static CreateTorrentDialog instance;

	private Shell shell;
	private Button bCancel, bPrevious, bNext, bFinish;
	private Composite page0, page1;
	private Display display;
	private StackLayout sLayout;
	private int pageNum = 0;
	private Label fileDirLabel;

	//Settings
	private int trackerInt;
	private String customTracker;
	private boolean boolMultiTracker = false;
	private boolean boolOtherHashes = false;
	private boolean boolFile = true;
	private String comment = "";
	private String fileDir = "";


	private CreateTorrentDialog(final String url){
		instance = this;
		display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;

		//Shell
		shell = new Shell(display);
		final GridLayout layout = new GridLayout(1,false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout(layout);
		shell.setText(I18N.translate(PFX + "shell.text"));
		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));

		//Comp on shell
		final Composite parentComp = new Composite(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 600;
		parentComp.setLayoutData(gridData);

		sLayout = new StackLayout ();
		sLayout.marginHeight = 0;
		sLayout.marginWidth = 0;
		parentComp.setLayout (sLayout);


		// create the first page's content
		createPage0(parentComp);
		// create the second page's content
		createPage1(parentComp);


		sLayout.topControl = page0;
		parentComp.layout ();



		//------------------  BUTTON COMP --------------------------\\

		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;

		button_comp.setLayout(gridLayout);

		bCancel = new Button(button_comp,SWT.PUSH);
		bCancel.setText(I18N.translate("global.cancel"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 75;
		bCancel.setLayoutData(gridData);
		bCancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
			}
		});


		bPrevious = new Button(button_comp,SWT.PUSH);
		bPrevious.setText(I18N.translate("global.previous"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.widthHint = 75;
		bPrevious.setLayoutData(gridData);
		bPrevious.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				//TODO
				pageNum = --pageNum % 2;
				sLayout.topControl = pageNum == 0 ? page0 : page1;
				parentComp.layout ();
				bPrevious.setEnabled(false);
				bNext.setEnabled(true);
			}
		});
		bPrevious.setEnabled(false);

		bNext = new Button(button_comp,SWT.PUSH);
		bNext.setText(I18N.translate("global.next"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.widthHint = 75;
		bNext.setLayoutData(gridData);
		bNext.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				//TODO
				pageNum = ++pageNum % 2;
				sLayout.topControl = pageNum == 0 ? page0 : page1;
				parentComp.layout ();
				bPrevious.setEnabled(true);
				bNext.setEnabled(false);
			}
		});

		bFinish = new Button(button_comp,SWT.PUSH);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.widthHint = 75;
		bFinish.setLayoutData(gridData);
		bFinish.setText(I18N.translate("global.finish"));
		bFinish.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				//TODO
			}
		});
		bFinish.setEnabled(false);


		//Center Shell and open
		GUI_Utilities.centerShellOpenAndFocus(shell);
	}


	/**
	 * Create contents for page0 (first page)
	 * @param parentComp
	 */
	private void createPage0(Composite parentComp){
		page0 = new Composite (parentComp, SWT.BORDER);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		page0.setLayoutData(gridData);


		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		page0.setLayout(gridLayout);

		//two composits.. top white one and bottom settings one

		//top white
		Composite topWhiteComp = new Composite(page0,SWT.BORDER);
		topWhiteComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topWhiteComp.setLayout(new GridLayout(1,false));
		topWhiteComp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));


		Label label = new Label (topWhiteComp, SWT.NONE);
		label.setText(I18N.translate(PFX + "page0.topWhiteComp.label.text"));
		label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));


		//Settings Comp
		Composite settingsComp = new Composite(page0, SWT.BORDER);
		settingsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		settingsComp.setLayout(new GridLayout(1,false));

		Button radio0 = new Button(settingsComp, SWT.RADIO);
		radio0.setText(I18N.translate(PFX + "page0.settings.tracker0.text"));

		Button radio1 = new Button(settingsComp, SWT.RADIO);
		radio1.setText(I18N.translate(PFX + "page0.settings.tracker1.text"));

		final Composite customTrackerComp = new Composite(settingsComp, SWT.NULL);
		customTrackerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customTrackerComp.setLayout(new GridLayout(2,false));

		final Label customTrackerLabel = new Label(customTrackerComp,SWT.NULL);
		customTrackerLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		customTrackerLabel.setText(I18N.translate(PFX + "page0.settings.customannounceURL.label"));

		final Text customTrackerText = new Text(customTrackerComp, SWT.BORDER);
		customTrackerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customTrackerText.setText("http://");
		customTrackerText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				customTracker = customTrackerText.getText();
			}
		});


		Button radio2 = new Button(settingsComp, SWT.RADIO);
		radio2.setText(I18N.translate(PFX + "page0.settings.tracker2.text"));


		//Listeners for radiobuttons
		radio0.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				trackerInt = 0;
				customTrackerLabel.setEnabled(false);
				customTrackerText.setEnabled(false);
			}
		});


		radio1.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				trackerInt = 1;
				customTrackerLabel.setEnabled(true);
				customTrackerText.setEnabled(true);
			}
		});



		radio2.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				trackerInt = 2;
				customTrackerLabel.setEnabled(false);
				customTrackerText.setEnabled(false);
			}
		});

		switch(trackerInt){
		case 0:
			radio0.setSelection(true);
			customTrackerLabel.setEnabled(false);
			customTrackerText.setEnabled(false);
			break;
		case 1:
			radio1.setSelection(true);
			customTrackerLabel.setEnabled(true);
			customTrackerText.setEnabled(true);
			customTrackerText.setText(customTracker.equals("")?"http://":customTracker);
			break;
		case 2:
			radio2.setSelection(true);
			customTrackerLabel.setEnabled(false);
			customTrackerText.setEnabled(false);
			break;

		}


		//Separator
		Label separatorLabel1 = new Label(settingsComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		//Mode check boxes
		//Depends on:
		//private boolean boolMultiTracker
		//private boolean boolOtherHashes


		final Button bMode0 = new Button(settingsComp, SWT.CHECK);
		bMode0.setText(I18N.translate(PFX + "page0.settings.mode0.text"));
		bMode0.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bMode0.setSelection(boolMultiTracker);
		bMode0.addListener(SWT.Selection,new Listener(){
			public void handleEvent(Event arg0) {
				boolMultiTracker = bMode0.getSelection();
			}
		});

		Button bMode1 = new Button(settingsComp, SWT.CHECK);
		bMode1.setText(I18N.translate(PFX + "page0.settings.mode1.text"));
		bMode1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bMode1.setSelection(boolOtherHashes);
		bMode1.addListener(SWT.Selection,new Listener(){
			public void handleEvent(Event arg0) {
				boolOtherHashes = bMode0.getSelection();
			}
		});


		//Separator
		Label separatorLabel2 = new Label(settingsComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));





		//Comment Section

		final Label commentLabel = new Label(settingsComp,SWT.NULL);
		commentLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		commentLabel.setText(I18N.translate(PFX + "page0.settings.comment.label"));

		final Text commentText = new Text(settingsComp, SWT.BORDER);
		commentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		commentText.setText(comment);
		commentText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				comment = customTrackerText.getText();
			}
		});
	}


	/**
	 * Create contents for page1 (second page)
	 * @param parentComp
	 */
	private void createPage1(Composite parentComp){
		page1 = new Composite (parentComp, SWT.BORDER);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		page1.setLayoutData(gridData);


		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		page1.setLayout(gridLayout);

		//two composits.. top white one and bottom settings one

		//top white
		Composite topWhiteComp = new Composite(page1,SWT.BORDER);
		topWhiteComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topWhiteComp.setLayout(new GridLayout(1,false));
		topWhiteComp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));


		Label label = new Label (topWhiteComp, SWT.NONE);
		label.setText(I18N.translate(PFX + "page1.topWhiteComp.label.text"));
		label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		final Composite buttonComp = new Composite(page1, SWT.NULL);
		buttonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonComp.setLayout(new GridLayout(2,true));

		final Composite fileDirComp = new Composite(page1, SWT.NULL);
		fileDirComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fileDirComp.setLayout(new GridLayout(3,true));






		Button bFile = new Button(buttonComp, SWT.RADIO);
		bFile.setText(I18N.translate(PFX + "page0.fileButton.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gridData.grabExcessHorizontalSpace = true;
		bFile.setLayoutData(gridData);
		bFile.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				boolFile = true;
				fileDirLabel.setText(I18N.translate(PFX + "page1.fileDirLabel.file.text"));
				fileDirComp.layout();
			}
		});

		Button bDir = new Button(buttonComp, SWT.RADIO);
		bDir.setText(I18N.translate(PFX + "page0.dirButton.text"));
		bDir.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		bDir.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				boolFile = false;
				fileDirLabel.setText(I18N.translate(PFX + "page1.fileDirLabel.dir.text"));
				fileDirComp.layout();
			}
		});

		if(boolFile)
			bFile.setSelection(true);
		else
			bDir.setSelection(false);

		//Separator
		Label separatorLabel3 = new Label(buttonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separatorLabel3.setLayoutData(gd);




		//Label
		fileDirLabel = new Label(fileDirComp, SWT.NULL);
		fileDirLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		if(boolFile)
			fileDirLabel.setText(I18N.translate(PFX + "page1.fileDirLabel.file.text"));
		else
			fileDirLabel.setText(I18N.translate(PFX + "page1.fileDirLabel.dir.text"));

		//TextBox
		final Text fileDirText = new Text(fileDirComp,SWT.BORDER);
		fileDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileDirText.addListener(SWT.Modify, new Listener(){
			public void handleEvent(Event arg0) {
				if(!fileDirText.getText().equalsIgnoreCase(""))
					bFinish.setEnabled(true);
				else
					bFinish.setEnabled(false);
			}
		});


		Button chooseFileDir = new Button(fileDirComp, SWT.BORDER);
		chooseFileDir.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		
	}


	/**
	 * Open createTorrent dialog
	 *
	 */
	public static void open(){
		final Display display = RCMain.getRCMain().getDisplay();
		final String url = RCMain.getRCMain().getAWTClipboardString();
		if(display == null) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if(display == null) return;
				if (instance == null || instance.shell == null || instance.shell.isDisposed()){
					new CreateTorrentDialog(url);
				}else
					instance.shell.setActive();

			}
		});
	}





}//EOF