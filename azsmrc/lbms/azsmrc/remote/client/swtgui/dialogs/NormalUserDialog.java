package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.UserNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NormalUserDialog {

	//I18N prefix
	public static final String PFX = "dialog.normaluserdialog.";

	private NormalUserDialog(Display display, String username) {
		if (display == null)
			return;

		final Shell shell = new Shell(display.getActiveShell());
		shell.setText(I18N.translate(PFX + "shell.text"));
		final User user;

		try {
			user = RCMain.getRCMain().getClient().getUserManager().getUser(username);
			if(!user.equals(RCMain.getRCMain().getClient().getUserManager().getActiveUser())){
				MessageBox messageBox = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
				messageBox.setText(I18N.translate("global.error"));
				messageBox.setMessage(I18N.translate(PFX + "error_userNotActive.message"));
				messageBox.open();
				shell.dispose();
				return;
			}
		} catch (UserNotFoundException e1) {
			e1.printStackTrace();
			MessageBox messageBox = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
			messageBox.setText(I18N.translate("global.error"));
			messageBox.setMessage(I18N.translate(PFX + "error_userNotFound.message"));
			messageBox.open();
			shell.dispose();
			return;
		}


		GridLayout layout = new GridLayout(2, false);
		shell.setLayout(layout);

		Label newPasswordLabel = new Label(shell, SWT.WRAP);
		newPasswordLabel.setText(I18N.translate(PFX + "newpassword.label.text"));
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		newPasswordLabel.setLayoutData(gridData);

		final Text newPasswordText = new Text(shell, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;
		newPasswordText.setLayoutData(gridData);

		Label verifyPasswordLabel = new Label(shell, SWT.WRAP);
		verifyPasswordLabel.setText(I18N.translate(PFX + "verifypassword.label.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		verifyPasswordLabel.setLayoutData(gridData);

		final Text verifyPasswordText = new Text(shell, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;
		verifyPasswordText.setLayoutData(gridData);


		Composite panel = new Composite(shell, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		panel.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		panel.setLayoutData(gridData);
		Button ok = new Button(panel, SWT.PUSH);
		ok.setText(I18N.translate("global.accept"));
		gridData = new GridData();
		gridData.widthHint = 70;
		ok.setLayoutData(gridData);
		shell.setDefaultButton(ok);
		ok.addListener(SWT.Selection, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				try {
					if(!newPasswordText.getText().equals(verifyPasswordText.getText())){
						MessageBox messageBox = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "error_PW_notMatched.message"));
						messageBox.open();
						newPasswordText.setText("");
						verifyPasswordText.setText("");
						return;
					}

					user.setPassword(newPasswordText.getText());


					MessageBox mb = new MessageBox(shell,SWT.ICON_INFORMATION);
					mb.setText(I18N.translate(PFX + "info_changedPW.title"));
					mb.setMessage(I18N.translate(PFX + "info_changedPW.message"));
					mb.open();

					RCMain.getRCMain().disconnect();
					if(RCMain.getRCMain().getMainWindow() != null){
						RCMain.getRCMain().getMainWindow().setLogInOutButtons(false);
						RCMain.getRCMain().getMainWindow().setSSLStatusBar(false, false);
						RCMain.getRCMain().getMainWindow().setConnectionStatusBar(0);
					}
					RCMain.getRCMain().setTrayIcon(0);
					shell.dispose();


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button cancel = new Button(panel, SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		gridData = new GridData();
		gridData.widthHint = 70;
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.pack();
		GUI_Utilities.centerShellandOpen(shell);

		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		return;
	}

	public static void open(String username) {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		Shell[] shells = display.getShells();
		for(Shell shell:shells){
			if(shell.getText().equalsIgnoreCase(I18N.translate(PFX + "shell.text"))){
				shell.setActive();
				return;
			}
		}
		new NormalUserDialog(display, username);
	}




}


