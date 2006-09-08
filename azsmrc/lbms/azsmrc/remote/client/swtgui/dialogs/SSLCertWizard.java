package lbms.azsmrc.remote.client.swtgui.dialogs;


import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
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
	private static SSLCertWizard instance;
	private Display display;
	private Composite parent;
	private Button next,cancel,finish;

//	I18N prefix
	public static final String PFX = "dialog.sslcertwizard.";

	public SSLCertWizard(){
		instance = this;
		display = RCMain.getRCMain().getDisplay();

		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));

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
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				shell.close();
			}
		});

		//Next
		next = new Button(buttonComp,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		next.setLayoutData(gd);
		next.setText(I18N.translate(PFX + "next_button.text"));
		next.setEnabled(false);



		//Finish
		finish = new Button(buttonComp,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		finish.setLayoutData(gd);
		finish.setText(I18N.translate(PFX + "finish_button.text"));
		finish.setEnabled(false);


		step1();

		//Center and open Shell
		GUI_Utilities.centerShellandOpen(shell);

	}


	public void step1(){
		shell.setText(I18N.translate(PFX + "step1.shell.text"));

		Label info = new Label(parent,SWT.WRAP | SWT.CENTER);
		info.setText(I18N.translate(PFX + "step1.info.text"));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		info.setLayoutData(gd);
		info.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label label = new Label(parent,SWT.BEGINNING);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		label.setText(I18N.translate(PFX + "step1.label.text"));

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
		shell.setText(I18N.translate(PFX + "step2.shell.text"));

		Control[] controls = parent.getChildren();
		for(Control control:controls){
			control.dispose();
		}

		Label info = new Label(parent,SWT.WRAP | SWT.CENTER);
		info.setText(I18N.translate(PFX + "step2.info.text"));

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
		alias_label.setText(I18N.translate(PFX + "step2.alias.text"));
		alias_label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		alias_label.setLayoutData(gridData);

		final Text alias_field =new Text(parent,SWT.BORDER);

		alias_field.setText(I18N.translate(PFX + "step2.alias.example"));


		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		alias_field.setLayoutData(gridData);

		Label alias_about = new Label(parent, SWT.NULL);
		alias_about.setText(I18N.translate(PFX + "step2.alias_about.text"));
		alias_about.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		alias_about.setLayoutData(gridData);



		// strength

		Label strength_label = new Label(parent,SWT.NULL);
		strength_label.setText(I18N.translate(PFX + "step2.strength.text"));
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
				I18N.translate(PFX + "step2.name.text"),
				I18N.translate(PFX + "step2.unit.text"),
				I18N.translate(PFX + "step2.org.text"),
				I18N.translate(PFX + "step2.city.text"),
				I18N.translate(PFX + "step2.state.text"),
				I18N.translate(PFX + "step2.country.text")
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
		shell.setText(I18N.translate(PFX + "step3.shell.text"));

		Control[] controls = parent.getChildren();
		for(Control control:controls){
			control.dispose();
		}


		Label info = new Label(parent,SWT.WRAP);
		info.setText(I18N.translate(PFX + "step3.info.text"));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		info.setLayoutData(gd);
		info.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		final Button sslEnabled = new Button(parent, SWT.CHECK);
		sslEnabled.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		sslEnabled.setText(I18N.translate(PFX + "step3.enablessl_button.text"));
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
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		if (instance == null || instance.shell == null || instance.shell.isDisposed()){
			new SSLCertWizard();
		}else
			instance.shell.setActive();

	}
}
