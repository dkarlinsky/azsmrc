/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.container.Container;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;

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


public class ChangeCategoryDialog {

	//I18N prefix
	public static final String PFX = "dialog.changecategorydialog.";

	private static ChangeCategoryDialog instance;

	private Shell shell;

	private ChangeCategoryDialog(final Container[] downloads){
		instance = this;

		//Shell
		shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));


		//Comp on shell
		Group comp = new Group(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);




		//first line
		Label category_label = new Label(comp,SWT.NULL);
		category_label.setText(I18N.translate(PFX + "category_label.text"));


		final Text category_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 450;
		category_text.setLayoutData(gridData);


		//pull the clipboard
		/*final Clipboard cb = new Clipboard(display);
		TextTransfer transfer = TextTransfer.getInstance();
		String clipboard = (String)cb.getContents(transfer);*/



		//Buttons
		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

		Button change = new Button(button_comp,SWT.PUSH);
		change.setText(I18N.translate(PFX + "changebutton.text"));
		change.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				if (category_text != null && !category_text.isDisposed()) {
					String cat_name = category_text.getText();
					for (Container dl : downloads) {
						dl.getDownload().setTorrentAttribute(RemoteConstants.TA_CATEGORY, cat_name);
					}
				}
				shell.close();
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
			}
		});


		//Center Shell and open
		GUI_Utilities.centerShellOpenAndFocus(shell);
	}



	/**
	 * Open a OpenByURLDialog without a URL
	 *
	 */
	public static void open(final Container[] downloads){
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				if(display == null) {
					return;
				}
				if (instance == null || instance.shell == null || instance.shell.isDisposed()){
					new ChangeCategoryDialog(downloads);
				} else {
					instance.shell.setActive();
				}

			}
		});
	}

}//EOF