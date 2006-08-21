/*
 * Created on Jan 25, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;


import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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


		//Shell
		shell = new Shell(display);
		shell.setLayout(new GridLayout(1,false));
		shell.setText(I18N.translate(PFX + "shell.text"));

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

		if(properties.containsKey("connection_https_0")){
			use_https.setSelection(Boolean.parseBoolean(properties.getProperty("connection_https_0")));
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

		port_text.setText(properties.containsKey("connection_port_0")?
				properties.getProperty("connection_port_0"):"49009");


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


		if(properties.containsKey("connection_username_0"))
			username_text.setText(properties.getProperty("connection_username_0"));

		//Fourth Line
		Label password_label = new Label(comp,SWT.NULL);
		password_label.setText(I18N.translate(PFX + "password.label"));


		password_text = new Text(comp,SWT.PASSWORD | SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 100;
		password_text.setLayoutData(gridData);

		if(properties.containsKey("connection_password_0"))
			password_text.setText(properties.getProperty("connection_password_0"));


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


		save_settings.setSelection(properties.containsKey("connection_username_0"));
		save_password.setEnabled(properties.containsKey("connection_username_0"));
		save_password.setSelection(properties.containsKey("connection_password_0"));


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
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);

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

				if(index == 0){

					removePropertySet(0);

					if(containsNumber(1) && containsNumber(2)){
						shiftProperties(1, 0);
						shiftProperties(2, 1);
						removePropertySet(2);
					}else if(containsNumber(1) && !containsNumber(2)){
						shiftProperties(1, 0);
						removePropertySet(1);
					}
				}else if(index == 1){
					if(containsNumber(2)){
						shiftProperties(2,1);
						removePropertySet(2);
					}else
						removePropertySet(1);
				}else if(index == 2){
					removePropertySet(2);
				}


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



		fillCombo();

		//Center and open shell
		GUI_Utilities.centerShellandOpen(shell);
	}


	private void shiftProperties(int from, int to){

		if(properties.containsKey("connection_url_" + from))
			properties.setProperty("connection_url_" + to, properties.getProperty("connection_url_" + from));

		//we always have a ssl setting.. so shift it down
		if(properties.containsKey("connection_https_" + from))
			properties.setProperty("connection_https_" + to, properties.getProperty("connection_https_" + from));

		//we always have a port, so shift it down
		if(properties.containsKey("connection_port_" + from))
			properties.setProperty("connection_port_" + to, properties.getProperty("connection_port_" + from));

		//we should always have the lastURL.. so shift it down
		if(properties.containsKey("connection_lastURL_" + from))
			properties.setProperty("connection_lastURL_" + to, properties.getProperty("connection_lastURL_" + from));

		//we are in a save settings situation.. so shift it down
		if(properties.containsKey("connection_username_" + from))
			properties.setProperty("connection_username_" + to, properties.getProperty("connection_username_" + from));


		if(properties.containsKey("connection_password_" + from))
			properties.setProperty("connection_password_" + to, properties.getProperty("connection_password_" + from));

		RCMain.getRCMain().saveConfig();
	}

	private void removePropertySet(int set){
		properties.remove("connection_https_" + set);
		properties.remove("connection_lastURL_" + set);
		properties.remove("connection_port_" + set);
		properties.remove("connection_password_" + set);
		properties.remove("connection_url_" + set);
		properties.remove("connection_username_" + set);
		RCMain.getRCMain().saveConfig();
	}

	private void loadIndex(int index){
		//https button
		if(properties.containsKey("connection_https_" + index )){
			use_https.setSelection(Boolean.parseBoolean(properties.getProperty("connection_https_" + index)));
		}

		//port
		if(properties.containsKey("connection_port_" + index))
			port_text.setText(properties.getProperty("connection_port_" + index));
		else
			port_text.setText("49009");

		//username
		if(properties.containsKey("connection_username_" + index))
			username_text.setText(properties.getProperty("connection_username_" + index));


		//password
		if(properties.containsKey("connection_password_" + index))
			password_text.setText(properties.getProperty("connection_password_" + index));


		//button settings
		if(properties.containsKey("connection_username_" + index)){
			save_settings.setSelection(true);
			save_password.setEnabled(true);
		}
		if(properties.containsKey("connection_password_" + index)){
			save_password.setEnabled(true);
			save_password.setSelection(true);
		}else{
			if(save_settings.getSelection())
				save_password.setEnabled(true);
			else
				save_password.setEnabled(false);
		}
	}

	private boolean containsNumber(int number){
		if(properties.containsKey("connection_url_" + number) &&
				properties.containsKey("connection_username_" + number))
			return true;
		else
			return false;
	}

	private void fillCombo(){
		urlCombo.removeAll();
		if(!containsNumber(0) && containsNumber(2)){
			 shiftProperties(2,0);
			 removePropertySet(2);
			 if(containsNumber(3)){
				 shiftProperties(3,1);
				 removePropertySet(3);
			 }
		}


		for(int i = 0; i < 3; i++){
			if(containsNumber(i)){
				urlCombo.add(properties.getProperty("connection_url_" + i));
				urlCombo.select(0);
			}
		}

		if(containsNumber(0))
			loadIndex(0);
	}


	private void connectGo(){
		RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

			public void run() {
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
					if(port < 1 || port > 65000 ){
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
				final URL url;
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
					url = new URL(url_string);
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

					boolean bpresent = false;

					for(int i = 0; i < 3; i++){
						if(properties.containsKey("connection_lastURL_" + i)){
							/*System.out.println("connection_lastURL_" + i + ":  " + properties.getProperty("connection_lastURL_" + i) + " | " + url_string +
									" || " + properties.getProperty("connection_username_" + i) + " | " + username_text.getText());*/
							if(properties.getProperty("connection_lastURL_" + i).equalsIgnoreCase(url_string) &&
									properties.getProperty("connection_username_" + i).equalsIgnoreCase(username_text.getText())){
								bpresent = true;
							}
						}
					}

					if(!bpresent){
						if(properties.containsKey("connection_url_1"))
							shiftProperties(1, 2);
						if(properties.containsKey("connection_url_0"))
							shiftProperties(0, 1);
					}



					properties.setProperty("connection_url_0", urlCombo.getText());

					properties.setProperty("connection_https_0", Boolean.toString(use_https.getSelection()));
					properties.setProperty("connection_port_0", port_text.getText());
					properties.setProperty("connection_lastURL_0", url_string);
					properties.setProperty("connection_username_0", username_text.getText());

					if(save_password.getSelection()){
						properties.setProperty("connection_password_0", password_text.getText());
					}else
						properties.remove("connection_password_0");
				}
				RCMain.getRCMain().saveConfig();

				final String username = username_text.getText();
				final String password = password_text.getText();

				Client client = RCMain.getRCMain().getClient();
				client.setServer(url);
				client.setUsername(username);
				client.setPassword(password);
				client.getDownloadManager().update(false);
				RCMain.getRCMain().connect(true);

				//once connection is established.. send for all the right data
				RCMain.getRCMain().getMainWindow().initializeConnection();

				shell.close();
			}
		});
	}

	private void add_CR_KeyListener(final Control control){
		RCMain.getRCMain().getDisplay().asyncExec(new Runnable(){

			public void run() {
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


}//EOF
