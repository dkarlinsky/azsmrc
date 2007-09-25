/*
 * Created on Feb 18, 2006
 * Created by omschaub
 * Adaptation of LoggerViewer.java from core
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lbms.azsmrc.remote.client.history.DownloadHistory;
import lbms.azsmrc.remote.client.history.DownloadHistoryEntry;
import lbms.azsmrc.remote.client.history.DownloadHistoryListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.ExtendedProperties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

public class DownloadHistoryTab implements DownloadHistoryListener {

	// I18N prefix
	public static final String		PFX				= "tab.downloadhistorytab.";

	private long					currentTimespan	= 0;

	private ToolItem				lastHourItem	= null;
	private ToolItem				last12HoursItem	= null;
	private ToolItem				lastDayItem		= null;
	private ToolItem				lastWeekItem	= null;
	private ToolItem				lastMonthItem	= null;
	private ToolItem				allItem			= null;

	private List<ToolItem>			toolItems		= new ArrayList<ToolItem>();

	private DownloadHistoryEntry[]	entryArray		= null;

	private Table					entryTable		= null;

	private SimpleDateFormat		dateFormat		= new SimpleDateFormat(
															"yyyy-MM-dd HH:mm:ss");
	private int						sortColumn		= 1;
	private int						sortDirection	= 1;
	private Listener				sortListener	= new Listener() {
														/*
														 * (non-Javadoc)
														 * 
														 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
														 */
														public void handleEvent(
																Event e) {
															TableColumn col = (TableColumn) e.widget;
															if (entryTable.indexOf(col) == sortColumn) {
																sortDirection *= -1;

															} else {
																sortDirection = 1;
																sortColumn = entryTable.indexOf(col);
															}
															switch (sortColumn) {
															case 0:
																Arrays.sort(
																		entryArray,
																		new Comparator<DownloadHistoryEntry>() {
																			/*
																			 * (non-Javadoc)
																			 * 
																			 * @see java.util.Comparator#compare(java.lang.Object,
																			 *      java.lang.Object)
																			 */
																			public int compare(
																					DownloadHistoryEntry o1,
																					DownloadHistoryEntry o2) {
																				return sortDirection
																						* o1.getDlName().compareToIgnoreCase(
																								o2.getDlName());
																			}
																		});
																break;
															case 1:
																Arrays.sort(
																		entryArray,
																		new Comparator<DownloadHistoryEntry>() {
																			/*
																			 * (non-Javadoc)
																			 * 
																			 * @see java.util.Comparator#compare(java.lang.Object,
																			 *      java.lang.Object)
																			 */
																			public int compare(
																					DownloadHistoryEntry o1,
																					DownloadHistoryEntry o2) {
																				return sortDirection
																						* o1.compareTo(o2);
																			}
																		});
																break;
															case 2:
																Arrays.sort(
																		entryArray,
																		new Comparator<DownloadHistoryEntry>() {
																			/*
																			 * (non-Javadoc)
																			 * 
																			 * @see java.util.Comparator#compare(java.lang.Object,
																			 *      java.lang.Object)
																			 */
																			public int compare(
																					DownloadHistoryEntry o1,
																					DownloadHistoryEntry o2) {
																				return sortDirection
																						* o1.getCategory().compareToIgnoreCase(
																								o2.getCategory());
																			}
																		});
																break;
															}
															if (SWT.getVersion() > 3220) {
																entryTable.setSortDirection(sortDirection > 0 ? SWT.UP
																		: SWT.DOWN);
																entryTable.setSortColumn(col);
															}
															entryTable.clearAll();

														}
													};

	private DownloadHistoryTab(CTabFolder parentTab) {
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText(I18N.translate(PFX + "tab.text"));

		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		// gridData.grabExcessHorizontalSpace = false;
		// gridData.heightHint = 24;
		toolbar.setSize(200, 24);
		toolbar.setLayoutData(gridData);

		ToolItem refreshItem = new ToolItem(toolbar, SWT.PUSH | SWT.FLAT);
		refreshItem.setImage(ImageRepository.getImage("refresh"));
		refreshItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.refresh.tooltip"));
		refreshItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				long now = System.currentTimeMillis() / 1000;
				DownloadHistory.getInstance().getEntriesForce(
						now - currentTimespan, now, DownloadHistoryTab.this);
			}
		});

		new ToolItem(toolbar, SWT.SEPARATOR);

		lastHourItem = new ToolItem(toolbar, SWT.CHECK);
		lastHourItem.setText(I18N.translate(PFX + "toolbar.lastHour"));
		lastHourItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.lastHour.tooltip"));
		lastHourItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(lastHourItem, 60 * 60);
			}
		});

		last12HoursItem = new ToolItem(toolbar, SWT.CHECK);
		last12HoursItem.setText(I18N.translate(PFX + "toolbar.last12Hours"));
		last12HoursItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.last12Hours.tooltip"));
		last12HoursItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(last12HoursItem, 60 * 60 * 12);
			}
		});

		lastDayItem = new ToolItem(toolbar, SWT.CHECK);
		lastDayItem.setText(I18N.translate(PFX + "toolbar.lastDay"));
		lastDayItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.lastDay.tooltip"));
		lastDayItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(lastDayItem, 60 * 60 * 24);
			}
		});

		lastWeekItem = new ToolItem(toolbar, SWT.CHECK);
		lastWeekItem.setText(I18N.translate(PFX + "toolbar.lastWeek"));
		lastWeekItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.lastWeek.tooltip"));
		lastWeekItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(lastWeekItem, 60 * 60 * 24 * 7);
			}
		});

		lastMonthItem = new ToolItem(toolbar, SWT.CHECK);
		lastMonthItem.setText(I18N.translate(PFX + "toolbar.lastMonth"));
		lastMonthItem.setToolTipText(I18N.translate(PFX
				+ "toolbar.lastMonth.tooltip"));
		lastMonthItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(lastMonthItem, 60 * 60 * 24 * 30);
			}
		});

		allItem = new ToolItem(toolbar, SWT.CHECK);
		allItem.setText(I18N.translate(PFX + "toolbar.allItems"));
		allItem.setToolTipText(I18N.translate(PFX + "toolbar.allItems.tooltip"));
		allItem.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection(allItem,
						(System.currentTimeMillis() / 1000) - 10000);
			}
		});

		toolbar.pack();

		toolItems.add(lastHourItem);
		toolItems.add(last12HoursItem);
		toolItems.add(lastDayItem);
		toolItems.add(lastWeekItem);
		toolItems.add(lastMonthItem);
		toolItems.add(allItem);

		entryTable = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		entryTable.setLayoutData(gd);
		entryTable.setHeaderVisible(true);

		TableColumn nameColumn = new TableColumn(entryTable, SWT.RIGHT);
		nameColumn.setText(I18N.translate(PFX + "table.nameColumn"));
		nameColumn.setWidth(400);
		nameColumn.addListener(SWT.Selection, sortListener);
		nameColumn.setResizable(true);

		TableColumn dateColumn = new TableColumn(entryTable, SWT.RIGHT);
		dateColumn.setText(I18N.translate(PFX + "table.dateColumn"));
		dateColumn.setWidth(160);
		dateColumn.addListener(SWT.Selection, sortListener);

		TableColumn catColumn = new TableColumn(entryTable, SWT.RIGHT);
		catColumn.setText(I18N.translate(PFX + "table.catColumn"));
		catColumn.setWidth(200);
		catColumn.setResizable(true);
		catColumn.addListener(SWT.Selection, sortListener);

		if (SWT.getVersion() > 3220) {
			entryTable.setSortDirection(sortDirection > 0 ? SWT.UP : SWT.DOWN);
			entryTable.setSortColumn(dateColumn);
		}

		entryTable.addListener(SWT.SetData, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event e) {
				// pull the item
				TableItem item = (TableItem) e.item;

				// get the index of the item
				int index = entryTable.indexOf(item);

				try {
					DownloadHistoryEntry entry = entryArray[index];
					item.setText(0, entry.getDlName());
					item.setText(1, dateFormat.format(new Date(
							entry.getTimestamp() * 1000)));
					item.setText(2, entry.getCategory());
					// gray if needed
					if (index % 2 != 0) {
						item.setBackground(ColorUtilities.getBackgroundColor());
					}
				} catch (Exception e1) {

				}

			}
		});

		// Dispose Listener for tab
		detailsTab.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent arg0) {

			}
		});

		detailsTab.setControl(parent);
		ExtendedProperties props = RCMain.getRCMain().getProperties();
		if (props.getPropertyAsBoolean("downloadHistory.autoLoad")) {
			int i = props.getPropertyAsInt("downloadHistory.autoLoad.type", 2);
			switch (i) {
			case 0:
				updateSelection(lastHourItem, 60 * 60);
				break;
			case 1:
				updateSelection(last12HoursItem, 60 * 60 * 12);
				break;
			case 2:
				updateSelection(lastDayItem, 60 * 60 * 24);
				break;
			case 3:
				updateSelection(lastWeekItem, 60 * 60 * 24 * 7);
				break;
			case 4:
				updateSelection(lastMonthItem, 60 * 60 * 24 * 30);
				break;
			case 5:
				updateSelection(allItem,
						(System.currentTimeMillis() / 1000) - 10000);
				break;
			}
		}
		parentTab.setSelection(detailsTab);
	}

	private void updateSelection(ToolItem selectedItem, long timespan) {
		currentTimespan = timespan;
		for (ToolItem item : toolItems) {
			if (item == selectedItem) {
				item.setSelection(true);
			} else {
				item.setSelection(false);
			}
		}
		long now = System.currentTimeMillis() / 1000;
		DownloadHistory.getInstance().getEntries(now - timespan, now, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lbms.azsmrc.remote.client.history.DownloadHistoryListener#updatedEntries(lbms.azsmrc.remote.client.history.DownloadHistoryEntry[])
	 */
	public void updatedEntries(DownloadHistoryEntry[] entries) {
		this.entryArray = entries;
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void runSafe() {
				if (entryTable != null && !entryTable.isDisposed()) {
					entryTable.setItemCount(entryArray.length);
					entryTable.clearAll();
				}
			}
		});
	}

	public static void open(final CTabFolder parentTab) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe() {
				CTabItem[] tabs = parentTab.getItems();
				for (CTabItem tab : tabs) {
					if (tab.getText().equalsIgnoreCase(
							I18N.translate(PFX + "tab.text"))) {
						parentTab.setSelection(tab);
						return;
					}
				}
				new DownloadHistoryTab(parentTab);
			}
		});
	}

}// EOF
