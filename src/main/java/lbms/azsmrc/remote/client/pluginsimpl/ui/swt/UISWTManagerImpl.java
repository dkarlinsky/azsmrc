package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent;
import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEventListener;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;
import lbms.azsmrc.remote.client.plugins.ui.swt.ViewID;
import lbms.azsmrc.remote.client.swtgui.DownloadManagerShell;
import lbms.azsmrc.remote.client.swtgui.RCMain;

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

	public UIPluginViewImpl getViewInstance (ViewID parentID, String viewID, Object datasource) {
		if (!eventListener.containsKey(parentID)) return null;

		UIPluginEventListener eListener = eventListener.get(parentID).get(viewID);
		if (eListener == null) return null;

		boolean create = eListener.eventOccurred(new UIPluginEventImpl(null,UIPluginEvent.TYPE_CREATE,datasource));

		if (!views.containsKey(parentID))
			views.put(parentID, new HashMap<String, UIPluginViewImpl>());
		Map<String, UIPluginViewImpl> map = views.get(parentID);

		//create a new Instance everytime
		if (create) {
			UIPluginViewImpl pv = new UIPluginViewImpl(parentID,eListener);
			if (!map.containsKey(viewID)) map.put(viewID, pv);
			return pv;
		} else if (map.containsKey(viewID)) {
				return map.get(viewID);
		} else return null;
	}

	public String[] getViewIDs (ViewID parentID) {
		if (!eventListener.containsKey(parentID)) return new String[0];
		else return eventListener.get(parentID).keySet().toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#getDisplay()
	 */
	public Display getDisplay() {
		// TODO Auto-generated method stub
		return RCMain.getRCMain().getDisplay();
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#openMainView(java.lang.String)
	 */
	public void openMainView(String viewID) {
		DownloadManagerShell dms = RCMain.getRCMain().getMainWindow();
		if (dms != null) {
			dms.openPluginView(viewID);
		}

	}
}
