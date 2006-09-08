package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;
import lbms.azsmrc.remote.client.util.TimerEventPeriodic;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.Download;
import lbms.tools.DownloadListener;
import lbms.tools.stats.StatsStreamGlobalManager;
import lbms.tools.updater.UpdateProgressListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
	private Label statusLabel, picLabel, speedLabel;
	private Button close, cancel;
	private Color ltGray;
	private Cursor handCursor;
	private TimerEventPeriodic speedUpdateTimer;

	private UpdateProgressDialog (Download[] dls, Display d) {
		display = d;
		downloads = dls;
	}

	private void initDownloads () {
		handCursor = new Cursor(display, SWT.CURSOR_HAND);
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

		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));


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
		gl.verticalSpacing = 10;
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

		speedLabel = new Label(shell,SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		speedLabel.setLayoutData(gd);
		speedLabel.setText(I18N.translate(PFX+"speedPerSec"));

		speedUpdateTimer = RCMain.getRCMain().getMainTimer().addPeriodicEvent(500, new TimerEventPerformer() {
			String label = I18N.translate(PFX+"speedPerSec");
			public void perform(TimerEvent event) {
				display.asyncExec(new SWTSafeRunnable () {
					public void runSafe() {
						if(speedLabel != null && !speedLabel.isDisposed()) {
							speedLabel.setText(label+" "
									+DisplayFormatters.formatByteCountToBase10KBEtcPerSec(
											StatsStreamGlobalManager.getBpsDownload()));

						} else {
							speedUpdateTimer.cancel();
						}
					}
				});
			}
		});

		cancel = new Button(shell, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		cancel.setLayoutData(gd);
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RCMain.getRCMain().getUpdater().abortUpdate();
			}
		});


		close = new Button(shell, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		close.setLayoutData(gd);
		close.setText(I18N.translate("global.close"));
		close.setEnabled(false);
		close.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(shell != null && !shell.isDisposed())
					shell.close();
				if(ltGray != null && !ltGray.isDisposed())
					ltGray.dispose();
				if(handCursor != null && !handCursor.isDisposed())
					handCursor.dispose();
				speedUpdateTimer.cancel();
			}
		});

		shell.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				if(ltGray != null && !ltGray.isDisposed())
					ltGray.dispose();
				if(handCursor != null && !handCursor.isDisposed())
					handCursor.dispose();
			}
		});

		final UpdateProgressListener upl = new UpdateProgressListener() {
			/* (non-Javadoc)
			 * @see lbms.tools.updater.UpdateProgressListener#stateChanged(int)
			 */
			public void stateChanged(int state) {
				setStatusLabel(state);
				if (state == UpdateProgressListener.STATE_FINISHED
					|| state == UpdateProgressListener.STATE_ERROR)
					speedUpdateTimer.cancel();
			}
		};

		RCMain.getRCMain().getUpdater().addProgressListener(upl);

		shell.addShellListener(new ShellAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ShellAdapter#shellClosed(org.eclipse.swt.events.ShellEvent)
			 */
			@Override
			public void shellClosed(ShellEvent e) {
				super.shellClosed(e);
				RCMain.getRCMain().getUpdater().removeProgressListener(upl);
				for (DownloadContainer dc : dcs)
					dc.shellClosed();
				if(ltGray != null && !ltGray.isDisposed())
					ltGray.dispose();
				if(handCursor != null && !handCursor.isDisposed())
					handCursor.dispose();
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
		display.syncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if(statusLabel != null && !statusLabel.isDisposed()){
						statusLabel.setText(I18N.translate(PFX + "status." + intStat));
				}

				if(intStat == UpdateProgressListener.STATE_FINISHED
						|| intStat == UpdateProgressListener.STATE_ABORTED
						|| intStat == UpdateProgressListener.STATE_ERROR)
					if (close != null && !close.isDisposed())
						close.setEnabled(true);

			}
		});
	}

	public static UpdateProgressDialog initialize(Download[] dls) {
		Display dis = RCMain.getRCMain().getDisplay();
		final UpdateProgressDialog upd = new UpdateProgressDialog (dls, dis);
		dis.syncExec(new SWTSafeRunnable() {
			public void runSafe() {
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
		DownloadListener dll;


		DownloadContainer (Download d, Composite cmp) {
			dl = d;
			comp = cmp;


			self = new Composite (comp,SWT.NULL);
			GridLayout gl = new GridLayout(2,false);
			gl.marginTop = 10;
			self.setLayout(gl);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			self.setLayoutData(gd);
			self.setBackground(ltGray);

			final Label nameLabel = new Label (self,SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			nameLabel.setLayoutData(gd);
			nameLabel.setBackground(ltGray);
			nameLabel.setText(dl.getSource().getFile().substring(dl.getSource().getFile().lastIndexOf('/')+1));
			//Set it bold
			Font initialFont = nameLabel.getFont();
			FontData[] fontData = initialFont.getFontData();
			for (int i = 0; i < fontData.length; i++) {
				fontData[i].setStyle(SWT.BOLD);
				fontData[i].setHeight(fontData[i].getHeight() + 2);
			}
			Font newFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
			nameLabel.setFont(newFont);
			newFont.dispose();


			final ProgressBar pb = new ProgressBar(self,SWT.SMOOTH);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			pb.setLayoutData(gd);
			pb.setBackground(ltGray);

			final Label cancelButton = new Label(self,SWT.NONE);
			cancelButton.setImage(ImageRepository.getImage("progress_stop"));
			cancelButton.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent arg0) {
					if (cancelButton.isEnabled()) {
						dl.abortDownload();
						cancelButton.setEnabled(false);
					}
				}
			});
			cancelButton.setCursor(handCursor);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
			cancelButton.setLayoutData(gd);
			cancelButton.setBackground(ltGray);

			final Label progressLabel = new Label (self,SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			progressLabel.setLayoutData(gd);
			progressLabel.setBackground(ltGray);

			final Label urlLabel = new Label (self,SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			urlLabel.setLayoutData(gd);
			urlLabel.setBackground(ltGray);

			urlLabel.setText(dl.getSource().toExternalForm());

			comp.layout();
			comp.getParent().layout();

			dll = new DownloadListener() {
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
					if (newState == Download.STATE_FINISHED
							|| newState == Download.STATE_FAILURE
							|| newState == Download.STATE_ABORTED) {
						if(display == null || display.isDisposed()) return;
						display.asyncExec(new SWTSafeRunnable(){
							public void runSafe() {
								cancelButton.setEnabled(false);
							}
						});
					}
				}

				public void updateLabel() {
					if(display == null || display.isDisposed()) return;
					display.asyncExec(new SWTSafeRunnable(){
						public void runSafe() {
							if(progressLabel != null && !progressLabel.isDisposed()){
								progressLabel.setText(I18N.translate(PFX + "dlstate."+lastState)+ " - "
										+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesRead)+" / "
										+ DisplayFormatters.formatByteCountToBase10KBEtc(lastBytesTotal));
							}
							if(pb != null && !pb.isDisposed()){
								pb.setMaximum((int)lastBytesTotal);
								pb.setSelection((int)lastBytesRead);
							}
						}
					});
				}//End of UpdateLabel()
			};

			dl.addDownloadListener(dll);
		}

		public void shellClosed() {
			if (dl != null)
				dl.removeDownloadListener(dll);
		}
	}
}
