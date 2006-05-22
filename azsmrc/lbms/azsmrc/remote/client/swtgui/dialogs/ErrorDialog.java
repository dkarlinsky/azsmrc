package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.swtgui.ErrorReporter;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.tools.i18n.I18N;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ErrorDialog {


	private String errorLog;

	private String email;

	private String additionalInfo;

	private String systemInfo;

	private String title;

	private ErrorReporter er;

	final static private String PFX = "dialog.errordialog.";



	public ErrorDialog(String title, String additionalInfo) {
		this.title = title;

		this.additionalInfo = additionalInfo;

		er = new ErrorReporter();

		this.systemInfo = er.getSystemInfo();

		this.errorLog = er.getErrorLog();


	}

	public void open() {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) return;

		final Shell shell = new Shell(display.getActiveShell());
		shell.setText(title);


		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		Text label = new Text(shell, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		label.setText(additionalInfo);


		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 400;
		label.setLayoutData(gridData);



		final Text text = new Text(shell, SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.widthHint = 400;
		gridData.heightHint = 100;
		text.setLayoutData(gridData);
		text.setText(errorLog);


		final Text sysText = new Text(shell, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 300;

		sysText.setLayoutData(gridData);
		sysText.setText(systemInfo);


		Group gSend = new Group(shell, SWT.NULL);
		gSend.setLayout(new GridLayout(1,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gSend.setLayoutData(gridData);


		final Button bSendEmail = new Button(gSend, SWT.CHECK);
		bSendEmail.setText(I18N.translate(PFX + "sendemail.text"));		


		final Text emailText = new Text(gSend, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		emailText.setLayoutData(gridData);

		bSendEmail.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {				
				emailText.setEnabled(bSendEmail.getSelection());
			}			
		});




		Composite panel = new Composite(shell, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		panel.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		panel.setLayoutData(gridData);


		Button ok = new Button(panel, SWT.PUSH);
		ok.setText(I18N.translate(I18N.translate(PFX + "send_button.text")));
		gridData = new GridData();
		gridData.widthHint = 70;
		ok.setLayoutData(gridData);
		shell.setDefaultButton(ok);
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					
					shell.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button cancel = new Button(panel, SWT.PUSH);
		cancel.setText(I18N.translate("global.close"));
		gridData = new GridData();
		gridData.widthHint = 70;
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.pack();
		GUI_Utilities.centerShellandOpen(shell);





	}




}
