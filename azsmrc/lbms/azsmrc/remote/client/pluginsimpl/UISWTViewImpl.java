package lbms.azsmrc.remote.client.pluginsimpl;

import lbms.azsmrc.remote.client.plugins.ui.swt.AbstractIView;
import lbms.azsmrc.remote.client.plugins.ui.swt.UIRuntimeException;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTView;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTViewEvent;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTViewEventListener;
import lbms.azsmrc.remote.client.pluginsimpl.ui.swt.UISWTViewEventImpl;


import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;






/**
 * @author TuxPaper
 *
 */
public class UISWTViewImpl extends AbstractIView implements UISWTView {
	public static final String CFG_PREFIX = "Views.plugins.";

	private Object dataSource = null;

	private final UISWTViewEventListener eventListener;

	private Composite composite;

	private final String sViewID;

	private boolean bFirstGetCompositeCall = true;

	private final String sParentID;

	private String sTitle = null;

	private static Logger logger;

	/**
	 * 
	 * @param sViewID
	 * @param eventListener
	 */
	public UISWTViewImpl(String sParentID, String sViewID, UISWTViewEventListener eventListener)
	throws Exception {
		this.sParentID = sParentID;
		this.sViewID = sViewID;
		this.eventListener = eventListener;
		logger = Logger.getLogger(UISWTViewImpl.class);

		if (!eventListener.eventOccurred(new UISWTViewEventImpl(this,
				UISWTViewEvent.TYPE_CREATE, this)))
			throw new Exception();
	}

	// UISWTPluginView implementation
	// ==============================

	public Object getDataSource() {
		return dataSource;
	}

	public String getViewID() {
		return sViewID;
	}

	public void closeView() {
		try {

			// Code later to close plugin view


			/*UIFunctionsSWT uiFunctions = UIFunctionsManagerSWT.getUIFunctionsSWT();
			if (uiFunctions != null) {
				uiFunctions.closePluginView(this);
			}*/
		} catch (Exception e) {
			logger.debug(e);
		}
	}

	public void triggerEvent(int eventType, Object data) {
		try {
			eventListener.eventOccurred(new UISWTViewEventImpl(this, eventType, data));
		} catch (Throwable t) {
			throw (new UIRuntimeException("UISWTView.triggerEvent:: ViewID="
					+ sViewID + "; EventID=" + eventType + "; data=" + data, t));
		}
	}


	public void setTitle(String title) {
		sTitle = title;

	}

	// AbstractIView Implementation
	// ============================

	public void dataSourceChanged(Object newDataSource) {
		dataSource = newDataSource;

		triggerEvent(UISWTViewEvent.TYPE_DATASOURCE_CHANGED, newDataSource);
	}

	public void delete() {
		triggerEvent(UISWTViewEvent.TYPE_DESTROY, null);
		super.delete();
	}

	public Composite getComposite() {
		if (bFirstGetCompositeCall) {
			bFirstGetCompositeCall = false;
		}
		return composite;
	}

	public String getData() {
		//final String key = CFG_PREFIX + sViewID + ".title";
/*		if (MessageText.keyExists(key))
			return key;*/
		// For now, to get plugin developers to update their plugins
		// return key;
		// For release, change it to this, to make it at least shorter:
		return sViewID;
	}

	public String getFullTitle() {
		if (sTitle != null)
			return sTitle;

		return super.getFullTitle();
	}

	public void initialize(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		triggerEvent(UISWTViewEvent.TYPE_INITIALIZE, composite);

		if (composite.getLayout() instanceof GridLayout) {
			// Force children to have GridData layoutdata.
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				Object layoutData = control.getLayoutData();
				if (layoutData == null || !(layoutData instanceof GridData)) {
					if (layoutData != null)
						logger.debug("Plugin View '" + sViewID + "' tried to setLayouData of "
								+ control + " to a " + layoutData.getClass().getName());

					if (children.length == 1)
						gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
					else
						gridData = new GridData();

					control.setLayoutData(gridData);
				}
			}
		}


		if (composite != null) {
			composite.addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					triggerEvent(UISWTViewEvent.TYPE_FOCUSGAINED, null);
				}
			});

			composite.addListener(SWT.Deactivate, new Listener() {
				public void handleEvent(Event event) {
					triggerEvent(UISWTViewEvent.TYPE_FOCUSLOST, null);
				}
			});
		}
	}

	public void refresh() {
		triggerEvent(UISWTViewEvent.TYPE_REFRESH, null);
	}


	// Core Functions
	public String getParentID() {
		return sParentID;
	}


	public String getShortTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateLanguage() {
		// TODO Auto-generated method stub

	}
}
