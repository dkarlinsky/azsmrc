package lbms.tools.i18n.swt;

import org.eclipse.swt.widgets.Display;

public class I18NMe {

	protected Display display;
	protected boolean terminated;
	private MainWindow mainWindow;

	private static I18NMe i18nMe;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		start();
	}


	public static void start(){
		if(i18nMe == null){
			i18nMe = new I18NMe();
		}

		i18nMe.open();

	}


	public I18NMe(){
		System.out.println("Starting up I18NMe");
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

	public static I18NMe getI18NMe(){
		return i18nMe;
	}

	public MainWindow getMainWindow(){
		return mainWindow;
	}

}//EOF
