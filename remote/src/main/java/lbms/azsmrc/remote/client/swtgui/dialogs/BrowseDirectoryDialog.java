/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.callbacks.BrowseDirectoryCallback;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.ExtendedProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

/**
 * @author Leonard
 * 
 */
public class BrowseDirectoryDialog {
	// I18N prefix
	public static final String	PFX					= "dialog.browsedirectorydialog.";
	private static Set<String>	recentDirectories	= new HashSet<String>();

	private String[]			directories			= new String[0];
	private boolean				requesting;

	private Shell				shell;
	private Text				selectDirectoryText;
	private Table				directoryList;
	private Label				currentDirectory;
	private Label				errorLabel;
	private ProgressBar			busyBar;

	private ExtendedProperties configProperties;



	private BrowseDirectoryDialog(String startDir, final DirectorySelectedCallback callback,
			Shell parentShell) {

		this.configProperties = RCMain.getRCMain().getProperties();

		loadRecentDirs();


		shell = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM
				| SWT.RESIZE);
		shell.setLayout(new GridLayout(2, false));
		shell.setText(I18N.translate(PFX + "title"));
		shell.setMinimumSize(400, 600);

		Label selectDirLabel = new Label(shell, SWT.None);
		selectDirLabel.setText(I18N.translate(PFX + "selectDirectory"));

		selectDirectoryText = new Text(shell, SWT.BORDER);
		selectDirectoryText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased (KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					browseDirectory(selectDirectoryText.getText());
				}
			}

			@Override
			public void keyPressed (KeyEvent e) {

			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		selectDirectoryText.setLayoutData(gd);

		currentDirectory = new Label(shell, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		currentDirectory.setLayoutData(gd);

		directoryList = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.VIRTUAL);
		directoryList.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent (Event event) {
				TableItem item = (TableItem) event.item;
				item.setText(directories[event.index]);
			}
		});
		directoryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected (SelectionEvent e) {
				if (directoryList.getSelection() != null
						&& directoryList.getSelection().length > 0) {
					browseDirectory(currentDirectory.getText() + "/"
							+ directoryList.getSelection()[0].getText());
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected (SelectionEvent e) {
				if (directoryList.getSelection() != null
						&& directoryList.getSelection().length > 0) {
					selectDirectoryText.setText(currentDirectory.getText()
							+ "/" + directoryList.getSelection()[0].getText());
				}
			}
		});
		directoryList.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed (KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased (KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					if (directoryList.getSelection() != null
							&& directoryList.getSelection().length > 0) {
						browseDirectory(currentDirectory.getText() + "/"
								+ directoryList.getSelection()[0].getText());
					}
				}
			}
		});

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessVerticalSpace = true;
		directoryList.setLayoutData(gd);

		final Combo oldDirs = new Combo(shell, SWT.BORDER);

		for (String recentDir : dirHistorySorted()) {
			oldDirs.add(recentDir);
		}
		if (oldDirs.getItemCount() == 0) {
			oldDirs.setEnabled(false);
		}
		oldDirs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				if (oldDirs.getSelectionIndex() != -1) {
					browseDirectory(oldDirs.getText());
				}
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		oldDirs.setLayoutData(gd);

		busyBar = new ProgressBar(shell, SWT.INDETERMINATE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		busyBar.setLayoutData(gd);
		busyBar.setVisible(false);

		errorLabel = new Label(shell, SWT.None);
		gd = new GridData();
		gd.horizontalSpan = 2;
		errorLabel.setLayoutData(gd);
		errorLabel.setForeground(shell.getDisplay().getSystemColor(
				SWT.COLOR_RED));

		Button okBtn = new Button(shell, SWT.PUSH);
		okBtn.setText(I18N.translate("global.ok"));
		okBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				callback.directorySelected(currentDirectory.getText());
				recentDirectories.add(currentDirectory.getText());

				BrowseDirectoryDialog.recentDirectories.add(BrowseDirectoryDialog.this.currentDirectory.getText());

				BrowseDirectoryDialog.this.configProperties.setProperty("rerecent.remote.dirs", BrowseDirectoryDialog.this.toDelimitedList(BrowseDirectoryDialog.recentDirectories, "|"));

				RCMain.getRCMain().saveConfig();


				shell.close();
			}
		});

		Button cancelBtn = new Button(shell, SWT.PUSH);
		cancelBtn.setText(I18N.translate("global.cancel"));
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				shell.close();
			}
		});

		browseDirectory(emptyToNull(startDir));

		GUI_Utilities.centerShellandOpen(shell);
	}

	private String emptyToNull(String str) {
	   if(str == null || str.trim().isEmpty()) return null;
	   return str;
    }

	private List<String> dirHistorySorted() {
		ArrayList<String> list = new ArrayList<String>(recentDirectories);
		Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        });
		return list;
	}

	private void loadRecentDirs()
	{
		String recentDirsProp = this.configProperties.getProperty("rerecent.remote.dirs");
		if (recentDirsProp != null) {
			recentDirectories.addAll(Arrays.asList(recentDirsProp.split("\\|")));
		}
	}

	private String toDelimitedList(Set<String> collection, String delimiter)
	{
		StringBuilder res = new StringBuilder(100);
		for (String element : collection)
		{
			if (res.length() > 0) {
				res.append(delimiter);
			}
			res.append(element);
		}
		return res.toString();
	}

	private void browseDirectory (String dir) {
		if (requesting) {
			return;
		}
		errorLabel.setText("");
		requesting = true;
		busyBar.setVisible(true);
		RCMain.getRCMain().getClient().sendBrowseDirectory(dir,
				new BrowseDirectoryCallback() {
					@Override
					public void subdirList (final String parentDir,
							final String[] subdirs) {
						Arrays.sort(subdirs);
						directories = new String[subdirs.length + 1];
						directories[0] = "..";
						System.arraycopy(subdirs, 0, directories, 1,
								subdirs.length);
						requesting = false;
						RCMain.getRCMain().getDisplay().asyncExec(
								new SWTSafeRunnable() {
									@Override
									public void runSafe () {
										if (shell == null || shell.isDisposed()) {
											return;
										}
										directoryList.clearAll();
										directoryList
												.setItemCount(directories.length);
										currentDirectory.setText(parentDir);
										selectDirectoryText.setText(parentDir);
										busyBar.setVisible(false);
										shell.layout();
									}
								});
					}

					@Override
					public void errorOccured (final String msg) {
						RCMain.getRCMain().getDisplay().asyncExec(
								new SWTSafeRunnable() {
									@Override
									public void runSafe () {
										if (shell == null || shell.isDisposed()) {
											return;
										}
										errorLabel.setText(msg);
										busyBar.setVisible(false);
										shell.layout();
									}
								});
						requesting = false;
					}

					@Override
					public void requestTimeout () {
						RCMain.getRCMain().getDisplay().asyncExec(
								new SWTSafeRunnable() {
									@Override
									public void runSafe () {
										if (shell == null || shell.isDisposed()) {
											return;
										}
										errorLabel
												.setText("Request Timed out.");
										busyBar.setVisible(false);
										shell.layout();
									}
								});
						requesting = false;
					}
				});
	}

	public static void open (String startDir, DirectorySelectedCallback callback,
			Shell parentShell) {
		new BrowseDirectoryDialog(startDir, callback, parentShell);
	}

	public static interface DirectorySelectedCallback {
		public void directorySelected (String directory);
	}
}
