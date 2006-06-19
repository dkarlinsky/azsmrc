package lbms.azsmrc.remote.client.swtgui.tabs;

import lbms.azsmrc.remote.client.TrackerListener;
import lbms.azsmrc.remote.client.TrackerTorrent;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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

		TrackerListener tl = new TrackerListener(){

			public void torrentAdded(TrackerTorrent torrent) {
				// TODO Auto-generated method stub
				System.out.println(torrent.getName() + " added!");
			}

			public void torrentChanged(TrackerTorrent torrent) {
				// TODO Auto-generated method stub

			}

			public void torrentRemoved(TrackerTorrent torrent) {
				// TODO Auto-generated method stub

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
				// TODO Auto-generated method stub

			}
		});


		//Stop
		stop = new ToolItem(tb, SWT.PUSH);
		stop.setImage(ImageRepository.getImage("toolbar_stop"));
		stop.setToolTipText(I18N.translate(PFX + "toolbar.stop.toolTip"));
		stop.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub

			}
		});

		//Remove
		remove = new ToolItem(tb, SWT.PUSH);
		remove.setImage(ImageRepository.getImage("toolbar_remove"));
		remove.setToolTipText(I18N.translate(PFX + "toolbar.remove.toolTip"));
		remove.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub

			}
		});

		//-----end of toolitems


		//Main Table
		table = new Table(parent, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		table.setHeaderVisible(true);

		TableColumn number = new TableColumn (table, SWT.NULL);
		number.setText("#");
		number.setWidth(20);

		TableColumn name = new TableColumn (table, SWT.NULL);
		name.setText(I18N.translate(PFX + "tableColumn.name.text"));
		name.setWidth(200);

		TableColumn seeds = new TableColumn (table, SWT.NULL);
		seeds.setText(I18N.translate(PFX + "tableColumn.seeds.text"));
		seeds.pack();

		TableColumn leechers = new TableColumn (table, SWT.NULL);
		leechers.setText(I18N.translate(PFX + "tableColumn.leechers.text"));
		leechers.pack();

		TableColumn completed = new TableColumn (table, SWT.NULL);
		completed.setText(I18N.translate(PFX + "tableColumn.completed.text"));
		completed.pack();

		TableColumn status = new TableColumn (table, SWT.NULL);
		status.setText(I18N.translate(PFX + "tableColumn.status.text"));
		status.pack();

		TableColumn bytesIn = new TableColumn (table, SWT.NULL);
		bytesIn.setText(I18N.translate(PFX + "tableColumn.bytesIn.text"));
		bytesIn.pack();

		TableColumn bytesOut = new TableColumn (table, SWT.NULL);
		bytesOut.setText(I18N.translate(PFX + "tableColumn.bytesOut.text"));
		bytesOut.pack();

		TableColumn totalDownloaded = new TableColumn (table, SWT.NULL);
		totalDownloaded.setText(I18N.translate(PFX + "tableColumn.totalDownloaded.text"));
		totalDownloaded.pack();

		TableColumn totalUploaded = new TableColumn (table, SWT.NULL);
		totalUploaded.setText(I18N.translate(PFX + "tableColumn.totalUploaded.text"));
		totalUploaded.pack();

		TableColumn totalLeft = new TableColumn (table, SWT.NULL);
		totalLeft.setText(I18N.translate(PFX + "tableColumn.totalLeft.text"));
		totalLeft.pack();


		//setData listener for drawing on the table
		table.addListener(SWT.SetData, new Listener(){
			public void handleEvent(Event e) {
				TableItem item = (TableItem) e.item;
				int index = table.indexOf (item);

				//set all the data!
				item.setText(0, String.valueOf(index));

				TrackerTorrent tt = torrents[index];
				item.setText(1,tt.getName());
			}
		});

		table.setItemCount(torrents.length);

		mysharesTab.setControl(parent);
		parentTab.setSelection(mysharesTab);
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
