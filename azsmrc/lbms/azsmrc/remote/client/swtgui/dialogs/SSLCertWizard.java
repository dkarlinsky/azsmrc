package lbms.azsmrc.remote.client.swtgui.dialogs;


import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.RemoteConstants;


import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class SSLCertWizard {
	private Shell shell;
	private Display display;
	private Composite parent;
	private Button next,cancel,finish;
	

	public SSLCertWizard(){
		display = RCMain.getRCMain().getDisplay();

		shell = new Shell(display);		
		shell.setLayout(new GridLayout(1,false));

		parent = new Composite(shell,SWT.BORDER);
		parent.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		gd.widthHint = 500;
		parent.setLayoutData(gd);
		parent.setLayout(new GridLayout(3,false));




		//Button Comp
		Composite buttonComp = new Composite(shell,SWT.NULL);
		buttonComp.setLayout(new GridLayout(3,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		buttonComp.setLayoutData(gd);

		//Cancel
		cancel = new Button(buttonComp,SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		cancel.setLayoutData(gd);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {				
				shell.close();				
			}
		});

		//Next
		next = new Button(buttonComp,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);		
		next.setLayoutData(gd);
		next.setText("Next >");
		next.setEnabled(false);



		//Finish
		finish = new Button(buttonComp,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);		
		finish.setLayoutData(gd);
		finish.setText("Finish");
		finish.setEnabled(false);


		step1();

		//Center and open Shell
		GUI_Utilities.centerShellandOpen(shell);

	}


	public void step1(){
		shell.setText("SSL Setup Wizard: Step 1 of 3");

		Label info = new Label(parent,SWT.WRAP | SWT.CENTER);
		info.setText("Step 1 is to identify the location of the 'tools.jar' file located" +
				"on the server.  This file is distributed with the Java Development Kit (JDK)" +
				"and will be used to generate the certificate used for secure communication " +
		"with the server\n\n\n");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		info.setLayoutData(gd);
		info.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label label = new Label(parent,SWT.BEGINNING);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		label.setText("Input directory that contains tools.jar:");

		final Text text = new Text(parent,SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		text.setLayoutData(gd);

		text.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				if(text.getText().length() > 0){
					next.setEnabled(true);
				}else
					next.setEnabled(false);

			}

		});

		next.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
				RCMain.getRCMain().getClient().sendSetCoreParameter("Security.JAR.tools.dir", text.getText(),RemoteConstants.PARAMETER_STRING);
				next.removeSelectionListener(this);				
				step2();
			}

		});


	}


	public void step2(){
		shell.setText("SSL Setup Wizard: Step 2 of 3");

		Control[] controls = parent.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label info = new Label(parent,SWT.WRAP | SWT.CENTER);
		info.setText("Step 2 is certificate creation.  \nSimply fill out " +
		"the provided information (bogus or real) and a certificate will be created\n");

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		info.setLayoutData(gridData);
		info.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		// line

		Label labelSeparator = new Label(parent,SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 2;
		labelSeparator.setLayoutData(gridData);

		// alias

		Label alias_label = new Label(parent,SWT.NULL);
		alias_label.setText("Alias");
		alias_label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		alias_label.setLayoutData(gridData);

		final Text alias_field =new Text(parent,SWT.BORDER);

		alias_field.setText("Azureus");

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		alias_field.setLayoutData(gridData);

		// strength

		Label strength_label = new Label(parent,SWT.NULL);
		strength_label.setText("Strength");
		strength_label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		strength_label.setLayoutData(gridData);

		final Combo strength_combo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);

		final int[] strengths = { 512, 1024, 1536, 2048 };

		for (int i=0;i<strengths.length;i++){

			strength_combo.add(""+strengths[i]);
		}

		strength_combo.select(1);
		strength_combo.setVisibleItemCount(4);

		new Label(parent,SWT.NULL);

		// first + last name

		String[]	field_names = { 
				"First and last name",
				"Organisational Unit",
				"Organisation",
				"City or Locality",
				"State or Province",
				"Two-letter country code"
		};

		final String[]		field_rns = {"CN", "OU", "O", "L", "ST", "C" };

		final Text[]		fields = new Text[field_names.length];

		for (int i=0;i<fields.length;i++){

			Label resource_label = new Label(parent,SWT.NULL);
			resource_label.setText(field_names[i]);
			resource_label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.horizontalSpan = 1;
			resource_label.setLayoutData(gridData);

			Text field = fields[i] = new Text(parent,SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			field.setLayoutData(gridData);
		}


		next.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
				String	alias	= alias_field.getText().trim();
				int		strength	= strengths[strength_combo.getSelectionIndex()];

				String	dn = "";
				for (int i=0;i<fields.length;i++){

					String	rn = fields[i].getText().trim();

					if ( rn.length() == 0 ){

						rn = "Unknown";
					}

					dn += (dn.length()==0?"":",") + field_rns[i] + "=" + rn;
				}


				//Make Certificante
				RCMain.getRCMain().getClient().sendCreateSLLCertificate(alias, dn, strength);


				next.removeSelectionListener(this);
				next.setEnabled(false);
				finish.setEnabled(true);
				step3();
			}

		});



		parent.layout();
		
	}

	private void step3(){
		shell.setText("SSL Setup Wizard: Step 3 of 3");

		Control[] controls = parent.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		
		Label info = new Label(parent,SWT.WRAP);
		info.setText("\nNow that the certificate has been created, you can enable ssl " +
				"communication with the server from the remote.\n\nPlease verify that you want ssl to be enabled," +
				" then choose Finish to restart your Azureus server.  Canceling at this point will not turn " +
				"on ssl nor will it restart the server.\n\nOnce the server has restarted, you will need " +
				"to re-connect to the server using the ssl mode enabled.\n");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		info.setLayoutData(gd);
		info.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		final Button sslEnabled = new Button(parent, SWT.CHECK);
		sslEnabled.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		sslEnabled.setText("Enable SSL communication mode on the server");
		sslEnabled.setSelection(true);
		
		finish.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
				
				RCMain.getRCMain().getClient().transactionStart();			
				
				RCMain.getRCMain().getClient().sendSetPluginParameter("use_ssl", Boolean.toString(sslEnabled.getSelection()), RemoteConstants.PARAMETER_BOOLEAN);
				
				RCMain.getRCMain().getClient().sendRestartAzureus();
				
				RCMain.getRCMain().getClient().transactionCommit();
				
				shell.close();
				
			}
			
		});
		parent.layout();
		
	}


	public static void open() {		
		new SSLCertWizard();
	}
}
