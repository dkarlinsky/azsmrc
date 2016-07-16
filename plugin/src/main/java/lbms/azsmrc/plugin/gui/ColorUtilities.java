/*
 * Created on Dec 6, 2005 Created by omschaub
 * 
 */
package lbms.azsmrc.plugin.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorUtilities {
	private static Color	BACKGROUND;
	private static Color	DARK_BACKGROUND;

	/**
	 * Pull a BackgroundColor for a list for shading (rgb of listbackground -
	 * 10)
	 * 
	 * @return Color
	 */
	public static Color getBackgroundColor () {

		if (SWTUtil.getDisplay() == null && SWTUtil.getDisplay().isDisposed()) {
			BACKGROUND = null;
			return BACKGROUND;
		}
		try {
			BACKGROUND = new Color(SWTUtil.getDisplay(), new RGB(SWTUtil
					.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND)
					.getRed() - 10, SWTUtil.getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND).getGreen() - 10, SWTUtil
					.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND)
					.getBlue() - 10));

		} catch (Exception e) {
			BACKGROUND = SWTUtil.getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND);
		}

		return BACKGROUND;
	}

	/**
	 * Pulls a darker background color (rgb of widget background - 10)
	 * 
	 * @return Color
	 */
	public static Color getDark_BackgroundColor () {

		if (SWTUtil.getDisplay() == null && SWTUtil.getDisplay().isDisposed()) {
			DARK_BACKGROUND = null;
			return DARK_BACKGROUND;
		}
		try {
			DARK_BACKGROUND = new Color(SWTUtil.getDisplay(), new RGB(SWTUtil
					.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
					.getRed() - 10, SWTUtil.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND).getGreen() - 10, SWTUtil
					.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
					.getBlue() - 10));
		} catch (Exception e) {
			DARK_BACKGROUND = SWTUtil.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND);
		}

		return DARK_BACKGROUND;
	}

	public static void unloadColors () {
		if (BACKGROUND != null && !BACKGROUND.isDisposed()) {
			BACKGROUND.dispose();
		}
		if (DARK_BACKGROUND != null && !DARK_BACKGROUND.isDisposed()) {
			DARK_BACKGROUND.dispose();
		}
	}
}
