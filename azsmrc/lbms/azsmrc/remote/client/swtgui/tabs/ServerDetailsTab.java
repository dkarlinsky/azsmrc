/*
 * Created on Apr 11, 2006
 * Created by omschaub
 * 
 */
package lbms.azsmrc.remote.client.swtgui.tabs;


import lbms.azsmrc.remote.client.RemoteInfo;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.tools.stats.StatsStreamGlobalManager;

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
	private Label kbpsDown, kbpsUp;
	private Label totalDown, totalUp;


	public ServerDetailsTab(CTabFolder parentTab, final RemoteInfo remoteInfo){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText("Server Details");
		
		
		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		Group details1 = new Group(parent,SWT.NULL);
		details1.setText("Server Details");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 30;
		details1.setLayout(gridLayout);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		details1.setLayoutData(gridData);

		//Server Name
		Label nameL = new Label(details1, SWT.NULL);
		nameL.setText("Server Name:");
		
		Label name = new Label(details1,SWT.NULL);
		name.setText(RCMain.getRCMain().getClient().getServer().getHost().toString());
		
		//port		
		Label portL = new Label(details1,SWT.NULL);
		portL.setText("Port:");
	
		Label port = new Label(details1, SWT.NULL);
		port.setText(String.valueOf(RCMain.getRCMain().getClient().getServer().getPort()));
		
		
		//Protocol
		Label protocolL = new Label(details1,SWT.NULL);
		protocolL.setText("Protocol:");
		
		Label protocol = new Label(details1,SWT.NULL);
		protocol.setText(RCMain.getRCMain().getClient().getServer().getProtocol());
		
						
		//Connected User
		Label userL = new Label(details1,SWT.NULL);
		userL.setText("Connected as:");
		
		Label user = new Label(details1, SWT.NULL);
		user.setText(RCMain.getRCMain().getClient().getUsername());
		
		//Azureus Version
		Label azVerL = new Label(details1, SWT.NULL);
		azVerL.setText("Azureus Version:");
		
		azVer = new Label(details1,SWT.NULL);
		if(remoteInfo == null)
			azVer.setText("Not Received Yet");
		else
			azVer.setText(remoteInfo.getAzureusVersion());
		
		//Plugin Version
		Label plVerL = new Label(details1, SWT.NULL);
		plVerL.setText("AzSMRC Plugin Version:");
		
		plVer = new Label(details1, SWT.NULL);
		if(remoteInfo == null)
			plVer.setText("Not Received Yet");
		else
			plVer.setText(remoteInfo.getPluginVersion());
		
		
		

		Group details2 = new Group(parent,SWT.NULL);
		details2.setText("Connection Details");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 30;
		details2.setLayout(gridLayout);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		details2.setLayoutData(gridData);
		
		
		//Speed Down
		Label kbpsDownL = new Label(details2,SWT.NULL);
		kbpsDownL.setText("Speed Receiving:");
		
		kbpsDown = new Label(details2, SWT.NULL);
		kbpsDown.setText(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(StatsStreamGlobalManager.getKbpsDownload()));
		
		//Speed Up
		Label kbpsUpL = new Label(details2,SWT.NULL);
		kbpsUpL.setText("Speed Sending:");
		
		kbpsUp = new Label(details2, SWT.NULL);
		kbpsUp.setText(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(StatsStreamGlobalManager.getKbpsUpload()));
				
		
		//Total download
		Label totalDownL = new Label(details2,SWT.NULL);
		totalDownL.setText("Total Received:");
		
		totalDown = new Label(details2, SWT.NULL);
		totalDown.setText(DisplayFormatters.formatByteCountToBase10KBEtc(StatsStreamGlobalManager.getTotalDownload()));
		
		//Total upload
		Label totalUpL = new Label(details2,SWT.NULL);
		totalUpL.setText("Total Sent:");
		
		totalUp = new Label(details2, SWT.NULL);
		totalUp.setText(DisplayFormatters.formatByteCountToBase10KBEtc(StatsStreamGlobalManager.getTotalUpload()));
		
		
		//Update Listener
		
		
		
		
		//Dispose Listener for tab
		detailsTab.addDisposeListener(new DisposeListener (){

			public void widgetDisposed(DisposeEvent arg0) {
				
			}

		});

		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	
}//EOF
