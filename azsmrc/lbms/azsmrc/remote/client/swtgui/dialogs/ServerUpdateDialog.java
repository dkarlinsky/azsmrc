/*
 * Created on Apr 13, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;



import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.RemoteUpdate;
import lbms.azsmrc.remote.client.RemoteUpdateManager;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.eclipse.swt.SWT;
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



	private ServerUpdateDialog(){
		final RemoteUpdateManager rum = RCMain.getRCMain().getClient().getRemoteUpdateManager();


		//Shell
		final Shell shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setLayout(new GridLayout(1,false));
		shell.setText("Updates Available");

		//Comp on shell
		Composite comp = new Composite(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);


		//first line
		Label infoLabel = new Label(comp,SWT.BORDER | SWT.CENTER);
		infoLabel.setText("Azureus Updates Available\nServer: " +
				RCMain.getRCMain().getClient().getServer().getHost() +
				"\nNumber of Updates: " + rum.getUpdates().length);
		infoLabel.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_GRAY));

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		infoLabel.setLayoutData(gd);



		//Table for updates
		final Table table = new Table(comp,SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.SINGLE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 30;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);

		TableColumn spacerForCheck = new TableColumn(table,SWT.NULL);
		spacerForCheck.setWidth(30);

		TableColumn name = new TableColumn(table,SWT.NULL);
		name.setText("Name");
		name.setWidth(300);

		TableColumn version = new TableColumn(table,SWT.NULL);
		version.setText("New Version");
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
		commit.setText("Send Checked to Server");
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
		cancel.setText("Cancel");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		cancel.setLayoutData(gd);
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
			 }
		 });



		//Center and open shell
		GUI_Utilities.centerShellandOpen(shell);
	}

	/**
	 * Check to make sure that there are no other ones open and if not 
	 * open the ServerUpdateDialog
	 */
	public static void open() {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.asyncExec(new Runnable(){
			public void run() {
				Shell[] shells = RCMain.getRCMain().getDisplay().getShells();
				for(int i = 0; i < shells.length; i++){
					if(shells[i].getText().equalsIgnoreCase("Updates Available")){
						shells[i].setActive();
						shells[i].setFocus();
						return;
					}
				}
			   new ServerUpdateDialog();
				
			}
			
		});

	}
	
}//EOF
