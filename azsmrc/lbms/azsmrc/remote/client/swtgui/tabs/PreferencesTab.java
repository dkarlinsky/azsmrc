/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.swtgui.FireFrogMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

public class PreferencesTab {

    private Text updateIntervalOpen_Text, updateIntervalClosed_Text;
    private Button autoOpen, autoConnect, autoUpdateCheck, autoUpdate;
    private Button trayMinimize, trayExit, popupsEnabled, autoClipboard, autoConsole;

    public PreferencesTab(final CTabFolder parentTab){

        final CTabItem prefsTab = new CTabItem(parentTab, SWT.CLOSE);
        prefsTab.setText("Preferences");

        //ScrollComp on shell
        final ScrolledComposite scrolledComposite = new ScrolledComposite(parentTab,  SWT.V_SCROLL);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);


        final Composite parent = new Composite(scrolledComposite, SWT.NULL);
        parent.setLayout(new GridLayout(1,false));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 1;
        parent.setLayoutData(gridData);

        //top label
        Composite grayLabel = new Composite(parent,SWT.BORDER);
        grayLabel.setBackground(FireFrogMain.getFFM().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        grayLabel.setLayout(new GridLayout(1,false));
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        grayLabel.setLayoutData(gridData);

        Label title = new Label(grayLabel,SWT.NONE);
        title.setText("No changes are actually made until committed");
        title.setBackground(grayLabel.getBackground());

        //Set it bold
        Font initialFont = title.getFont();
        FontData[] fontData = initialFont.getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setStyle(SWT.BOLD);
            fontData[i].setHeight(fontData[i].getHeight() + 2);
        }
        Font newFont = new Font(FireFrogMain.getFFM().getDisplay(), fontData);
        title.setFont(newFont);
        newFont.dispose();

        grayLabel.pack();




        //Open properties for reading and saving
        final Properties properties = FireFrogMain.getFFM().getProperties();


        //Buttons
        Composite button_comp = new Composite(parent, SWT.NULL);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        button_comp.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        button_comp.setLayout(gridLayout);




