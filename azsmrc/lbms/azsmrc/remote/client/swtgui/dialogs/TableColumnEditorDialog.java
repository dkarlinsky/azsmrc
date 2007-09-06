package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Choose columns to display, and in what order
 */
public class TableColumnEditorDialog {

	private static TableColumnEditorDialog instance;

	private final Shell shell;
	private final Color blue;
	private final Table table;


	private List<Integer> tableColumns;

	private final boolean bIsDownloadContainer;
	private boolean mousePressed;
	private TableItem selectedItem;
	private Point oldPoint;
	private Image oldImage;

//	I18N prefix
	public static final String PFX = "dialog.tablecolumneditordialog.";

	private static List<Integer> downloadsAllowedColumns = new ArrayList<Integer>();
	private static List<Integer> seedsAllowedColumns = new ArrayList<Integer>();

	static {

		//These are all of the allowed columns for each of the tables

		//download Table allowed columns
		downloadsAllowedColumns.add(RemoteConstants.ST_HEALTH);
		downloadsAllowedColumns.add(RemoteConstants.ST_POSITION);
		downloadsAllowedColumns.add(RemoteConstants.ST_NAME);
		downloadsAllowedColumns.add(RemoteConstants.ST_COMPLETITION);
		downloadsAllowedColumns.add(RemoteConstants.ST_AVAILABILITY);
		downloadsAllowedColumns.add(RemoteConstants.ST_DOWNLOAD_AVG);
		downloadsAllowedColumns.add(RemoteConstants.ST_UPLOAD_AVG);
		downloadsAllowedColumns.add(RemoteConstants.ST_ALL_SEEDS);
		downloadsAllowedColumns.add(RemoteConstants.ST_ALL_LEECHER);
		downloadsAllowedColumns.add(RemoteConstants.ST_UPLOADED);
		downloadsAllowedColumns.add(RemoteConstants.ST_DOWNLOADED);
		downloadsAllowedColumns.add(RemoteConstants.ST_ETA);
		downloadsAllowedColumns.add(RemoteConstants.ST_STATUS);
		downloadsAllowedColumns.add(RemoteConstants.ST_TRACKER);
		downloadsAllowedColumns.add(RemoteConstants.ST_DISCARDED);
		downloadsAllowedColumns.add(RemoteConstants.ST_SHARE);
		downloadsAllowedColumns.add(RemoteConstants.ST_SIZE);
		downloadsAllowedColumns.add(RemoteConstants.ST_ELAPSED_TIME);
		downloadsAllowedColumns.add(RemoteConstants.ST_TOTAL_AVG);
		downloadsAllowedColumns.add(RemoteConstants.ST_LIMIT_UP);
		downloadsAllowedColumns.add(RemoteConstants.ST_LIMIT_DOWN);
		downloadsAllowedColumns.add(RemoteConstants.ST_CATEGORY);

		//Seeds Table allowed columns
		seedsAllowedColumns.add(RemoteConstants.ST_HEALTH);
		seedsAllowedColumns.add(RemoteConstants.ST_POSITION);
		seedsAllowedColumns.add(RemoteConstants.ST_NAME);
		seedsAllowedColumns.add(RemoteConstants.ST_AVAILABILITY);
		seedsAllowedColumns.add(RemoteConstants.ST_UPLOAD_AVG);
		seedsAllowedColumns.add(RemoteConstants.ST_ALL_SEEDS);
		seedsAllowedColumns.add(RemoteConstants.ST_ALL_LEECHER);
		seedsAllowedColumns.add(RemoteConstants.ST_UPLOADED);
		seedsAllowedColumns.add(RemoteConstants.ST_STATUS);
		seedsAllowedColumns.add(RemoteConstants.ST_TRACKER);
		seedsAllowedColumns.add(RemoteConstants.ST_DISCARDED);
		seedsAllowedColumns.add(RemoteConstants.ST_SHARE);
		seedsAllowedColumns.add(RemoteConstants.ST_SIZE);
		seedsAllowedColumns.add(RemoteConstants.ST_ELAPSED_TIME);
		seedsAllowedColumns.add(RemoteConstants.ST_TOTAL_AVG);
		seedsAllowedColumns.add(RemoteConstants.ST_LIMIT_UP);
		seedsAllowedColumns.add(RemoteConstants.ST_CATEGORY);

	}

