/*
 * Created on Jan 27, 2006 Created by omschaub
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.DownloadManagerShell;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.URLTransfer;
import lbms.azsmrc.remote.client.swtgui.container.AddTorrentContainer;
import lbms.azsmrc.remote.client.swtgui.dialogs.BrowseDirectoryDialog.DirectorySelectedCallback;
import lbms.azsmrc.remote.client.torrent.TOTorrentAnnounceURLGroup;
import lbms.azsmrc.remote.client.torrent.TOTorrentAnnounceURLSet;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;
import lbms.azsmrc.remote.client.torrent.TOTorrentFile;
import lbms.azsmrc.remote.client.torrent.scraper.ScrapeListener;
import lbms.azsmrc.remote.client.torrent.scraper.ScrapeResult;
import lbms.azsmrc.remote.client.torrent.scraper.Scraper;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.DownloadListener;
import lbms.tools.TorrentDownload;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OpenByFileDialog {

	private Table								filesTable, detailsTable;

	private CTabFolder							tabFolder;

	Button										deleteOnSend;

	private String								lastDir;

	private Label								totalSLabel, totalS;

	private Label								saveDir, saveDirSize, destDir,
			destDirSize;

	private Text								saveTo;

	private Shell								shell;

	private Map<String, AddTorrentContainer>	tMap		= new HashMap<String, AddTorrentContainer>();
	private Map<String, String>					driveMap	= new HashMap<String, String>();

	private AddTorrentContainer					activeATC;

	private static OpenByFileDialog				instance;

	private static Logger						logger		= Logger
																	.getLogger(OpenByFileDialog.class);

	// private int drag_drop_line_start = -1;

	// I18N prefix
	public static final String					PFX			= "dialog.openbyfiledialog.";

	private OpenByFileDialog(Display display) {
		// set the static instance
		instance = this;

		// pull last dir if available
		lastDir = RCMain.getRCMain().getProperties().getProperty(
				"Last.Directory");

		// Shell
		shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText(I18N.translate(PFX + "shell.text"));
		if (!lbms.azsmrc.remote.client.Utilities.isOSX) {
			shell.setImage(ImageRepository.getImage("open_by_file"));
		}

		tabFolder = new CTabFolder(shell, SWT.FLAT);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gd);
		tabFolder.setLayout(new GridLayout(1, false));
		tabFolder.setSimple(false);

		final CTabItem mainTab = new CTabItem(tabFolder, SWT.NULL);
		mainTab.setText(I18N.translate(PFX + "maintab.title"));

		// Comp on shell
		final Group comp = new Group(tabFolder, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);

		// first line

		Composite buttonComp = new Composite(comp, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		buttonComp.setLayout(gl);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		buttonComp.setLayoutData(gridData);

		Button open_file_button = new Button(buttonComp, SWT.PUSH);
		open_file_button.setToolTipText(I18N
				.translate(PFX + "openfile.tooltip"));
		open_file_button.setText(I18N.translate(PFX + "openfile.text"));
		open_file_button.setImage(ImageRepository.getImage("open_by_file"));
		open_file_button.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
                openFileDialog();
			}
		});

		Button remove = new Button(buttonComp, SWT.PUSH);
		remove.setText(I18N.translate(PFX + "remove.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessHorizontalSpace = false;
		remove.setLayoutData(gridData);
		remove.setImage(ImageRepository.getImage("toolbar_remove"));
		remove.addSelectionListener(new SelectionListener() {

			public void widgetSelected (SelectionEvent arg0) {
				int[] items = filesTable.getSelectionIndices();
				if (items.length == 0) {
					return;
				}
				for (int index : items) {
					TableItem item = filesTable.getItem(index);
					if (tMap.containsKey(item.getText(0))) {
						tMap.remove(item.getText(0));
						CTabItem[] tabItems = tabFolder.getItems();
						for (CTabItem tabItem : tabItems) {
							if (tabItem.getText().equals(item.getText(0))) {
								tabItem.dispose();
							}

						}
					}
				}

				filesTable.remove(items);
				filesTable.deselectAll();
				detailsTable.removeAll();
				setTotalSize();
			}

			public void widgetDefaultSelected (SelectionEvent arg0) {
			}

		});

		Button scrapeSelected = new Button(buttonComp, SWT.PUSH);
		scrapeSelected.setText(I18N.translate(PFX
				+ "scrapeSelected.button.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessHorizontalSpace = true;
		scrapeSelected.setLayoutData(gridData);
		scrapeSelected.setImage(ImageRepository.getImage("scrape"));
		scrapeSelected.addSelectionListener(new SelectionListener() {

			public void widgetSelected (SelectionEvent arg0) {
				int[] items = filesTable.getSelectionIndices();
				if (items.length == 0) {
					return;
				}
				for (int index : items) {
					TableItem item = filesTable.getItem(index);
					if (tMap.containsKey(item.getText(0))) {
						torrentTabOpen(tabFolder, tMap.get(item.getText(0)));
					}
				}
			}

			public void widgetDefaultSelected (SelectionEvent arg0) {
			}

		});

		// -------Server Free Disk stuff -----------\\
		final Group details3 = new Group(comp, SWT.NULL);
		details3.setText(I18N.translate(PFX + "serverdrive.group.text"));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.horizontalSpacing = 30;
		details3.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		details3.setLayoutData(gridData);

		// save.dir is the default save dir
		Label saveDirL = new Label(details3, SWT.NULL);
		saveDirL.setText(I18N
				.translate(PFX + "serverdrive.defaultsavedir.text"));

		saveDir = new Label(details3, SWT.NULL);
		saveDir.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		saveDirSize = new Label(details3, SWT.NULL);
		saveDirSize.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		// destination.dir is the user dir
		Label destDirL = new Label(details3, SWT.NULL);
		destDirL.setText(I18N
				.translate(PFX + "serverdrive.destinationdir.text"));

		destDir = new Label(details3, SWT.NULL);
		destDir.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		destDirSize = new Label(details3, SWT.NULL);
		destDirSize.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		// CUL
		final ClientUpdateListener serverDetails = new ClientUpdateListener() {

			public void update (long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_DRIVE_INFO) != 0) {
					RCMain.getRCMain().getDisplay().asyncExec(
							new SWTSafeRunnable() {
								@Override
								public void runSafe () {
									try {
										driveMap = RCMain.getRCMain()
												.getClient().getRemoteInfo()
												.getDriveInfo();

										if (driveMap.containsKey("save.dir")
												&& driveMap
														.containsKey("save.dir.path")) {
											saveDir.setText(driveMap
													.get("save.dir.path"));
											saveDirSize
													.setText(DisplayFormatters
															.formatKBCountToBase10KBEtc(Long
																	.parseLong(driveMap
																			.get("save.dir")))
															+ " Free");
										}

										if (driveMap
												.containsKey("destination.dir")
												&& driveMap
														.containsKey("destination.dir.path")) {
											destDir
													.setText(driveMap
															.get("destination.dir.path"));
											destDirSize
													.setText(DisplayFormatters
															.formatKBCountToBase10KBEtc(Long
																	.parseLong(driveMap
																			.get("destination.dir")))
															+ " Free");
										}

										// redraw the group
										details3.layout();
										comp.layout();
									} catch (SWTException e) {
										// do nothing as the tab was probably
										// disposed
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});

				}

			}
		};

		RCMain.getRCMain().getClient().addClientUpdateListener(serverDetails);

		if (RCMain.getRCMain().connected()) {
			RCMain.getRCMain().getClient().getRemoteInfo().refreshDriveInfo();
		}

		// list to shell for close so that we can remove cul
		shell.addShellListener(new ShellListener() {

			public void shellActivated (ShellEvent arg0) {
			}

			public void shellClosed (ShellEvent arg0) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(
						serverDetails);
			}

			public void shellDeactivated (ShellEvent arg0) {
			}

			public void shellDeiconified (ShellEvent arg0) {
			}

			public void shellIconified (ShellEvent arg0) {
			}
		});

		// --------------------------Total Size ------------------------\\

		totalSLabel = new Label(comp, SWT.NULL);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		totalSLabel.setLayoutData(gridData);

		totalS = new Label(comp, SWT.NULL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		totalS.setLayoutData(gridData);

		setTotalSize();

		// -------------------------- SASH------------------------------\\

		SashForm sash = new SashForm(comp, SWT.VERTICAL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginTop = 0;
		gridLayout.marginWidth = 0;
		sash.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 70;
		gridData.grabExcessVerticalSpace = true;
		sash.setLayoutData(gridData);

		filesTable = new Table(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.FULL_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 30;
		gridData.grabExcessVerticalSpace = true;
		filesTable.setLayoutData(gridData);
		filesTable.setHeaderVisible(true);

		TableColumn name = new TableColumn(filesTable, SWT.NULL);
		name.setText(I18N.translate(PFX + "filesTable.column.name"));
		name.setWidth(200);

		TableColumn path = new TableColumn(filesTable, SWT.NULL);
		path.setText(I18N.translate(PFX + "filesTable.column.localpath"));
		path.setWidth(350);

		createDragDrop(filesTable);

		filesTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected (SelectionEvent arg0) {
				TableItem item = (TableItem) arg0.item;
				generateDetails(item.getText(0));
			}

			public void widgetDefaultSelected (SelectionEvent arg0) {
			}

		});

		filesTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent (Event arg0) {
				TableItem[] items = filesTable.getSelection();
				if (items.length != 1) {
					return;
				}
				CTabItem[] tabs = tabFolder.getItems();
				for (CTabItem item : tabs) {
					if (item.getText().equalsIgnoreCase(items[0].getText(0))) {
						tabFolder.setSelection(item);
						return;
					}
				}
				torrentTabOpen(tabFolder, tMap.get(items[0].getText(0)));
			}
		});

		Group detailsGroup = new Group(sash, SWT.NULL);
		detailsGroup.setText(I18N.translate(PFX + "torrentdetail.group.text"));
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		detailsGroup.setLayout(gl);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 50;
		gridData.grabExcessVerticalSpace = true;
		detailsGroup.setLayoutData(gridData);

		Label saveToLabel = new Label(detailsGroup, SWT.NULL);
		saveToLabel.setText(I18N.translate(PFX
				+ "torrentdetail.saveToLabel.text"));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		saveToLabel.setLayoutData(gridData);

		saveTo = new Text(detailsGroup, SWT.BORDER | SWT.SINGLE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		saveTo.setLayoutData(gridData);
		saveTo.addModifyListener(new ModifyListener() {
			public void modifyText (ModifyEvent event) {
				if (activeATC != null) {
					activeATC.setSaveToDirectory(saveTo.getText());
				}
			}
		});

		Button browseButton = new Button(detailsGroup, SWT.PUSH);
		browseButton.setText(I18N.translate("global.browse"));
		browseButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
			 * .swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected (SelectionEvent e) {
				BrowseDirectoryDialog.open(new DirectorySelectedCallback() {
					@Override
					public void directorySelected (final String directory) {
						shell.getDisplay().asyncExec(new SWTSafeRunnable() {
							@Override
							public void runSafe () {
								saveTo.setText(directory);
							}
						});
					}
				}, shell);
			}
		});

		// detailsTAble for each torrent
		detailsTable = new Table(detailsGroup, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.CHECK | SWT.MULTI);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 20;
		gridData.grabExcessVerticalSpace = true;
		detailsTable.setLayoutData(gridData);
		detailsTable.setHeaderVisible(true);

		TableColumn dCheck = new TableColumn(detailsTable, SWT.CENTER);
		dCheck.setWidth(30);

		TableColumn dName = new TableColumn(detailsTable, SWT.LEFT);
		dName.setText(I18N.translate(PFX + "torrentdetail.table.column.name"));
		dName.setWidth(350);

		TableColumn dSize = new TableColumn(detailsTable, SWT.RIGHT);
		dSize.setText(I18N.translate(PFX + "torrentdetail.table.column.size"));
		dSize.setWidth(80);

		detailsTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				if (event.detail == SWT.CHECK) {
					TableItem item = (TableItem) event.item;
					int place = detailsTable.indexOf(item);
					AddTorrentContainer container = (AddTorrentContainer) filesTable
							.getSelection()[0].getData();
					if (item.getChecked()) {
						container.setFileProperty(place, 1);
					} else {
						container.setFileProperty(place, 0);
					}
					setTotalSize();
				}

			}
		});

		// menu for details Table
		final Menu menu = new Menu(detailsTable);

		final MenuItem selectAll = new MenuItem(menu, SWT.PUSH);
		selectAll.setText(I18N.translate(PFX + "detailsTable.menu.selectAll"));
		selectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (activeATC == null) {
					return;
				}
				TOTorrentFile[] files = activeATC.getFiles();
				TableItem[] detailItem = detailsTable.getItems();
				for (int i = 0; i < files.length; i++) {
					activeATC.setFileProperty(i, 1);
					detailItem[i].setChecked(true);
				}

				setTotalSize();
			}
		});

		final MenuItem unselectAll = new MenuItem(menu, SWT.PUSH);
		unselectAll.setText(I18N.translate(PFX
				+ "detailsTable.menu.unselectAll"));
		unselectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (activeATC == null) {
					return;
				}
				TOTorrentFile[] files = activeATC.getFiles();
				TableItem[] detailItem = detailsTable.getItems();
				for (int i = 0; i < files.length; i++) {
					activeATC.setFileProperty(i, 0);
					detailItem[i].setChecked(false);
				}

				setTotalSize();
			}
		});

		final MenuItem selectInverse = new MenuItem(menu, SWT.PUSH);
		selectInverse.setText(I18N.translate(PFX
				+ "detailsTable.menu.selectInverse"));
		selectInverse.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				if (activeATC == null) {
					return;
				}
				TOTorrentFile[] files = activeATC.getFiles();
				TableItem[] detailItem = detailsTable.getItems();
				int[] properties = activeATC.getFileProperties();
				for (int i = 0; i < files.length; i++) {
					if (properties[i] == 1) {
						activeATC.setFileProperty(i, 0);
						detailItem[i].setChecked(false);
					} else {
						activeATC.setFileProperty(i, 1);
						detailItem[i].setChecked(true);
					}
				}

				setTotalSize();
			}
		});

		menu.addMenuListener(new MenuListener() {

			public void menuHidden (MenuEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void menuShown (MenuEvent arg0) {
				boolean selection;
				if (detailsTable.getItemCount() == 0) {
					selection = false;
				} else {
					selection = true;
				}

				selectAll.setEnabled(selection);
				unselectAll.setEnabled(selection);
				selectInverse.setEnabled(selection);

			}

		});

		detailsTable.setMenu(menu);

		// Buttons
		Composite button_comp = new Composite(comp, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

		deleteOnSend = new Button(button_comp, SWT.CHECK);
		deleteOnSend.setText(I18N.translate(PFX + "deleteonsend.text"));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		deleteOnSend.setLayoutData(gridData);
		deleteOnSend.setSelection(Boolean.parseBoolean(RCMain.getRCMain()
                .getProperties().getProperty("delete.on.send", "false")));
		deleteOnSend.addListener(SWT.Selection, new Listener() {

			public void handleEvent (Event arg0) {
				if (deleteOnSend.getSelection()) {
					RCMain.getRCMain().getProperties().setProperty(
                            "delete.on.send", "true");
				} else {
					RCMain.getRCMain().getProperties().setProperty(
                            "delete.on.send", "false");
				}
				// Store the new setting
				RCMain.getRCMain().saveConfig();
			}

		});

		Button sendTorrents = new Button(button_comp, SWT.PUSH);
		sendTorrents.setText(I18N.translate(PFX + "sendfiles.text"));
		sendTorrents.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (tMap.size() == 0) {
                    MessageBox messageBox = new MessageBox(shell,
                            SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText(I18N.translate("global.error"));
                    messageBox.setMessage(I18N.translate(PFX
                            + "sendfiles.error1"));
                    messageBox.open();
                    return;
                } else {
                    RCMain.getRCMain().getClient().transactionStart();
                    for (AddTorrentContainer container : tMap.values()) {

                        // Check to see if the whole file is sent and if so,
                        // just add it normally
                        // else send it with the properties int[]
                        if (container.isWholeFileSent()) {
                            if (container.getSaveToDirectory()
                                    .equalsIgnoreCase("")) {
                                RCMain.getRCMain().getClient()
                                        .getDownloadManager().addDownload(
                                        container.getTorrent());
                            } else {
                                RCMain.getRCMain().getClient()
                                        .getDownloadManager().addDownload(
                                        container.getTorrent(),
                                        container.getSaveToDirectory());
                            }
                        } else {
                            int[] props = container.getFileProperties();
                            // Main add to Azureus
                            if (container.getSaveToDirectory()
                                    .equalsIgnoreCase("")) {
                                RCMain.getRCMain().getClient()
                                        .getDownloadManager().addDownload(
                                        container.getTorrent(), props);
                            } else {
                                RCMain.getRCMain().getClient()
                                        .getDownloadManager().addDownload(
                                        container.getTorrent(), props,
                                        container.getSaveToDirectory());
                            }
                        }

                        if (Boolean.parseBoolean(RCMain.getRCMain()
                                .getProperties().getProperty("delete.on.send",
                                        "false"))) {
                            if (!container.deleteFile()) {
                                MessageBox messageBox = new MessageBox(shell,
                                        SWT.ICON_ERROR | SWT.OK);
                                messageBox.setText(I18N
                                        .translate("global.error"));
                                messageBox.setMessage(I18N.translate(PFX
                                        + "sendfiles.error2")
                                        + " "
                                        + container.getTorrentFile().getName());
                                messageBox.open();
                            }
                        }
                    }
                    RCMain.getRCMain().getClient().transactionCommit();
                }
                shell.close();
            }
        });

		Button cancel = new Button(button_comp, SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		cancel.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                shell.close();
            }
        });

		mainTab.setControl(comp);

		// Center Shell and open
		GUI_Utilities.centerShellandOpen(shell);

        openFileDialog();

	}

    private void openFileDialog() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.torrent", "*.*" });
        dialog
                .setText(I18N.translate(PFX
                        + "openfile.filedialog.text"));
        if (lastDir != null) {
            dialog.setFilterPath(lastDir);
        }
        String choosen_file = dialog.open();

        if (choosen_file != null) {
            try {
                File test = new File(choosen_file);
                if (test.isFile() && test.canRead()) {
                    AddTorrentContainer container = new AddTorrentContainer(
                            test);

                    // check the encoding of the file
                    if (container.getTorrent().getAdditionalProperty(
                            "encoding") == null) {
                        EncodingDialog.open(RCMain.getRCMain()
                                .getDisplay(), container);

                    }

                    // Check to see if it is already there, and if so,
                    // alert the user and cancel
                    if (tMap.containsKey(container.getName())) {
                        MessageBox messageBox = new MessageBox(shell,
                                SWT.ICON_INFORMATION | SWT.OK);
                        messageBox
                                .setText(I18N
                                        .translate(PFX
                                                + "openfile.filedialog.duplicate.title"));
                        messageBox
                                .setMessage(I18N
                                        .translate(PFX
                                                + "openfile.filedialog.duplicate.message"));
                        messageBox.open();
                        return;
                    }

                    TableItem item = new TableItem(filesTable, SWT.NULL);

                    item.setText(0, container.getName());
                    item.setText(1, container.getFilePath());

                    tMap.put(container.getName(), container);
                    setTotalSize();
                    item.setData(container);
                    generateDetails(container.getName());
                    lastDir = container.getFilePath();
                    RCMain.getRCMain().getProperties().setProperty(
                            "Last.Directory", lastDir);
                    RCMain.getRCMain().saveConfig();

                    // select the item in the table
                    try {
                        filesTable.setSelection(filesTable
                                .indexOf(item));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        filesTable.setSelection(0);
                    }

                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                MessageBox messageBox = new MessageBox(shell,
                        SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(I18N.translate("global.error"));
                messageBox.setMessage(I18N.translate(PFX
                        + "openfile.error1"));
                messageBox.open();
            } catch (TOTorrentException e1) {
                e1.printStackTrace();
                MessageBox messageBox = new MessageBox(shell,
                        SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(I18N.translate("global.error"));
                messageBox.setMessage(I18N.translate(PFX
                        + "openfile.error2"));
                messageBox.open();
            } catch (Exception e1) {
                e1.printStackTrace();
                MessageBox messageBox = new MessageBox(shell,
                        SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(I18N.translate("global.error"));
                messageBox.setMessage(I18N.translate(PFX
                        + "openfile.error3"));
                messageBox.open();
                e1.printStackTrace();
            }
        }
    }

    /**
	 * public open method without a string
	 * 
	 */
	public static void open () {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) {
			return;
		}
		if (instance == null || instance.shell == null
				|| instance.shell.isDisposed()) {
			new OpenByFileDialog(display);
		} else {
			instance.shell.setActive();
		}
	}

	/**
	 * Static open with fileNames
	 * 
	 * @param display
	 * @param fileNames
	 */
	public static void open (final String[] fileNames) {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) {
			return;
		}
		display.syncExec(new SWTSafeRunnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see lbms.azsmrc.shared.SWTSafeRunnable#runSafe()
			 */
			@Override
			public void runSafe () {
				if (instance == null || instance.shell == null
						|| instance.shell.isDisposed()) {
					new OpenByFileDialog(display);
					instance.addFileToInstance(fileNames);
				} else {
					instance.shell.setActive();
					instance.addFileToInstance(fileNames);
				}
			}
		});

	}

	/*
	 * Add files to the instance as a string[]
	 */
	private void addFileToInstance (final String[] fileNames) {
		// See if we come in with a file already
		if (fileNames != null) {
			try {
				for (String filename : fileNames) {
					File test = new File(filename);
					if (test.isFile() && test.canRead()) {
						AddTorrentContainer container = new AddTorrentContainer(
								test);
						TableItem item = new TableItem(filesTable, SWT.NULL);
						item.setText(0, container.getName());
						item.setText(1, container.getFilePath());
						item.setData(container);
						tMap.put(container.getName(), container);
						filesTable.setSelection(item);
						generateDetails(container.getName());
						lastDir = container.getFilePath();
						setTotalSize();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Add files to the instance as a string[]
	 */
	private void addFileToInstance (final File[] files) {
		// See if we come in with a file already
		if (files != null) {
			try {
				for (File file : files) {
					if (file.isFile() && file.canRead()) {
						AddTorrentContainer container = new AddTorrentContainer(
								file);
						TableItem item = new TableItem(filesTable, SWT.NULL);
						item.setText(0, container.getName());
						item.setText(1, container.getFilePath());
						item.setData(container);
						tMap.put(container.getName(), container);
						filesTable.setSelection(item);
						generateDetails(container.getName());
						lastDir = container.getFilePath();
						setTotalSize();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createDragDrop (final Table parent) {
		try {

			DropTarget dropTarget = new DropTarget(parent, DND.DROP_DEFAULT
					| DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK
					| DND.DROP_TARGET_MOVE);

			dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance(),
					TextTransfer.getInstance() });

			dropTarget.addDropListener(new DropTargetAdapter() {
				@Override
				public void dropAccept (DropTargetEvent event) {
					event.currentDataType = URLTransfer.pickBestType(
							event.dataTypes, event.currentDataType);
				}

				@Override
				public void dragEnter (DropTargetEvent event) {
					// no event.data on dragOver, use drag_drop_line_start to
					// determine if
					// ours
					if (FileTransfer.getInstance().isSupportedType(
							event.currentDataType)) {
						event.detail = DND.DROP_COPY;
					} else if (TextTransfer.getInstance().isSupportedType(
							event.currentDataType)) {
						event.feedback = DND.FEEDBACK_EXPAND
								| DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT
								| DND.FEEDBACK_INSERT_BEFORE
								| DND.FEEDBACK_INSERT_AFTER;
						event.detail = event.item == null ? DND.DROP_NONE
								: DND.DROP_MOVE;
					}
				}

				@Override
				public void drop (DropTargetEvent event) {
					if (!(event.data instanceof String)
							|| !((String) event.data).equals("moveRow")) {
						openDroppedTorrents(event);
						return;
					}

				}
			});

		} catch (Throwable t) {
			logger.error("failed to init drag-n-drop + \n" + t);
		}
	}

	public void openDroppedTorrents (DropTargetEvent event) {
		if (event.data == null) {
			return;
		}

		// boolean bOverrideToStopped = event.detail == DND.DROP_COPY;

		if (event.data instanceof String[] || event.data instanceof String) {
			final String[] sourceNames = (event.data instanceof String[]) ? (String[]) event.data
					: new String[] { (String) event.data };
			if (sourceNames == null) {
				event.detail = DND.DROP_NONE;
			}
			if (event.detail == DND.DROP_NONE) {
				return;
			}

			for (int i = 0; (i < sourceNames.length); i++) {
				final File source = new File(sourceNames[i]);
				if (source.isFile()) {
					String filename = source.getAbsolutePath();
					try {
						if (!DownloadManagerShell.isTorrentFile(filename)) {
							logger
									.info("openDroppedTorrents: file not a torrent file");

						} else {
							// System.out.println("Dropped file IS torrent -- to
							// open: " + filename);
							try {

								File test = new File(filename);
								if (test.isFile() && test.canRead()) {
									AddTorrentContainer container = new AddTorrentContainer(
											test);
									TableItem item = new TableItem(filesTable,
											SWT.NULL);

									item.setText(0, container.getName());
									item.setText(1, container.getFilePath());
									item.setData(container);

									tMap.put(container.getName(), container);
									filesTable.setSelection(item);
									generateDetails(container.getName());
									lastDir = container.getFilePath();
									setTotalSize();
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						logger.info("Torrent open fails for '" + filename
								+ "'\n" + e.toString());
					}
				} else if (source.isDirectory()) {
					MessageBox messageBox = new MessageBox(RCMain.getRCMain()
							.getMainWindow().getShell(), SWT.ICON_ERROR
							| SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX
							+ "clipboard.error1"));
					messageBox.open();
					return;
				}
			}
		}
	}

	public void generateDetails (String tName) {
		if (tMap.containsKey(tName)) {
			detailsTable.removeAll();
			activeATC = tMap.get(tName);
			TOTorrentFile[] files = activeATC.getFiles();
			int[] properties = activeATC.getFileProperties();
			for (int i = 0; i < files.length; i++) {
				final TableItem detailItem = new TableItem(detailsTable,
						SWT.NULL);
				String name = files[i].getRelativePath();

				if (name == null || name.length() == 0
						|| name.equalsIgnoreCase("")) {
					name = "Error Decoding Name";
				}

				if (properties != null && properties[i] == 1) {
					detailItem.setChecked(true);
				}
				detailItem.setText(1, name);
				detailItem.setText(2, DisplayFormatters
						.formatByteCountToBase10KBEtc(files[i].getLength()));
				// Shade every other one
				if (filesTable.indexOf(detailItem) % 2 != 0) {
					detailItem.setBackground(ColorUtilities
							.getBackgroundColor());
				}
			}

			// add in the custom directory if there is one
			if (!activeATC.getSaveToDirectory().equalsIgnoreCase("")) {
				saveTo.setText(activeATC.getSaveToDirectory());
			} else if (driveMap.containsKey("save.dir.path")) {
				saveTo.setText(driveMap.get("save.dir.path"));
			} else {
				saveTo.setText("");
			}

		} else {
			System.out.println("Error... " + tName + " not in map!");
		}
	}

	public void setTotalSize () {
		long totalSize = 0;
		Iterator<String> it = tMap.keySet().iterator();
		while (it.hasNext()) {
			AddTorrentContainer atc = tMap.get(it.next());
			if (atc != null) {
				totalSize += atc.getTotalSizeOfDownloads();
			}
		}
		long totalSizeAdj = totalSize / 1024l;
		if (driveMap.containsKey("save.dir")
				&& driveMap.containsKey("save.dir.path")) {
			long saveDirFree = Long.parseLong(driveMap.get("save.dir"));
			// System.out.println(saveDirFree + " | " + (1024l*1024l*2l) + " | "
			// + totalSizeAdj + " | " + (saveDirFree - totalSizeAdj));
			if ((saveDirFree - totalSizeAdj) > (1024l * 1024l * 2l/* 2 GB */)) {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_GREEN));
			} else if ((saveDirFree - totalSizeAdj) > (1024l * 20l /* 20 MB */)) {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_YELLOW));
			} else {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_RED));
			}
			saveDirSize.setToolTipText(I18N.translate(PFX
					+ "serverdrive.defaultsavedir.tooltip")
					+ DisplayFormatters.formatKBCountToBase10KBEtc(saveDirFree
							- totalSizeAdj));

		} else {
			totalS.setForeground(RCMain.getRCMain().getDisplay()
					.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}

		if (driveMap.containsKey("destination.dir")
				&& driveMap.containsKey("destination.dir.path")) {
			long destDirFree = Long.parseLong(driveMap.get("destination.dir"));
			if ((destDirFree - totalSizeAdj) > (1024l * 1024l * 2l/* 2 GB */)) {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_GREEN));
			} else if ((destDirFree - totalSizeAdj) > (1024l * 20l /* 20 MB */)) {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_YELLOW));
			} else {
				totalS.setForeground(RCMain.getRCMain().getDisplay()
						.getSystemColor(SWT.COLOR_DARK_RED));
			}
			destDirSize.setToolTipText(I18N.translate(PFX
					+ "serverdrive.destinationdir.tooltip")
					+ DisplayFormatters.formatKBCountToBase10KBEtc(destDirFree
							- totalSizeAdj));
		} else {
			totalS.setForeground(RCMain.getRCMain().getDisplay()
					.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}
		if (totalSizeAdj == 0) {
			totalS.setForeground(RCMain.getRCMain().getDisplay()
					.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}
		totalSLabel.setText(I18N.translate(PFX + "totalsize.text") + " ");
		totalS.setText(DisplayFormatters
				.formatByteCountToBase10KBEtc(totalSize));
	}

	/**
	 * The main torrent details tab
	 * 
	 * @param tabFolder
	 * @param atc
	 */
	private void torrentTabOpen (CTabFolder tabFolder,
			final AddTorrentContainer atc) {
		// pull previous SR if available
		ScrapeResult sr = atc.getScrapeResults();

		CTabItem tab = new CTabItem(tabFolder, SWT.CLOSE);
		final Scraper scraper = new Scraper(atc.getTorrent());

		try {
			tab.setText(atc.getName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		final Composite parent = new Composite(tabFolder, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		parent.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		parent.setLayout(gl);

		Composite comboComp = new Composite(parent, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		comboComp.setLayoutData(gd);

		gl = new GridLayout();
		gl.marginWidth = 0;
		gl.numColumns = 3;
		comboComp.setLayout(gl);

		final Combo combo = new Combo(comboComp, SWT.DROP_DOWN | SWT.READ_ONLY);

		// Pull the URL from the torrent and put it in the combo
		combo.add(atc.getTorrent().getAnnounceURL().toString());
		combo.select(0);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		combo.setLayoutData(gd);

		// Pull the group from the torrent
		TOTorrentAnnounceURLGroup torrentGroup = atc.getTorrent()
				.getAnnounceURLGroup();

		// Check the length to see if a group is actually present
		if (torrentGroup.getAnnounceURLSets().length > 0) {
			// group is present, now pull the set
			TOTorrentAnnounceURLSet[] urlSets = torrentGroup
					.getAnnounceURLSets();
			// crawl through them and pull the titles for the table
			for (TOTorrentAnnounceURLSet urlSet : urlSets) {
				URI[] urls = urlSet.getAnnounceURLs();
				for (URI url : urls) {
					if (!url.toString().equalsIgnoreCase(
							atc.getTorrent().getAnnounceURL().toString())) {
						combo.add(url.toString());
					}
				}
			}
		}

		// button for Scrape -- still in comboComp
		Button scrape = new Button(comboComp, SWT.PUSH);
		scrape.setText(I18N.translate(PFX + "scrape_button.text"));
		scrape.setImage(ImageRepository.getImage("scrape"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		scrape.setLayoutData(gd);

		// Label for status
		final Label status = new Label(parent, SWT.NULL);
		if (sr != null) {
			status.setText(I18N.translate(PFX
					+ "detailstab.status.text.previous"));
		} else {
			status.setText(I18N.translate(PFX
					+ "detailstab.status.text.notscraped"));
		}
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		status.setLayoutData(gd);

		// ProgressBar
		final ProgressBar pb = new ProgressBar(parent, SWT.SMOOTH
				| SWT.HORIZONTAL | SWT.INDETERMINATE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		pb.setLayoutData(gd);
		pb.setVisible(false);

		// ----STATS
		final Group gStats = new Group(parent, SWT.NULL);
		gStats.setText(I18N.translate(PFX + "detailstab.stats.group.text"));
		gStats.setLayout(new GridLayout(2, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gStats.setLayoutData(gd);

		Label seedsL = new Label(gStats, SWT.NULL);
		seedsL.setText(I18N.translate(PFX + "detailstab.stats.seedsLabel.text")
				+ " ");

		final Label seeds = new Label(gStats, SWT.NULL);
		if (sr != null) {
			seeds.setText(String.valueOf(sr.getSeeds()));
		} else {
			seeds.setText(I18N.translate(PFX
					+ "detailstab.stats.notscraped.text"));
		}

		Label leechersL = new Label(gStats, SWT.NULL);
		leechersL.setText(I18N.translate(PFX
				+ "detailstab.stats.leechersLabel.text")
				+ " ");

		final Label leechers = new Label(gStats, SWT.NULL);
		if (sr != null) {
			leechers.setText(String.valueOf(sr.getLeechers()));
		} else {
			leechers.setText(I18N.translate(PFX
					+ "detailstab.stats.notscraped.text"));
		}

		Label downloadedL = new Label(gStats, SWT.NULL);
		downloadedL.setText(I18N.translate(PFX
				+ "detailstab.stats.downloadsLabel.text")
				+ " ");

		final Label downloaded = new Label(gStats, SWT.NULL);
		if (sr != null) {
			downloaded.setText(String.valueOf(sr.getDownloaded()));
		} else {
			downloaded.setText(I18N.translate(PFX
					+ "detailstab.stats.notscraped.text"));
		}

		Label srURLL = new Label(gStats, SWT.NULL);
		srURLL.setText(I18N.translate(PFX
				+ "detailstab.stats.scrapeURLLabel.text")
				+ " ");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURLL.setLayoutData(gd);

		final Label srURL = new Label(gStats, SWT.NULL);
		if (sr != null) {
			srURL.setText(sr.getScrapeUrl());
			srURL.setToolTipText(sr.getScrapeUrl());
		} else {
			srURL.setText(I18N.translate(PFX
					+ "detailstab.stats.notscraped.text"));
		}
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURL.setLayoutData(gd);

		// ----FILES

		Group gFiles = new Group(parent, SWT.NULL);
		gFiles.setText(I18N.translate(PFX + "detailstab.files.group.text"));
		gFiles.setLayout(new GridLayout(2, false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gFiles.setLayoutData(gd);

		Composite cLeft = new Composite(gFiles, SWT.NULL);
		cLeft.setLayout(new GridLayout(2, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		cLeft.setLayoutData(gd);

		// Size
		Label sizeL = new Label(cLeft, SWT.NULL);
		sizeL.setText(I18N.translate(PFX + "detailstab.files.size.text") + " ");

		Label size = new Label(cLeft, SWT.NULL);
		size.setText(DisplayFormatters.formatByteCountToBase10KBEtc(atc
				.getTorrent().getSize()));

		// Number of Pieces
		Label numPiecesL = new Label(cLeft, SWT.NULL);
		numPiecesL.setText(I18N.translate(PFX + "detailstab.files.pieces.text")
				+ " ");

		Label numPieces = new Label(cLeft, SWT.NULL);
		numPieces.setText(String.valueOf(atc.getTorrent().getNumberOfPieces()));

		// Piece Size
		Label pieceSizeL = new Label(cLeft, SWT.NULL);
		pieceSizeL.setText(I18N.translate(PFX
				+ "detailstab.files.pieceSize.text")
				+ " ");
		Label pieceSize = new Label(cLeft, SWT.NULL);
		pieceSize.setText(DisplayFormatters.formatByteCountToBase10KBEtc(atc
				.getTorrent().getPieceLength()));

		Composite cRight = new Composite(gFiles, SWT.NULL);
		cRight.setLayout(new GridLayout(2, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		cRight.setLayoutData(gd);

		// Created on
		Label dateL = new Label(cRight, SWT.NULL);
		dateL.setText(I18N.translate(PFX + "detailstab.files.createdOn.text")
				+ " ");

		Label date = new Label(cRight, SWT.NULL);
		date.setText(DisplayFormatters.formatDate(atc.getTorrent()
				.getCreationDate() * 1000));

		// Created by
		Label byL = new Label(cRight, SWT.NULL);
		byL.setText(I18N.translate(PFX + "detailstab.files.createdBy.text")
				+ " ");

		Label by = new Label(cRight, SWT.NULL);
		by.setText(EncodingUtil
				.nicePrint(atc.getTorrent().getCreatedBy(), true));

		// Is Private
		Label privL = new Label(cRight, SWT.NULL);
		privL.setText(I18N.translate(PFX + "detailstab.files.private.text")
				+ " ");

		Label priv = new Label(cRight, SWT.NULL);
		if (atc.getTorrent().getPrivate()) {
			priv.setText(I18N.translate("global.yes"));
		} else {
			priv.setText(I18N.translate("global.no"));
		}

		Composite cBottom = new Composite(gFiles, SWT.NULL);
		cBottom.setLayout(new GridLayout(1, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		cBottom.setLayoutData(gd);

		// URL
		Label tURL = new Label(cBottom, SWT.NULL);
		tURL.setText(I18N.translate(PFX + "detailstab.files.announceURL.text")
				+ " " + atc.getTorrent().getAnnounceURL());

		// Hash
		Label hash = new Label(cBottom, SWT.NULL);
		hash.setLayoutData(gd);
		try {
			hash
					.setText(I18N.translate(PFX + "detailstab.files.hash.text")
							+ " "
							+ EncodingUtil.nicePrint(
									atc.getTorrent().getHash(), false));
		} catch (TOTorrentException e) {
			hash.setText(I18N.translate(PFX + "detailstab.files.hash.error"));
		}

		// Comments
		Label commentsL = new Label(cBottom, SWT.NULL);
		commentsL.setText(I18N.translate(PFX
				+ "detailstab.files.commentsLabel.text")
				+ " ");

		Label comments = new Label(cBottom, SWT.NULL);
		try {
			comments.setText(new String(atc.getTorrent().getComment()));
		} catch (Exception e) {

		}

		// Table for files
		final Table filesTable = new Table(gFiles, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		filesTable.setLayoutData(gd);
		filesTable.setHeaderVisible(true);

		TableColumn ftName = new TableColumn(filesTable, SWT.NULL);
		ftName.setText(I18N.translate(PFX
				+ "detailstab.files.table.column.name"));
		ftName.setWidth(450);

		TableColumn ftSize = new TableColumn(filesTable, SWT.NULL);
		ftSize.setText(I18N.translate(PFX
				+ "detailstab.files.table.column.size"));
		ftSize.setWidth(100);

		TOTorrentFile[] files = atc.getFiles();
		for (int i = 0; i < files.length; i++) {
			final TableItem detailItem = new TableItem(filesTable, SWT.NULL);
			String name = files[i].getRelativePath();

			if (name == null || name.length() == 0 || name.equalsIgnoreCase("")) {
				name = "Error Decoding Name";
			}

			detailItem.setText(0, name);
			detailItem.setText(1, DisplayFormatters
					.formatByteCountToBase10KBEtc(files[i].getLength()));

			// Shade every other one
			if (filesTable.indexOf(detailItem) % 2 != 0) {
				detailItem.setBackground(ColorUtilities.getBackgroundColor());
			}
		}

		// Listener for the Scrape button
		scrape.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event arg0) {
				pb.setVisible(true);
				final String urlToScrape = combo.getItem(combo
						.getSelectionIndex());
				scraper.addListener(new ScrapeListener() {

					public void scrapeFailed (final String reason) {
						RCMain.getRCMain().getDisplay().asyncExec(
								new SWTSafeRunnable() {

									@Override
									public void runSafe () {
										pb.setVisible(false);
										status
												.setText(I18N
														.translate(PFX
																+ "detailstab.status.text.failed")
														+ " - " + reason);
										parent.layout();
									}
								});

					}

					public void scrapeFinished (final ScrapeResult sr) {
						RCMain.getRCMain().getDisplay().asyncExec(
								new SWTSafeRunnable() {
									@Override
									public void runSafe () {
										pb.setVisible(false);
										status
												.setText(I18N
														.translate(PFX
																+ "detailstab.status.text.success"));
										gStats
												.setText(I18N
														.translate(PFX
																+ "detailstab.stats.group.text.received")
														+ " "
														+ combo
																.getItem(combo
																		.getSelectionIndex()));
										seeds.setText(String.valueOf(sr
												.getSeeds()));
										leechers.setText(String.valueOf(sr
												.getLeechers()));
										downloaded.setText(String.valueOf(sr
												.getDownloaded()));
										srURL.setText(sr.getScrapeUrl());
										srURL.setToolTipText(sr.getScrapeUrl());
										atc.setScrapeResults(sr);
										parent.layout();
									}

								});

					}

				});

				Thread scrapeThread = new Thread(new SWTSafeRunnable() {
					@Override
					public void runSafe () {
						scraper.scrape(urlToScrape);
					}
				});
				scrapeThread.start();
			}
		});

		// set the tab to the parent
		tab.setControl(parent);
		tabFolder.setSelection(tab);
	}

	/**
	 * Opens the scrape dialog with an array of files already in place
	 * 
	 * @param File[] torrents
	 */
	public static void openFilesAndScrape (final File[] torrents) {

		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null) {
			return;
		}
		display.syncExec(new SWTSafeRunnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see lbms.azsmrc.shared.SWTSafeRunnable#runSafe()
			 */
			@Override
			public void runSafe () {
				if (instance == null || instance.shell == null
						|| instance.shell.isDisposed()) {
					new OpenByFileDialog(display);
					instance.addFileToInstance(torrents);

					for (File torrent : torrents) {
						instance.torrentTabOpen(instance.tabFolder,
								instance.tMap.get(torrent.getName()));
					}
				} else {
					instance.shell.setActive();
					instance.addFileToInstance(torrents);
					for (File torrent : torrents) {
						instance.torrentTabOpen(instance.tabFolder,
								instance.tMap.get(torrent.getName()));
					}
				}
			}
		});
	}

	/**
	 * Opens the scrape dialog with a Torrent that needs to be downloaded first.
	 * 
	 * It may fail to download the torrent and to display the scrapeDialog.
	 * 
	 * @param urlStr torrent URL
	 */
	public static void openURLandScrape (String urlStr) {
		try {
			final URL url = new URL(urlStr);
			Thread t = new Thread(new Runnable() {
				public void run () {
					try {
						File torTemp = File
								.createTempFile("azsmrc", ".torrent");
						torTemp.deleteOnExit();
						TorrentDownload tdl = new TorrentDownload(url, torTemp);
						if (RCMain.getRCMain().getProxy() != null) {
							tdl.setProxy(RCMain.getRCMain().getProxy());
						}
						tdl.addDownloadListener(new DownloadListener() {

							public void debugMsg (String msg) {
								logger.debug(msg);
							}

							public void progress (long bytesRead,
									long bytesTotal) {
							}

							public void stateChanged (int oldState, int newState) {
							}
						});
						tdl.call();
						if (!tdl.hasFailed() && tdl.hasFinished()) {
							openFilesAndScrape(new File[] { torTemp });
						} else {
							logger.debug("Download torrent for scrape failed: "
									+ tdl.getFailureReason());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

}// EOF
