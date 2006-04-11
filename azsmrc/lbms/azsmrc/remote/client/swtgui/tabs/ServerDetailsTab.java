/*
 * Created on Apr 11, 2006
 * Created by omschaub
 * 
 */
package lbms.azsmrc.remote.client.swtgui.tabs;


import lbms.azsmrc.remote.client.RemoteInfo;
import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;


public class ServerDetailsTab {

	private Label azVer;
	private Label plVer;


	public ServerDetailsTab(CTabFolder parentTab, final RemoteInfo remoteInfo){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText("Server Details");
		
		
		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		Group details1 = new Group(parent,SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		details1.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		details1.setLayoutData(gridData);

		//Server Name
		Label nameL = new Label(details1, SWT.NULL);
		nameL.setText("Server Name: ");
		
		Label name = new Label(details1,SWT.NULL);
		name.setText(RCMain.getRCMain().getClient().getServer().getHost().toString());
		
		//Connected User
		Label userL = new Label(details1,SWT.NULL);
		userL.setText("Connected as: ");
		
		Label user = new Label(details1, SWT.NULL);
		user.setText(RCMain.getRCMain().getClient().getUsername());
		
		//Azureus Version
		Label azVerL = new Label(details1, SWT.NULL);
		azVerL.setText("Server Azureus Version: ");
		
		azVer = new Label(details1,SWT.NULL);
		if(remoteInfo == null)
			azVer.setText("Not Received Yet");
		else
			azVer.setText(remoteInfo.getAzureusVersion());
		
		//Plugin Version
		Label plVerL = new Label(details1, SWT.NULL);
		plVerL.setText("AzSMRC Plugin Version: ");
		
		plVer = new Label(details1, SWT.NULL);
		if(remoteInfo == null)
			plVer.setText("Not Received Yet");
		else
			plVer.setText(remoteInfo.getPluginVersion());
		
		
		
		
		
		
		//Dispose Listener for tab
		detailsTab.addDisposeListener(new DisposeListener (){

			public void widgetDisposed(DisposeEvent arg0) {
				
			}

		});

		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	
}//EOF
