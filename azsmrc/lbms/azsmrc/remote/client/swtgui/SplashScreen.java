/*
 * Created on Mar 31, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class SplashScreen {

    /**
     * Open the main Splash Screen
     * @param display
     * @param tenths_secondsOpen -- tenths of seconds (100ms) to stay open
     */
    public SplashScreen(final Display display, final int tenths_secondsOpen){


        final Shell splash = new Shell(SWT.ON_TOP);
        //final ProgressBar bar = new ProgressBar(splash, SWT.NONE);
        //bar.setMaximum(count[0]);
        Label label = new Label(splash, SWT.NONE);
        label.setImage(ImageRepository.getImage("splash"));
        FormLayout layout = new FormLayout();
        splash.setLayout(layout);
        FormData labelData = new FormData();
        labelData.right = new FormAttachment(100, 0);
        labelData.bottom = new FormAttachment(100, 0);
        label.setLayoutData(labelData);
        FormData progressData = new FormData();
        progressData.left = new FormAttachment(0, 5);
        progressData.right = new FormAttachment(100, -5);
        progressData.bottom = new FormAttachment(100, -5);
        //bar.setLayoutData(progressData);
        splash.pack();

        GUI_Utilities.centerShellandOpen(splash);

        display.syncExec(new Runnable() {
            public void run() {
                for (int i = 0; i < tenths_secondsOpen; i++) {
                    //bar.setSelection(i + 1);
                    try {
                        Thread.sleep(100);
                    } catch (Throwable e) {
                    }
                }
                splash.close();
            }
        });
    }
}






