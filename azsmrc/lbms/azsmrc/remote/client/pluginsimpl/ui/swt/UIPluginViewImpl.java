/**
 * 
 */
package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import org.eclipse.swt.widgets.Composite;

import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEventListener;
import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView;
import lbms.azsmrc.remote.client.plugins.ui.swt.ViewID;

/**
 * @author Damokles
 *
 */
public class UIPluginViewImpl implements UIPluginView {

	private String id;
	private ViewID parentID;
	private UIPluginEventListener listener;



	public UIPluginViewImpl(ViewID parentID, String id, UIPluginEventListener listener) {
		super();
		this.parentID = parentID;
		this.id = id;
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#getComposite()
	 */
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#getID()
	 */
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#getParent()
	 */
	public ViewID getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#initialize(org.eclipse.swt.widgets.Composite)
	 */
	public void initialize(Composite parent) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginView#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
