/**
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.tools.Download;
import lbms.tools.DownloadListener;
import lbms.tools.updater.UpdateProgressListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Damokles
 *
 */
public class UpdateProgressDialog {
	public static final String PFX = "dialog.updateprogressdialog.";
	private Display display;
	private Download[] downloads;
	private List<DownloadContainer> dcs = new ArrayList<DownloadContainer>();
	private Composite parent;
	private Label statusLabel, picLabel;
	private Button close;
    private Color ltGray;
	
	private UpdateProgressDialog (Download[] dls, Display d) {
		display = d;
		downloads = dls;
	}

	private void initDownloads () {
		for (Download d:downloads) {
			dcs.add(new DownloadContainer(d, parent));
		}
	}

	private void createContents() {
		//define the color
		ltGray = new Color(display, new RGB(240,240,240));
		
		
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		picLabel = new Label(shell,SWT.NULL);
		picLabel.setImage(display.getSystemImage(SWT.ICON_INFORMATION));

		statusLabel = new Label(shell,SWT.NULL);
		setStatusLabel(UpdateProgressListener.STATE_INITIALIZING);


		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 400;
		gd.heightHint = 300;
		gd.horizontalSpan = 2;
		sc.setLayoutData(gd);

		parent = new Composite(sc, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		parent.setLayoutData(gd);
		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 15;
		parent.setLayout(gl);
		parent.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		sc.setContent(parent);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				sc.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		
		close = new Button(shell, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		close.setText(I18N.translate("global.close"));
		close.setEnabled(false);
		close.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(shell != null && !shell.isDisposed())
					shell.close();				
				if(ltGray != null && !ltGray.isDisposed())
					ltGray.dispose();
			}			
		});
		
		shell.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				if(ltGray != null && !ltGray.isDisposed())
					ltGray.dispose();				
			}			
		});
		
		
		RCMain.getRCMain().getUpdater().addProgressListener(new UpdateProgressListener() {
			/* (non-Javadoc)
			 * @see lbms.tools.updater.UpdateProgressListener#stateChanged(int)
			 */
			public void stateChanged(int state) {
				setStatusLabel(state);
			}
		});



		//open shell
		GUI_Utilities.centerShellOpenAndFocus(shell);
	}

	/**
	 * Set the status label on the main dialog
	 * @param intStat based on UpdateProgressListener
	 */
	public void setStatusLabel(final int intStat){
		display.syncExec(new Runnable(){
			public void run() {
				if(statusLabel != null && !statusLabel.isDisposed()){
						statusLabel.setText(I18N.translate(PFX + "status." + intStat));
				}
				
				if(intStat == UpdateProgressListener.STATE_FINISHED &&
						close != null && !close.isDisposed())
					close.setEnabled(true);
				
			}
		});
	}

	public static UpdateProgressDialog initialize(Download[] dls) {
		Display dis = RCMain.getRCMain().getDisplay();
		final UpdateProgressDialog upd = new UpdateProgressDialog (dls, dis);
		dis.syncExec(new Runnable() {
			public void run() {
				upd.createContents();
				upd.initDownloads();
			}
		});
		return upd;
	}

	private class DownloadContainer {
		Download dl;
		Composite comp;
		Composite self;


		DownloadContainer (Download d, Composite cmp) {
			dl = d;
			comp = cmp;


			self = new Composite (comp,SWT.NULL);
			GridLayout gl = new GridLayout();
			gl.marginTop = 10;
			self.setLayout(gl);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			self.setLayoutData(gd);
			self.setBackground(ltGray);

			final ProgressBar pb = new ProgressBar(self,SWT.FLAT);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			pb.setLayoutData(gd);
			pb.setBackground(ltGray);

			final Label progressLabel = new Label (self,SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			progressLabel.setLayoutData(gd);
			progressLabel.setBackground(ltGray);


			final Label urlLabel = new Label (self,SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			urlLabel.setLayoutData(gd);
			urlLabel.setBackground(ltGray);

			urlLabel.setText(dl.getSource().toExternalForm());

			comp.layout();
			comp.getParent().layout();

			dl.addDownloadListener(new DownloadListener() {
				long lastBytesRead;
				long lastBytesTotal;
				int lastState;
				/* (non-Javadoc)
				 * @see lbms.tools.DownloadListener#progress(long, long)
				 */
				public void progress(long bytesRead, long bytesTotal) {
					lastBytesRead = bytesRead;
					lastBytesTotal = bytesTotal;

					display.asyncExec(new Runnable(){
						public void run() {
							if(pb != null && !pb.isDisposed()){
								pb.setMaximum((int)lastBytesTotal);
								pb.setSelection((int)lastBytesRead);
							}
						}
					});


					updateLabel();
				}
				/* (non-Javadoc)
				 * @see lbms.tools.DownloadListener#stateChanged(int, int)
				 */
				public void stateChanged(int oldState, int newState) {
					lastState = newState;
					updateLabel();
				}

				public void updateLabel() {
					if(display == null || display.isDisposed()) return;
					display.asyncExec(new Runnable(){
						public void run() {
							if(progressLabel != null && !progressLabel.isDisposed()){
								progressLabel.setText(I18N.translate(PFX + ".dlstate."+lastState)+ " - "
										+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesRead)+" / "
										+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesTotal));

							}
						}
					});
				}//End of UpdateLabel()
			});
		}
	}
}
