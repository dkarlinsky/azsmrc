package lbms.azsmrc.remote.client.plugins.ui.swt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;



public class Tab {

	private static HashMap 	tabs;


	private static Composite _folder;

	private Composite folder;


	private Item tabItem;
	private static boolean eventCloseAllowed = true;
	private static Item selectedItem = null;

	private static Logger logger;

	private Composite composite;

	private IView view;

	// events
	private static List tabAddListeners;
	private static List tabRemoveListeners;


	static {
		tabs = new HashMap();
		tabAddListeners = new LinkedList();
		tabRemoveListeners = new LinkedList();
	}


	public Item getTabItem() {
		return tabItem;
	}

	public Tab(IView _view) {
		this(_view, true);
	}

	public Tab(IView _view, boolean bFocus) {

		this.view = _view;

		logger = Logger.getLogger(Tab.class);

		CTabFolder parentFolder = RCMain.getRCMain().getMainWindow().getTabFolder();
		if(parentFolder == null || parentFolder.isDisposed()) return;
		this.folder = parentFolder;

		tabItem = new CTabItem(parentFolder, SWT.NULL);

		folder.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent arg0) {
				if(arg0.button == 2) {
					if(eventCloseAllowed) {
						Rectangle rectangle =((CTabItem)tabItem).getBounds();
						if(rectangle.contains(arg0.x, arg0.y)) {
							eventCloseAllowed = false;
							folder.removeMouseListener(this);
							closed(tabItem);
						}
					}
				} else {
					selectedItem = ((CTabFolder) folder).getSelection();
				}
			}

			public void mouseUp(MouseEvent arg0) {
				eventCloseAllowed = true;
				if(selectedItem != null) {
					if(_folder instanceof CTabFolder)
						((CTabFolder) _folder).setSelection((CTabItem)selectedItem);
				}
			}
		});



		Listener activateListener = new Listener() {
			public void handleEvent(Event event) {
				IView view = null;
				Composite parent = (Composite)event.widget;
				IView oldView = getView(selectedItem);
				/*				if (oldView instanceof IViewExtension) {
					((IViewExtension)oldView).viewDeactivated();
				}
				 */
				while (parent != null && !parent.isDisposed() && view == null) {
					if (parent instanceof CTabFolder) {
						CTabFolder folder = (CTabFolder)parent;
						selectedItem = folder.getSelection();
						view = getView(selectedItem);
					} else if (parent instanceof TabFolder) {
						TabFolder folder = (TabFolder)parent;
						TabItem[] selection = folder.getSelection();
						if (selection.length > 0) {
							selectedItem = selection[0];
							view = getView(selectedItem);
						}
					}

					if (view == null)
						parent = parent.getParent();
				}

				if (view != null) {
					/*					if (view instanceof IViewExtension) {
						((IViewExtension)view).viewActivated();
					}*/
					view.refresh();
				}
			}
		};

		tabs.put(tabItem, view);

		try {
			// Always create a composite around the IView, because a lot of them
			// assume that their parent is of GridLayout layout.
			Composite tabArea = new Composite(folder, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			tabArea.setLayout(layout);

			_view.initialize(tabArea);
			tabItem.setText(escapeAccelerators(view.getShortTitle()));

			Composite viewComposite = _view.getComposite();
			if (viewComposite != null && !viewComposite.isDisposed()) {
				viewComposite.addListener(SWT.Activate, activateListener);

				// make sure the view's layout data is of GridLayoutData
				if ((tabArea.getLayout() instanceof GridLayout)
						&& !(viewComposite.getLayoutData() instanceof GridData)) {
					viewComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
				}
			}


			((CTabItem) tabItem).setControl(tabArea);
//			Disabled for SWT 3.2RC5.. CTabItem tooltip doesn't always disappear
//			((CTabItem) tabItem).setToolTipText(view.getFullTitle());
			if (bFocus)
				((CTabFolder) folder).setSelection((CTabItem) tabItem);

		} catch (Exception e) {
			tabs.remove(tabItem);
			logger.debug(e);
		}

		if (bFocus) {
			//mainwindow.refreshIconBar();
			selectedItem = tabItem;
		}

		// events
		notifyListeners(tabAddListeners, tabItem);
		tabItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				notifyListeners(tabRemoveListeners, tabItem);
			}
		});
