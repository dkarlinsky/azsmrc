/*
 * Created on Jan 25, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.LoginData;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class ConnectionDialog {

	//private int remoteConstants = RemoteConstants.ST_ALL;

	private Properties properties;

	private List<LoginData> loginDataList = new ArrayList<LoginData>();

	//SWT Stuff
	private Combo urlCombo;
	private Button use_https;
	private Text port_text;
	private Text username_text;
	private Text password_text;
	private Button save_settings;
	private Button save_password;

	private Shell shell;

	private static ConnectionDialog instance;

	//I18N prefix
	private static final String PFX = "dialog.connectiondialog.";

	private ConnectionDialog(Display display){
		instance = this;

		properties = RCMain.getRCMain().getProperties();

		LoginData lastCon = null;
		if (properties.containsKey("lastConnection"))
			try {
				lastCon = new LoginData(properties.getProperty("lastConnection"));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

		//Shell
		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));
		if(!lbms.azsmrc.remote.client.Utilities.isOSX)
			shell.setImage(ImageRepository.getImage("connect"));

		//Comp on shell
		Group comp = new Group(shell,SWT.NULL);
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 2;
		comp.setLayout(gridLayout);


		Composite miniComp1 = new Composite(comp, SWT.NULL);
		miniComp1.setLayout(new GridLayout(3,false));
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		miniComp1.setLayoutData(gridData);


		//first line
		Label url_label = new Label(miniComp1,SWT.NULL);
		url_label.setText(I18N.translate(PFX + "url.label"));


		urlCombo = new Combo(miniComp1,SWT.BORDER | SWT.FLAT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 235;
		urlCombo.setLayoutData(gridData);
		urlCombo.addListener(SWT.FocusOut, new Listener(){
			public void handleEvent(Event arg0) {
				if(urlCombo.getText().startsWith("http://")){
					urlCombo.setText(urlCombo.getText().substring(7));
				}else if(urlCombo.getText().startsWith("https://")){
					urlCombo.setText(urlCombo.getText().substring(8));
				}
			}
		});



		urlCombo.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int index = urlCombo.getSelectionIndex();
				loadIndex(index);
			}
		});


		Label help1 = new Label(miniComp1,SWT.NULL);
		help1.setImage(ImageRepository.getImage("information"));
		help1.setToolTipText(I18N.translate(PFX + "help1.tooltip"));



		Composite miniComp2 = new Composite(comp, SWT.NULL);
		miniComp2.setLayout(new GridLayout(2,false));
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		miniComp2.setLayoutData(gridData);

		//add in a second line for https
		use_https = new Button(miniComp2,SWT.CHECK);
		use_https.setText(I18N.translate(PFX + "use_https.label"));
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 1;
		use_https.setLayoutData(gridData);

		if(lastCon != null){
			use_https.setSelection(lastCon.isHttps());
		}

		Label help2 = new Label(miniComp2,SWT.NULL);
		help2.setImage(ImageRepository.getImage("information"));
		help2.setToolTipText(I18N.translate(PFX + "help2.tooltip"));
		help2.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		help2.addListener(SWT.MouseDown, new Listener(){
			public void handleEvent(Event arg0) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setText(I18N.translate(PFX + "help2.messagebox.title"));
				messageBox.setMessage(I18N.translate(PFX + "help2.messagebox.message"));
				messageBox.open();
			}

		});

		//second line
		Label port_label = new Label(comp,SWT.NULL);
		port_label.setText(I18N.translate(PFX + "port.label"));


		port_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.BEGINNING);
		gridData.widthHint = 40;
		port_text.setLayoutData(gridData);

		port_text.setText(lastCon != null?
				Integer.toString(lastCon.getPort()):"49009");


		port_text.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		//Third Line
		Label username_label = new Label(comp,SWT.NULL);
		username_label.setText(I18N.translate(PFX + "username.label"));


		username_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 100;
		username_text.setLayoutData(gridData);


		if(lastCon != null)
			username_text.setText(lastCon.getUsername());

		//Fourth Line
		Label password_label = new Label(comp,SWT.NULL);
		password_label.setText(I18N.translate(PFX + "password.label"));


		password_text = new Text(comp,SWT.PASSWORD | SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 100;
		password_text.setLayoutData(gridData);

		if(lastCon != null)
			password_text.setText(lastCon.getPassword());


		//Fifth and Sixth line
		save_settings = new Button(comp,SWT.CHECK);
		save_password = new Button(comp,SWT.CHECK);


		save_settings.setText(I18N.translate(PFX + "save_settings.label"));
		save_password.setText(I18N.translate(PFX + "save_password.label"));


		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		save_settings.setLayoutData(gridData);

		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		save_password.setLayoutData(gridData);

		if (lastCon != null) {
			save_settings.setSelection(true);
			save_password.setEnabled(true);
			save_password.setSelection(!lastCon.getPassword().equals(""));
		} else {
			save_settings.setSelection(false);
			save_password.setEnabled(false);
			save_password.setSelection(false);
		}

		save_settings.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				if(save_settings.getSelection()){
					save_password.setEnabled(true);
				}else{
					save_password.setSelection(false);
					save_password.setEnabled(false);
				}
			}
		});

		//Seventh Line
		Composite button_comp = new Composite(shell, SWT.NULL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		button_comp.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

		Button saveProfile = new Button(button_comp,SWT.PUSH);
		saveProfile.setText(I18N.translate(PFX + "saveprofile.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		saveProfile.setLayoutData(gridData);
		saveProfile.setFocus();
		saveProfile.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				addConnection();
				loadConnections();
			}
		});

		Button delete = new Button(button_comp, SWT.PUSH);
		delete.setText(I18N.translate(PFX + "removeprofile.text"));
		delete.setToolTipText(I18N.translate(PFX + "removeprofile.tooltip"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessHorizontalSpace = true;
		delete.setLayoutData(gridData);
		delete.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				int index = urlCombo.getSelectionIndex();

				use_https.setSelection(false);
				port_text.setText("");
				username_text.setText("");
				save_settings.setSelection(false);
				save_password.setSelection(false);
				save_password.setEnabled(false);
				password_text.setText("");

				deleteConnection(index);

				fillCombo();

				if(port_text.getText().equalsIgnoreCase(""))
					port_text.setText("49009");
			}
		});



		Button connect = new Button(button_comp,SWT.PUSH);
		connect.setText(I18N.translate(PFX + "connect.text"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		connect.setLayoutData(gridData);
		connect.setFocus();
		connect.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				connectGo();
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				shell.close();
			}
		});

		//Add a CR listener to everything on comp
		Control[] controls = comp.getChildren();
		for(Control control:controls){
			add_CR_KeyListener(control);
		}

		shell.addShellListener(new ShellAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ShellAdapter#shellClosed(org.eclipse.swt.events.ShellEvent)
			 */
			@Override
			public void shellClosed(ShellEvent e) {
				saveToConfig();
			}
		});

		fillCombo();

		//Center and open shell
		GUI_Utilities.centerShellandOpen(shell);
	}

	private void loadIndex(int index){
		if (index < 0) return;

		LoginData x = null;
		if (index == loginDataList.size())
			try {
				x = new LoginData(properties.getProperty("lastConnection"));
			} catch (MalformedURLException e) {
				if (loginDataList.size() > 0)
					x = loginDataList.get(0);
				else x = new LoginData("",49009,"","",false); //default values
			}
		else
			x = loginDataList.get(index);
		//https button
		use_https.setSelection(x.isHttps());

		//port
		port_text.setText(Integer.toString(x.getPort()));

		//username
		username_text.setText(x.getUsername());

		//password
		password_text.setText(x.getPassword());

		//button settings
		if(!x.getUsername().equals("")){
			save_settings.setSelection(true);
			save_password.setEnabled(true);
		}
		if(!x.getPassword().equals("")){
			save_password.setEnabled(true);
			save_password.setSelection(true);
		}else{
			if(save_settings.getSelection())
				save_password.setEnabled(true);
			else
				save_password.setEnabled(false);
		}
	}

	private void fillCombo(){
		urlCombo.removeAll();
		loadConnections();
		LoginData lastCon = null;
		if (properties.containsKey("lastConnection")) {
			if (properties.containsKey("lastConnection"))
				try {
					lastCon = new LoginData(properties.getProperty("lastConnection"));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					lastCon = null;
				}
		}

		for(int i = 0; i < loginDataList.size(); i++) {
			urlCombo.add(loginDataList.get(i).getHost());
			if (lastCon != null && lastCon.equals(loginDataList.get(i))) {
				urlCombo.select(i);
				loadIndex(i);
			}
		}

		if (lastCon != null && !loginDataList.contains(lastCon)) {
			urlCombo.add(lastCon.getHost());
			urlCombo.select(urlCombo.getItems().length-1);
			loadIndex(urlCombo.getItems().length-1);
		}
	}


	private void connectGo(){
		RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable(){

			public void runSafe() {

				// Add protection as someone got an error.. although I have
				// NO idea how this was ever disposed!
				if(urlCombo == null || urlCombo.isDisposed()) return;


				if(urlCombo.getText().equalsIgnoreCase("")
						|| port_text.getText().equalsIgnoreCase("")
						|| username_text.getText().equalsIgnoreCase("")
						|| password_text.getText().equalsIgnoreCase("")){
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "connect.messagebox.message.fillall"));
					messageBox.open();
					return;
				}

				//parse port and see if it is in range
				int port;
				try{
					port = Integer.parseInt(port_text.getText());
					if(port < 1 || port > 65535 ){
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate("global.error"));
						messageBox.setMessage(I18N.translate(PFX + "connect.messagebox.message.portrange"));
						messageBox.open();
						return;
					}
				}catch(Exception f){
					f.printStackTrace();
					port = 49009; //default as a fallback although we should never get here
				}

				//Check the URL
				String url_string = urlCombo.getText();
				if(use_https.getSelection() && !url_string.startsWith("https://")){
					if(url_string.startsWith("http://")){
						url_string = url_string.substring(7);
					}
					url_string = "https://" + url_string + ":" + port_text.getText();
				}else if(!use_https.getSelection() && !url_string.startsWith("http://")){
					if(url_string.startsWith("https://")){
						url_string = url_string.substring(8);
					}
					url_string = "http://" + url_string + ":" + port_text.getText();
				}else
					url_string = url_string + ":" + port_text.getText();
				try{
					new URL(url_string);
					//System.out.println(url_string);
				}catch(Exception f){
					f.printStackTrace();
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "connect.messagebox.message.validURL"));
					messageBox.open();
					return;
				}
				Properties properties = RCMain.getRCMain().getProperties();

				if(save_settings.getSelection()){
					LoginData logData;
					if(save_password.getSelection())
						logData = new LoginData(urlCombo.getText(),port,username_text.getText(),password_text.getText(),use_https.getSelection());
					else
						logData = new LoginData(urlCombo.getText(),port,username_text.getText(),"",use_https.getSelection());
					properties.setProperty("lastConnection",logData.serialize());
					RCMain.getRCMain().saveConfig();
				}

				LoginData logData = new LoginData(urlCombo.getText(),port,username_text.getText(),password_text.getText(),use_https.getSelection());
				Client client = RCMain.getRCMain().getClient();

				client.setLoginData(logData);

				client.getDownloadManager().update(false);
				RCMain.getRCMain().connect(true);

				//once connection is established.. send for all the right data
				RCMain.getRCMain().getMainWindow().initializeConnection();

				shell.close();
			}
		});
	}

	private void add_CR_KeyListener(final Control control){
		RCMain.getRCMain().getDisplay().asyncExec(new SWTSafeRunnable(){

			public void runSafe() {
				control.addKeyListener(new KeyListener(){
					public void keyPressed(KeyEvent arg0) {
						if(arg0.keyCode == SWT.CR){
							connectGo();
						}
					}
					public void keyReleased(KeyEvent arg0) {}

				});
			}
		});
	}


	/**
	 * Static open method
	 */
	public static void open(Display display){
		if(display == null) return;
		if (instance == null || instance.shell == null || instance.shell.isDisposed()){
			new ConnectionDialog(display);
		}else
			instance.shell.setActive();
	}

	/**
	 * Deletes a connection.
	 * 
	 * @param id connection ID to delete
	 */
	public void deleteConnection (int id) {
		if (loginDataList.size()<=id) return;
		loginDataList.remove(id);
		saveToConfig ();
		fillCombo();
	}

	/**
	 * Saves a Connection to a new ID
	 */
	public void addConnection () {
		LoginData logData;
		if (save_password.getSelection()) {
			logData = new LoginData(urlCombo.getText(),Integer.parseInt(port_text.getText()),username_text.getText(),password_text.getText(),use_https.getSelection());
		} else {
			logData = new LoginData(urlCombo.getText(),Integer.parseInt(port_text.getText()),username_text.getText(),"",use_https.getSelection());
		}

		if (loginDataList.contains(logData)) {
			//TODO: Popup a warning or something else to notify of duplicated Connection
		} else {
			loginDataList.add(logData);
			Collections.sort(loginDataList);
			fillCombo();
			saveToConfig ();
		}
	}

	public void saveToConfig () {
		int i;
		for (i = 0; i<loginDataList.size();i++) {
			properties.setProperty("connection_"+i, loginDataList.get(i).serialize());
		}
		//remove non used connections
		while (properties.containsKey("connection_"+i)) {
			properties.remove("connection_"+i);
			i++;
		}
		RCMain.getRCMain().saveConfig();
	}

	public void loadConnections () {
		loginDataList.clear();
		for (int i = 0;properties.containsKey("connection_"+i);i++) {
			try {
				loginDataList.add(new LoginData(properties.getProperty("connection_"+i)));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(loginDataList);
	}
}//EOF
