package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import org.eclipse.swt.widgets.Composite;


import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTView;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTViewEvent;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTViewEventListener;

public class UISWTViewImpl implements UISWTView{

	//initial declarations
	private Object dataSource = null;

	private final UISWTViewEventListener eventListener;

	private Composite composite;

	private final String sViewID;
	
	private final String sParentID;
	
	private String sTitle = null;

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

		if (!eventListener.eventOccurred(new UISWTViewEventImpl(this,
				UISWTViewEvent.TYPE_CREATE, this)))
			throw new Exception();
	}

	
	
	
	public void closeView() {
		// TODO Auto-generated method stub
		
	}

	public Object getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getViewID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	public void triggerEvent(int eventType, Object data) {
		// TODO Auto-generated method stub
		
	}

}
