/*
 * Created on Jan 25, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;


import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectionDialog {

    private int remoteConstants = RemoteConstants.ST_ALL;

    public ConnectionDialog(Display display){
        Properties properties = RCMain.getRCMain().getProperties();


        //Shell
        final Shell shell = new Shell(display);
        shell.setLayout(new GridLayout(1,false));
        shell.setText("Connect to Remote Server");

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
        url_label.setText("Servername or IP:");


        final Text url_text = new Text(miniComp1,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.widthHint = 228;
        url_text.setLayoutData(gridData);

        if(properties.containsKey("connection_url"))
            url_text.setText(properties.getProperty("connection_url"));

        url_text.addListener(SWT.FocusOut, new Listener(){

            public void handleEvent(Event arg0) {
                if(url_text.getText().startsWith("http://")){
                    url_text.setText(url_text.getText().substring(7));
                }else if(url_text.getText().startsWith("https://")){
                    url_text.setText(url_text.getText().substring(8));
                }

            }

        });

        Label help1 = new Label(miniComp1,SWT.NULL);
        help1.setImage(ImageRepository.getImage("information"));
        help1.setToolTipText("Examples:  \n'localhost' \n'workstation.azureusathome.net' \n'123.4.56.78'");



        Composite miniComp2 = new Composite(comp, SWT.NULL);
        miniComp2.setLayout(new GridLayout(2,false));
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        miniComp2.setLayoutData(gridData);

        //add in a second line for https
        final Button use_https = new Button(miniComp2,SWT.CHECK);
        use_https.setText("Use secure https protocol to connect to this server?");
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 1;
        use_https.setLayoutData(gridData);

        if(properties.containsKey("connection_https")){
            use_https.setSelection(properties.getProperty("connection_https").equalsIgnoreCase("true"));
        }

        Label help = new Label(miniComp2,SWT.NULL);
        help.setImage(ImageRepository.getImage("information"));
        help.setToolTipText("Click for insturctions on setting up a SSL connection");
        help.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        help.addListener(SWT.MouseDown, new Listener(){
            public void handleEvent(Event arg0) {
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText("SSL Information");
                messageBox.setMessage("a) Server - create certificate\n" + "    (Tools->Options->Security)\n\nb) Server - enable SSL in plugin options\n" + "    (Tools->Options->Plugins->AzMultiUser)\n\n" +  "c) Restart Azureus\n\n" + "d) Client - Click the SSL box on connection dialog");
                messageBox.open();
            }

        });

        //second line
        Label port_label = new Label(comp,SWT.NULL);
        port_label.setText("Port to Connect To:");


        final Text port_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
        gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 40;
        port_text.setLayoutData(gridData);

        if(properties.containsKey("connection_port"))
            port_text.setText(properties.getProperty("connection_port"));
        else
            port_text.setText("49009");


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
        username_label.setText("Username:");


        final Text username_text = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.widthHint = 100;
        username_text.setLayoutData(gridData);

        if(properties.containsKey("connection_username"))
            username_text.setText(properties.getProperty("connection_username"));

        //Fourth Line
        Label password_label = new Label(comp,SWT.NULL);
        password_label.setText("Password:");


        final Text password_text = new Text(comp,SWT.PASSWORD | SWT.BORDER | SWT.SINGLE | SWT.LEFT);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.widthHint = 100;
        password_text.setLayoutData(gridData);

        if(properties.containsKey("connection_password"))
            password_text.setText(properties.getProperty("connection_password"));

        //Fifth and Sixth line
        final Button save_settings = new Button(comp,SWT.CHECK);
        final Button save_password = new Button(comp,SWT.CHECK);


        save_settings.setText("Save settings for use on next connection");
        save_password.setText("Save password (Note: password will be saved in plain text)");


        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        save_settings.setLayoutData(gridData);

        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        save_password.setLayoutData(gridData);

        if(properties.containsKey("connection_username")){
            save_settings.setSelection(true);
            save_password.setEnabled(true);
        }
        if(properties.containsKey("connection_password")){
            save_password.setEnabled(true);
            save_password.setSelection(true);
        }else{
            if(save_settings.getSelection())
                save_password.setEnabled(true);
            else
                save_password.setEnabled(false);
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
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        button_comp.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        button_comp.setLayout(gridLayout);

        Button connect = new Button(button_comp,SWT.PUSH);
        connect.setText("Connect");
        connect.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                if(url_text.getText().equalsIgnoreCase("")
                    || port_text.getText().equalsIgnoreCase("")
                        || username_text.getText().equalsIgnoreCase("")
                            || password_text.getText().equalsIgnoreCase("")){
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("Please fill out all of the information.");
                    messageBox.open();
                    return;
                }

                //parse port and see if it is in range
                int port;
                try{
                    port = Integer.parseInt(port_text.getText());
                    if(port < 1 || port > 65000 ){
                        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText("Error");
                        messageBox.setMessage("Port given is out of range.  Please provide a port number between 1 and 65000.");
                        messageBox.open();
                        return;
                    }
                }catch(Exception f){
                   f.printStackTrace();
                   port = 49009; //default as a fallback although we should never get here
                }

                //Check the URL
                String url_string = url_text.getText();
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
                    messageBox.setText("Error");
                    messageBox.setMessage("The server URL is not valid.  Please fix.");
                    messageBox.open();
                    return;
                }
                Properties properties = RCMain.getRCMain().getProperties();
                if(save_settings.getSelection()){
                    properties.setProperty("connection_url", url_text.getText());
                    if(use_https.getSelection())
                        properties.setProperty("connection_https", "true");
                    else
                        properties.setProperty("connection_https", "false");
                    properties.setProperty("connection_port", port_text.getText());
                    properties.setProperty("connection_lastURL", url_string);
                    properties.setProperty("connection_username", username_text.getText());

                    if(save_password.getSelection()){
                        properties.setProperty("connection_password", password_text.getText());
                    }else
                        properties.remove("connection_password");
                }else{
                    //Clear out the keys here
                    properties.remove("connection_url");
                    properties.remove("connection_https");
                    properties.remove("connection_port");
                    properties.remove("connection_username");
                    properties.remove("connection_password");
                    properties.remove("connection_lastURL");

                }
                RCMain.getRCMain().saveConfig();

                final String username = username_text.getText();
                final String password = password_text.getText();

                Client client = RCMain.getRCMain().getClient();
                client.setServer(url);
                client.setUsername(username);
                client.setPassword(password);
                client.sendListTransfers(remoteConstants);
                RCMain.getRCMain().connect(true);

                //once connection is established.. send for all the right data
                RCMain.getRCMain().getMainWindow().initializeConnection();

                shell.close();
             }
         });


        Button cancel = new Button(button_comp,SWT.PUSH);
        cancel.setText("Cancel");
        cancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                shell.close();
             }
         });



        //Center and open shell
        GUI_Utilities.centerShellandOpen(shell);
    }
}
