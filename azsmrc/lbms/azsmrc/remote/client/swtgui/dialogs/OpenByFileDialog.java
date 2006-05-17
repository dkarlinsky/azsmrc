/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.DownloadManagerShell;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.URLTransfer;
import lbms.azsmrc.remote.client.swtgui.container.AddTorrentContainer;
import lbms.azsmrc.remote.client.torrent.TOTorrentException;
import lbms.azsmrc.remote.client.torrent.TOTorrentFile;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.tools.i18n.I18N;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class OpenByFileDialog {

	private Table filesTable, detailsTable;

	Button deleteOnSend;

	private String lastDir;

	private Label totalSLabel, totalS;

	private Label saveDir, saveDirSize, destDir, destDirSize;

	private Map<String, AddTorrentContainer> tMap = new HashMap<String, AddTorrentContainer>();
	private Map<String,String> driveMap = new HashMap<String,String>();

	//private int drag_drop_line_start = -1;

	//I18N prefix
	public static final String PFX = "dialog.openbyfiledialog.";

	public OpenByFileDialog(Display display, final String[] filenames) {
		//pull last dir if available
		lastDir =  RCMain.getRCMain().getProperties().getProperty("Last.Directory");

		// Shell
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		// Comp on shell
		final Group comp = new Group(shell, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);

		// first line
		Button open_file_button = new Button(comp, SWT.PUSH);
		open_file_button.setToolTipText(I18N.translate(PFX + "openfile.tooltip"));
		open_file_button.setText(I18N.translate(PFX + "openfile.text"));
		if(Utilities.isLinux())
			open_file_button.setImage(ImageRepository.getImage("open_by_file"));
		open_file_button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.torrent", "*.*" });
				dialog.setText(I18N.translate(PFX + "openfile.filedialog.text"));
				if (lastDir != null) {
					dialog.setFilterPath(lastDir);
				}
				String choosen_file = dialog.open();

				if (choosen_file != null)
					try {
						File test = new File(choosen_file);
						if (test.isFile() && test.canRead()) {
							AddTorrentContainer container = new AddTorrentContainer(test);
							if(tMap.containsKey(container.getName())){
								MessageBox messageBox = new MessageBox(shell,SWT.ICON_INFORMATION | SWT.OK);
								messageBox.setText(I18N.translate(PFX + "openfile.filedialog.duplicate.title"));
								messageBox.setMessage(I18N.translate(PFX + "openfile.filedialog.duplicate.message"));
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
							RCMain.getRCMain().getProperties().setProperty("Last.Directory", lastDir);
							RCMain.getRCMain().saveConfig();
							filesTable.setSelection(item);
						}
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "openfile.error1"));
						messageBox.open();
					} catch (TOTorrentException e1) {
						e1.printStackTrace();
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "openfile.error2"));
						messageBox.open();
					} catch (Exception e1) {
						e1.printStackTrace();
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox
								.setMessage(I18N.translate(PFX + "openfile.error3"));
						messageBox.open();
						e1.printStackTrace();
					}
			}
		});

		Button remove = new Button(comp, SWT.PUSH);
		remove.setText(I18N.translate(PFX + "remove.text"));
		if(Utilities.isLinux())
			remove.setImage(ImageRepository.getImage("toolbar_remove"));
		remove.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				int[] items = filesTable.getSelectionIndices();
				if (items.length == 0)
					return;
				for (int index : items) {
					TableItem item = filesTable.getItem(index);
					if(tMap.containsKey(item.getText(0))){
						tMap.remove(item.getText(0));
					}
				}
				filesTable.remove(items);
				filesTable.deselectAll();
				detailsTable.removeAll();
				setTotalSize();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

		});



		//      -------Server Free Disk stuff -----------\\
		final Group details3 = new Group(comp,SWT.NULL);
		details3.setText(I18N.translate(PFX + "serverdrive.group.text"));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.horizontalSpacing = 30;
		details3.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		details3.setLayoutData(gridData);



		//save.dir is the default save dir
		Label saveDirL = new Label(details3,SWT.NULL);
		saveDirL.setText(I18N.translate(PFX + "serverdrive.defaultsavedir.text"));

		saveDir = new Label(details3,SWT.NULL);
		saveDir.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		saveDirSize = new Label(details3, SWT.NULL);
		saveDirSize.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		//destination.dir is the user dir
		Label destDirL = new Label(details3,SWT.NULL);
		destDirL.setText(I18N.translate(PFX + "serverdrive.destinationdir.text"));

		destDir = new Label(details3,SWT.NULL);
		destDir.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

		destDirSize = new Label(details3, SWT.NULL);
		destDirSize.setText(I18N.translate(PFX + "serverdrive.notreceivedyet"));

