package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.container.AddTorrentContainer;
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;



public class ScrapeDialog {

	private String lastDir;
	private SortedMap<String,AddTorrentContainer> addTorrentMap = new TreeMap<String,AddTorrentContainer>();


	//SWT components
	private Table torrentTable;
	private CTabFolder tabFolder;
	private Display display;
	private Button deleteOnSend;
	private Shell shell;
	private static ScrapeDialog instance;
	private File torrentFile;

	//I18N prefix
	public static final String PFX = "dialog.scrapedialog.";

	private ScrapeDialog(){
		instance = this;
		//set the display
		display = RCMain.getRCMain().getDisplay();

		//pull in the properties for the lastDir if available
		lastDir = RCMain.getRCMain().getProperties().getProperty("Last.Directory");

		//Shell
		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));

		tabFolder = new CTabFolder(shell, SWT.FLAT);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gd);
		tabFolder.setLayout(new GridLayout(1,false));
		tabFolder.setSimple(false);


		final CTabItem mainTab = new CTabItem(tabFolder, SWT.NULL);
		mainTab.setText(I18N.translate(PFX + "detailstab.maintab.text"));

		//Main Composite on shell
		Composite parent = new Composite(tabFolder,SWT.NULL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		//gd.verticalSpan = 125;
		parent.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		gl.verticalSpacing = 20;
		gl.marginHeight = 20;
		parent.setLayout(gl);

		CLabel infoLabel = new CLabel(parent,SWT.CENTER | SWT.SHADOW_IN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;
		gd.verticalSpan = 2;
		gd.horizontalSpan = 2;
		infoLabel.setLayoutData(gd);

		infoLabel.setImage(ImageRepository.getImage("information"));
		infoLabel.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
		infoLabel.setText(I18N.translate(PFX + "infolabel.text"));

		//Load Torrent button
		Button loadTorrent = new Button(parent,SWT.PUSH);
		loadTorrent.setText(I18N.translate(PFX + "loadtorrent_button.text"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		loadTorrent.setLayoutData(gd);

		loadTorrent.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.torrent", "*.*" });
				dialog.setText(I18N.translate(PFX + "filedialog.text"));
				if (lastDir != null) {
					dialog.setFilterPath(lastDir);
				}
				String choosen_file = dialog.open();

				if (choosen_file != null)
					try {
						//Make the file
						torrentFile = new File(choosen_file);
						lastDir = torrentFile.getParent();
						RCMain.getRCMain().getProperties().setProperty("Last.Directory", lastDir);
						RCMain.getRCMain().saveConfig();

						//put the torrent in a container and add to the map
						AddTorrentContainer atc = new AddTorrentContainer(torrentFile);

						//if already in the map, then do not add it to the map again and just
						//open the tab for this file
						if(addTorrentMap.containsKey(atc.getName())){
							CTabItem[] items = tabFolder.getItems();
							for(CTabItem item:items){
								if(item.getText().equalsIgnoreCase(atc.getName())){
									tabFolder.setSelection(item);
									return;
								}
							}
							torrentTabOpen(tabFolder,addTorrentMap.get(atc.getName()));
							return;
						}

						addTorrentMap.put(atc.getName(), atc);
/*
						//Add it to the table
						TableItem item = new TableItem(torrentTable,SWT.NULL);
						item.setText(0,atc.getName());

						//Shade every other one
						if(torrentTable.indexOf(item)%2!=0){
							item.setBackground(ColorUtilities.getBackgroundColor());
						}
*/
						//redraw the table
						redrawTable();

						//open its tab
						torrentTabOpen(tabFolder,addTorrentMap.get(atc.getName()));

					}catch(TOTorrentException e){
						MessageBox messageBox = new MessageBox(shell,SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText(I18N.translate(PFX + "filedialog.error.title"));
						messageBox.setMessage(I18N.translate(PFX + "filedialog.error.message"));
						messageBox.open();
						return;
					}catch(Exception e){
						e.printStackTrace();
					}

			}

		});


		//Group for urlTable and buttons
		Group ttGroup = new Group(parent,SWT.NULL);
		ttGroup.setText(I18N.translate(PFX + "ttGroup.text"));
		ttGroup.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 20;
		ttGroup.setLayoutData(gd);

		//urlTable
		torrentTable = new Table(ttGroup,SWT.BORDER | SWT.SINGLE |  SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		gd = new GridData(GridData.FILL_BOTH);
		torrentTable.setLayoutData(gd);
		torrentTable.setHeaderVisible(true);




		TableColumn ttName = new TableColumn(torrentTable,SWT.NULL);
		ttName.setText(I18N.translate(PFX + "torrentTable.column.name"));
		ttName.setWidth(600);


		//doubleclick listener for the table
		torrentTable.addListener(SWT.MouseDoubleClick, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = torrentTable.getSelection();
				if(items.length != 1) return;
				CTabItem[] tabs = tabFolder.getItems();
				for(CTabItem item:tabs){
					if(item.getText().equalsIgnoreCase(items[0].getText(0))){
						tabFolder.setSelection(item);
						return;
					}
				}
				torrentTabOpen(tabFolder,addTorrentMap.get(items[0].getText(0)));
			}

		});



		//Virtual listener for table
		torrentTable.addListener(SWT.SetData, new Listener(){
			public void handleEvent(Event event) {
				try{
					AddTorrentContainer[] atcArray = addTorrentMap.values().toArray(new AddTorrentContainer[addTorrentMap.size()]);


					TableItem item = (TableItem) event.item;
					int index = torrentTable.indexOf(item);

					AddTorrentContainer atc = atcArray[index];

					item.setText(0,atc.getName());

					//Shade every other one
					if(torrentTable.indexOf(item)%2!=0){
						item.setBackground(ColorUtilities.getBackgroundColor());
					}

				}catch(Exception e){
					e.printStackTrace();
				}



			}

		});


		//buttons under the urlTable
		Composite utButtonComp = new Composite(ttGroup,SWT.NULL);
		utButtonComp.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		utButtonComp.setLayoutData(gd);

		//Clear Table
		Button clearTable = new Button(utButtonComp,SWT.PUSH);
		clearTable.setText(I18N.translate(PFX + "clearTable_button.text"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		clearTable.setLayoutData(gd);
		clearTable.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				CTabItem[] items = tabFolder.getItems();
				for(CTabItem item:items){
					if(!item.equals(mainTab)){
						item.dispose();
					}
				}
				addTorrentMap.clear();
				//redraw the table
				redrawTable();
			}
		});


		//Close Dialog
		Button close = new Button(utButtonComp,SWT.PUSH);
		close.setText(I18N.translate("global.close"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		close.setLayoutData(gd);
		close.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				shell.dispose();
			}
		});

		mainTab.setControl(parent);

		//Center and Open Shell
		GUI_Utilities.centerShellandOpen(shell);
	}





	/**
	 * The main torrent details tab
	 * @param tabFolder
	 * @param atc
	 */
	private void torrentTabOpen(CTabFolder tabFolder, final AddTorrentContainer atc){
		//pull previous SR if available
		ScrapeResult sr = atc.getScrapeResults();


		CTabItem tab = new CTabItem(tabFolder,SWT.CLOSE);
		final Scraper scraper = new Scraper(atc.getTorrent());

		try {
			tab.setText(atc.getName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		final Composite parent = new Composite(tabFolder,SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		parent.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		parent.setLayout(gl);

		Composite comboComp = new Composite(parent,SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		comboComp.setLayoutData(gd);

		gl = new GridLayout();
		gl.marginWidth = 0;
		gl.numColumns = 3;
		comboComp.setLayout(gl);

		final Combo combo = new Combo(comboComp,SWT.DROP_DOWN | SWT.READ_ONLY);


		//Pull the URL from the torrent and put it in the combo
		combo.add(atc.getTorrent().getAnnounceURL().toString());
		combo.select(0);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		combo.setLayoutData(gd);


		//Pull the group from the torrent
		TOTorrentAnnounceURLGroup torrentGroup = atc.getTorrent().getAnnounceURLGroup();

		//Check the length to see if a group is actually present
		if(torrentGroup.getAnnounceURLSets().length > 0){
			//group is present, now pull the set
			TOTorrentAnnounceURLSet[] urlSets = torrentGroup.getAnnounceURLSets();
			//crawl through them and pull the titles for the table
			for(TOTorrentAnnounceURLSet urlSet:urlSets){
				URL[] urls = urlSet.getAnnounceURLs();
				for(URL url:urls){
					if(!url.toString().equalsIgnoreCase(atc.getTorrent().getAnnounceURL().toString()))
						combo.add(url.toString());
				}
			}
		}


		//button for Scrape -- still in comboComp
		Button scrape = new Button(comboComp,SWT.PUSH);
		scrape.setText(I18N.translate(PFX + "scrape_button.text"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		scrape.setLayoutData(gd);

		//button for Add Torrent
		final Button add = new Button(comboComp, SWT.PUSH);
		add.setText(I18N.translate(PFX + "add_button.text"));
		add.setToolTipText(I18N.translate(PFX + "add_button.tooltip"));
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		add.setLayoutData(gd);
		add.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(RCMain.getRCMain().connected()){
					if(atc.isWholeFileSent()){
						RCMain.getRCMain().getClient().getDownloadManager().addDownload(atc.getTorrentFile());
					}else{
						int[] props = atc.getFileProperties();
						//Main add to Azureus
						RCMain.getRCMain().getClient().getDownloadManager().addDownload(atc.getTorrentFile(), props);
					}
					if(Boolean.parseBoolean(RCMain.getRCMain().getProperties().getProperty("delete.on.send", "false"))){
						if(!atc.deleteFile()){
							MessageBox messageBox = new MessageBox(shell,
									SWT.ICON_ERROR | SWT.OK);
							messageBox.setText(I18N.translate("global.error"));
							messageBox.setMessage(I18N.translate(PFX + "add_button.error1.message") + " " + atc.getTorrentFile().getName());
							messageBox.open();
						}
					}
				}else{
					//we are not connected .. so alert the user
					MessageBox messageBox = new MessageBox(add.getShell(),SWT.ICON_INFORMATION | SWT.OK);
					messageBox.setText(I18N.translate(PFX + "add_button.error2.title"));
					messageBox.setMessage(I18N.translate(PFX + "add_button.error2.message"));
					messageBox.open();
					return;
				}

			}

		});

		//Label for status
		final Label status = new Label(parent,SWT.NULL);
		if(sr != null)
			status.setText(I18N.translate(PFX + "detailstab.status.text.previous"));
		else
			status.setText(I18N.translate(PFX + "detailstab.status.text.notscraped"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		status.setLayoutData(gd);


		deleteOnSend = new Button(parent,SWT.CHECK);
		deleteOnSend.setText(I18N.translate(PFX + "detailstab.delete_on_send.text"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 1;
		deleteOnSend.setLayoutData(gd);
		deleteOnSend.setSelection(Boolean.parseBoolean(RCMain.getRCMain().getProperties().getProperty("delete.on.send", "false")));
		deleteOnSend.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				RCMain.getRCMain().getProperties().setProperty("delete.on.send", Boolean.toString(deleteOnSend.getSelection()));
				//Store the new setting
				RCMain.getRCMain().saveConfig();
			}

		});

		//ProgressBar
		final ProgressBar pb = new ProgressBar(parent,SWT.SMOOTH | SWT.HORIZONTAL | SWT.INDETERMINATE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		pb.setLayoutData(gd);
		pb.setVisible(false);


		//----STATS
		final Group gStats = new Group(parent,SWT.NULL);
		gStats.setText(I18N.translate(PFX + "detailstab.stats.group.text"));
		gStats.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan = 2;
		gStats.setLayoutData(gd);



		Label seedsL = new Label(gStats,SWT.NULL);
		seedsL.setText(I18N.translate(PFX + "detailstab.stats.seedsLabel.text") + " ");

		final Label seeds = new Label(gStats,SWT.NULL);
		if(sr != null)
			seeds.setText(String.valueOf(sr.getSeeds()));
		else
			seeds.setText(I18N.translate(PFX + "detailstab.stats.notscraped.text"));

		Label leechersL = new Label(gStats,SWT.NULL);
		leechersL.setText(I18N.translate(PFX + "detailstab.stats.leechersLabel.text") + " ");

		final Label leechers = new Label(gStats, SWT.NULL);
		if(sr != null)
			leechers.setText(String.valueOf(sr.getLeechers()));
		else
			leechers.setText(I18N.translate(PFX + "detailstab.stats.notscraped.text"));

		Label downloadedL = new Label(gStats,SWT.NULL);
		downloadedL.setText(I18N.translate(PFX + "detailstab.stats.downloadsLabel.text") + " ");

		final Label downloaded = new Label(gStats,SWT.NULL);
		if(sr != null)
			downloaded.setText(String.valueOf(sr.getDownloaded()));
		else
			downloaded.setText(I18N.translate(PFX + "detailstab.stats.notscraped.text"));


		Label srURLL = new Label(gStats,SWT.NULL);
		srURLL.setText(I18N.translate(PFX + "detailstab.stats.scrapeURLLabel.text") + " ");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURLL.setLayoutData(gd);

		final Label srURL = new Label(gStats,SWT.NULL);
		if(sr != null){
			srURL.setText(sr.getScrapeUrl());
			srURL.setToolTipText(sr.getScrapeUrl());
		}else
			srURL.setText(I18N.translate(PFX + "detailstab.stats.notscraped.text"));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURL.setLayoutData(gd);



		//----FILES

		Group gFiles = new Group(parent,SWT.NULL);
		gFiles.setText(I18N.translate(PFX + "detailstab.files.group.text"));
		gFiles.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan = 2;
		gFiles.setLayoutData(gd);

		Composite cLeft = new Composite(gFiles,SWT.NULL);
		cLeft.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		cLeft.setLayoutData(gd);

		//Size
		Label sizeL = new Label(cLeft,SWT.NULL);
		sizeL.setText(I18N.translate(PFX + "detailstab.files.size.text") + " ");

		Label size = new Label(cLeft,SWT.NULL);
		size.setText(DisplayFormatters.formatByteCountToBase10KBEtc(atc.getTorrent().getSize()));

		//Number of Pieces
		Label numPiecesL = new Label(cLeft,SWT.NULL);
		numPiecesL.setText(I18N.translate(PFX + "detailstab.files.pieces.text") + " ");

		Label numPieces = new Label(cLeft,SWT.NULL);
		numPieces.setText(String.valueOf(atc.getTorrent().getNumberOfPieces()));

		//Piece Size
		Label pieceSizeL = new Label(cLeft,SWT.NULL);
		pieceSizeL.setText(I18N.translate(PFX + "detailstab.files.pieceSize.text") + " ");
		Label pieceSize = new Label(cLeft,SWT.NULL);
		pieceSize.setText(DisplayFormatters.formatByteCountToBase10KBEtc(atc.getTorrent().getPieceLength()));



		Composite cRight = new Composite(gFiles,SWT.NULL);
		cRight.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan = 1;
		cRight.setLayoutData(gd);


		//Created on
		Label dateL = new Label(cRight,SWT.NULL);
		dateL.setText(I18N.translate(PFX + "detailstab.files.createdOn.text") + " ");

		Label date = new Label(cRight,SWT.NULL);
		date.setText(DisplayFormatters.formatDate(atc.getTorrent().getCreationDate()));

		//Created by
		Label byL = new Label(cRight,SWT.NULL);
		byL.setText(I18N.translate(PFX + "detailstab.files.createdBy.text") + " ");

		Label by = new Label(cRight,SWT.NULL);
		by.setText(EncodingUtil.nicePrint(atc.getTorrent().getCreatedBy(),true));

		//Is Private
		Label privL = new Label(cRight,SWT.NULL);
		privL.setText(I18N.translate(PFX + "detailstab.files.private.text") + " ");

		Label priv = new Label(cRight,SWT.NULL);
		if(atc.getTorrent().getPrivate())
			priv.setText(I18N.translate("global.yes"));
		else
			priv.setText(I18N.translate("global.no"));

		Composite cBottom = new Composite(gFiles,SWT.NULL);
		cBottom.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan = 2;
		cBottom.setLayoutData(gd);

		//URL
		Label tURL = new Label(cBottom,SWT.NULL);
		tURL.setText(I18N.translate(PFX + "detailstab.files.announceURL.text") + " " + atc.getTorrent().getAnnounceURL());

		//Hash
		Label hash = new Label(cBottom,SWT.NULL);
		hash.setLayoutData(gd);
		try {
			hash.setText(I18N.translate(PFX + "detailstab.files.hash.text") + " " + EncodingUtil.nicePrint(atc.getTorrent().getHash(),false));
		} catch (TOTorrentException e) {
			hash.setText(I18N.translate(PFX + "detailstab.files.hash.error"));
		}

		//Comments
		Label commentsL = new Label(cBottom,SWT.NULL);
		commentsL.setText(I18N.translate(PFX + "detailstab.files.commentsLabel.text") + " ");

		Label comments = new Label(cBottom,SWT.NULL);
		try{
			comments.setText(new String(atc.getTorrent().getComment()));
		}catch(Exception e){

		}



		//Table for files
		final Table filesTable = new Table(gFiles,SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		filesTable.setLayoutData(gd);
		filesTable.setHeaderVisible(true);

		TableColumn ftCheck = new TableColumn(filesTable,SWT.NULL);
		ftCheck.setWidth(30);

		TableColumn ftName = new TableColumn(filesTable,SWT.NULL);
		ftName.setText(I18N.translate(PFX + "detailstab.files.table.column.name"));
		ftName.setWidth(450);

		TableColumn ftSize = new TableColumn(filesTable,SWT.NULL);
		ftSize.setText(I18N.translate(PFX + "detailstab.files.table.column.size"));
		ftSize.setWidth(100);

		TOTorrentFile[] files = atc.getFiles();
		int[] properties = atc.getFileProperties();
		for (int i = 0; i < files.length; i++) {
			final TableItem detailItem = new TableItem(filesTable,SWT.NULL);
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

		//Listener for table DND selection
		filesTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					TableItem item = (TableItem) event.item;
					int place = filesTable.indexOf(item);

					if (item.getChecked()) {
						atc.setFileProperty(place, 1);
					} else
						atc.setFileProperty(place, 0);
				}

			}
		});


		//Listener for the Scrape button
		scrape.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				pb.setVisible(true);
				final String urlToScrape = combo.getItem(combo.getSelectionIndex());
				scraper.addListener(new ScrapeListener(){

					public void scrapeFailed(final String reason) {
						display.asyncExec(new SWTSafeRunnable(){

							public void runSafe() {
								pb.setVisible(false);
								status.setText(I18N.translate(PFX + "detailstab.status.text.failed") + " - " + reason);
								parent.layout();
							}
						});

					}

					public void scrapeFinished(final ScrapeResult sr) {
						display.asyncExec(new SWTSafeRunnable(){
							public void runSafe() {
								pb.setVisible(false);
								status.setText(I18N.translate(PFX + "detailstab.status.text.success"));
								gStats.setText(I18N.translate(PFX + "detailstab.stats.group.text.received") + " " + combo.getItem(combo.getSelectionIndex()));
								seeds.setText(String.valueOf(sr.getSeeds()));
								leechers.setText(String.valueOf(sr.getLeechers()));
								downloaded.setText(String.valueOf(sr.getDownloaded()));
								srURL.setText(sr.getScrapeUrl());
								srURL.setToolTipText(sr.getScrapeUrl());
								atc.setScrapeResults(sr);
								parent.layout();
							}

						});


					}

				});

				Thread scrapeThread = new Thread(new SWTSafeRunnable(){
					public void runSafe() {
						scraper.scrape(urlToScrape);
					}
				});
				scrapeThread.start();
			}
		});



		//set the tab to the parent
		tab.setControl(parent);
		tabFolder.setSelection(tab);
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return shell;
	}



	/**
	 * Check to make sure that there are no other ones open and if not
	 * open the ScrapeDialog
	 */
	public static void open() {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if (instance == null || instance.getShell() == null || instance.getShell().isDisposed())
					new ScrapeDialog();
			}
		});
	}

	/**
	 * Opens the scrape dialog with a file already in place
	 * @param File torrent
	 */
	public static void openFileAndScrape(final File torrent) {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if (instance == null || instance.getShell() == null || instance.getShell().isDisposed()){
					new ScrapeDialog();
				}
				addTorrentToMap(torrent);
				instance.redrawTable();
			}
		});
	}


	/**
	 * Opens the scrape dialog with an array of files already in place
	 * @param File[] torrents
	 */
	public static void openFilesAndScrape(final File[] torrents) {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if (instance == null || instance.getShell() == null || instance.getShell().isDisposed()){
					new ScrapeDialog();
				}
				for(File torrent:torrents){
					addTorrentToMap(torrent);
				}
				instance.redrawTable();
			}
		});
	}


	private static void addTorrentToMap(final File torrent){
		try {
			AddTorrentContainer	atc = new AddTorrentContainer(torrent);
			if(!instance.addTorrentMap.containsKey(atc.getName())){
				instance.addTorrentMap.put(atc.getName(), atc);
			}
		} catch (TOTorrentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Clears the main table so that .setData can redraw everything
	 *
	 */
	private void redrawTable() {
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){
			public void runSafe() {
				if(torrentTable != null){
					torrentTable.setItemCount(addTorrentMap.size());
					torrentTable.clearAll();
				}
			}
		});
	}

}//EOF
