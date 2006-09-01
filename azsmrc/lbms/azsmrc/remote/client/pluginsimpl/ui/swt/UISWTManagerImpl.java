package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEventListener;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;
import lbms.azsmrc.remote.client.plugins.ui.swt.ViewID;

/**
 * @author Damokles
 *
 */
public class UISWTManagerImpl implements UISWTManager {

	private Map<ViewID, Map<String, UIPluginEventListener>> eventListener = new HashMap<ViewID, Map<String, UIPluginEventListener>>();
	private Map<ViewID, Map<String, UIPluginViewImpl>> views = new HashMap<ViewID, Map<String, UIPluginViewImpl>>();

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#addUIEventHandler(lbms.azsmrc.remote.client.plugins.ui.swt.UISWTPluginEventHandler)
	 */
	public void addPluginView(ViewID parentID, String viewID, UIPluginEventListener eListener) {
		if (!eventListener.containsKey(parentID))
			eventListener.put(parentID, new HashMap<String, UIPluginEventListener>());
		Map<String, UIPluginEventListener> map = eventListener.get(parentID);
		map.put(viewID, eListener);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#removeUIEventHandler(lbms.azsmrc.remote.client.plugins.ui.swt.UISWTPluginEventHandler)
	 */
	public void removePluginView(ViewID parentID, String viewID) {
		if (eventListener.containsKey(parentID)) {
			eventListener.get(parentID).remove(viewID);
			if (views.containsKey(parentID)) {
				Map<String, UIPluginViewImpl> map = views.get(parentID);
				map.remove(viewID);
			}
		}
	}

	public UIPluginViewImpl getView (ViewID parentID, String viewID) {
		if (!views.containsKey(parentID)) return null;
		else return views.get(parentID).get(viewID);
	}

	public UIPluginViewImpl[] getViews (ViewID parentID) {
		if (!views.containsKey(parentID)) return new UIPluginViewImpl[0];
		else return views.get(parentID).values().toArray(new UIPluginViewImpl[0]);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#getDisplay()
	 */
	public Display getDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#openMainView(java.lang.String)
	 */
	public void openMainView(String viewID) {
		// TODO Auto-generated method stub

	}
}
