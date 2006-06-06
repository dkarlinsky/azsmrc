/*
 * Created on Aug 4, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;


import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;



public class GUI_Utilities {

	/** Centers a Shell and opens it relative to the users Monitor
	 *
	 * @param shell
	 */

	public static void centerShellandOpen(Shell shell){
		//open shell
		shell.pack();

		//Center Shell
		Monitor primary = RCMain.getRCMain().getDisplay().getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = shell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y +(bounds.height - rect.height) / 2;
		shell.setLocation (x, y);

		//open shell
		shell.open();
	}

	public static void centerShellOpenAndFocus(Shell shell){
		centerShellandOpen(shell);
		shell.forceFocus();
	}

	/** Centers a Shell and opens it relative to given control
	 *
	 * @param shell
	 * @param control
	 */

	public static void centerShellRelativeToandOpen(final Shell shell, final Control control){
		//open shell
		shell.pack();

		//Center Shell

		final Rectangle bounds = control.getBounds();
		final Point shellSize = shell.getSize();
		shell.setLocation(
				bounds.x + (bounds.width / 2) - shellSize.x / 2,
				bounds.y + (bounds.height / 2) - shellSize.y / 2
		);

		//open shell
		shell.open();
	}

 /*   *//**
	 * returns the display by using the Plugin.java and the UISWTInstance
	 * @return Display
	 *//*

	public static Display getDisplay(){
		return Plugin.getDisplay();
	}*/


	/**
	 * Crops a name that is greater than 70 characters
	 * @param name
	 * @return name cropped to 70 characters
	 */
	public static String verifyName(String name){
		if(name == null)
			return name;

		if(name.length() > 70){
			name =  name.substring(0,68) + " . . . ";
		}


		return name.replaceAll("&","&&");

	}

	/** Open a messagebox with title and text
	 *
	 * @param parent
	 * @param style
	 * @param title
	 * @param text
	 * @return
	 */
	public static int openMessageBox(Shell parent, int style, String title,
			String text) {
		MessageBox mb = new MessageBox(parent, style);
		mb.setMessage(text);
		mb.setText(title);
		return mb.open();
	}

	/**
	 * Returns a string holding the rgb format of the color from the properties
	 * @param color in r000g000b000 format
	 * @return RGB
	 */
	public static RGB getRGB(String color){
		RGB rgb;
		try{
			int red = color.indexOf("r") + 1;
			int green = color.indexOf("g") + 1 ;
			int blue = color.indexOf("b") + 1;

			rgb = new RGB(new Integer(color.substring(red,green-1)).intValue(),
			new Integer(color.substring(green, blue-1)).intValue(),
			new Integer(color.substring(blue,color.length())).intValue());
			return rgb;
		}catch(Exception e){
			e.printStackTrace();
			return new RGB(0,0,0);
		}
	}



	public static String colorChooserDialog(RGB originalColor){
		//Choose color
		ColorDialog colorDialog = new ColorDialog(RCMain.getRCMain().getMainWindow().getShell());
		colorDialog.setText("Choose Color");
		colorDialog.setRGB(originalColor);
		RGB selectedColor = colorDialog.open();
		//Return String of color
		if(selectedColor != null){
			return ("r"+selectedColor.red + "g" + selectedColor.green + "b" + selectedColor.blue);
		}else return ("r"+originalColor.red + "g" + originalColor.green + "b" + originalColor.blue);

	}


}
