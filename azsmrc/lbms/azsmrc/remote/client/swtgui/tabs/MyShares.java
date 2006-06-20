package lbms.azsmrc.remote.client.swtgui.tabs;

import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.TrackerListener;
import lbms.azsmrc.remote.client.TrackerTorrent;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.util.DisplayFormatters;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class MyShares {

	//  I18N prefix
	public static final String PFX = "tab.myshares.";

	private TrackerTorrent[] torrents;

	//SWT Items
	private ToolItem addTorrent, refresh, stop, start, remove;
	private Table table;

	private MyShares(CTabFolder parentTab){
		final CTabItem mysharesTab = new CTabItem(parentTab, SWT.CLOSE);
		mysharesTab.setText(I18N.translate(PFX + "tab.text"));
		RCMain.getRCMain().getClient().getTracker().update();
		torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();

		final ClientUpdateListener cul = new ClientUpdateListener(){

			public void update(long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_TRACKER) != 0){
					torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
					RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

						public void run() {
							if(table != null && !table.isDisposed()){
								table.clearAll();
								table.setItemCount(torrents.length);
							}
						}

					});

				}
			}

		};

		RCMain.getRCMain().getClient().addClientUpdateListener(cul);


		final TrackerListener tl = new TrackerListener(){

			public void torrentAdded(TrackerTorrent torrent) {
				torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
				RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

					public void run() {
						if(table != null && !table.isDisposed()){
							table.clearAll();
							table.setItemCount(torrents.length);
						}
					}

				});
			}

			public void torrentChanged(TrackerTorrent torrent) {
				// NOT USED YET

			}

			public void torrentRemoved(TrackerTorrent torrent) {
				torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
				RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

					public void run() {
						if(table != null && !table.isDisposed()){
							table.clearAll();
							table.setItemCount(torrents.length);
						}
					}

				});
			}

		};

		RCMain.getRCMain().getClient().getTracker().addListener(tl);

		//Main parent Comp
		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		parent.setLayoutData(gridData);

		//Top Toolbar
		ToolBar tb = new ToolBar(parent,SWT.FLAT | SWT.HORIZONTAL);

		//-----toolitems

		//Add Torrent toolitem
		addTorrent = new ToolItem(tb, SWT.PUSH);
		addTorrent.setImage(ImageRepository.getImage("open_by_file"));
		addTorrent.setToolTipText(I18N.translate(PFX + "toolbar.addTorrent.toolTip") );
		addTorrent.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub

			}
		});

		//Separator
		new ToolItem(tb, SWT.SEPARATOR);

		refresh = new ToolItem(tb, SWT.PUSH);
		refresh.setImage(ImageRepository.getImage("refresh"));
		refresh.setToolTipText(I18N.translate(PFX + "toolbar.refresh.toolTip") );
		refresh.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				RCMain.getRCMain().getClient().getTracker().update();
				torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
				table.setItemCount(torrents.length);
			}
		});

		//Separator
		new ToolItem(tb, SWT.SEPARATOR);

		//Start
		start = new ToolItem(tb, SWT.PUSH);
		start.setImage(ImageRepository.getImage("toolbar_queue"));
		start.setToolTipText(I18N.translate(PFX + "toolbar.start.toolTip"));
		start.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getSelection();
				for(TableItem item:items){
					TrackerTorrent tt = (TrackerTorrent) item.getData("tt");
					if(tt.getStatus() == TrackerTorrent.TS_STOPPED)
						tt.start();
				}
				RCMain.getRCMain().getClient().getTracker().update();
				torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
				table.setItemCount(torrents.length);
			}
		});


		//Stop
		stop = new ToolItem(tb, SWT.PUSH);
		stop.setImage(ImageRepository.getImage("toolbar_stop"));
		stop.setToolTipText(I18N.translate(PFX + "toolbar.stop.toolTip"));
		stop.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getSelection();
				for(TableItem item:items){
					TrackerTorrent tt = (TrackerTorrent) item.getData("tt");
					if(tt.getStatus() != TrackerTorrent.TS_STOPPED)
						tt.stop();
				}
				RCMain.getRCMain().getClient().getTracker().update();
				torrents = RCMain.getRCMain().getClient().getTracker().getTorrents();
				table.setItemCount(torrents.length);
			}
		});

		//Remove
		remove = new ToolItem(tb, SWT.PUSH);
		remove.setImage(ImageRepository.getImage("toolbar_remove"));
		remove.setToolTipText(I18N.translate(PFX + "toolbar.remove.toolTip"));
		remove.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getSelection();
				for(TableItem item:items){
					TrackerTorrent tt = (TrackerTorrent) item.getData("tt");
					if(tt.canBeRemoved()){
						tt.remove();
					}else{
						try{
							tt.stop();
							tt.remove();
						}catch(Exception e){}  // just a try.. no need to catch
					}
				}
			}
		});

		//turn off the buttons until a selection event
		setButtons(false);

		//-----end of toolitems


		//Main Table
		table = new Table(parent, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		table.setHeaderVisible(true);

		TableColumn number = new TableColumn (table, SWT.LEFT);
		number.setText("#");
		number.setWidth(20);

		TableColumn name = new TableColumn (table, SWT.LEFT);
		name.setText(I18N.translate(PFX + "tableColumn.name.text"));
		name.setWidth(200);

		TableColumn seeds = new TableColumn (table, SWT.RIGHT);
		seeds.setText(I18N.translate(PFX + "tableColumn.seeds.text"));
		seeds.pack();

		TableColumn leechers = new TableColumn (table, SWT.RIGHT);
		leechers.setText(I18N.translate(PFX + "tableColumn.leechers.text"));
		leechers.pack();

		TableColumn completed = new TableColumn (table, SWT.RIGHT);
		completed.setText(I18N.translate(PFX + "tableColumn.completed.text"));
		completed.pack();

		TableColumn status = new TableColumn (table, SWT.RIGHT);
		status.setText(I18N.translate(PFX + "tableColumn.status.text"));
		status.pack();

		TableColumn bytesIn = new TableColumn (table, SWT.RIGHT);
		bytesIn.setText(I18N.translate(PFX + "tableColumn.bytesIn.text"));
		bytesIn.pack();

		TableColumn bytesOut = new TableColumn (table, SWT.RIGHT);
		bytesOut.setText(I18N.translate(PFX + "tableColumn.bytesOut.text"));
		bytesOut.pack();

		TableColumn totalDownloaded = new TableColumn (table, SWT.RIGHT);
		totalDownloaded.setText(I18N.translate(PFX + "tableColumn.totalDownloaded.text"));
		totalDownloaded.pack();

		TableColumn totalUploaded = new TableColumn (table, SWT.RIGHT);
		totalUploaded.setText(I18N.translate(PFX + "tableColumn.totalUploaded.text"));
		totalUploaded.pack();

		TableColumn totalLeft = new TableColumn (table, SWT.RIGHT);
		totalLeft.setText(I18N.translate(PFX + "tableColumn.totalLeft.text"));
		totalLeft.pack();


		//setData listener for drawing on the table
		table.addListener(SWT.SetData, new Listener(){
			public void handleEvent(Event e) {
				TableItem item = (TableItem) e.item;
				if(item == null) return;
				int index = table.indexOf (item);
				TrackerTorrent tt = (TrackerTorrent) torrents[index];
				if(tt == null) return;
				//set all the data!
				item.setText(0, String.valueOf(index + 1));
				item.setText(1,tt.getName());
				item.setText(2,String.valueOf(tt.getSeedCount()));
				item.setText(3,String.valueOf(tt.getLeecherCount()));
				item.setText(4,String.valueOf(tt.getCompletedCount()));
				item.setText(5,String.valueOf(statusToString(tt.getStatus())));
				item.setText(6,DisplayFormatters.formatByteCountToBase10KBEtcPerSec(tt.getAverageBytesIn()));
				item.setText(7,DisplayFormatters.formatByteCountToBase10KBEtcPerSec(tt.getAverageBytesOut()));
				item.setText(8,DisplayFormatters.formatByteCountToBase10KBEtc(tt.getTotalDownloaded()));
				item.setText(9,DisplayFormatters.formatByteCountToBase10KBEtc(tt.getTotalUploaded()));
				item.setText(10,DisplayFormatters.formatByteCountToBase10KBEtc(tt.getTotalLeft()));
				item.setData("tt", tt);
				if(index%2!=0){
					item.setBackground(ColorUtilities.getBackgroundColor());
				}
			}
		});

		table.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getSelection();
				if(items.length == 0) return;
				setButtons(true);

			}
		});


		table.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if(e.button == 1) {
					if(table.getItem(new Point(e.x,e.y))==null){
						table.deselectAll();
						setButtons(false);
					}
				}
			}
		});

		table.setItemCount(torrents.length);

		mysharesTab.setControl(parent);
		parentTab.setSelection(mysharesTab);
	}


	private void setButtons(final boolean bOn){
		RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){
			public void run() {
				if(stop != null || !stop.isDisposed())
					stop.setEnabled(bOn);
				if(start != null || !start.isDisposed())
					start.setEnabled(bOn);
				if(remove != null || !remove.isDisposed())
					remove.setEnabled(bOn);
			}
		});
	}

	private String statusToString(int status){
		switch (status){
		case TrackerTorrent.TS_STARTED:
			return I18N.translate(PFX + "status.TS_STARTED");
		case TrackerTorrent.TS_STOPPED:
			return I18N.translate(PFX + "status.TS_STOPPED");
		case TrackerTorrent.TS_PUBLISHED:
			return I18N.translate(PFX + "status.TS_PUBLISHED");
		}
		return I18N.translate("gloabal.error");
	}

	//Static public open ... call this to open
	public static void open(final CTabFolder parentTab){
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.asyncExec(new Runnable(){
			public void run() {
				CTabItem[] tabs = parentTab.getItems();
				for(CTabItem tab:tabs){
					if(tab.getText().equalsIgnoreCase(I18N.translate(PFX + "tab.text"))){
						parentTab.setSelection(tab);
						return;
					}
				}
				new MyShares(parentTab);

			}

		});
	}

}