        Button commit = new Button(button_comp,SWT.PUSH);
        commit.setText("Commit Changes");
        commit.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {

                String open = updateIntervalOpen_Text.getText();
                String closed = updateIntervalClosed_Text.getText();
                if(open.equalsIgnoreCase("") || closed.equalsIgnoreCase("") ){
                    MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("None of the intervals can be blank.");
                    messageBox.open();
                    return;
                }else if(Long.parseLong(open) < 3){
                    updateIntervalOpen_Text.setText("3");
                    MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("The interval for when the main window is open is too low.  Please set to 3 seconds or above.");
                    messageBox.open();
                    return;
                }else if(Long.parseLong(closed) < 3){
                    updateIntervalClosed_Text.setText("3");
                    MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("The interval for when the main window is closed is too low.  Please set to 3 seconds or above.");
                    messageBox.open();
                    return;
                }else{
                    properties.setProperty("connection_interval_open", String.valueOf(Long.parseLong(open)*1000));
                    properties.setProperty("connection_interval_closed", String.valueOf(Long.parseLong(closed)*1000));
                }

                //Store AutoOpen
                if(autoOpen.getSelection())
                    properties.setProperty("auto_open", "true");
                else
                    properties.setProperty("auto_open", "false");

                //Store AutoSave
                if(autoConnect.getSelection())
                    properties.setProperty("auto_connect", "true");
                else
                    properties.setProperty("auto_connect", "false");

                //Store AutoUpdateCheck
                if(autoUpdateCheck.getSelection())
                    properties.setProperty("update.autocheck", "true");
                else
                    properties.setProperty("update.autocheck", "false");


                //Store AutoUpdate
                if(autoUpdate.getSelection())
                    properties.setProperty("update.autoupdate", "true");
                else
                    properties.setProperty("update.autoupdate", "false");

                //Store tray options
                if(trayMinimize.getSelection())
                    properties.setProperty("tray.minimize","true");
                else
                    properties.setProperty("tray.minimize","false");

                if(trayExit.getSelection())
                    properties.setProperty("tray.exit","true");
                else
                    properties.setProperty("tray.exit","false");

                //Store popupsEnabled
                if(popupsEnabled.getSelection())
                    properties.setProperty("popups_enabled", "true");
                else
                    properties.setProperty("popups_enabled", "false");

                //Store the autoClipboard setting
                if(autoClipboard.getSelection())
                    properties.setProperty("auto_clipboard", "true");
                else
                    properties.setProperty("auto_clipboard", "false");

                //Store auto console setting
                if(autoConsole.getSelection())
                    properties.setProperty("auto_console", "true");
                else
                    properties.setProperty("auto_console", "false");

                FireFrogMain.getFFM().saveConfig();


                FireFrogMain.getFFM().updateTimer(true);
                prefsTab.dispose();
            }
        });


        Button cancel = new Button(button_comp,SWT.PUSH);
        cancel.setText("Cancel");
        cancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                prefsTab.dispose();
            }
        });





        final Composite comp = new Composite(parent,SWT.NULL);
        gridData = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginWidth = 2;
        comp.setLayout(gridLayout);

        //---------------------- Connection Preferences -------------------\\

        Group gConnection = new Group(comp,SWT.NULL);
        gConnection.setText("Connection Preferences");
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gConnection.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        gConnection.setLayout(gridLayout);



        //Auto Connect
        autoConnect = new Button(gConnection,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoConnect.setLayoutData(gridData);
        autoConnect.setText("AutoConnect: If connection data and password are saved, attempt last connection on startup");

        if (Boolean.parseBoolean(properties.getProperty("auto_connect","true"))) {
            autoConnect.setSelection(true);
        }


        //Update Interval Open
        Label updateIntervalOpen_Label = new Label(gConnection,SWT.NULL);
        updateIntervalOpen_Label.setText("Update Interval while the main window is open (in seconds, minimum of 3):       ");

        updateIntervalOpen_Text = new Text(gConnection, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = 30;
        updateIntervalOpen_Text.setLayoutData(gridData);
        updateIntervalOpen_Text.setText(String.valueOf(Long.parseLong((properties.getProperty("connection_interval_open","5000")))/1000));
        updateIntervalOpen_Text.addListener (SWT.Verify, new Listener () {
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


        //Update Interval Closed
        Label updateIntervalClosed_Label = new Label(gConnection,SWT.NULL);
        updateIntervalClosed_Label.setText("Update Interval while the main window is closed (in seconds, minimum of 3):      ");

        updateIntervalClosed_Text = new Text(gConnection, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = 30;
        updateIntervalClosed_Text.setLayoutData(gridData);
        updateIntervalClosed_Text.setText(String.valueOf(Long.parseLong((properties.getProperty("connection_interval_closed","15000")))/1000));
        updateIntervalClosed_Text.addListener (SWT.Verify, new Listener () {
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



        //---------------------- Main Window Preferences -------------------\\


        Group gMW = new Group(comp,SWT.NULL);
        gMW.setText("Main Window Preferences");
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gMW.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        gMW.setLayout(gridLayout);


        //Auto Open
        autoOpen = new Button(gMW,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoOpen.setLayoutData(gridData);
        autoOpen.setText("AutoOpen: Auto open main window when program is started");

        if (Boolean.parseBoolean(properties.getProperty("auto_open","false"))) {
            autoOpen.setSelection(true);
        }




        //Tray options
        trayMinimize = new Button(gMW,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        trayMinimize.setLayoutData(gridData);
        trayMinimize.setText("Minimizing the main window will send it to the tray");
        trayMinimize.setSelection(Boolean.parseBoolean(properties.getProperty("tray.minimize","true"))?true:false);

        //Tray options
        trayExit = new Button(gMW,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        trayExit.setLayoutData(gridData);
        trayExit.setText("Exiting the main window will send it to the tray");
        trayExit.setSelection(Boolean.parseBoolean(properties.getProperty("tray.exit","true"))?true:false);



        //auto console open
        autoConsole = new Button(gMW,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoConsole.setLayoutData(gridData);
        autoConsole.setText("Auto open console when opening main window");
        autoConsole.setSelection(Boolean.parseBoolean(properties.getProperty("auto_console","false"))?true:false);





        //---------------------- Misc Preferences -------------------\\

        Group gMisc = new Group(comp,SWT.NULL);
        gMisc.setText("Miscellaneous Preferences");
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gMisc.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        gMisc.setLayout(gridLayout);

        //popupsEnabled
        popupsEnabled = new Button(gMisc,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        popupsEnabled.setLayoutData(gridData);
        popupsEnabled.setText("Popup Alerts Enabled");

        if (Boolean.parseBoolean(properties.getProperty("popups_enabled","true"))) {
            popupsEnabled.setSelection(true);
        }else
            popupsEnabled.setSelection(false);

        //AutoClipboard
        autoClipboard = new Button(gMisc,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoClipboard.setLayoutData(gridData);
        autoClipboard.setText("Clipboard Monitor: Monitor the users clipboard at all times for torrent URLs and files");

        if (Boolean.parseBoolean(properties.getProperty("auto_clipboard",Utilities.isLinux()? "false" : "true"))) {
            autoClipboard.setSelection(true);
        }

        //---------------------- Update Preferences -------------------\\


        Group gUpdate = new Group(comp,SWT.NULL);
        gUpdate.setText("Update Preferences");
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gUpdate.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        gUpdate.setLayout(gridLayout);



        //Auto Update Check

        autoUpdateCheck = new Button(gUpdate,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoUpdateCheck.setLayoutData(gridData);
        autoUpdateCheck.setText("Auto Check for Updates: Allow FireFrog to check for and alert the user to updates");

        if (Boolean.parseBoolean(properties.getProperty("update.autocheck","true"))) {
            autoUpdateCheck.setSelection(true);
        }


        //Perform Auto Update

        autoUpdate = new Button(gUpdate,SWT.CHECK);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        autoUpdate.setLayoutData(gridData);
        autoUpdate.setText("Auto Update: If an update is found, automatically merge the files without any user interaction");

        if (Boolean.parseBoolean(properties.getProperty("update.autoupdate","false"))) {
            autoUpdate.setSelection(true);
        }





        //update button
        final Button updateCheck = new Button(gUpdate,SWT.PUSH);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        updateCheck.setLayoutData(gridData);
        updateCheck.setText("Check online for updates");
        updateCheck.addListener(SWT.Selection, new Listener(){

            public void handleEvent(Event arg0) {
                final Updater updater;
                try {
                    updater = new Updater(new URL("http://azcvsupdater.sourceforge.net/azmultiuser/ffupdate.xml.gz"),new File("update.xml.gz"),new File(System.getProperty("user.dir")));
                    updater.addListener(new UpdateListener() {
                        public void exception(Exception e) {


                        }
                        public void noUpdate() {
                            if (FireFrogMain.getFFM().getMainWindow() != null) {
                                FireFrogMain.getFFM().getMainWindow().setStatusBarText("No Update Available");
                            }
                            FireFrogMain.getFFM().getNormalLogger().info("No Update Available");
                        }
                        public void updateAvailable(Update update) {
                            if (FireFrogMain.getFFM().getMainWindow() != null) {
                                FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Available: Version "+update.getVersion());
                            }
                            FireFrogMain.getFFM().getNormalLogger().info("Update Available: Version "+update.getVersion());
                            if (Boolean.parseBoolean(properties.getProperty("update.autoupdate", "false"))) {
                                updater.doUpdate();
                            }else{
                                new UpdateDialog(FireFrogMain.getFFM().getDisplay(),update,updater);
                            }
                        }
                        public void updateFailed() {
                            if (FireFrogMain.getFFM().getMainWindow() != null) {
                                FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Failed",SWT.COLOR_RED);
                            }
                            FireFrogMain.getFFM().getNormalLogger().info("Update Failed");
                        }
                        public void updateFinished() {
                            if (FireFrogMain.getFFM().getMainWindow() != null) {
                                FireFrogMain.getFFM().getMainWindow().setStatusBarText("Update Finished");
                            }
                            FireFrogMain.getFFM().getNormalLogger().info("Update Finished");
                        }
                    });

                    updater.checkForUpdates();
                } catch (MalformedURLException e2) {
                }



            }

        });


        scrolledComposite.setContent(parent);

        scrolledComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle r = scrolledComposite.getClientArea();
                System.out.println(r.width + " : " + parent.computeSize(r.width,SWT.DEFAULT));
                scrolledComposite.setMinSize(parent.computeSize(r.width, SWT.DEFAULT));
            }
        });



        prefsTab.setControl(scrolledComposite);
        parentTab.setSelection(prefsTab);
    }
}