	/**
	 * Default Constructor
	 * @param bIsDownloadConatiner
	 */
	private TableColumnEditorDialog(final boolean _bIsDownloadConatiner) {
		instance = this;

		bIsDownloadContainer = _bIsDownloadConatiner;
		if(bIsDownloadContainer) {
			tableColumns = DownloadContainer.getColumns();
		} else {
			tableColumns = SeedContainer.getColumns();
		}


		RowData rd;
		final Display display = RCMain.getRCMain().getDisplay();

		blue = new Color(display,0,0,128);

		shell = new Shell(SWT.DIALOG_TRIM | SWT.RESIZE);

		shell.setText(I18N.translate(PFX + "shell.text"));

		GridLayout layout = new GridLayout();
		shell.setLayout (layout);

		if(!lbms.azsmrc.remote.client.Utilities.isOSX) {
			shell.setImage(ImageRepository.getImage("TrayIcon_Blue"));
		}

		GridData gridData;

		Label label = new Label(shell,SWT.NULL);
		label.setText(I18N.translate(PFX + "infolabel.text"));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gridData);

		table = new Table (shell, SWT.VIRTUAL | SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		Composite cButtonArea = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		cButtonArea.setLayoutData(gridData);
		RowLayout rLayout = new RowLayout(SWT.HORIZONTAL);
		rLayout.marginLeft = 0;
		rLayout.marginTop = 0;
		rLayout.marginRight = 0;
		rLayout.marginBottom = 0;
		rLayout.spacing = 5;
		cButtonArea.setLayout (rLayout);

		Button bOk = new Button(cButtonArea,SWT.PUSH);
		bOk.setText(I18N.translate("global.ok"));
		rd = new RowData();
		rd.width = 70;
		bOk.setLayoutData(rd);
		bOk.addListener(SWT.Selection,new Listener() {
			public void handleEvent(Event e) {
				saveAndApply();
				close();
			}
		});

		Button bCancel = new Button(cButtonArea,SWT.PUSH);
		bCancel.setText(I18N.translate("global.cancel"));
		rd = new RowData();
		rd.width = 70;
		bCancel.setLayoutData(rd);
		bCancel.addListener(SWT.Selection,new Listener() {
			public void handleEvent(Event e) {
				close();
			}
		});

		Button bApply = new Button(cButtonArea,SWT.PUSH);
		bApply.setText(I18N.translate(PFX + "apply_button.text"));
		rd = new RowData();
		rd.width = 70;
		bApply.setLayoutData(rd);
		bApply.addListener(SWT.Selection,new Listener() {
			public void handleEvent(Event e) {
				saveAndApply();
			}
		});




		TableColumn column_name = new TableColumn(table, SWT.NONE);
		column_name.setText(I18N.translate(PFX + "table.column.name"));

		TableColumn column_description = new TableColumn(table, SWT.NONE);
		column_description.setText(I18N.translate(PFX + "table.column.description"));


		table.getColumn(0).setWidth(160);
		table.getColumn(1).setWidth(500);

		table.addListener(SWT.Selection,new Listener() {
			public void handleEvent(Event e) {
				if (e.detail != SWT.CHECK) {
					return;
				//TableItem item = (TableItem) e.item;
				//int index = item.getParent().indexOf(item);
				//in case we need to do anything on a selection event
				}
			}
		});

		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				final TableItem item = (TableItem) event.item;
				if (item == null) {
					return;
				}
				Table table = item.getParent();
				int index = table.indexOf(item);

				if (index < 0) {
					// Trigger a Table._getItem, which assigns the item to the array
					// in Table, so indexOf(..) can find it.  This is a workaround for
					// a WinXP bug.
					Rectangle r = item.getBounds(0);
					table.getItem(new Point(r.x, r.y));
					index = table.indexOf(item);
					if (index < 0) {
						return;
					}
				}

				if(bIsDownloadContainer){
					String[] column_text = lookUpTableItem(downloadsAllowedColumns.get(index));
					item.setText(0,column_text[0]);
					item.setText(1,column_text[1]);
					if(tableColumns.contains(downloadsAllowedColumns.get(index))){
						item.setChecked(true);
					}
				}else{
					String[] column_text = lookUpTableItem(seedsAllowedColumns.get(index));
					item.setText(0,column_text[0]);
					item.setText(1,column_text[1]);
					if(tableColumns.contains(seedsAllowedColumns.get(index))){
						item.setChecked(true);
					}
				}

			}
		});

		if(bIsDownloadContainer) {
			table.setItemCount(downloadsAllowedColumns.size());
		} else {
			table.setItemCount(seedsAllowedColumns.size());
		}

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent arg0) {
				mousePressed = true;
				selectedItem = table.getItem(new Point(arg0.x,arg0.y));
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mousePressed = false;
				//1. Restore old image
				if(oldPoint != null && oldImage != null) {
					GC gc = new GC(table);
					gc.drawImage(oldImage,oldPoint.x,oldPoint.y);
					oldImage.dispose();
					oldImage = null;
					oldPoint = null;
				}
				Point p = new Point(e.x,e.y);
				TableItem item = table.getItem(p);
				if(item != null && selectedItem != null) {
					int index = table.indexOf(item);
					int oldIndex = table.indexOf(selectedItem);
					if(index == oldIndex) {
						return;
					}

					if(bIsDownloadContainer){
						Integer value = downloadsAllowedColumns.get(oldIndex);
						downloadsAllowedColumns.remove(oldIndex);
						downloadsAllowedColumns.add(index, value);
						table.clearAll();
					}else{
						Integer value = seedsAllowedColumns.get(oldIndex);
						seedsAllowedColumns.remove(oldIndex);
						seedsAllowedColumns.add(index, value);
						table.clearAll();
					}


				}
			}
		});

		table.addMouseMoveListener(new MouseMoveListener(){
			public void mouseMove(MouseEvent e) {
				if (!mousePressed || selectedItem == null) {
					return;
				}

				Point p = new Point(e.x,e.y);
				TableItem item = table.getItem(p);
				if (item == null) {
					return;
				}

				GC gc = new GC(table);
				Rectangle bounds = item.getBounds(0);
				int selectedPosition = table.indexOf(selectedItem);
				int newPosition = table.indexOf(item);

				//1. Restore old image
				if(oldPoint != null && oldImage != null) {
					gc.drawImage(oldImage,oldPoint.x,oldPoint.y);
					oldImage.dispose();
					oldImage = null;
					oldPoint = null;
				}
				//bounds.y += (-table.getHeaderHeight());
				if(newPosition <= selectedPosition) {
					oldPoint = new Point(bounds.x,bounds.y);
				} else {
					oldPoint = new Point(bounds.x,bounds.y+bounds.height);
				}
				//2. Store the image
				oldImage = new Image(display,bounds.width,2);
				gc.copyArea(oldImage,oldPoint.x,oldPoint.y);
				//3. Draw a thick line
				gc.setBackground(blue);
				gc.fillRectangle(oldPoint.x,oldPoint.y,bounds.width,2);
			}
		});

		shell.pack();
		Point p = shell.getSize();
		p.x = 550;
		// For Windows, to get rid of the scrollbar
		p.y += 2;

		if (p.y + 64 > display.getClientArea().height) {
			p.y = display.getBounds().height - 64;
		}

		shell.setSize(p);

		GUI_Utilities.centerShellandOpen(shell);
	}

	private void close() {
		if(blue != null && ! blue.isDisposed()) {
			blue.dispose();
		}
		if (!shell.isDisposed()) {
			shell.dispose();
		}
	}

	private void saveAndApply() {
		tableColumns.clear();
		TableItem[] items = table.getItems();
		int position = 0;
		for(int i = 0 ; i < items.length ; i++) {
			if(items[i].getChecked()){
				if(bIsDownloadContainer) {
					tableColumns.add(position, downloadsAllowedColumns.get(i));
				} else {
					tableColumns.add(position, seedsAllowedColumns.get(i));
				}

				position++;
			}
		}
		//save the tableColumns back to the original set
		if(bIsDownloadContainer){
			DownloadContainer.saveColumns();
		} else {
			SeedContainer.saveColumns();
		}

		//redraw the tables
		RCMain.getRCMain().getMainWindow().redrawColumnsonTables();
	}



	private String[] lookUpTableItem(int remoteConstantInteger){
		switch (remoteConstantInteger) {

		case RemoteConstants.ST_NAME:
			return new String[] {I18N.translate(PFX + "tableitem.name.title"),
					I18N.translate(PFX + "tableitem.name.description")};

		case RemoteConstants.ST_POSITION:
			return new String[] {I18N.translate(PFX + "tableitem.position.title"),
					I18N.translate(PFX + "tableitem.position.description")};

		case RemoteConstants.ST_DOWNLOAD_AVG:
			return new String[] {I18N.translate(PFX + "tableitem.downloadAvg.title"),
					I18N.translate(PFX + "tableitem.downloadAvg.description")};

		case RemoteConstants.ST_UPLOAD_AVG:
			return new String[] {I18N.translate(PFX + "tableitem.uploadAverage.title"),
					I18N.translate(PFX + "tableitem.uploadAverage.description")};

		case RemoteConstants.ST_DOWNLOADED:
			return new String[] {I18N.translate(PFX + "tableitem.downloaded.title"),
					I18N.translate(PFX + "tableitem.downloaded.description")};

		case RemoteConstants.ST_UPLOADED:
			return new String[] {I18N.translate(PFX + "tableitem.uploaded.title"),
					I18N.translate(PFX + "tableitem.uploaded.description")};

		case RemoteConstants.ST_HEALTH:
			return new String[] {I18N.translate(PFX + "tableitem.health.title"),
					I18N.translate(PFX + "tableitem.health.description")};

		case RemoteConstants.ST_COMPLETITION:
			return new String[] {I18N.translate(PFX + "tableitem.completion.title"),
					I18N.translate(PFX + "tableitem.completion.description")};

		case RemoteConstants.ST_AVAILABILITY:
			return new String[] {I18N.translate(PFX + "tableitem.availability.title"),
					I18N.translate(PFX + "tableitem.availability.description")};

		case RemoteConstants.ST_ETA:
			return new String[] {I18N.translate(PFX + "tableitem.eta.title"),
					I18N.translate(PFX + "tableitem.eta.description")};

		case RemoteConstants.ST_STATE:
			return new String[] {I18N.translate(PFX + "tableitem.state.title"),
					I18N.translate(PFX + "tableitem.state.description")};

		case RemoteConstants.ST_STATUS:
			return new String[] {I18N.translate(PFX + "tableitem.status.title"),
					I18N.translate(PFX + "tableitem.status.description")};

		case RemoteConstants.ST_SHARE:
			return new String[] {I18N.translate(PFX + "tableitem.ratio.title"),
					I18N.translate(PFX + "tableitem.ratio.description")};

		case RemoteConstants.ST_ALL_SEEDS:
			return new String[] {I18N.translate(PFX + "tableitem.seeds.title"),
					I18N.translate(PFX + "tableitem.seeds.description")};

		case RemoteConstants.ST_ALL_LEECHER:
			return new String[] {I18N.translate(PFX + "tableitem.leechers.title"),
					I18N.translate(PFX + "tableitem.leechers.description")};

		case RemoteConstants.ST_SIZE:
			return new String[] {I18N.translate(PFX + "tableitem.size.title"),
					I18N.translate(PFX + "tableitem.size.description")};

		case RemoteConstants.ST_TRACKER:
			return new String[] {I18N.translate(PFX + "tableitem.tracker.title"),
					I18N.translate(PFX + "tableitem.tracker.description")};

		case RemoteConstants.ST_DISCARDED:
			return new String[] {I18N.translate(PFX + "tableitem.discarded.title"),
					I18N.translate(PFX + "tableitem.discarded.description")};

		case RemoteConstants.ST_ELAPSED_TIME:
			return new String[] {I18N.translate(PFX + "tableitem.elapsed.title"),
					I18N.translate(PFX + "tableitem.elapsed.description")};

		case RemoteConstants.ST_TOTAL_AVG:
			return new String[] {I18N.translate(PFX + "tableitem.totalavg.title"),
					I18N.translate(PFX + "tableitem.totalavg.description")};

		case RemoteConstants.ST_LIMIT_UP:
			return new String[] {I18N.translate(PFX + "tableitem.upLimit.title"),
					I18N.translate(PFX + "tableitem.upLimit.description")};

		case RemoteConstants.ST_LIMIT_DOWN:
			return new String[] {I18N.translate(PFX + "tableitem.downLimit.title"),
					I18N.translate(PFX + "tableitem.downLimit.description")};

		case RemoteConstants.ST_CATEGORY:
			return new String[] {I18N.translate(PFX + "tableitem.category.title"),
					I18N.translate(PFX + "tableitem.category.description")};

		default:
			return new String[] {I18N.translate("global.error"),
				I18N.translate("global.error")};
		}
	}


	/**
	 *Static open method
	 */
	public static void open(final boolean _bIsDownloadConatiner) {
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null) {
			return;
		}
		if (instance == null || instance.shell == null || instance.shell.isDisposed()){
			new TableColumnEditorDialog(_bIsDownloadConatiner);
		} else {
			instance.shell.setActive();
		}
	}
}//EOF