//		System.out.println("selected: "+selectedItem.getText());
	}

	//public static IView getView(TabItem item) {
	//public static IView getView(CTabItem item) {
	public static IView getView(Item item) {
		return (IView) tabs.get(item);
	}

	public IView
	getView()
	{
		return( view );
	}

	public static Item
	getTab(
			IView	view )
	{

		Iterator iter = tabs.keySet().iterator();

		while( iter.hasNext()){

			Item item = (Item) iter.next();

			IView this_view = (IView) tabs.get(item);

			if ( this_view == view ){

				return( item );
			}
		}

		return( null );


	}

	public static Item[] getAllTabs() {

		Item[] tabItems = new Item[tabs.size()];
		if (tabItems.length > 0) {
			tabItems = (Item[]) tabs.keySet().toArray(tabItems);
		}

		return tabItems;

	}

	public static IView[] getAllViews() {

		IView[] views = new IView[tabs.size()];
		if (views.length > 0) {
			views = (IView[])tabs.values().toArray(views);
		}

		return views;

	}


	public static void refresh() {
			Iterator iter = tabs.keySet().iterator();
			while (iter.hasNext()) {
				//TabItem item = (TabItem) iter.next();
				//CTabItem item = (CTabItem) iter.next();
				Item item = (Item) iter.next();
				IView view = (IView) tabs.get(item);
				try {
					if (item.isDisposed())
						continue;
					String lastTitle = item.getText();
					String newTitle = view.getShortTitle();
					if (lastTitle == null || !lastTitle.equals(newTitle)) {
						item.setText(escapeAccelerators(newTitle));
					}
					if (item instanceof CTabItem) {
//						Disabled for SWT 3.2RC5.. CTabItem tooltip doesn't always disappear
//						String lastToolTip = ((CTabItem) item).getToolTipText();
//						String newToolTip = view.getFullTitle();
//						if (lastToolTip == null || !lastToolTip.equals(newToolTip)) {
//						((CTabItem) item).setToolTipText(newToolTip);
//						}
					}
					else if (item instanceof TabItem) {
						String lastToolTip = ((TabItem) item).getToolTipText();
						String newToolTip = view.getFullTitle(); /* + " " +
						//MessageText.getString("Tab.closeHint");*/
						if (lastToolTip == null || !lastToolTip.equals(newToolTip)) {
							((TabItem) item).setToolTipText(newToolTip);
						}
					}
				}
				catch (Exception e){
					logger.debug(e);
				}
			}

	}

	public static void
	updateLanguage()
	{
		IView[] views;

		views = (IView[]) tabs.values().toArray(new IView[tabs.size()]);

		for (int i = 0; i < views.length; i++) {

			IView view = views[i];

			try {
				//view.updateLanguage();
				view.refresh();
			}
			catch (Exception e) {
				logger.debug(e);
			}
		}
	}


	public static void
	closeAllTabs()
	{
		Item[] tab_items;

		tab_items = (Item[]) tabs.keySet().toArray(new Item[tabs.size()]);

		for (int i = 0; i < tab_items.length; i++) {

			closed(tab_items[i]);
		}
	}

/*	public static boolean hasDetails()
	{
		boolean hasDetails = false;
		try
		{
			class_mon.enter();

			Iterator iter = tabs.values().iterator();
			while (iter.hasNext())
			{
				IView view = (IView) iter.next();
				if(view instanceof ManagerView)
				{
					hasDetails = true;
					break;
				}
			}
		}
		finally
		{
			class_mon.exit();
		}

		return hasDetails;
	}*/

