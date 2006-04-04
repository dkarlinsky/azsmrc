package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;

import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
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

import org.eclipse.swt.SWT;
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
	private HashMap<String,AddTorrentContainer> map = new HashMap<String,AddTorrentContainer>();


	//SWT components
	private Table torrentTable;
	private Button ckSel,ckAll;
	private CTabFolder tabFolder;
	private Display display;


	public ScrapeDialog(Display mainDisplay){
		//set the display
		display = mainDisplay;
		
		//Shell
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setText("Scrape a Torrent File");



		tabFolder = new CTabFolder(shell, SWT.FLAT);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gd);
		tabFolder.setLayout(new GridLayout(1,false));
		tabFolder.setSimple(false);


		CTabItem mainTab = new CTabItem(tabFolder, SWT.NULL);
		mainTab.setText("Main");

		//Main Composite on shell
		Composite parent = new Composite(tabFolder,SWT.NULL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		parent.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		parent.setLayout(gl);


		//Load Torrent button
		Button loadTorrent = new Button(parent,SWT.PUSH);
		loadTorrent.setText("Load Torrent");
		loadTorrent.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.torrent", "*.*" });
				dialog.setText("Choose Torrent File to Load");
				if (lastDir != null) {
					dialog.setFilterPath(lastDir);
				}
				String choosen_file = dialog.open();

				if (choosen_file != null)
					try {
						//Make the file
						File test = new File(choosen_file);
						lastDir = test.getParent();


						//put the torrent in the container and add to the map
						AddTorrentContainer atc = new AddTorrentContainer(test);
						if(map.containsKey(atc.getName())){
							CTabItem[] items = tabFolder.getItems();
							for(CTabItem item:items){
								if(item.getText().equalsIgnoreCase(atc.getName())){
									tabFolder.setSelection(item);
									return;
								}
							}
							torrentTabOpen(tabFolder,map.get(atc.getName()));
							return;
						}
						map.put(atc.getName(), atc);

						//Add it to the table
						TableItem item = new TableItem(torrentTable,SWT.NULL);
						item.setText(0,atc.getName());

						//open its tab
						torrentTabOpen(tabFolder,map.get(atc.getName()));

					}catch(TOTorrentException e){
						MessageBox messageBox = new MessageBox(shell,SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText("Invalid Torrent");
						messageBox.setMessage("Error loading torrent.  Please check the file and try again.");
						messageBox.open();
						return;
					}catch(Exception e){
						e.printStackTrace();
					}

			}

		});


		//Group for urlTable and buttons
		Group ttGroup = new Group(parent,SWT.NULL);
		ttGroup.setText("Loaded Torrents (Double click torrent to view details)");
		ttGroup.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 100;
		ttGroup.setLayoutData(gd);

		//urlTable		
		torrentTable = new Table(ttGroup,SWT.BORDER | SWT.SINGLE |  SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);		
		torrentTable.setLayoutData(gd);
		torrentTable.setHeaderVisible(true);


		TableColumn ttName = new TableColumn(torrentTable,SWT.NULL);
		ttName.setText("Torrent Name");
		ttName.setWidth(600);


		//doubleclick listener for the table
		torrentTable.addListener(SWT.MouseDoubleClick, new Listener(){
			public void handleEvent(Event arg0) {		
				TableItem[] items = torrentTable.getSelection();
				if(items.length > 1) return;
				CTabItem[] tabs = tabFolder.getItems();
				for(CTabItem item:tabs){
					if(item.getText().equalsIgnoreCase(items[0].getText(0))){
						tabFolder.setSelection(item);
						return;
					}
				}
				torrentTabOpen(tabFolder,map.get(items[0].getText(0)));
			}

		});


		//buttons under the urlTable
		Composite utButtonComp = new Composite(ttGroup,SWT.NULL);
		utButtonComp.setLayout(new GridLayout(3,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		utButtonComp.setLayoutData(gd);

		//Check Selected
		ckSel = new Button(utButtonComp,SWT.PUSH);
		ckSel.setText("Scrape Selected");
		ckSel.setEnabled(false);
		ckSel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {


			}			
		});


		//Check Selected
		ckAll = new Button(utButtonComp,SWT.PUSH);
		ckAll.setText("Scrape All");
		ckAll.setEnabled(false);
		gd = new GridData(GridData.END);
		ckAll.setLayoutData(gd);
		ckAll.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {


			}			
		});		

		mainTab.setControl(parent);

		//Center and Open Shell
		GUI_Utilities.centerShellandOpen(shell);
	}


	private void torrentTabOpen(CTabFolder tabFolder, AddTorrentContainer atc){

		CTabItem tab = new CTabItem(tabFolder,SWT.CLOSE);
		final Scraper scraper = new Scraper(atc.getTorrent());

		try {			
			tab.setText(atc.getName());
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}

		Composite parent = new Composite(tabFolder,SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		parent.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		parent.setLayout(gl);


		final Combo combo = new Combo(parent,SWT.DROP_DOWN | SWT.READ_ONLY);


		//Pull the URL from the torrent and put it in the combo
		combo.add(atc.getTorrent().getAnnounceURL().toString());
		combo.select(0);

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
					combo.add(url.toString());                           		
				}                        		
			}
		}


		//button for Scrape
		Button scrape = new Button(parent,SWT.PUSH);
		scrape.setText("Scrape");


		//Label for status
		final Label status = new Label(parent,SWT.NULL);
		status.setText("Status:  Not Scraped Yet");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		status.setLayoutData(gd);


		//ProgressBar
		final ProgressBar pb = new ProgressBar(parent,SWT.SMOOTH | SWT.HORIZONTAL | SWT.INDETERMINATE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		pb.setLayoutData(gd);
		pb.setVisible(false);

		
		//----STATS
		final Group gStats = new Group(parent,SWT.NULL);
		gStats.setText("Scrape Results");
		gStats.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan = 2;
		gStats.setLayoutData(gd);

		
		Label seedsL = new Label(gStats,SWT.NULL);
		seedsL.setText("Seeds: ");
		
		final Label seeds = new Label(gStats,SWT.NULL);
		seeds.setText("Not Scraped");

		Label leechersL = new Label(gStats,SWT.NULL);
		leechersL.setText("Leechers: ");
		
		final Label leechers = new Label(gStats, SWT.NULL);
		leechers.setText("Not Scraped");

		Label downloadedL = new Label(gStats,SWT.NULL);
		downloadedL.setText("Downloadeds: ");
				
		final Label downloaded = new Label(gStats,SWT.NULL);
		downloaded.setText("Not Scraped");

				
		Label srURLL = new Label(gStats,SWT.NULL);
		srURLL.setText("Scrape URL: ");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURLL.setLayoutData(gd);
		
		final Label srURL = new Label(gStats,SWT.NULL);
		srURL.setText("Not Scraped");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		srURL.setLayoutData(gd);
		
		
		
		//----FILES
		
		Group gFiles = new Group(parent,SWT.NULL);
		gFiles.setText("Torrent Details");
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
		sizeL.setText("Size: ");
		
		Label size = new Label(cLeft,SWT.NULL);
		size.setText(DisplayFormatters.formatByteCountToBase10KBEtc(atc.getTorrent().getSize()));
		
		//Number of Pieces
		Label numPiecesL = new Label(cLeft,SWT.NULL);
		numPiecesL.setText("Pieces: ");
		
		Label numPieces = new Label(cLeft,SWT.NULL);
		numPieces.setText(String.valueOf(atc.getTorrent().getNumberOfPieces()));
		
		//Piece Size
		Label pieceSizeL = new Label(cLeft,SWT.NULL);
		pieceSizeL.setText("Piece Size: ");
		
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
		dateL.setText("Created On: ");
		
		Label date = new Label(cRight,SWT.NULL);
		date.setText(DisplayFormatters.formatDate(atc.getTorrent().getCreationDate()));
		
		//Created by
		Label byL = new Label(cRight,SWT.NULL);
		byL.setText("Created By: ");
		
		Label by = new Label(cRight,SWT.NULL);
		by.setText(EncodingUtil.nicePrint(atc.getTorrent().getCreatedBy(),true));
		
		//Is Private
		Label privL = new Label(cRight,SWT.NULL);
		privL.setText("Private: ");
		
		Label priv = new Label(cRight,SWT.NULL);
		if(atc.getTorrent().getPrivate())
			priv.setText("Yes");
		else
			priv.setText("No");
		
		Composite cBottom = new Composite(gFiles,SWT.NULL);
		cBottom.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace= true;		
		gd.horizontalSpan = 2;
		cBottom.setLayoutData(gd);
		
		//URL
		Label tURL = new Label(cBottom,SWT.NULL);
		tURL.setText("Announce URL: " + atc.getTorrent().getAnnounceURL());
		
		//Hash
		Label hash = new Label(cBottom,SWT.NULL);		
		hash.setLayoutData(gd);
		try {			
			hash.setText("Hash: " + EncodingUtil.nicePrint(atc.getTorrent().getHash(),false));
		} catch (TOTorrentException e) {
			hash.setText("Hash: Unable to properly decode hash");			
		}
        
		//Comments
		Label commentsL = new Label(cBottom,SWT.NULL);
		commentsL.setText("Comments: ");
		
		Label comments = new Label(cBottom,SWT.NULL);
		try{
			comments.setText(new String(atc.getTorrent().getComment()));
		}catch(Exception e){
			
		}
		
		
		
		//Table for files
		Table filesTable = new Table(gFiles,SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		filesTable.setLayoutData(gd);
		filesTable.setHeaderVisible(true);
		
		TableColumn ftName = new TableColumn(filesTable,SWT.NULL);
		ftName.setText("File Name");
		ftName.setWidth(450);
		
		TableColumn ftSize = new TableColumn(filesTable,SWT.NULL);
		ftSize.setText("Size");
		ftSize.setWidth(100);
		
		TOTorrentFile[] files = atc.getFiles();
        int[] properties = atc.getFileProperties();
        for (int i = 0; i < files.length; i++) {
            final TableItem detailItem = new TableItem(filesTable,
                    SWT.NULL);
            String name = files[i].getRelativePath();

            if (name == null || name.length() == 0 || name.equalsIgnoreCase("")) {
                name = "Error Decoding Name";
            }

            if (properties != null && properties[i] == 1) {
                detailItem.setChecked(true);
            }
            detailItem.setText(0, name);
            detailItem.setText(1, DisplayFormatters
                    .formatByteCountToBase10KBEtc(files[i].getLength()));

        }


		//Listener for the Scrape button
		scrape.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				pb.setVisible(true);	
				final String urlToScrape = combo.getItem(combo.getSelectionIndex());
				scraper.addListener(new ScrapeListener(){

					public void scrapeFailed(final String reason) {
						display.asyncExec(new Runnable(){

							public void run() {
								pb.setVisible(false);
								status.setText("Status:  Failed - " + reason);								
							}							
						});
						
					}

					public void scrapeFinished(final ScrapeResult sr) {
						display.asyncExec(new Runnable(){
							public void run() {
								pb.setVisible(false);
								status.setText("Status:  Success");
								gStats.setText("Scrape Results for " + combo.getItem(combo.getSelectionIndex()));
								seeds.setText(String.valueOf(sr.getSeeds()));
								leechers.setText(String.valueOf(sr.getLeechers()));
								downloaded.setText(String.valueOf(sr.getDownloaded()));
								srURL.setText(sr.getScrapeUrl());
								srURL.setToolTipText(sr.getScrapeUrl());
							}
							
						});
						
						
					}

				});
				
				Thread scrapeThread = new Thread(new Runnable(){
					public void run() {
						scraper.scrape(urlToScrape);						
					}
				});
				scrapeThread.run();
			}        	
		});



		//set the tab to the parent
		tab.setControl(parent);
		tabFolder.setSelection(tab);
	}
}
