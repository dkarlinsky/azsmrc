package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTPluginView;

/**
 * @author Damokles
 *
 */
public class UISWTManagerImpl implements UISWTManager {

	private List<UISWTPluginView> pluginViews = new ArrayList<UISWTPluginView>();

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#addUIEventHandler(lbms.azsmrc.remote.client.plugins.ui.swt.UISWTPluginEventHandler)
	 */
	public void addPluginView(UISWTPluginView h) {
		pluginViews.add(h);
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UISWTManager#removeUIEventHandler(lbms.azsmrc.remote.client.plugins.ui.swt.UISWTPluginEventHandler)
	 */
	public void removePluginView(UISWTPluginView h) {
		pluginViews.remove(h);
	}
}
