package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.swtgui.ErrorReporter;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.tools.i18n.I18N;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
		if(errorLog == null || errorLog.equalsIgnoreCase(""))
			errorLog = "The error that triggered this did not get reported in the error log file.";

	}

	public void open() {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) return;

		final Shell shell = new Shell(display.getActiveShell());
		shell.setText(title);
		if(!Utilities.isOSX){
			shell.setImage(display.getSystemImage(SWT.ICON_ERROR));
		}

		GridLayout layout = new GridLayout(1,false);
		shell.setLayout(layout);

		if(Utilities.isOSX){
			CLabel titleLabel = new CLabel(shell,SWT.NULL);
			titleLabel.setText(title);
			titleLabel.setImage(display.getSystemImage(SWT.ICON_ERROR));
		}

		Label detailsLabel = new Label(shell,SWT.NULL);
		detailsLabel.setText(I18N.translate(PFX + "detailsLabel.text"));


		Text label = new Text(shell, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		label.setText(additionalInfo);


		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 400;
		gridData.heightHint = 100;
		label.setLayoutData(gridData);

		Label errorlogLabel = new Label(shell,SWT.NULL);
		errorlogLabel.setText(I18N.translate(PFX + "errorlogLabel.text"));

		final Text text = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 400;
		gridData.heightHint = 100;
		text.setLayoutData(gridData);
		text.setText(errorLog);

		Label sysTextLabel = new Label(shell, SWT.NULL);
		sysTextLabel.setText(I18N.translate(PFX + "sysTextLabel.text"));

		final Text sysText = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;

		sysText.setLayoutData(gridData);
		sysText.setText(systemInfo);


		Group gSend = new Group(shell, SWT.NULL);
		gSend.setLayout(new GridLayout(1,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gSend.setLayoutData(gridData);


		Label infoLabel = new Label(gSend, SWT.WRAP);
		infoLabel.setText(I18N.translate(PFX + "infoLabel.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		infoLabel.setLayoutData(gridData);

		final Button bSendEmail = new Button(gSend, SWT.CHECK);
		bSendEmail.setText(I18N.translate(PFX + "sendemail.text"));


		final Text emailText = new Text(gSend, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		emailText.setLayoutData(gridData);
		emailText.setEnabled(false);

		bSendEmail.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				emailText.setEnabled(bSendEmail.getSelection());
			}
		});




		Composite panel = new Composite(shell, SWT.NULL);
		panel.setLayout(new GridLayout(3,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		panel.setLayoutData(gridData);


		Button ok = new Button(panel, SWT.PUSH);
		ok.setText(I18N.translate(I18N.translate(PFX + "send_button.text")));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		ok.setLayoutData(gridData);
		shell.setDefaultButton(ok);
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					if(emailText.getEnabled())
						er.setEmail(emailText.getText());
					er.setAdditionalInfo(additionalInfo);
					er.sendToServer();
					shell.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button cancel = new Button(panel, SWT.PUSH);
		cancel.setText(I18N.translate("global.close"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
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
