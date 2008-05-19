/**
 * 
 */
package lbms.azsmrc.plugin.gui;

import org.eclipse.swt.widgets.Display;
import org.gudy.azureus2.ui.swt.plugins.UISWTInstance;

/**
 * @author Leonard
 * 
 */
public class SWTUtil {
	private static Display	display;

	public static Display getDisplay () {
		return display;
	}

	public static void setDisplay (UISWTInstance swtInstance) {
		display = swtInstance.getDisplay();
	}
}
