package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.UnsupportedEncodingException;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.internat.LocaleUtil;
import lbms.azsmrc.remote.client.internat.LocaleUtilDecoderCandidate;
import lbms.azsmrc.remote.client.internat.LocaleUtilEncodingException;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.container.AddTorrentContainer;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class EncodingDialog

{
	// I18N prefix
	private static final String				PFX			= "dialog.encodingdialog.";

	private Shell							encodingShell;

	private LocaleUtil						localeUtil	= LocaleUtil
																.getSingleton();

	private LocaleUtilDecoderCandidate[]	candidates;

	private static EncodingDialog			instance;

	private EncodingDialog(final Display display,
			final AddTorrentContainer container) {
		instance = this;

		// Pull the encoding candidates
		try {
			candidates = localeUtil
					.getTorrentCandidates(container.getTorrent());
		} catch (UnsupportedEncodingException e) {
			MessageBox mb = new MessageBox(display.getActiveShell(), SWT.OK
					| SWT.ICON_ERROR);
			mb.setText(I18N.translate(PFX + "encoding.messagebox.title"));
			mb.setMessage(I18N.translate(PFX + "encoding.messagebox.message"));
			e.printStackTrace();
			return;
		} catch (TOTorrentException e) {
			e.printStackTrace();
		}

		encodingShell = new Shell(display, SWT.RESIZE | SWT.DIALOG_TRIM
				| SWT.PRIMARY_MODAL);

		encodingShell.setText(I18N.translate(PFX + "shell.text"));
		GridData gridData;
		encodingShell.setLayout(new GridLayout(1, true));

		Label label = new Label(encodingShell, SWT.LEFT);
		label.setText(I18N.translate(PFX + "encoding.label.text"));

		final Table table = new Table(encodingShell, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] titlesPieces = { "encoding", "text" };
		for (int i = 0; i < titlesPieces.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(I18N.translate(PFX + "column." + titlesPieces[i]));
		}

		// add candidates to table
		for (int i = 0; i < candidates.length; i++) {
			TableItem item = new TableItem(table, SWT.NULL);
			String name = candidates[i].getDecoder().getName();
			item.setText(0, name);
			item.setText(1, candidates[i].getValue());
		}
		table.select(0);

		// resize all columns to fit the widest entry
		table.getColumn(0).pack();
		table.getColumn(1).pack();

		label = new Label(encodingShell, SWT.LEFT);
		label.setText(I18N.translate(PFX + "label.hint.doubleclick.text"));

		Composite composite = new Composite(encodingShell, SWT.NULL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);

		GridLayout subLayout = new GridLayout();
		subLayout.numColumns = 2;

		composite.setLayout(subLayout);

		final Button ok = new Button(composite, SWT.PUSH);
		ok.setText(I18N.translate("global.ok"));
		gridData = new GridData(GridData.END);
		gridData.widthHint = 100;
		ok.setLayoutData(gridData);

		encodingShell.setSize(500, 500);
		encodingShell.layout();

		GUI_Utilities.centerShellOpenAndFocus(encodingShell);

		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int selectedIndex = table.getSelectionIndex();

				if (-1 == selectedIndex) {
					return;
				}
				String encodingChoice = candidates[selectedIndex].getDecoder()
						.getName();
				try {
					localeUtil.setTorrentEncoding(container.getTorrent(),
							encodingChoice);
				} catch (LocaleUtilEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				encodingShell.dispose();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent mEvent) {
				int selectedIndex = table.getSelectionIndex();

				if (-1 == selectedIndex) {
					return;
				}
				String encodingChoice = candidates[selectedIndex].getDecoder()
						.getName();
				try {
					localeUtil.setTorrentEncoding(container.getTorrent(),
							encodingChoice);
				} catch (LocaleUtilEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				encodingShell.dispose();
			}
		});

		GUI_Utilities.centerShellOpenAndFocus(encodingShell);
	}

	/**
	 * Static method to open the update dialog
	 * 
	 * @param parent
	 * @param update
	 * @param updater
	 */
	public static void open(final Display display,
			final AddTorrentContainer container) {
		if (display == null) {
			return;
		}
		if (instance == null || instance.encodingShell == null
				|| instance.encodingShell.isDisposed()) {
			new EncodingDialog(display, container);
		} else {
			instance.encodingShell.setActive();
		}

	}

}
