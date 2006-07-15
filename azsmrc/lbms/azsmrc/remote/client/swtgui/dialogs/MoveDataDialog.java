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
import org.eclipse.swt.widgets.Display;
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
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.container.Container;

public class MoveDataDialog {

	private Download download;
	private DownloadAdvancedStats das;
	private Label tDir;
	private Group gName;

	private Shell shell;

	private static MoveDataDialog instance;

	//I18N prefix
	private static final String PFX = "dialog.movedatadialog.";

	private MoveDataDialog(Container container){
		instance = this;

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
							gName.layout();

						}
					});
				}

			}
		};

		//Add the CUL to the Client
		RCMain.getRCMain().getClient().addClientUpdateListener(cul);

		shell = new Shell(RCMain.getRCMain().getDisplay());
		shell.setText(I18N.translate(PFX + "shell.text"));
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

		gName = new Group(parent,SWT.NULL);
		gName.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		gName.setLayoutData(gd);

		gName.setText(I18N.translate(PFX + "torrentdetails.group.text"));

		Label tNameL = new Label(gName,SWT.NULL);
		tNameL.setText(I18N.translate(PFX + "torrentdetails.name.text"));

		Label tName = new Label(gName,SWT.NULL);
		String torrentName = download.getName();
		if(torrentName.length() > 53){
			torrentName = torrentName.substring(0,50) + "...";
		}
		tName.setText(torrentName);

		Label tDirL = new Label(gName,SWT.NULL);
		tDirL.setText(I18N.translate(PFX + "torrentdetails.currentdir.text"));

		tDir = new Label(gName,SWT.WRAP);
		tDir.setText(das.getSaveDir());

		//--------------------

		Label moveToL = new Label(parent,SWT.NULL);
		moveToL.setText(I18N.translate(PFX + "torrentdetails.moveto.text"));
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
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "move.samelocal.message"));
						messageBox.open();
						return;
					}else{
						download.moveDataFiles(moveTo.getText());
						shell.dispose();
						MessageBox messageBox = new MessageBox(RCMain.getRCMain().getMainWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText(I18N.translate(PFX + "move.title"));
						messageBox.setMessage(I18N.translate(PFX + "move.message"));
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
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "move.samelocal.message"));
					messageBox.open();
					return;
				}else{
					download.moveDataFiles(moveTo.getText());
					shell.dispose();
					MessageBox messageBox = new MessageBox(RCMain.getRCMain().getMainWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
					messageBox.setText(I18N.translate(PFX + "move.title"));
					messageBox.setMessage(I18N.translate(PFX + "move.message"));					messageBox.open();
					return;
				}

			}
		});

		Button cancel = new Button(parent, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		cancel.setLayoutData(gd);
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				shell.dispose();
			}
		});


		//Center Shell and open
		shell.pack();
		GUI_Utilities.centerShellandOpen(shell);

	}

	/**
	 * Static open method
	 * @param container
	 */
	public static void open(final Container container){
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
				if(display == null) return;
				if (instance == null || instance.shell == null || instance.shell.isDisposed()){
					new MoveDataDialog(container);
				}else
					instance.shell.setActive();
		}
}
