package lbms.azsmrc.remote.client.swtgui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadAdvancedStats;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.container.Container;

public class MoveDataDialog {

    private Download download;    
    private DownloadAdvancedStats das;
	private Label tDir;
    
	public MoveDataDialog(Container container){
		download = container.getDownload();		
        das = download.getAdvancedStats();
		
        if(!das._isLoaded()){
            das.load();
        }
        
        //The Client update listener
        final ClientUpdateListener cul = new ClientUpdateListener(){

            public void update(long updateSwitches) {

                if((updateSwitches & Constants.UPDATE_ADVANCED_STATS) != 0){
                    das = download.getAdvancedStats();
                    RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
						public void run() {
							tDir.setText(das.getSaveDir());							
						}                    	
                    });
                }

            }
        };

        //Add the CUL to the Client
        RCMain.getRCMain().getClient().addClientUpdateListener(cul);
    
		final Shell shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setText("Move Data on Server");
		shell.setLayout(new GridLayout(1,false));

        //Listen for when tab is closed and make sure to remove the client update listener
        shell.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                RCMain.getRCMain().getClient().removeClientUpdateListener(cul);                
            }
        });
		
		
		//Main Composite on shell
		Composite parent = new Composite(shell,SWT.NULL);
		parent.setLayout(new GridLayout(2,false));		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;		
		parent.setLayoutData(gd);
		
		Group gName = new Group(parent,SWT.NULL);
		gName.setLayout(new GridLayout(2,false));		
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;		
		gd.horizontalSpan = 2;		 
		gName.setLayoutData(gd);
		
		gName.setText("Torrent Information");
		
		Label tNameL = new Label(gName,SWT.NULL);
		tNameL.setText("Torrent Name: ");
		
		Label tName = new Label(gName,SWT.NULL);
		String torrentName = download.getName();
		if(torrentName.length() > 53){
			torrentName = torrentName.substring(0,50) + "...";
		}
		tName.setText(torrentName);
		
		Label tDirL = new Label(gName,SWT.NULL);
		tDirL.setText("Current Remote Directory: ");
		
		tDir = new Label(gName,SWT.WRAP);
		tDir.setText(das.getSaveDir());
		
		//--------------------
		
		Label moveToL = new Label(parent,SWT.NULL);
		moveToL.setText("Input remote server directory");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		moveToL.setLayoutData(gd);	
		
		final Text moveTo = new Text(parent,SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		moveTo.setLayoutData(gd);
		moveTo.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent arg0) {}

			public void keyReleased(KeyEvent arg0) {
				if(arg0.character == SWT.CR){
					if(das.getSaveDir().equalsIgnoreCase(moveTo.getText())){
						MessageBox messageBox = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Error");
						messageBox.setMessage("You have put in the same directory where the torrent is already stored.");
						messageBox.open();
						return;
					}else{
						download.moveDataFiles(moveTo.getText());
						shell.dispose();
						MessageBox messageBox = new MessageBox(RCMain.getRCMain().getMainWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText("Request Sent");
						messageBox.setMessage("Your move data request has been sent to the server.");
						messageBox.open();
						return;
					}
				}
				
			}
			
		});
		
		
		Button ok = new Button(parent, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		ok.setLayoutData(gd);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(das.getSaveDir().equalsIgnoreCase(moveTo.getText())){
					MessageBox messageBox = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("Error");
					messageBox.setMessage("You have put in the same directory where the torrent is already stored.");
					messageBox.open();
					return;
				}else{
					download.moveDataFiles(moveTo.getText());
					shell.dispose();
					MessageBox messageBox = new MessageBox(RCMain.getRCMain().getMainWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
					messageBox.setText("Request Sent");
					messageBox.setMessage("Your move data request has been sent to the server.");
					messageBox.open();
					return;
				}
				
			}			
		});
		
		Button cancel = new Button(parent, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		cancel.setLayoutData(gd);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				shell.dispose();				
			}			
		});		
		
		
		//Center Shell and open
		shell.pack();
        GUI_Utilities.centerShellandOpen(shell);

	}
}
