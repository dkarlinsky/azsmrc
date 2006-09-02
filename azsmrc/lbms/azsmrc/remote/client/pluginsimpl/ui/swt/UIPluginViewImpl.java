/**
 *
 */
package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent;
import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEventListener;
import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView;
import lbms.azsmrc.remote.client.plugins.ui.swt.ViewID;

/**
 * @author Damokles
 *
 */
public class UIPluginViewImpl implements UIPluginView {

	private UIPluginEventListener listener;
	private ViewID viewID;
	private Composite composite;

	public UIPluginViewImpl(ViewID viewID, UIPluginEventListener listener) {
		super();
		this.viewID = viewID;
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#initialize(org.eclipse.swt.widgets.Composite)
	 */
	public void initialize(Composite _composite) {
		this.composite = _composite;

		//Listener to trigger dispose
		this.composite.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				delete();
			}
		});
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#getViewID()
	 */
	public ViewID getViewID() {
		return viewID;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#getComposite()
	 */
	public Composite getComposite() {
		return composite;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#delete()
	 */
	public void delete() {
		triggerEvent(composite,UIPluginEvent.TYPE_DESTROY,null);
		if(composite != null || !composite.isDisposed()) composite.dispose();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#getData()
	 */
	public String getData() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#getFullTitle()
	 */
	public String getFullTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#getShortTitle()
	 */
	public String getShortTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.IView#refresh()
	 */
	public void refresh() {
		// TODO Auto-generated method stub

	}

	private void triggerEvent (Object data, int type, Object dataSource) {
		listener.eventOccurred(new UIPluginEventImpl(data,type,dataSource));
	}

}
