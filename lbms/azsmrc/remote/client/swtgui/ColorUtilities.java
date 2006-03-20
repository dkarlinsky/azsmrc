/*
 * Created on Feb 24, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorUtilities {
    private static Color BACKGROUND;
    private static Color DARK_BACKGROUND;

    public static Color getBackgroundColor(){


            if(FireFrogMain.getFFM().getDisplay()==null && FireFrogMain.getFFM().getDisplay().isDisposed()){
                BACKGROUND = null;
                return BACKGROUND;
            }
            try{
                BACKGROUND = new Color(FireFrogMain.getFFM().getDisplay() ,
                        new RGB(FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRed()-10,
                                FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getGreen()-10,
                                FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getBlue()-10));

            }catch(Exception e){
                BACKGROUND = FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
            }



        return BACKGROUND;
    }

    public static Color getDark_BackgroundColor(){


            if(FireFrogMain.getFFM().getDisplay()==null && FireFrogMain.getFFM().getDisplay().isDisposed()){
                DARK_BACKGROUND = null;
                return DARK_BACKGROUND;
            }
            try{
                DARK_BACKGROUND = new Color(FireFrogMain.getFFM().getDisplay() ,
                        new RGB(FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRed()-10,
                                FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getGreen()-10,
                                FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getBlue()-10));
            }catch(Exception e){
                DARK_BACKGROUND = FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
            }




        return DARK_BACKGROUND;
    }

    public static void unloadColors(){
        if (BACKGROUND != null && !BACKGROUND.isDisposed())
            BACKGROUND.dispose();
        if (DARK_BACKGROUND != null && !DARK_BACKGROUND.isDisposed())
            DARK_BACKGROUND.dispose();
    }
}
