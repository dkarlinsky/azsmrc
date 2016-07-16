/*
 * Created on Mar 31, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;

import java.io.InputStream;

import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class SplashScreen {

	private static SplashScreen instance;

	private ProgressBar bar;
	private Label status;
	private Display display;
	private Shell splash, statusShell;
	private Image image;

	/**
	 * The main splashscreen
	 * 
	 * ** Be sure to set the maximum on the progress bar or by default it only goes to 10
	 * 
	 * @param _display
	 * @param tenths_secondsOpen
	 */
	private SplashScreen(Display _display){
		//set the display
		display = _display;
		instance = this;

		//load the image
		//String res = "lbms/azsmrc/remote/client/swtgui/resources/AzSMRC_Splash_New.png";
		String res = "lbms/azsmrc/remote/client/swtgui/resources/frogandpad.png";
		InputStream is = ImageRepository.class.getClassLoader().getResourceAsStream(res);
		image = new Image(display,is);
		ImageData data = image.getImageData();

		//The shell with it's FormLayout
		splash = new Shell(SWT.ON_TOP | SWT.NO_TRIM | SWT.NO_BACKGROUND);

		//define a region
		final Region region = new Region();
		Rectangle pixel = new Rectangle(0, 0, 1, 1);
		for (int y = 0; y < 263; y++) {
				for (int x = 0; x < 350; x++) {
					pixel.x = x;
					pixel.y = y;
					if(data.getAlpha(x, y) == 255)
						region.add(pixel);
				}
			}




		//define the shape of the shell using setRegion
		splash.setRegion(region);
		Rectangle size = region.getBounds();
		splash.setSize(size.width, size.height);
		splash.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle bounds = image.getBounds();
				Point size = splash.getSize();
				e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 10, 10, size.x-20, size.y-20);
			}
		});


		splash.addDisposeListener(new DisposeListener () {

			public void widgetDisposed(DisposeEvent arg0) {
				region.dispose();
				statusShell.close();
				image.dispose();
				instance = null;
			}
		});


		FormLayout layout = new FormLayout();
		splash.setLayout(layout);
		//splash.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		statusShell = new Shell(display, SWT.ON_TOP);

		statusShell.setLayout(new FormLayout());
		statusShell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		//ProgressBar
		bar = new ProgressBar(statusShell, SWT.SMOOTH);
		bar.setMaximum(100);
		bar.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		FormData progressData = new FormData();
		progressData.width = 350;
		progressData.left = new FormAttachment(0, 5);
		progressData.right = new FormAttachment(100, -5);
		progressData.bottom = new FormAttachment(100, -5);
		bar.setLayoutData(progressData);
		bar.pack();

		//Status Label for the splash screen
		status = new Label(statusShell, SWT.NONE);

		//increase the size of the font
		Font initialFont = status.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(display, fontData);
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
		label.setImage(image);

		FormData labelData = new FormData();
		labelData.right = new FormAttachment(100, 0);
		labelData.bottom = new FormAttachment(100, 0 /*(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y + 7 Size of the Bar
				+  status.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y)*/); /*Size of the status label*/
		label.setLayoutData(labelData);

		//pack the shell
		splash.pack();
		statusShell.pack();
	}



	/**
	 * Sets the progressbar on the Splash Screen
	 * @param int count
	 */
	private void setProgressBarSelection(final int count){
		display.syncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				if(count >= bar.getMaximum()) {
					bar.setSelection(bar.getMaximum());
					close();
				}
				else
					bar.setSelection(count);
			}
		});
	}

	private void close() {
		splash.close();
		/*if(!splash.isDisposed())
			splash.dispose();*/
	}

	/**
	 * Sets the maximum number on the progressbar on the Splash Screen
	 * @param int max
	 */
	private void setStatusText(final String text){
		display.syncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				status.setText(text);
			}
		});
	}

	public static void open(final Display _display, final int tenths_secondsOpen){
		_display.syncExec(new SWTSafeRunnable() {
			public void runSafe() {
				new SplashScreen(_display);
				GUI_Utilities.centerShellandOpen(instance.splash);

				//open shell
				instance.statusShell.pack();

				//Center Shell
				Monitor primary = RCMain.getRCMain().getDisplay().getPrimaryMonitor ();
				Rectangle bounds = primary.getBounds ();
				Rectangle rect = instance.statusShell.getBounds ();
				int x = bounds.x + (bounds.width - rect.width) / 2;
				//int y = bounds.y +(bounds.height - rect.height) / 2;
				instance.statusShell.setLocation (x, instance.splash.getLocation().y + 310);

				//open shell
				instance.statusShell.open();

			}
		});
	}

	public static void setProgress (int prog) {
		if (instance != null)
			instance.setProgressBarSelection(prog);
	}

	public static void setText (String text) {
		System.out.println ("Startup: "+text);
		if (instance != null)
			instance.setStatusText(text);
	}

	public static void setProgressAndText (String text, int progress) {
		System.out.println ("Startup: "+text);
		if (instance != null) {
			instance.setStatusText(text);
			instance.setProgressBarSelection(progress);
		}
	}

}






