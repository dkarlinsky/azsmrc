/*
 * Created on Mar 31, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;

import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class SplashScreen {

	/**
	 * Open the main Splash Screen
	 * @param display
	 * @param tenths_secondsOpen -- tenths of seconds (100ms) to stay open
	 */

	private ProgressBar bar;
	private Label status;
	private Display display;
	private Shell splash;

	/**
	 * The main splashscreen
	 * 
	 * ** Be sure to set the maximum on the progress bar or by default it only goes to 10
	 * 
	 * @param _display
	 * @param tenths_secondsOpen
	 */
	public SplashScreen(Display _display){
		//set the display
		display = _display;


		//The shell with it's FormLayout
		splash = new Shell(SWT.ON_TOP);
		FormLayout layout = new FormLayout();
		splash.setLayout(layout);
		splash.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		//ProgressBar
		bar = new ProgressBar(splash, SWT.SMOOTH);
		bar.setMaximum(10);
		bar.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		FormData progressData = new FormData();
		progressData.left = new FormAttachment(0, 5);
		progressData.right = new FormAttachment(100, -5);
		progressData.bottom = new FormAttachment(100, -5);
		bar.setLayoutData(progressData);
		bar.pack();

		//Status Label for the splash screen
		status = new Label(splash, SWT.NONE);

		//increase the size of the font
		Font initialFont = status.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
		status.setFont(newFont);
		newFont.dispose();

		status.setText("Starting AzSMRC");
		status.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		FormData statusData = new FormData();
		statusData.left = new FormAttachment(0, 5);
		statusData.right = new FormAttachment(100, -5);
		statusData.bottom = new FormAttachment(100, -(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 7));
		status.setLayoutData(statusData);
		status.pack();

		//Image for the splash screen
		Label label = new Label(splash, SWT.NONE);
		label.setImage(ImageRepository.getImage("splash"));

		FormData labelData = new FormData();
		labelData.right = new FormAttachment(100, 0);
		labelData.bottom = new FormAttachment(100, - (bar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 7 /*Size of the Bar*/
				+  status.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y)); /*Size of the status label*/
		label.setLayoutData(labelData);

		//pack the shell
		splash.pack();




	}

	/**
	 * Sets the progressbar on the Splash Screen
	 * @param int count
	 */
	public void setProgressBarSelection(final int count){
		display.asyncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				if(count > bar.getMaximum())
					bar.setSelection(bar.getMaximum());
				else
					bar.setSelection(count);
			}
		});
	}

	/**
	 * Sets the maximum number on the progressbar on the Splash Screen
	 * @param int max
	 */
	public void setProgressBarMaximum(final int max){
		display.asyncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				bar.setMaximum(max);
			}
		});
	}

	/**
	 * Sets the maximum number on the progressbar on the Splash Screen
	 * @param int max
	 */
	public void setStatusText(final String text){
		display.asyncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				status.setText(text);
			}
		});
	}

	public void open(final int tenths_secondsOpen){
		display.asyncExec(new SWTSafeRunnable() {
			public void runSafe() {
				//open
				GUI_Utilities.centerShellandOpen(splash);
				//Timed kill
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






