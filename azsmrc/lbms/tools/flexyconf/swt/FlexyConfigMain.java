/*
 * Created on Apr 25, 2006
 * Created by omschaub
 *
 */

package lbms.tools.flexyconf.swt;

import org.eclipse.swt.widgets.Display;

public class FlexyConfigMain {

	
	private Display display;
	private boolean terminated;
	private MainWindow mainWindow;
	private static FlexyConfigMain fcm;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		start();

	}
	
	
	public static void start(){
		if(fcm == null){
			fcm = new FlexyConfigMain();
		}

		fcm.open();

	}
	
	public void open(){
		display = Display.getDefault();
		terminated = false;

		mainWindow = MainWindow.open();


		while (!terminated) { //runnig
			if (!display.readAndDispatch ()) display.sleep ();
		}
	}
	
	
	
	public void close() {
		terminated = true;
	}


	public Display getDisplay(){
		return display;
	}
	
	public MainWindow getMainWindow(){
		return mainWindow;
	}
	
	public static FlexyConfigMain getFlexyConfigMain(){
		return fcm;
	}

}
