/*
 * Created on Feb 18, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;


import java.text.DecimalFormat;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadAdvancedStats;
import lbms.azsmrc.remote.client.DownloadFile;
import lbms.azsmrc.remote.client.DownloadFileManager;
import lbms.azsmrc.remote.client.DownloadStats;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.EncodingUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TorrentDetailsTab {
	private Composite transfer1, transfer2, transfer3;
	//labels for transfer1
	private Label timeElapsed, downloaded, uploaded,seeds, swarmSpeed;
	//labels for transfer2
	private Label remaining, downloadSpeed, uploadSpeed, peers, averageCompletion;
	//labels for transfer3
	private Label shareRatio, hashFails, uploadRate;

	private Composite info1, info2;
	//updatable labels for info1
	private Label trackerURL, trackerStatus, saveIn, numPieces, trackerComment;
	private Label pieceSize, createdOn, lastScrape, nextScrape;

	private Table filesTable;

	private Button scrape, announce;

	private Download download;
	private DownloadStats ds;
	private DownloadAdvancedStats das;
	private DecimalFormat df;
	private DownloadFileManager dfm;
	private DownloadFile[] dfm_files;


	public TorrentDetailsTab(CTabFolder parentTab, Download _download){
		download = _download;
		ds = download.getStats();
		das = download.getAdvancedStats();
		dfm = download.getFileManager();



		df = new DecimalFormat();
		df.applyPattern("##0.00");


		final CTabItem masterTab = new CTabItem(parentTab, SWT.CLOSE);
		masterTab.setText(download.getName());


		final Composite parent = new Composite(parentTab, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		CTabFolder subTabFolder = new CTabFolder(parent, SWT.BORDER);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		subTabFolder.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		subTabFolder.setLayoutData(gridData);


		loadSubTab1(subTabFolder);
		loadSubTab2(subTabFolder);

		if(!das._isLoaded()){
			das.load();
		}


		if(!dfm._isLoaded()){
			dfm.update();
		}else{
			dfm_files = dfm.getFiles();
			redrawTable();
		}
		//----------------------------------------------\\
		masterTab.setControl(parent);
		parentTab.setSelection(masterTab);


		//The Client update listener
		final ClientUpdateListener cul = new ClientUpdateListener(){

			public void update(long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_DOWNLOAD_FILES) != 0){
					dfm_files = dfm.getFiles();
					redrawTable();
				}

				if((updateSwitches & Constants.UPDATE_ADVANCED_STATS) != 0){
					das = download.getAdvancedStats();
					update_elements();
					redrawTable();

				}

				if((updateSwitches & Constants.UPDATE_LIST_TRANSFERS) != 0){
					update_elements();
					redrawTable();
				}


			}
		};

		//Add the CUL to the Client
		RCMain.getRCMain().getClient().addClientUpdateListener(cul);


		//Listen for when tab is closed and make sure to remove the client update listener
		masterTab.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(cul);

				//TODO store table column widths
			}
		});

	}

	private void loadSubTab1(CTabFolder tabFolder){

		final CTabItem detailsTab = new CTabItem(tabFolder, SWT.NULL);
		detailsTab.setText("Details");


		final Composite parent = new Composite(tabFolder, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);


		Group transfer = new Group(parent,SWT.NONE);
		transfer.setText("Transfer");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		transfer.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;

		transfer.setLayoutData(gridData);

		//Transfer Composite 1 of 3
		transfer1 = new Composite(transfer, SWT.NONE);
		transfer1.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		transfer1.setLayoutData(gridData);

		//Time Elapsed
		Label timeElapsed_Label = new Label(transfer1, SWT.NULL);
		timeElapsed_Label.setText("Time Elapsed: ");

		timeElapsed = new Label(transfer1, SWT.NULL);
		timeElapsed.setText(ds.getElapsedTime());

		//downloaded
		Label downloaded_Label = new Label(transfer1,SWT.NULL);
		downloaded_Label.setText("Downloaded:  ");

		downloaded = new Label(transfer1,SWT.NULL);
		downloaded.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getDownloaded()));

		//Uploaded
		Label uploaded_Label = new Label(transfer1,SWT.NULL);
		uploaded_Label.setText("Uploaded:");

		uploaded = new Label(transfer1,SWT.NULL);
		uploaded.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getUploaded()));

		//Seeds
		Label seeds_Label = new Label(transfer1, SWT.NULL);
		seeds_Label.setText("Seeds: ");

		seeds = new Label(transfer1, SWT.NULL);
		if(download.getTotalSeeds() < 0)
			seeds.setText(download.getSeeds() + " connected ( 0 in swarm )");
		else
			seeds.setText(download.getSeeds() + " connected ( " + download.getTotalSeeds() + " in swarm )");
		//Swarm Speed
		Label swarmSpeed_Label = new Label(transfer1, SWT.NULL);
		swarmSpeed_Label.setText("Swarm Speed: ");

		swarmSpeed = new Label(transfer1, SWT.NULL);
		swarmSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getTotalAverage()) + "/s");


		//_________________\\


		//Transfer Composite 2 of 3
		transfer2 = new Composite(transfer, SWT.NONE);
		transfer2.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		transfer2.setLayoutData(gridData);

		//Remaining
		Label remaining_Label = new Label(transfer2, SWT.NULL);
		remaining_Label.setText("Remaining: ");

		remaining = new Label(transfer2, SWT.NULL);
		remaining.setText(ds.getETA());

		//Download Speed
		Label downloadSpeed_Label = new Label(transfer2, SWT.NULL);
		downloadSpeed_Label.setText("Download Speed: ");

		downloadSpeed = new Label(transfer2, SWT.NULL);
		downloadSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getDownloadAverage()) + "/s");

		//Upload Speed
		Label uploadSpeed_Label = new Label(transfer2, SWT.NULL);
		uploadSpeed_Label.setText("Upload Speed: ");

		uploadSpeed = new Label(transfer2, SWT.NULL);
		uploadSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getUploadAverage()) + "/s");

		//peers
		Label peers_Label = new Label(transfer2, SWT.NULL);
		peers_Label.setText("Peers: ");

		peers = new Label(transfer2, SWT.NULL);
		if(download.getTotalLeecher() < 0)
			peers.setText(download.getLeecher() + " connected ( 0 in swarm )");
		else
			peers.setText(download.getLeecher() + " connected ( " + download.getTotalLeecher() + " in swarm )");

		//Average Completion
		Label averageCompletion_Label = new Label(transfer2, SWT.NULL);
		averageCompletion_Label.setText("Average Completion:  ");

		averageCompletion = new Label(transfer2, SWT.NULL);

		if(ds.getAvailability() < 0)
			averageCompletion.setText("");
		else
			averageCompletion.setText(df.format(ds.getAvailability()));

		//_________________\\


		//Transfer Composite 3 of 3
		transfer3 = new Composite(transfer, SWT.NULL);
		gridLayout = new GridLayout();
		transfer3.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		transfer3.setLayoutData(gridData);

		//Share Ratio
		Label shareRatio_Label = new Label(transfer3, SWT.NULL);
		shareRatio_Label.setText("Share Ratio: ");

		shareRatio = new Label(transfer3, SWT.NULL);
		if(((float)ds.getShareRatio()/100) < 0)
			shareRatio.setText("0.00");
		else
			shareRatio.setText(df.format((float)ds.getShareRatio()/1000));

		//Hash Fails
		Label hashFails_Label = new Label(transfer3, SWT.NULL);
		hashFails_Label.setText("Amount Discarded: ");

		hashFails = new Label(transfer3, SWT.NULL);
		hashFails.setText(DisplayFormatters.formatByteCountToBase10KBEtc(download.getDiscarded()));

		//Upload Rate
		Label uploadRate_Label = new Label(transfer3, SWT.NULL);
		uploadRate_Label.setText("Upload Rate: ");

		uploadRate = new Label(transfer3, SWT.NULL);
		int rate = download.getUploadRateLimitBytesPerSecond();
		if(rate <= 0)
			uploadRate.setText("Maximum");
		else
			uploadRate.setText(DisplayFormatters.formatByteCountToBase10KBEtc(rate));


		//------------------------------------------------------------\\
		//--------------------- Information ---------------------------\\
		Group info = new Group(parent,SWT.NONE);
		info.setText("Information");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		info.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		info.setLayoutData(gridData);


		info1 = new Composite(info, SWT.NULL);
		gridLayout = new GridLayout();
		info1.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		info1.setLayoutData(gridData);

		// Name
		Label name_Label = new Label(info1,SWT.NONE);
		name_Label.setText("Name:  ");

		Label name = new Label(info1,SWT.NONE);
		name.setText(download.getName());

		//Save in
		Label saveIn_Label = new Label(info1,SWT.NULL);
		saveIn_Label.setText("Save In: ");

		saveIn = new Label(info1, SWT.NULL);
		saveIn.setText(das.getSaveDir());

		// # of Pieces
		Label numPieces_Label = new Label(info1, SWT.NULL);
		numPieces_Label.setText("# of Pieces: ");

		numPieces = new Label(info1, SWT.NULL);
		numPieces.setText(Long.toString(das.getPieceCount()));

		//Tracker URL
		Label trackerURL_Label = new Label(info1, SWT.NULL);
		trackerURL_Label.setText("Tracker URL: ");

		trackerURL = new Label(info1, SWT.NULL);
		trackerURL.setText(das.getTrackerUrl());
		trackerURL.setToolTipText("Click to open main tracker page in browser");
		trackerURL.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		trackerURL.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		trackerURL.addListener(SWT.MouseDown, new Listener(){
			public void handleEvent(Event arg0) {
				String url = trackerURL.getText();
				if(url.endsWith("/announce"))
					Program.launch(url.substring(0,url.indexOf("/announce") ));
				else
					Program.launch(url);

			}

		});

		// Tracker Status
		Label trackerStatus_Label = new Label(info1, SWT.NULL);
		trackerStatus_Label.setText("Tracker Status: ");

		trackerStatus = new Label(info1, SWT.NULL);
		trackerStatus.setText(ds.getTrackerStatus());

		//Tracker Comment
		Label trackerComment_Label = new Label(info1, SWT.NULL);
		trackerComment_Label.setText("Comment: ");

		trackerComment = new Label(info1, SWT.NULL);
		trackerComment.setText(das.getComment());


		//----------Info2-----------
		info2 = new Composite(info, SWT.NULL);
		gridLayout = new GridLayout();
		info2.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		info2.setLayoutData(gridData);


		//Total Size
		Label totalSize_Label = new Label(info2, SWT.NULL);
		totalSize_Label.setText("Total Size: ");

		Label totalSize = new Label(info2, SWT.NULL);
		totalSize.setText(DisplayFormatters.formatByteCountToBase10KBEtc(download.getSize()));


		//HASH
		Label hash_label = new Label(info2, SWT.NULL);
		hash_label.setText("Hash: ");

		Label hash = new Label(info2, SWT.NULL);
		byte[] hashByte = EncodingUtil.decode(download.getHash());
		hash.setText(EncodingUtil.nicePrint(hashByte,false));

		//Piece Size
		Label pieceSize_Label = new Label(info2, SWT.NULL);
		pieceSize_Label.setText("Piece Size: ");

		pieceSize = new Label(info2, SWT.NULL);
		pieceSize.setText(DisplayFormatters.formatByteCountToBase10KBEtc(das.getPieceSize()));

		//Torrent Created On
		Label createdOn_Label = new Label(info2, SWT.NULL);
		createdOn_Label.setText("Created On: ");

		createdOn = new Label(info2, SWT.NULL);
		createdOn.setText(das.getCreatedOn());


		// Last Scrape
		Label lastScrape_label = new Label(info2, SWT.NULL);
		lastScrape_label.setText("Last Scrape:");

		lastScrape = new Label(info2, SWT.NULL);
		lastScrape.setText(DisplayFormatters.formatDate(download.getLastScrapeTime()));

		//Next Scrape
		Label nextScrape_label = new Label(info2, SWT.NULL);
		nextScrape_label.setText("Next Scrape:");

		nextScrape = new Label(info2, SWT.NULL);
		nextScrape.setText(DisplayFormatters.formatDate(download.getNextScrapeTime()));


		//Buttons to update info from tracker
		scrape = new Button(info2, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		scrape.setLayoutData(gridData);
		scrape.setText("Scrape Tracker");
		scrape.setToolTipText("Scraping the tracker will update the torrent information (seeds, leechers, etc.)");
		scrape.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				download.requestScrape();
			}
		});


		announce = new Button(info2, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		announce.setLayoutData(gridData);
		announce.setText("Request Announce");
		announce.setToolTipText("Request an announce from the tracker.  This will update the peers and peer info.");
		announce.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				download.requestAnnounce();
			}
		});
		//disable it until an update occurs
		announce.setEnabled(false);

		detailsTab.setControl(parent);
	}



	private void update_elements(){
		if(RCMain.getRCMain().getDisplay() == null || RCMain.getRCMain().getDisplay().isDisposed()) return;
		RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

			public void run() {
				//----Put all update elements here-----\\

				//composite 1
				timeElapsed.setText(ds.getElapsedTime());
				downloaded.setText(DisplayFormatters.formatByteCountToBase10KBEtc(download.getStats().getDownloaded()));
				uploaded.setText(DisplayFormatters.formatByteCountToBase10KBEtc(download.getStats().getUploaded()));
				if(download.getTotalSeeds() < 0)
					seeds.setText(download.getSeeds() + " connected ( 0 in swarm )");
				else
					seeds.setText(download.getSeeds() + " connected ( " + download.getTotalSeeds() + " in swarm )");
				swarmSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getTotalAverage()) + "/s");
				transfer1.layout();

				//composite 2
				remaining.setText(ds.getETA());
				downloadSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getDownloadAverage()) + "/s");
				uploadSpeed.setText(DisplayFormatters.formatByteCountToBase10KBEtc(ds.getUploadAverage()) + "/s");
				if(download.getTotalLeecher() < 0)
					peers.setText(download.getLeecher() + " connected ( 0 in swarm )");
				else
					peers.setText(download.getLeecher() + " connected ( " + download.getTotalLeecher() + " in swarm )");
				if(ds.getAvailability() < 0)
					averageCompletion.setText("");
				else
					averageCompletion.setText(df.format(ds.getAvailability()));
				transfer2.layout();


				//composite 3
				if(((float)ds.getShareRatio()/100) < 0)
					shareRatio.setText("0.00");
				else
					shareRatio.setText(df.format((float)ds.getShareRatio()/1000));
				hashFails.setText(DisplayFormatters.formatByteCountToBase10KBEtc(download.getDiscarded()));
				int rate = download.getUploadRateLimitBytesPerSecond();
				if(rate <= 0)
					uploadRate.setText("Maximum");
				else
					uploadRate.setText(DisplayFormatters.formatByteCountToBase10KBEtc(rate));
				transfer3.layout();

				//info1
				trackerURL.setText(das.getTrackerUrl());
				trackerStatus.setText(ds.getTrackerStatus());
				saveIn.setText(das.getSaveDir());
				numPieces.setText(Long.toString(das.getPieceCount()));
				trackerComment.setText(das.getComment());
				pieceSize.setText(DisplayFormatters.formatByteCountToBase10KBEtc(das.getPieceSize()));
				createdOn.setText(das.getCreatedOn());
				lastScrape.setText(DisplayFormatters.formatDate(download.getLastScrapeTime()));
				nextScrape.setText(DisplayFormatters.formatDate(download.getNextScrapeTime()));

				//check for state and enable announce button if torrent is 'on'
				if(download.getState() == Download.ST_DOWNLOADING
						|| download.getState() == Download.ST_SEEDING){
					announce.setEnabled(true);
				}else
					announce.setEnabled(false);


				info1.layout();
				info2.layout();
			}

		});
	}

	private void loadSubTab2(CTabFolder tabFolder){

		final CTabItem filesTab = new CTabItem(tabFolder, SWT.NULL);
		filesTab.setText("Files");


		final Composite parent = new Composite(tabFolder, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);


		//Table for files present in the download
		filesTable = new Table(parent,SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;


		filesTable.setLayoutData(gridData);
		filesTable.setHeaderVisible(true);

		TableColumn name = new TableColumn(filesTable,SWT.LEFT);
		name.setText("Name");
		name.setWidth(350);

		TableColumn size = new TableColumn(filesTable,SWT.RIGHT);
		size.setText("Size");
		size.setWidth(100);

		TableColumn done = new TableColumn(filesTable,SWT.RIGHT);
		done.setText("Done");
		done.setWidth(50);

		TableColumn percent = new TableColumn(filesTable,SWT.RIGHT);
		percent.setText("%");
		percent.setWidth(75);


		TableColumn numPieces = new TableColumn(filesTable,SWT.RIGHT);
		numPieces.setText("# of Pieces");
		numPieces.pack();

		TableColumn priority = new TableColumn(filesTable, SWT.LEFT);
		priority.setText("Priority");
		priority.pack();

		filesTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event e) {
				//pull the item
				TableItem item = (TableItem)e.item;

				//get the index of the item
				int index = filesTable.indexOf(item);

				try {
					item.setText(0,dfm_files[index].getName());
					item.setText(1,DisplayFormatters.formatByteCountToBase10KBEtc(dfm_files[index].getLength()));
					item.setText(2,DisplayFormatters.formatByteCountToBase10KBEtc(dfm_files[index].getDownloaded()));
					item.setText(3,(df.format((double)dfm_files[index].getDownloaded()/(double)dfm_files[index].getLength() * 100)) + "%");
					item.setText(4,Integer.toString(dfm_files[index].getNumPieces()));
					String priority_status;
					if(dfm_files[index].getPriority())
						priority_status="High";
					else if(dfm_files[index].getSkipped())
						priority_status="Do Not Download";
					else if(dfm_files[index].getDeleted())
						priority_status="Deleted";
					else
						priority_status="Normal";
					item.setText(5, priority_status);

//                  gray if needed
					if(index%2!=0){
						item.setBackground(ColorUtilities.getBackgroundColor());
					}
				}catch(Exception e1){

				}
			}
		});


		//---menu for filesTable---\\
		final Menu menu = new Menu(filesTable);

		final MenuItem setPriority = new MenuItem(menu, SWT.CASCADE);
		setPriority.setText("Set Priority");
		Menu submenu = new Menu(setPriority);

		final MenuItem priority_high = new MenuItem(submenu, SWT.PUSH);
		priority_high.setText("High");
		priority_high.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int[] indices = filesTable.getSelectionIndices();
				if(indices.length <= 0) return;
				else{
					for(int index:indices){
						if(dfm_files[index].getSkipped())
							dfm_files[index].setSkipped(false);
						dfm_files[index].setPriority(true);
					}
				}
			}
		});

		final MenuItem priority_normal = new MenuItem(submenu, SWT.PUSH);
		priority_normal.setText("Normal");
		priority_normal.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int[] indices = filesTable.getSelectionIndices();
				if(indices.length <= 0) return;
				else{
					for(int index:indices){
						if(dfm_files[index].getSkipped())
							dfm_files[index].setSkipped(false);
						dfm_files[index].setPriority(false);
					}
				}
			}
		});

		final MenuItem do_not_download = new MenuItem(submenu, SWT.PUSH);
		do_not_download.setText("Do Not Download");
		do_not_download.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int[] indices = filesTable.getSelectionIndices();
				if(indices.length <= 0) return;
				else{
					for(int index:indices){
						dfm_files[index].setSkipped(true);
					}
				}
			}
		});

		final MenuItem delete = new MenuItem(submenu, SWT.PUSH);
		delete.setText("Delete");
		delete.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int[] indices = filesTable.getSelectionIndices();
				if(indices.length <= 0) return;
				else{
					for(int index:indices){
						if(!dfm_files[index].getDeleted())
							dfm_files[index].setDeleted(true);
					}
				}
			}
		});

		setPriority.setMenu(submenu);
		filesTable.setMenu(menu);




		//-----end stuff---\\
		redrawTable();

		filesTab.setControl(parent);
	}




	/**
	 * Redraws the userTable.. since it is virtual, we need to repopulate it
	 * each time the user array is modified
	 *
	 */
	public void redrawTable(){
		// Reset the data so that the SWT.Virtual picks up the array
		RCMain.getRCMain().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (filesTable == null || filesTable.isDisposed())
					return;

				try{
					filesTable.setItemCount(dfm.getFiles().length);
				}catch (Exception e){
					filesTable.setItemCount(0);
				}

				filesTable.clearAll();



			}
		});
	}
}//EOF
