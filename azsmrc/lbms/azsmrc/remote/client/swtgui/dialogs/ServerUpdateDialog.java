/*
 * Created on Apr 13, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;



import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.RemoteUpdate;
import lbms.azsmrc.remote.client.RemoteUpdateManager;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class ServerUpdateDialog {

//	I18N prefix
	public static final String PFX = "dialog.serverupdatedialog.";

	private Shell shell;
	private static ServerUpdateDialog instance;
	Label infoLabel;
	Table table;

	private ClientUpdateListener clientUpdate = new ClientUpdateListener() {
		/* (non-Javadoc)
		 * @see lbms.azsmrc.remote.client.events.ClientUpdateListener#update(long)
		 */
		public void update(long updateSwitches) {
			if ((updateSwitches & Constants.UPDATE_UPDATE_INFO) != 0) {
				RCMain.getRCMain().getDisplay().asyncExec(new SWTSafeRunnable() {
					public void runSafe() {
						RemoteUpdateManager rum = RCMain.getRCMain().getClient().getRemoteUpdateManager();
						infoLabel.setText(I18N.translate(PFX + "infolabel.line1.text") + " " +
								RCMain.getRCMain().getClient().getServer().getHost() +
								I18N.translate(PFX + "infolabel.line2.text") + " " + rum.getUpdates().length);

						table.removeAll();

						RemoteUpdate[] rus = rum.getUpdates();
						for(RemoteUpdate ru:rus){
							TableItem item = new TableItem(table,SWT.NULL);
							item.setText(1,ru.getName());
							item.setText(ru.getNewVersion());
							item.setChecked(true);
							item.setData(ru);
						}
					};
				});
			}

		}
	};

	private ServerUpdateDialog(){
		instance = this;

		final RemoteUpdateManager rum = RCMain.getRCMain().getClient().getRemoteUpdateManager();
		rum.load();

		//Shell
		shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		//Comp on shell
		Composite comp = new Composite(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);


		//first line
		infoLabel = new Label(comp,SWT.BORDER | SWT.CENTER);
		infoLabel.setText(I18N.translate(PFX + "infolabel.line1.text") + " " +
				RCMain.getRCMain().getClient().getServer().getHost() +
				I18N.translate(PFX + "infolabel.line2.text") + " " + rum.getUpdates().length);
		infoLabel.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_GRAY));

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		infoLabel.setLayoutData(gd);



		//Table for updates
		table = new Table(comp,SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.SINGLE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 30;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);

		TableColumn spacerForCheck = new TableColumn(table,SWT.NULL);
		spacerForCheck.setWidth(30);

		TableColumn name = new TableColumn(table,SWT.NULL);
		name.setText(I18N.translate(PFX + "table.column.name.text"));
		name.setWidth(300);

		TableColumn version = new TableColumn(table,SWT.NULL);
		version.setText(I18N.translate(PFX + "table.column.newversion.text"));
		version.setWidth(100);

		//populate the table with the updates
		RemoteUpdate[] rus = rum.getUpdates();
		for(RemoteUpdate ru:rus){
			TableItem item = new TableItem(table,SWT.NULL);
			item.setText(1,ru.getName());
			item.setText(ru.getNewVersion());
			item.setChecked(true);
			item.setData(ru);
		}

		//Bottom Buttons
		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);


		Button commit = new Button(button_comp,SWT.PUSH);
		commit.setText(I18N.translate(PFX + "commit_button.text"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		commit.setLayoutData(gd);
		commit.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getItems();
				List<String> names = new ArrayList<String>();
				for(TableItem item:items){
					if(item.getChecked()){
						RemoteUpdate ru = (RemoteUpdate)item.getData();
						names.add(ru.getName());
					}
				}
				rum.applyUpdates(names.toArray(new String[]{}));
			}
		});




		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		cancel.setLayoutData(gd);
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
			 }
		 });

		RCMain.getRCMain().getClient().addClientUpdateListener(clientUpdate);

		shell.addShellListener(new ShellListener() {
			public void shellActivated(ShellEvent e) {}

			public void shellClosed(ShellEvent e) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(clientUpdate);
			}

			public void shellDeactivated(ShellEvent e) {}
			public void shellDeiconified(ShellEvent e) {}
			public void shellIconified(ShellEvent e) {}
		});
		//Center and open shell
		GUI_Utilities.centerShellandOpen(shell);
	}

	/**
	 * Check to make sure that there are no other ones open and if not
	 * open the ServerUpdateDialog
	 */
	public static void open() {
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if(display == null) return;
				if (instance == null || instance.shell == null || instance.shell.isDisposed()){
					new ServerUpdateDialog();
				}else
					instance.shell.setActive();


			}

		});

	}

}//EOF
