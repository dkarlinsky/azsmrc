/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.tools.Download;
import lbms.tools.DownloadListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * @author Damokles
 *
 */
public class UpdateProgressDialog {
	public static final String PFX = "dialog.updateprogressdialog.";
	private Display display;
	private Download[] downloads;
	private List<DownloadContainer> dcs = new ArrayList<DownloadContainer>();
	private Composite container;

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
		//TODO omshaub
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
