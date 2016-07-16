/*
 * Created on Apr 25, 2006
 * Created by omschaub
 *
 */

package lbms.tools.flexyconf.swt;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import lbms.tools.flexyconf.ContentProvider;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.FCInterface;
import lbms.tools.flexyconf.FlexyConfiguration;

import org.eclipse.swt.widgets.Display;

public class FlexyConfigMain {


	private Display display;
	private boolean terminated;
	private MainWindow mainWindow;
	private static FlexyConfigMain fcm;
	private FlexyConfiguration fc;
	private FCInterface fci;
	private Properties props;


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
		try {
			props = new Properties();
			fc = FlexyConfiguration.readFromFile(new File("lbms/tools/flexyconf/sampleConf.xml"));
			fci = fc.getFCInterface();
			fci.setContentProvider(new ContentProvider() {
				public String getDefaultValue(String key, int type) {
					System.out.println("Get Def: "+key+" type: "+type );
					String v = props.getProperty(key);
					if (v==null) {
						switch (type) {
						case Entry.TYPE_STRING:
							return "";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}
				public String getValue(String key, int type) {
					System.out.println("Get: "+key+" type: "+type );
					String v = props.getProperty(key);
					if (v==null) {
						switch (type) {
						case Entry.TYPE_STRING:
							return "";
						case Entry.TYPE_BOOLEAN:
							return "false";
						default:
							return "0";
						}
					}
					else return v;
				}
				public void setValue(String key, String value, int type) {
					System.out.println("Set: "+key+" value: "+value+" type: "+type);
					props.setProperty(key, value);
				}
			});
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		display = Display.getDefault();
		terminated = false;

		mainWindow = MainWindow.open(fc);


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
