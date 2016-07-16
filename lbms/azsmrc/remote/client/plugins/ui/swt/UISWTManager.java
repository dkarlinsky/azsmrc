package lbms.azsmrc.remote.client.plugins.ui.swt;

import org.eclipse.swt.widgets.Display;

/**
 * @author Damokles
 *
 */
public interface UISWTManager {

	public void addPluginView(ViewID parentID, String viewID, UIPluginEventListener view);

	public void openMainView (String viewID);

	public Display getDisplay();

}