//		CUL
		final ClientUpdateListener serverDetails = new ClientUpdateListener(){

			public void update(long updateSwitches) {
				if((updateSwitches & Constants.UPDATE_DRIVE_INFO) != 0){
					RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
						public void run() {
							try{
								driveMap = RCMain.getRCMain().getClient().getRemoteInfo().getDriveInfo();

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
								comp.layout();
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

		if (RCMain.getRCMain().connected())
			RCMain.getRCMain().getClient().getRemoteInfo().refreshDriveInfo();

		//list to shell for close so that we can remove cul
		shell.addShellListener(new ShellListener(){

			public void shellActivated(ShellEvent arg0) {}

			public void shellClosed(ShellEvent arg0) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(serverDetails);
			}

			public void shellDeactivated(ShellEvent arg0) {}

			public void shellDeiconified(ShellEvent arg0) {}

			public void shellIconified(ShellEvent arg0) {}
		});


		//--------------------------Total Size ------------------------\\

		totalSLabel = new Label(comp,SWT.NULL);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		totalSLabel.setLayoutData(gridData);


		totalS = new Label(comp,SWT.NULL);
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

		filesTable = new Table(sash, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
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

			public void widgetSelected(SelectionEvent arg0) {
				TableItem item = (TableItem) arg0.item;
				generateDetails(item.getText(0));
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

		});

		Group detailsGroup = new Group(sash, SWT.NULL);
		detailsGroup.setText(I18N.translate(PFX + "torrentdetail.group.text"));
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		detailsGroup.setLayout(gl);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 50;
		gridData.grabExcessVerticalSpace = true;
		detailsGroup.setLayoutData(gridData);

		// detailsTAble for each torrent
		detailsTable = new Table(detailsGroup, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.CHECK | SWT.MULTI);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
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
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {

					TableItem item = (TableItem) event.item;
					int place = detailsTable.indexOf(item);
					AddTorrentContainer container = (AddTorrentContainer) filesTable
							.getSelection()[0].getData();
					if (item.getChecked()) {
						container.setFileProperty(place, 1);
					} else
						container.setFileProperty(place, 0);
					setTotalSize();
				}

			}
		});

		// Buttons
		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

		deleteOnSend = new Button(button_comp,SWT.CHECK);
		deleteOnSend.setText(I18N.translate(PFX + "deleteonsend.text"));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		deleteOnSend.setLayoutData(gridData);
		deleteOnSend.setSelection(Boolean.parseBoolean(RCMain.getRCMain().getProperties().getProperty("delete.on.send", "false")));
		deleteOnSend.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				if(deleteOnSend.getSelection())
					RCMain.getRCMain().getProperties().setProperty("delete.on.send", "true");
				else
					RCMain.getRCMain().getProperties().setProperty("delete.on.send", "false");
				//Store the new setting
				RCMain.getRCMain().saveConfig();
			}

		});


		Button connect = new Button(button_comp, SWT.PUSH);
		connect.setText(I18N.translate(PFX + "sendfiles.text"));
		connect.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (tMap.size() == 0) {
					MessageBox messageBox = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "sendfiles.error1"));
					messageBox.open();
					return;
				} else {
					Iterator iterator = tMap.keySet().iterator();
					while (iterator.hasNext()) {
						AddTorrentContainer container = tMap.get(iterator.next());

						//Check to see if the whole file is sent and if so, just add it normally
						//else send it with the properties int[]
						if(container.isWholeFileSent()){
							RCMain.getRCMain().getClient().sendAddDownload(container.getTorrentFile());
						}else{
							int[] props = container.getFileProperties();
							//Main add to Azureus
							RCMain.getRCMain().getClient().sendAddDownload(container.getTorrentFile(), props);
						}

						if(Boolean.parseBoolean(RCMain.getRCMain().getProperties().getProperty("delete.on.send", "false"))){
							if(!container.deleteFile()){
								MessageBox messageBox = new MessageBox(shell,
										SWT.ICON_ERROR | SWT.OK);
								messageBox.setText(I18N.translate("global.error"));
								messageBox.setMessage(I18N.translate(PFX + "sendfiles.error2")+ " " + container.getTorrentFile().getName());
								messageBox.open();
							}
						}
					}
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

		// See if we come in with a file already
		if (filenames != null) {
			try {
				for (String filename : filenames) {
					File test = new File(filename);
					if (test.isFile() && test.canRead()) {
						AddTorrentContainer container = new AddTorrentContainer(
								test);
						TableItem item = new TableItem(filesTable, SWT.NULL);
						item.setText(0, container.getName());
						item.setText(1, container.getFilePath());
						tMap.put(container.getName(), container);
						filesTable.setSelection(item);
						generateDetails(container.getName());
						lastDir = container.getFilePath();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Center Shell and open
		GUI_Utilities.centerShellandOpen(shell);

	}

	public OpenByFileDialog(Display display) {
		new OpenByFileDialog(display, null);
	}

	private void createDragDrop(final Table parent) {
		try {

			DropTarget dropTarget = new DropTarget(parent, DND.DROP_DEFAULT
					| DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK
					| DND.DROP_TARGET_MOVE);


			dropTarget.setTransfer(new Transfer[] {FileTransfer.getInstance(),
						TextTransfer.getInstance() });


			dropTarget.addDropListener(new DropTargetAdapter() {
				public void dropAccept(DropTargetEvent event) {
					event.currentDataType = URLTransfer.pickBestType(
							event.dataTypes, event.currentDataType);
				}

				public void dragEnter(DropTargetEvent event) {
					// no event.data on dragOver, use drag_drop_line_start to
					// determine if
					// ours
					if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
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

				public void drop(DropTargetEvent event) {
					if (!(event.data instanceof String)
							|| !((String) event.data).equals("moveRow")) {
						openDroppedTorrents(event);
						return;
					}

				}
			});

		} catch (Throwable t) {
			RCMain.getRCMain().getDebugLogger().severe(
					"failed to init drag-n-drop + \n" + t);
		}
	}

	public void openDroppedTorrents(DropTargetEvent event) {
		if (event.data == null)
			return;

		// boolean bOverrideToStopped = event.detail == DND.DROP_COPY;

		if (event.data instanceof String[] || event.data instanceof String) {
			final String[] sourceNames = (event.data instanceof String[]) ? (String[]) event.data
					: new String[] { (String) event.data };
			if (sourceNames == null)
				event.detail = DND.DROP_NONE;
			if (event.detail == DND.DROP_NONE)
				return;

			for (int i = 0; (i < sourceNames.length); i++) {
				final File source = new File(sourceNames[i]);
				if (source.isFile()) {
					String filename = source.getAbsolutePath();
					try {
						if (!DownloadManagerShell.isTorrentFile(filename)) {
							RCMain
									.getRCMain()
									.getDebugLogger()
									.info(
											"openDroppedTorrents: file not a torrent file");

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
						RCMain.getRCMain().getDebugLogger().info(
								"Torrent open fails for '" + filename + "'\n"
										+ e.toString());
					}
				} else if (source.isDirectory()) {
					MessageBox messageBox = new MessageBox(RCMain.getRCMain()
							.getMainWindow().getShell(), SWT.ICON_ERROR
							| SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "clipboard.error1"));
					messageBox.open();
					return;
				}
			}
		}
	}

	public void generateDetails(String tName) {
		if (tMap.containsKey(tName)) {
			detailsTable.removeAll();
			final AddTorrentContainer container = tMap.get(tName);
			TOTorrentFile[] files = container.getFiles();
			int[] properties = container.getFileProperties();
			for (int i = 0; i < files.length; i++) {
				final TableItem detailItem = new TableItem(detailsTable,
						SWT.NULL);
				String name = files[i].getRelativePath();

				if (name == null || name.length() == 0 || name.equalsIgnoreCase("")) {
					name = "Error Decoding Name";
				}

				if (properties != null && properties[i] == 1) {
					detailItem.setChecked(true);
				}
				detailItem.setText(1, name);
				detailItem.setText(2, DisplayFormatters
						.formatByteCountToBase10KBEtc(files[i].getLength()));
				//Shade every other one
				if(filesTable.indexOf(detailItem)%2!=0){
					detailItem.setBackground(ColorUtilities.getBackgroundColor());
				}
			}

		}else{
			System.out.println("Error... " + tName + " not in map!");
		}
	}


	public void setTotalSize(){
		long totalSize = 0;
		Iterator it = tMap.keySet().iterator();
		while (it.hasNext()){
			AddTorrentContainer atc = tMap.get(it.next());
			if(atc != null)
				totalSize += atc.getTotalSizeOfDownloads();
		}
		long totalSizeAdj = totalSize / 1024l;
		if(driveMap.containsKey("save.dir") && driveMap.containsKey("save.dir.path")){
			long saveDirFree = Long.parseLong(driveMap.get("save.dir"));
			System.out.println(saveDirFree +  " | " + (1024l*1024l*2l) + " | " + totalSizeAdj +  " | " + (saveDirFree - totalSizeAdj));
			if((saveDirFree - totalSizeAdj) > (1024l * 1024l * 2l/*2 GB */) ){
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
			}else if((saveDirFree - totalSizeAdj) > (1024l * 20l /*20 MB */)){
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			}else {
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
			}
			saveDirSize.setToolTipText(I18N.translate(PFX + "serverdrive.defaultsavedir.tooltip") +
					DisplayFormatters.formatKBCountToBase10KBEtc(saveDirFree - totalSizeAdj));

		}else{
			totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}

		if(driveMap.containsKey("destination.dir") && driveMap.containsKey("destination.dir.path")){
			long destDirFree = Long.parseLong(driveMap.get("destination.dir"));
			if((destDirFree - totalSizeAdj) > (1024l * 1024l * 2l/*2 GB */) ){
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
			}else if((destDirFree - totalSizeAdj) > (1024l * 20l /*20 MB */)){
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			}else {
				totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
			}
			destDirSize.setToolTipText(I18N.translate(PFX + "serverdrive.destinationdir.tooltip") +
					DisplayFormatters.formatKBCountToBase10KBEtc(destDirFree - totalSizeAdj));
		}else{
			totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}
		if(totalSizeAdj == 0){
			totalS.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}
		totalSLabel.setText(I18N.translate(PFX + "totalsize.text") + " ");
		totalS.setText(DisplayFormatters.formatByteCountToBase10KBEtc(totalSize));
	}

}// EOF
