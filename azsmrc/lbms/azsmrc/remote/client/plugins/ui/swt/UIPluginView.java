/**
 * 
 */
package lbms.azsmrc.remote.client.plugins.ui.swt;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Damokles
 *
 */
public interface UIPluginView {

	public void initialize(Composite parent);

	public String getID ();

	public ViewID getParent();

	public Composite getComposite();

	public void destroy();
}
