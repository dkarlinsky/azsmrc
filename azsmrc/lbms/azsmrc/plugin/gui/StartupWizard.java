/**
 * First time startup wizard.. should only run once 
 * and allow the user to set up their settings
 * 
 * 
 */

package lbms.azsmrc.plugin.gui;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.remote.client.plugins.PluginConfig;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gudy.azureus2.core3.config.COConfigurationManager;


public class StartupWizard {

	//Global Display
	private Display display;

	//Main Shell for wizard
	private Shell shell;

	//Sash for shell
	private SashForm sash;

	//Font for shell
	Font font16, font12;

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


	//Stored Info
	private int comPort = 49009;
	private boolean useSSL = false;
	private boolean useStats = true;
	private String dirString;

	//instance to make sure no more than one of these is open
	StartupWizard instance;




	/**
	 * Main open
	 * @param _display
	 */
	private void open(Display _display){
		display = _display;
		instance = this;
		shell = new Shell(display /*SWT.APPLICATION_MODAL*/);
		shell.setLayout(new GridLayout(1,false));
		shell.setText("AzSMRC Startup Wizard");

		//Shell listener to make sure we dispose of everything
		shell.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {
				if(font16 != null || !font16.isDisposed())
					font16.dispose();
				if(font12 != null || !font12.isDisposed())
					font12.dispose();
			}

		});


		//put sash on shell
		sash = new SashForm(shell,SWT.HORIZONTAL);
		sash.setLayout(new GridLayout());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint =300;
		gridData.widthHint = 600;
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
					loadStep(step-1);
			}
		});

		btnContinue = new Button(bComp, SWT.PUSH);
		btnContinue.setText("Continue");
		btnContinue.setEnabled(true);
		btnContinue.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(step!=5)
					loadStep(step+1);
				else{
					//This is where we need to commit everything
					//TODO
					try{
						org.gudy.azureus2.plugins.PluginConfig pc = Plugin.getPluginInterface().getPluginconfig();
						pc.setPluginParameter("remote_port", comPort);
						pc.setPluginParameter("use_ssl", useSSL);
						pc.setPluginParameter("statistics.allow", useStats);
						//Save all
						pc.save();
						//right now we only have a core call to store the string settings
						//COConfigurationManager.setParameter(xmlRequest.getAttributeValue("key"), xmlRequest.getAttributeValue("value"));
						if(dirString != null && !dirString.equals("")){
							COConfigurationManager.setParameter("Default save path", dirString);
							COConfigurationManager.save();
						}



					}catch(Exception e){

					}

					shell.dispose();
				}

			}
		});



		sash.setWeights(new int[] {10,90});
		//open up the first step
		loadStep(1);

		//open shell
		centerShellandOpen(shell, display);

	}

	/**
	 * Step 1
	 */
	private void step1(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		step = 1;
		btnPrevious.setEnabled(false);
		btnContinue.setText("Continue");

		//Welcome stuff here
		Label welcome1 = new Label(parent, SWT.CENTER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		welcome1.setLayoutData(gridData);

		welcome1.setText("Welcome to AzSMRC");
		Font initialFont = welcome1.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(16);
		}
		font16 = new Font(display, fontData);

		for(int i = 0; i < fontData.length; i++){
			fontData[i].setHeight(12);
		}
		font12 = new Font(display, fontData);
		welcome1.setFont(font16);


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
	 * Configure the port question
	 */
	private void step2(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		step = 2;
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");

		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(2,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);


		Label cLabel = new Label(comp, SWT.NULL);
		cLabel.setText("Communication Port: ");
		cLabel.setFont(font12);


		final Text port = new Text(comp, SWT.SINGLE | SWT.BORDER);
		port.setText(String.valueOf(comPort));
		port.setFont(font12);
		port.addListener (SWT.Verify, new Listener () {
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

		port.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0) {
				try{
					int num = Integer.valueOf(port.getText());
					if(num > 65000){
						port.setText("65000");
						num = 65000;
					}
					comPort = num;
				}catch(Exception e){
					port.setText("49009");
					comPort = 49009;
				}

			}
		});

		//Label explaining everything
		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("\n\nPlease give the port number that you would like the " +
				"AzSMRC plugin to listen on" +
				"\n\nNotes:\n - Default port is 49009\n - Valid range is 1-65000\n - A restart of Azureus is needed if the port is changed from the default\n");


		parent.layout();
	}


	/**
	 * Step 3
	 */
	private void step3(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		step = 3;
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");


		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(1,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		final Button btnUseSSL = new Button(comp, SWT.CHECK);
		btnUseSSL.setText("Use SSL (Secured Socket Layer) Communication");
		btnUseSSL.setSelection(useSSL);
		btnUseSSL.setFont(font12);
		btnUseSSL.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				useSSL = btnUseSSL.getSelection();

			}

		});
		//Label explaining everything
		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("\n\nEnabling this option allow for secure communication\nbetween Azureus running AzSMRC and the remote interface" +
				"\n\nNotes:\n - Default is off" +
				"\n - You need to create a certificate in Azurues to use this feature (Tools->Options->Security)");


		parent.layout();
	}

	/**
	 * Step 4
	 */
	private void step4(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		step = 4;
		btnPrevious.setEnabled(true);
		btnContinue.setText("Continue");

		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(1,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		final Button btnUseStats = new Button(comp, SWT.CHECK);
		btnUseStats.setText("Allow Anonymous Statistics Reporting");
		btnUseStats.setSelection(useStats);
		btnUseStats.setFont(font12);
		btnUseStats.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				useStats = btnUseStats.getSelection();

			}

		});
		//Label explaining everything
		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("\n\nAllow AzSMRC to send version and random ID for anonymous usage statistics" +
				"\n\nNotes:\n - Default is off" +
				"\n - The data is stored for 24 hours and will be deleted afterwards");


		parent.layout();
	}


	/**
	 * Step 5
	 */
	private void step5(){
		Control[] controls = parent.getChildren();
		for(Control control:controls)
			control.dispose();
		step = 5;
		btnPrevious.setEnabled(true);
		btnContinue.setText("Finish");

		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(new GridLayout(2,false));

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		final Label labelDir = new Label(comp, SWT.NULL);
		labelDir.setText("Default Save Directory");
		labelDir.setFont(font12);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		labelDir.setLayoutData(gd);


		final Text dir = new Text(comp,SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		dir.setLayoutData(gd);
		if(dirString != null)
			dir.setText(dirString);
		else{
			dir.setText(Plugin.getPluginInterface().getPluginconfig().getStringParameter("Default save path"));
		}
		Label dirImg = new Label(comp, SWT.NULL);
		dirImg.setImage(ImageRepository.getImage("folder"));
		dirImg.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {}

			public void mouseDown(MouseEvent arg0) {
				DirectoryDialog dialog = new DirectoryDialog (shell);
				String results = dialog.open();
				if(results != null){
					dirString = results;
					dir.setText(results);
				}
			}

			public void mouseUp(MouseEvent arg0) {}

		});



		//Label explaining everything
		Label label = new Label(comp, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		label.setText("\n\nFor AzSMRC to function properly, the default save directory for Azureus must " +
				"be specified");


		parent.layout();
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
			step4.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			step5();
			break;
		}
	}

	/**
	 * Test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();

		StartupWizard sw = new StartupWizard();

		sw.open(display);
		while (!sw.shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}

	/** Centers a Shell and opens it relative to the users Monitor
	 *
	 * @param shell
	 */

	public static void centerShellandOpen(Shell shell, Display display){
		//open shell
		shell.pack();

		//Center Shell
		Monitor primary = display.getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = shell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y +(bounds.height - rect.height) / 2;
		shell.setLocation (x, y);

		//open shell
		shell.open();
	}

}
