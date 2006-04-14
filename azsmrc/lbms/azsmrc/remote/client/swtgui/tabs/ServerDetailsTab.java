/*
 * Created on Apr 11, 2006
 * Created by omschaub
 * 
 */
package lbms.azsmrc.remote.client.swtgui.tabs;


import java.util.Map;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.remote.client.util.TimerEventPeriodic;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;


public class ServerDetailsTab {

	private Label azVer;
	private Label plVer;
	
	private Label saveDirSize, saveDir;
	private Label destDirSize, destDir;
	
	private Label totalDown, totalUp;

	private Group details1, details2, details3;
	private Composite parent;
	
	
	
	public ServerDetailsTab(CTabFolder parentTab){
		
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText("Server Details");
		
		
		parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		details1 = new Group(parent,SWT.NULL);
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
		azVer.setText("Not Received Yet");
		
		//Plugin Version
		Label plVerL = new Label(details1, SWT.NULL);
		plVerL.setText("AzSMRC Plugin Version:");
		
		plVer = new Label(details1, SWT.NULL);		
		plVer.setText("Not Received Yet");
		
		
		

		details2 = new Group(parent,SWT.NULL);
		details2.setText("Connection Details");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 30;
		details2.setLayout(gridLayout);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		details2.setLayoutData(gridData);
		
		
		
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
		
		//-------Details 3
		details3 = new Group(parent,SWT.NULL);
		details3.setText("Server Drive Information");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.horizontalSpacing = 30;
		details3.setLayout(gridLayout);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 2;
		details3.setLayoutData(gridData);
		
		
		
		//save.dir is the default save dir
		Label saveDirL = new Label(details3,SWT.NULL);
		saveDirL.setText("Default Save Directory:");
		
		saveDir = new Label(details3,SWT.NULL);
		saveDir.setText("Not Received Yet");
		
		saveDirSize = new Label(details3, SWT.NULL);
		saveDirSize.setText("Not Received Yet");
		
		//destination.dir is the user dir
		Label destDirL = new Label(details3,SWT.NULL);
		destDirL.setText("User Directory:");
		
		destDir = new Label(details3,SWT.NULL);
		destDir.setText("Not Received Yet");
		
		destDirSize = new Label(details3, SWT.NULL);
		destDirSize.setText("Not Received Yet");
		
		Button updateDriveInfo = new Button(details3,SWT.PUSH);
		updateDriveInfo.setText("Update Drive Information");
		updateDriveInfo.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				RCMain.getRCMain().getClient().getRemoteInfo().refreshDriveInfo();				
			}			
		});
		
		//call a drive refresh now that the labels are there
		//remoteInfo.refreshDriveInfo();
		
		
		//Button to restart server
		
		Button restartB = new Button(parent, SWT.PUSH);
		restartB.setText("Restart Server");
		restartB.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				MessageBox messageBox = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				messageBox.setText("Restart Azureus");
				messageBox.setMessage("Are you sure?");
				int response = messageBox.open();
				switch (response){
				case SWT.OK:
					RCMain.getRCMain().getClient().sendRestartAzureus();
					break;
				case SWT.CANCEL:
					break;
				}
				
			}
			
		});
		
		
		
		//Update Listener
		

		final TimerEventPeriodic updateTimerEvent =  RCMain.getRCMain().getMainTimer().addPeriodicEvent(1000,
				new TimerEventPerformer() {
			public void perform(TimerEvent event) {
				RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
					public void run() {
						try{							
							totalDown.setText(DisplayFormatters.formatByteCountToBase10KBEtc(StatsStreamGlobalManager.getTotalDownload()));
							totalUp.setText(DisplayFormatters.formatByteCountToBase10KBEtc(StatsStreamGlobalManager.getTotalUpload()));
							details2.layout();
							parent.layout();
						}catch(SWTException e){
							//do nothing.. if it cannot update, then who cares... don't throw an error
						}							
					}					
				});
			}
		});

		//CUL
		final ClientUpdateListener serverDetails = new ClientUpdateListener(){

			public void update(long updateSwitches) {
				if((updateSwitches & Constants.UPDATE_REMOTE_INFO) != 0){
					RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
						public void run() {
							try{
								azVer.setText(RCMain.getRCMain().getClient().getRemoteInfo().getAzureusVersion());
								plVer.setText(RCMain.getRCMain().getClient().getRemoteInfo().getPluginVersion());								//redraw the group
								details1.layout();
								parent.layout();
							}catch(SWTException e){
								//do nothing as the tab was probably disposed
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					});

					
				}
				
				if((updateSwitches & Constants.UPDATE_DRIVE_INFO) != 0){
					RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
						public void run() {
							try{			
								RCMain.getRCMain().getClient().getRemoteInfo().refreshDriveInfo();
								Map<String,String> driveMap = RCMain.getRCMain().getClient().getRemoteInfo().getDriveInfo();
								
								if(driveMap.containsKey("save.dir") && driveMap.containsKey("save.dir.path")){
									saveDir.setText(driveMap.get("save.dir.path"));
									saveDirSize.setText(DisplayFormatters.formatKBCountToBase10KBEtc(Long.parseLong(driveMap.get("save.dir"))) + " Free");
								}		
								
								if(driveMap.containsKey("destination.dir") && driveMap.containsKey("destination.dir.path")){
									destDir.setText(driveMap.get("destination.dir.path"));
									destDirSize.setText(DisplayFormatters.formatKBCountToBase10KBEtc(Long.parseLong(driveMap.get("destination.dir"))) + " Free");
								}
								
								//redraw the group
								details3.layout();
								parent.layout();
							}catch(SWTException e){
								//do nothing as the tab was probably disposed
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					});

				}
				
			}
		};

		RCMain.getRCMain().getClient().addClientUpdateListener(serverDetails);

		RCMain.getRCMain().getClient().getRemoteInfo().load();		
		
		//Dispose Listener for tab
		detailsTab.addDisposeListener(new DisposeListener (){

			public void widgetDisposed(DisposeEvent arg0) {
				updateTimerEvent.cancel();
				RCMain.getRCMain().getClient().removeClientUpdateListener(serverDetails);
			}

		});

		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	
}//EOF
