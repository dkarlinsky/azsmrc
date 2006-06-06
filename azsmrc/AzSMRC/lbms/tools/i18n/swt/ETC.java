package lbms.tools.i18n.swt;

import org.eclipse.swt.widgets.Display;

public class ETC {

	protected Display display;
	protected boolean terminated;
	private MainWindow mainWindow;

	private static ETC etc;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		start();
	}


	public static void start(){
		if(etc == null){
			etc = new ETC();
		}

		etc.open();

	}


	public ETC(){
		System.out.println("Starting up ETC");
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

	public static ETC getETC(){
		return etc;
	}

	public MainWindow getMainWindow(){
		return mainWindow;
	}

}//EOF
