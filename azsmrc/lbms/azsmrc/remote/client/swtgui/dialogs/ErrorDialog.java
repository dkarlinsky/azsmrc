package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ErrorReporter;
import lbms.azsmrc.remote.client.swtgui.ErrorReporterListener;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.SWTSafeRunnable;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ErrorDialog {


	private String errorLog;

	private String systemInfo;

	private String errorID;

	private String title;

	private ErrorReporter er;

	final static private String PFX = "dialog.errordialog.";



	public ErrorDialog() {
		this.title = I18N.translate(PFX + "shell.text");

		er = new ErrorReporter();

		this.systemInfo = er.getSystemInfo();
		this.errorID = er.getErrorID();

		this.errorLog = er.getStackTrace();
		if(errorLog == null || errorLog.equalsIgnoreCase(""))
			errorLog = "The error that triggered this did not get reported in the error log file.";

	}

	public void open() {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) return;

		final Shell shell = new Shell();
		shell.setText(title);
		if(!Utilities.isOSX){
			shell.setImage(display.getSystemImage(SWT.ICON_ERROR));
		}

		er.addListener(new ErrorReporterListener() {
			public void errorSubmitted(boolean submitted) {
				if (submitted) {
					display.asyncExec(new SWTSafeRunnable() {
						public void runSafe() {
							shell.dispose();
						}
					});
				} else {
					display.asyncExec(new SWTSafeRunnable() {
						public void runSafe() {
							MessageBox messageBox = new MessageBox(shell,SWT.ICON_INFORMATION | SWT.OK);
							messageBox.setText(I18N.translate("global.error"));
							messageBox.setMessage(I18N.translate(PFX + "errorbox.message"));
							messageBox.open();
							return;
						}
					});
				}
			}
			/* (non-Javadoc)
			 * @see lbms.azsmrc.remote.client.swtgui.ErrorReporterListener#redirectTo(java.lang.String)
			 */
			public void redirectTo(String url) {
				try {
					Desktop.getDesktop().browse(new URI (url));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

			}
			/* (non-Javadoc)
			 * @see lbms.azsmrc.remote.client.swtgui.ErrorReporterListener#showText(java.lang.String)
			 */
			public void showText(String text) {
				// TODO Auto-generated method stub

			}
		});

		GridLayout layout = new GridLayout(1,false);
		shell.setLayout(layout);

		if(Utilities.isOSX){
			CLabel titleLabel = new CLabel(shell,SWT.NULL);
			titleLabel.setText(title);
			titleLabel.setImage(display.getSystemImage(SWT.ICON_ERROR));
		}


		Label errorlogLabel = new Label(shell,SWT.NULL);
		errorlogLabel.setText(I18N.translate(PFX + "errorlogLabel.text"));

		final Text text = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.widthHint = 500;
		gridData.heightHint = 75;
		text.setLayoutData(gridData);
		text.setText(errorLog);

		Label errorIDTextLabel = new Label(shell,SWT.NULL);
		errorIDTextLabel.setText(I18N.translate(PFX + "errorIDTextLabel.text"));

		final Text errorIDText = new Text(shell, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		errorIDText.setLayoutData(gridData);
		errorIDText.setText(errorID);

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


		Label additionalInfoLabel = new Label(gSend,SWT.NULL);
		additionalInfoLabel.setText(I18N.translate(PFX + "additionalInfoLabel.text"));

		final Text additionalInfoText = new Text(gSend, SWT.MULTI | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 100;
		additionalInfoText.setLayoutData(gridData);



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
		panel.setLayout(new GridLayout(4,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		panel.setLayoutData(gridData);


		final Button ok = new Button(panel, SWT.PUSH);
		ok.setText(I18N.translate(I18N.translate(PFX + "send_button.text")));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		ok.setLayoutData(gridData);
		shell.setDefaultButton(ok);
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					if(emailText.getEnabled())
						er.setEmail(emailText.getText());
					if(additionalInfoText != null)
						er.setAdditionalInfo(additionalInfoText.getText());
					er.sendToServer();
					ok.setEnabled(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		final Button btnEmail = new Button(panel, SWT.PUSH);
		btnEmail.setText(I18N.translate(I18N.translate(PFX + "email_button.text")));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		btnEmail.setLayoutData(gridData);

		btnEmail.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					if(additionalInfoText != null)
						er.setAdditionalInfo(additionalInfoText.getText());
					er.sendPerEMail();
					shell.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		final Button copyToClip = new Button(panel, SWT.PUSH);
		copyToClip.setText(I18N.translate(PFX + "copyToClip_button.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		copyToClip.setLayoutData(gridData);

		copyToClip.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				try{
					if(additionalInfoText != null)
						er.setAdditionalInfo(additionalInfoText.getText());
					final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(er.getFormattedReport()),null);
				}catch(Exception e){
					e.printStackTrace();
				}

			}

		});

		final Button cancel = new Button(panel, SWT.PUSH);
		cancel.setText(I18N.translate("global.close"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.pack();
		GUI_Utilities.centerShellandOpen(shell);
	}

	public static ErrorDialog createAndOpen() {
		ErrorDialog ed = new ErrorDialog();
		ed.open();
		return ed;
	}

}
