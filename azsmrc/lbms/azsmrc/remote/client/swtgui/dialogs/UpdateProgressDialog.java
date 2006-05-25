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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
	private Composite container, parent;
	private Label statusLabel, picLabel;

	private UpdateProgressDialog (Download[] dls, Display d) {
		display = d;
		downloads = dls;
	}

	private void initDownloads () {
		for (Download d:downloads) {
			dcs.add(new DownloadContainer(d, container));
		}
	}

	private void createContents() {
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		picLabel = new Label(shell,SWT.NULL);
		picLabel.setImage(display.getSystemImage(SWT.ICON_INFORMATION));

		statusLabel = new Label(shell,SWT.NULL);
		setStatusLabel(0);


		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 300;
		gd.heightHint = 200;
		gd.horizontalSpan = 2;
		sc.setLayoutData(gd);

		parent = new Composite(sc, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gd);
		parent.setLayout(new GridLayout(1,false));
		parent.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		sc.setContent(parent);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(parent.computeSize(r.width, SWT.DEFAULT));
			}
		});



		//open shell
		GUI_Utilities.centerShellOpenAndFocus(shell);
	}

	/**
	 * Set the status label on the main dialog
	 * 0 for installing
	 * 1 for finished
	 * 2 for error
	 * @param intStat
	 */
	public void setStatusLabel(final int intStat){
		display.asyncExec(new Runnable(){

			public void run() {
				if(statusLabel != null && !statusLabel.isDisposed()){
					switch(intStat){
					case 0:
						statusLabel.setText(I18N.translate(PFX + "status.0"));
						break;
					case 1:
						statusLabel.setText(I18N.translate(PFX + "status.1"));
						break;
					case 2:
						statusLabel.setText(I18N.translate(PFX + "status.2"));
					}
				}

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
			self = new Composite (comp,SWT.NONE);
			GridLayout gl = new GridLayout();
			self.setLayout(gl);
			gl.numColumns = 1;


			final ProgressBar pb = new ProgressBar(self,SWT.INDETERMINATE);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
			pb.setLayoutData(gd);

			final Label progressLabel = new Label (self,SWT.NONE);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
			progressLabel.setLayoutData(gd);

			final Label urlLabel = new Label (self,SWT.NONE);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
			urlLabel.setLayoutData(gd);

			urlLabel.setText(dl.getSource().toExternalForm());

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
					progressLabel.setText(I18N.translate(PFX + ".dlstate."+lastState)+ " - "
							+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesRead)+" / "
							+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesTotal));
				}
			});
		}
	}
}
