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


public class OpenByURLDialog {

	//I18N prefix
	public static final String PFX = "dialog.openbyurldialog.";

	private static OpenByURLDialog instance;

	private Shell shell;

	private OpenByURLDialog(final String url){
		instance = this;

		//Shell
		shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));
		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("open_by_url"));

		//Comp on shell
		Group comp = new Group(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);




		//first line
		Label url_label = new Label(comp,SWT.NULL);
		url_label.setText(I18N.translate(PFX + "url_label.text"));


		final Text url_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 450;
		url_text.setLayoutData(gridData);


		//pull the clipboard
		/*final Clipboard cb = new Clipboard(display);
		TextTransfer transfer = TextTransfer.getInstance();
		String clipboard = (String)cb.getContents(transfer);*/

		if(url != null){
			if(url.startsWith("http") || url.startsWith("magnet")){
				url_text.setText(url);
			}/*else if(clipboard.startsWith("www")){
				url_text.setText("http://" + clipboard);
			}*/
		}

		//Second Line
		final Button needUserPass_button = new Button(comp,SWT.CHECK);
		needUserPass_button.setText(I18N.translate(PFX + "needUserPass_button.text"));
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		needUserPass_button.setLayoutData(gridData);


		//Third Line
		final Label username_label = new Label(comp,SWT.NULL);
		username_label.setText(I18N.translate(PFX + "username"));


		final Text username_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 200;
		username_text.setLayoutData(gridData);


		//Fourth Line
		final Label password_label = new Label(comp,SWT.NULL);
		password_label.setText(I18N.translate(PFX + "password"));


		final Text password_text = new Text(comp,SWT.PASSWORD | SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 200;
		password_text.setLayoutData(gridData);

		username_label.setEnabled(false);
		username_text.setEnabled(false);
		password_label.setEnabled(false);
		password_text.setEnabled(false);

		needUserPass_button.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				if(needUserPass_button.getSelection()){
					username_label.setEnabled(true);
					username_text.setEnabled(true);
					password_label.setEnabled(true);
					password_text.setEnabled(true);
				}else{
					username_label.setEnabled(false);
					username_text.setEnabled(false);
					password_label.setEnabled(false);
					password_text.setEnabled(false);
				}
			}
		});


		//Buttons
		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

		Button connect = new Button(button_comp,SWT.PUSH);
		connect.setText("Send URL to Server");
		connect.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				if(url_text.getText().length() <= 1){
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "error1"));
					messageBox.open();
					return;
				}
				String url = url_text.getText();
				if(!url.startsWith("http://") && !url.startsWith("magnet")){
					url = "http://" + url;
				}

				if(needUserPass_button.getSelection()){
					if(username_text.getText().equalsIgnoreCase("")
							||password_text.getText().equalsIgnoreCase("")){
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "error2"));
						messageBox.open();
						return;
					}else
						RCMain.getRCMain().getClient().getDownloadManager().addDownload(url, username_text.getText(), password_text.getText(), null, null);
				}else{
					RCMain.getRCMain().getClient().getDownloadManager().addDownload(url);

				}
				shell.close();
				//Re-Add the AWT monitor
				//cb.dispose();
				//FireFrogMain.getFFM().addAWTClipboardMonitor();
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
				//Re-Add the AWT monitor
				//cb.dispose();
				RCMain.getRCMain().addAWTClipboardMonitor();
			}
		});


		//Center Shell and open
		GUI_Utilities.centerShellOpenAndFocus(shell);
	}


	/**
	 * Open a OpenByURLDialog with a URL
	 * @param URL
	 */
	public static void openWithURL(final String URL){
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if(display == null) return;
				if (instance == null || instance.shell == null || instance.shell.isDisposed()){
					new OpenByURLDialog(URL);
				}else
					instance.shell.setActive();

			}

		});

	}

	/**
	 * Open a OpenByURLDialog without a URL
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
					new OpenByURLDialog(url);
				}else
					instance.shell.setActive();

			}
		});
	}

}//EOF