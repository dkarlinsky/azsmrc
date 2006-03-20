/*
 * Created on Aug 4, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;


import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
        Monitor primary = FireFrogMain.getFFM().getDisplay().getPrimaryMonitor ();
        Rectangle bounds = primary.getBounds ();
        Rectangle rect = shell.getBounds ();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y +(bounds.height - rect.height) / 2;
        shell.setLocation (x, y);

        //open shell
        shell.open();
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
}