/*	public static void
	closeAllDetails()
	{
		Item[] tab_items;


		tab_items = (Item[]) tabs.keySet().toArray(new Item[tabs.size()]);



		for (int i = 0; i < tab_items.length; i++) {
			IView view = (IView) tabs.get(tab_items[i]);
			if (view instanceof ManagerView) {
				closed(tab_items[i]);
			}
		}
	}*/

	public static void closeCurrent() {
		if (_folder == null || _folder.isDisposed())
			return;
		if(_folder instanceof TabFolder) {
			TabItem[] items =  ((TabFolder)_folder).getSelection();
			if(items.length == 1) {
				closed(items[0]);
			}
		} else {
			closed(((CTabFolder)_folder).getSelection());
		}
	}

	/**
	 * @param selectNext if true, the next tab is selected, else the previous
	 *
	 * @author Rene Leonhardt
	 */
	public static void selectNextTab(boolean selectNext) {
		if (_folder == null || _folder.isDisposed())
			return;
		final int nextOrPrevious = selectNext ? 1 : -1;
		if(_folder instanceof TabFolder) {
			TabFolder tabFolder = (TabFolder)_folder;
			int index = tabFolder.getSelectionIndex() + nextOrPrevious;
			if(index == 0 && selectNext || index == -2 || tabFolder.getItemCount() < 2)
				return;
			if(index == tabFolder.getItemCount())
				index = 0;
			else if(index < 0)
				index = tabFolder.getItemCount() - 1;
			tabFolder.setSelection(index);
		} else {
			CTabFolder tabFolder = (CTabFolder)_folder;
			int index = tabFolder.getSelectionIndex() + nextOrPrevious;
			if(index == 0 && selectNext || index == -2 || tabFolder.getItemCount() < 2)
				return;
			if(index == tabFolder.getItemCount())
				index = 0;
			else if(index < 0)
				index = tabFolder.getItemCount() - 1;
			tabFolder.setSelection(index);
		}
	}

	//public static void setFolder(TabFolder folder) {
	//public static void setFolder(CTabFolder folder) {
	public static void initialize(/*MainWindow mainwindow,*/ Composite folder) {
		//Tab.mainwindow = mainwindow;
		_folder = folder;
	}

	public static void
	closed(Item item)
	{
		IView view = null;

		view = (IView) tabs.remove(item);


		if (view != null) {
			try {
			/*	if(view instanceof UISWTPluginView) {
					//TODO -- we need a way to remove the plugins here
					//mainwindow.removeActivePluginView(((UISWTPluginView)view).getPluginViewName());
				}
				if(view instanceof UISWTView)
					//mainwindow.removeActivePluginView(((UISWTView)view).getViewID());

				view.delete();*/
			} catch (Exception e) {
				logger.debug(e);
			}
		}
		try {
			/*Control control;
		if(item instanceof CTabItem) {
		  control = ((CTabItem)item).getControl();
		} else {
		  control = ((TabItem)item).getControl();
		}
		if (control != null && !control.isDisposed())
		  control.dispose();
			 */
			item.dispose();
		}
		catch (Exception e) {
			logger.debug(e);
		}
	}

	public void setFocus() {
		if (folder != null && !folder.isDisposed()) {
			((CTabFolder)folder).setSelection((CTabItem)tabItem);
		}
	}



	public void dispose() {
		IView localView = null;
		localView = (IView) tabs.get(tabItem);
		tabs.remove(tabItem);
		try {
			if (localView != null) {
				/*if(localView instanceof UISWTPluginView) {
					//TODO -- again.. we need a way to get rid of the view
					//mainwindow.removeActivePluginView(((UISWTPluginView)localView).getPluginViewName());
				}
*/
				localView.delete();
			}
			tabItem.dispose();
		}
		catch (Exception e) {}
		if (composite != null && !composite.isDisposed()) {
			composite.dispose();
		}
	}

	public static void addTabAddedListener(Listener listener)
	{
		addListener(tabAddListeners, listener);
	}

	public static void removeTabAddedListener(Listener listener)
	{
		removeListener(tabAddListeners, listener);
	}

	public static void addTabRemovedListener(Listener listener)
	{
		addListener(tabRemoveListeners, listener);
	}

	public static void removeTabRemovedListener(Listener listener)
	{
		removeListener(tabRemoveListeners, listener);
	}

	private static void addListener(List listenerList, Listener listener)
	{
			listenerList.add(listener);
	}

	private static void removeListener(List listenerList, Listener listener)
	{
		listenerList.remove(listener);
	}

	private static void notifyListeners(List listenerList, Item sender)
	{
		Iterator iter = listenerList.iterator();
		for (int i = 0; i < listenerList.size(); i++)
		{
			((Listener)iter.next()).handleEvent(getEvent(sender));
		}
	}


	protected static String
	escapeAccelerators(
			String	str )
	{
		if ( str == null ){

			return( str );
		}

		return( str.replaceAll( "&", "&&" ));
	}

	private static Event getEvent(Item sender)
	{
		Event e = new Event();
		e.widget = sender;
		return e;
	}

/*	public void
	generateDiagnostics(
			IndentWriter	writer )
	{
		view.generateDiagnostics( writer );
	}*/
}
