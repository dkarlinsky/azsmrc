/*
 * Created on Apr 30, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.events.ConnectionListener;
import lbms.azsmrc.remote.client.events.DownloadListener;
import lbms.azsmrc.remote.client.events.DownloadManagerListener;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.events.SpeedUpdateListener;
import lbms.azsmrc.remote.client.swtgui.container.Container;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.remote.client.swtgui.dialogs.ConnectionDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByFileDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.OpenByURLDialog;
import lbms.azsmrc.remote.client.swtgui.dialogs.TableColumnEditorDialog;
import lbms.azsmrc.remote.client.swtgui.tabs.ConsoleTab;
import lbms.azsmrc.remote.client.swtgui.tabs.ManageUsersTab;
import lbms.azsmrc.remote.client.swtgui.tabs.PreferencesTab;
import lbms.azsmrc.remote.client.swtgui.tabs.ReadmeTab;
import lbms.azsmrc.remote.client.swtgui.tabs.TorrentDetailsTab;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class DownloadManagerShell {

    private Shell DOWNLOAD_MANAGER_SHELL;
    private Table downloadsTable, seedsTable;
    private Semaphore semaphore = new Semaphore(1);
    private SashForm sash;
    private CTabFolder tabFolder;
    private CTabItem myTorrents;

    private DownloadListener dlL = new DownloadListener(){

        public void stateChanged(Download download, int old_state, int new_state) {
            try {
                semaphore.acquire();
                //Changing from Downloading to Seeding
                if (new_state == Download.ST_SEEDING && downloadsMap.containsKey(download.getHash())) {
                    downloadsMap.get(download.getHash()).removeFromTable();
                    downloadsMap.remove(download.getHash());
                    SeedContainer sc = new SeedContainer(download,seedsTable,SWT.NULL);
                    seedsMap.put(download.getHash(), sc);
                }
            } catch (InterruptedException e) {

                e.printStackTrace();
            } finally {
                semaphore.release();
            }
            //Other changes go here
        }

        public void positionChanged(Download download, int oldPosition, int newPosition) {
            debugLogger.finer("Position changed "+download+" "+oldPosition+" -> "+newPosition);
            if(downloadsMap.containsKey(download.getHash()))
                resortDownloads = true;
            else
                resortSeeds = true;
        }

    };

    private boolean COLLAPSED;
    private boolean bSingleUserMode;
    private String TITLE;

    private SortedMap<String,DownloadContainer> downloadsMap	= new TreeMap<String,DownloadContainer>();
    private SortedMap<String,SeedContainer> seedsMap			= new TreeMap<String,SeedContainer>();


    private ToolItem top,up,bottom,down,login,logout,quickconnect;
    private ToolItem refresh,manage_users, preferences, console;
    private ToolItem addTorrent_by_file,addTorrent_by_url, pauseAll, resumeAll ;
    private ToolItem stopTorrent, queueTorrent, removeTorrent;
    public ConsoleTab consoleTab;

    //status bar labels
    private CLabel statusBarText;
    private Composite statusbarComp;
    private CLabelPadding statusDown, statusUp, /*statusBarText,*/  connectionStatusIcon, sslStatusIcon;

    private String azureusDownload = "N/S";
    private String azureusUpload = "N/S";
    private int upSpeed = 0;
    private int downSpeed = 0;

    private boolean resortDownloads = false;
    private boolean resortSeeds = false;
    private Logger debugLogger;


    private int drag_drop_line_start = -1;

    public void open(){
        debugLogger = FireFrogMain.getFFM().getDebugLogger();
        if(DOWNLOAD_MANAGER_SHELL != null && !DOWNLOAD_MANAGER_SHELL.isDisposed()){
            DOWNLOAD_MANAGER_SHELL.setVisible(true);
            DOWNLOAD_MANAGER_SHELL.forceFocus();
            DOWNLOAD_MANAGER_SHELL.forceActive();
            return;
        }


        if(TITLE == null) TITLE = "FireFrog";

        DOWNLOAD_MANAGER_SHELL = new Shell(FireFrogMain.getFFM().getDisplay());

        DOWNLOAD_MANAGER_SHELL.setImage(ImageRepository.getImage("TrayIcon_Blue"));

        //Grid Layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing=0;
        gridLayout.horizontalSpacing=0;
        gridLayout.marginWidth = 0;
        DOWNLOAD_MANAGER_SHELL.setLayout(gridLayout);
        DOWNLOAD_MANAGER_SHELL.setMinimumSize(400,250);


        //--------------------------TOOLBAR--------------------------------\\


        //Toolbar for torrent adding and other features
        ToolBar bar = new ToolBar(DOWNLOAD_MANAGER_SHELL,SWT.HORIZONTAL | SWT.FLAT);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        bar.setLayoutData(gridData);


        login = new ToolItem(bar,SWT.PUSH);
        login.setImage(ImageRepository.getImage("connect"));
        login.setToolTipText("Connect to server");
        login.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                new ConnectionDialog(FireFrogMain.getFFM().getDisplay());
            }
        });


        quickconnect = new ToolItem(bar,SWT.PUSH);
        quickconnect.setImage(ImageRepository.getImage("connect_quick"));
        quickconnect.setToolTipText("Quickconnect using last saved settings");
        quickconnect.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                Properties properties = FireFrogMain.getFFM().getProperties();
                if (
                        properties.getProperty("connection_lastURL",null) != null &&
                        properties.getProperty("connection_username",null) != null &&
                        properties.getProperty("connection_password",null) != null) {
                    FireFrogMain.getFFM().connect(true);
                    FireFrogMain.getFFM().getClient().sendListTransfers(RemoteConstants.ST_ALL);
                } else
                    new ConnectionDialog(FireFrogMain.getFFM().getDisplay());
            }
        });

        logout = new ToolItem(bar,SWT.PUSH);
        logout.setImage(ImageRepository.getImage("logout"));
        logout.setToolTipText("Disconnect from server");
        logout.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                FireFrogMain.getFFM().disconnect();
                setLogInOutButtons(false);
                DOWNLOAD_MANAGER_SHELL.setText(TITLE);
                FireFrogMain.getFFM().setTrayIcon(0);
                downloadsMap.clear();
                seedsMap.clear();
                downloadsTable.removeAll();
                seedsTable.removeAll();
                Control[] children_downloads = downloadsTable.getChildren();
                for(Control child:children_downloads) child.dispose();
                Control[] children_seeds = seedsTable.getChildren();
                for(Control child:children_seeds) child.dispose();
            }
        });




        refresh = new ToolItem(bar,SWT.PUSH);
        refresh.setImage(ImageRepository.getImage("refresh"));
        refresh.setToolTipText("Refresh");
        refresh.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(FireFrogMain.getFFM().connected())
                    FireFrogMain.getFFM().getClient().sendListTransfers(RemoteConstants.ST_ALL);

                //new UpdateDialog(DOWNLOAD_MANAGER_SHELL,SWT.NULL).open();
            }
        });



        //separator
        new ToolItem(bar,SWT.SEPARATOR);


        addTorrent_by_file = new ToolItem(bar,SWT.PUSH);
        addTorrent_by_file.setImage(ImageRepository.getImage("open_by_file"));
        addTorrent_by_file.setToolTipText("Add a torrent from a local file");
        addTorrent_by_file.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                Shell[] shells = FireFrogMain.getFFM().getDisplay().getShells();
                for(int i = 0; i < shells.length; i++){
                    if(shells[i].getText().equalsIgnoreCase("Send Torrent File to Server")){
                        shells[i].setActive();
                        shells[i].setFocus();
                        return;
                    }
                }
                new OpenByFileDialog(FireFrogMain.getFFM().getDisplay());
            }
        });


        addTorrent_by_url = new ToolItem(bar,SWT.PUSH);
        addTorrent_by_url.setImage(ImageRepository.getImage("open_by_url"));
        addTorrent_by_url.setToolTipText("Add a torrent from a URL");
        addTorrent_by_url.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                Shell[] shells = FireFrogMain.getFFM().getDisplay().getShells();
                for(int i = 0; i < shells.length; i++){
                    if(shells[i].getText().equalsIgnoreCase("Add a Torrent by URL")){
                        shells[i].setActive();
                        shells[i].setFocus();
                        return;
                    }
                }
                new OpenByURLDialog(FireFrogMain.getFFM().getDisplay());
            }
        });



        //separator
        new ToolItem(bar,SWT.SEPARATOR);

        top = new ToolItem(bar, SWT.PUSH);
        top.setImage(ImageRepository.getImage("top"));
        top.setToolTipText("Move selected torrent to top of list");
        top.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position -1) > 0){
                        container.getDownload().moveTo(1);
                    }
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position -1) > 0){
                        container.getDownload().moveTo(1);
                    }
                }
            }
        });


        up = new ToolItem(bar, SWT.PUSH);
        up.setImage(ImageRepository.getImage("up"));
        up.setToolTipText("Move selected torrent up one position");
        up.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position -1) > 0){
                        container.getDownload().setPosition(current_position-1);
                    }
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position -1) > 0){
                        container.getDownload().setPosition(current_position-1);
                    }
                }
            }
        });

        down = new ToolItem(bar, SWT.PUSH);
        down.setImage(ImageRepository.getImage("down"));
        down.setToolTipText("Move selected torrent down one position");
        down.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position + 1) != (downloadsMap.size() + 1)){
                        container.getDownload().setPosition(current_position+1);
                    }
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if((current_position  + 1) != (seedsMap.size() + 1)){
                        container.getDownload().setPosition(current_position+1);
                    }
                }
            }
        });

        bottom = new ToolItem(bar, SWT.PUSH);
        bottom.setImage(ImageRepository.getImage("bottom"));
        bottom.setToolTipText("Move selected torrent to bottom of list");
        bottom.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if(current_position != downloadsMap.size()){
                        container.getDownload().setPosition(downloadsMap.size());
                    }
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    int current_position = container.getDownload().getPosition();
                    if(current_position != seedsMap.size()){
                        container.getDownload().setPosition(seedsMap.size());
                    }
                }
            }
        });

        //Set all torrent move buttons disabled
        setTorrentMoveButtons(false,false,false,false);

        //separator
        new ToolItem(bar,SWT.SEPARATOR);

        queueTorrent = new ToolItem(bar, SWT.PUSH);
        queueTorrent.setImage(ImageRepository.getImage("toolbar_queue"));
        queueTorrent.setToolTipText("Queue Torrent");
        queueTorrent.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().stopAndQueue();
                    FireFrogMain.getFFM().getClient().transactionCommit();
                    setToolBarTorrentIcons(false,true,true);
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().stopAndQueue();
                    FireFrogMain.getFFM().getClient().transactionCommit();
                    setToolBarTorrentIcons(false,true,true);
                }
            }
        });


        stopTorrent = new ToolItem(bar, SWT.PUSH);
        stopTorrent.setImage(ImageRepository.getImage("toolbar_stop"));
        stopTorrent.setToolTipText("Stop Torrent");
        stopTorrent.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().stop();
                    FireFrogMain.getFFM().getClient().transactionCommit();
                    setToolBarTorrentIcons(true,false,true);
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().stop();
                    FireFrogMain.getFFM().getClient().transactionCommit();
                    setToolBarTorrentIcons(true,false,true);
                }
            }
        });






        removeTorrent = new ToolItem(bar, SWT.PUSH);
        removeTorrent.setImage(ImageRepository.getImage("toolbar_remove"));
        removeTorrent.setToolTipText("Remove Torrents");
        removeTorrent.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                if(downloadsTable.isFocusControl()){
                    TableItem[] items = downloadsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().remove();
                    container.removeFromTable();
                    if(seedsMap.containsKey(container.getDownload().getHash())){
                        seedsMap.remove(container.getDownload().getHash());
                    }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                        downloadsMap.remove(container.getDownload().getHash());
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();
                }else{
                    TableItem[] items = seedsTable.getSelection();
                    if(items.length == 0 || items.length > 1) return;
                    Container container = (Container)items[0].getData();
                    container.getDownload().remove();
                    container.removeFromTable();
                    if(seedsMap.containsKey(container.getDownload().getHash())){
                        seedsMap.remove(container.getDownload().getHash());
                    }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                        downloadsMap.remove(container.getDownload().getHash());
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();


                }
            }
        });

        setToolBarTorrentIcons(false,false,false);


        //separator
        new ToolItem(bar,SWT.SEPARATOR);

        pauseAll = new ToolItem(bar, SWT.PUSH);
        pauseAll.setImage(ImageRepository.getImage("pause"));
        pauseAll.setToolTipText("Pause all torrents");
        pauseAll.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                FireFrogMain.getFFM().getClient().sendStopAll();
            }
        });


        resumeAll = new ToolItem(bar, SWT.PUSH);
        resumeAll.setImage(ImageRepository.getImage("resume"));
        resumeAll.setToolTipText("Resume all paused torrents");
        resumeAll.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                FireFrogMain.getFFM().getClient().sendStartAll();
            }
        });


        //separator
        new ToolItem(bar,SWT.SEPARATOR);

        preferences = new ToolItem(bar,SWT.PUSH);
        preferences.setImage(ImageRepository.getImage("preferences"));
        preferences.setToolTipText("Open Preferences");
        preferences.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {

                CTabItem[] tabs = tabFolder.getItems();
                for(CTabItem tab:tabs){
                    if(tab.getText().equalsIgnoreCase("Preferences")){
                        tabFolder.setSelection(tab);
                        return;
                    }
                }
                new PreferencesTab(tabFolder);

                //new PreferencesDialog(FireFrogMain.getFFM().getDisplay());
                //new MessageDialog(FireFrogMain.getFFM().getDisplay(),true,10000,20,"Error", "I need a background picture!");
            }
        });




        manage_users = new ToolItem(bar,SWT.PUSH);
        manage_users.setImage(ImageRepository.getImage("manager_users"));
        manage_users.setToolTipText("Manage Users");
        manage_users.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {

                CTabItem[] tabs = tabFolder.getItems();
                for(CTabItem tab:tabs){
                    if(tab.getText().equalsIgnoreCase("Manage Users")){
                        tabFolder.setSelection(tab);
                        return;
                    }
                }
                new ManageUsersTab(tabFolder);
            }
        });



        console = new ToolItem(bar,SWT.PUSH);
        console.setImage(ImageRepository.getImage("console"));
        console.setToolTipText("Open Console");
        console.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e){
                CTabItem[] tabs = tabFolder.getItems();
                for(CTabItem tab:tabs){
                    if(tab.getText().equalsIgnoreCase("Console")){
                        tabFolder.setSelection(tab);
                        return;
                    }
                }
                consoleTab = new ConsoleTab(tabFolder);
            }
        });

        ToolItem information = new ToolItem(bar,SWT.PUSH);
        information.setImage(ImageRepository.getImage("information"));
        information.setToolTipText("View AzMultiUser Guide and Information");
        information.addListener(SWT.Selection, new Listener(){
            public void handleEvent (Event e){
                //open readme shell here
                CTabItem[] tabs = tabFolder.getItems();
                for(CTabItem tab:tabs){
                    if(tab.getText().equalsIgnoreCase("Information")){
                        tabFolder.setSelection(tab);
                        return;
                    }
                }
                new ReadmeTab(tabFolder);
            }
        });

        setLogInOutButtons(false);

        //shell title
        DOWNLOAD_MANAGER_SHELL.setText(TITLE);

        //-------------------  Listeners for the shell ---------------------\\

        final SpeedUpdateListener sul = new SpeedUpdateListener() {
            public void setSpeed(final int d, final int u) {
                upSpeed = u;
                downSpeed = d;
                refreshStatusBar();
            }
        };

        FireFrogMain.getFFM().getClient().addSpeedUpdateListener(sul);

        final ConnectionListener cl = new ConnectionListener() {

            public void connectionState(final int state) {
                setConnectionStatusBar(state);
                if(state > 0){
                    if (FireFrogMain.getFFM().getClient().isSSLEncrypted()) {
                        setSSLStatusBar(true,true);
                    } else {
                        setSSLStatusBar(true,false);
                    }
                }

                //set the toolbar buttons
                if(state > 0){
                    setLogInOutButtons(true);
                    //reset the title of MyTorrents once Logged in
                    FireFrogMain.getFFM().getDisplay().asyncExec(new Runnable(){

                        public void run() {
                            String userLoggedIn = FireFrogMain.getFFM().getClient().getUsername();
                            if(myTorrents != null || !myTorrents.isDisposed()){
                                if(bSingleUserMode){
                                    myTorrents.setText("ALL Torrents");
                                }else if(userLoggedIn != null)
                                    myTorrents.setText(userLoggedIn + "'s Torrents");
                                else
                                    myTorrents.setText("My Torrents");
                            }

                        }

                    });
                }else{
                    setLogInOutButtons(false);
                }
            }
        };

        FireFrogMain.getFFM().getClient().addConnectionListener(cl);

        final DownloadManagerListener dml = new DownloadManagerListener(){

            public void downloadAdded(final Download download) {
                try {
                    if (DOWNLOAD_MANAGER_SHELL.isDisposed()) {
                        FireFrogMain.getFFM().getClient().getDownloadManager().removeListener(this);
                        return;
                    }

                    semaphore.acquire();
                    FireFrogMain.getFFM().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            if(download.getState() == Download.ST_SEEDING || download.getStats().getCompleted() == 1000){
                                if (!seedsMap.containsKey(download.getHash())) {
                                    SeedContainer sc = new SeedContainer(download);
                                    int pos = findInsertionPosition(sc, seedsTable);
                                    sc.addToTable(seedsTable, SWT.NONE, pos);
                                    seedsMap.put(download.getHash(), sc);
                                    resortSeeds = true; //just to make sure sometimes findInsertionPosition screws up
                                }
                            }else{
                                if (!downloadsMap.containsKey(download.getHash())) {
                                    DownloadContainer dc = new DownloadContainer(download);
                                    int pos = findInsertionPosition(dc, downloadsTable);
                                    dc.addToTable(downloadsTable, SWT.NONE, pos);
                                    downloadsMap.put(download.getHash(), dc);
                                    resortDownloads = true;
                                }
                            }
                        };
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
                download.addDownloadListener(dlL);
            }

            public void downloadRemoved(Download download) {
                if(downloadsMap.containsKey(download.getHash())){
                    downloadsMap.get(download.getHash()).removeFromTable();
                    downloadsMap.remove(download.getHash());
                    resortDownloads = true;
                }else if(seedsMap.containsKey(download.getHash())){
                    seedsMap.get(download.getHash()).removeFromTable();
                    seedsMap.remove(download.getHash());
                    resortSeeds = true;
                }
            }
        };

        FireFrogMain.getFFM().getClient().getDownloadManager().addListener(dml);

        final ClientUpdateListener cul = new ClientUpdateListener(){

            public void update(long updateSwitches) {
                if ((updateSwitches & Constants.UPDATE_LIST_TRANSFERS) != 0){
                    Download[] downloads = FireFrogMain.getFFM().getClient().getDownloadManager().getSortedDownloads();
                    for (int i = 0; i < downloads.length; i++){
                        if(!downloadsMap.containsKey(downloads[i].getHash()) && !seedsMap.containsKey(downloads[i].getHash())){
                            if(downloads[i].getState() == Download.ST_SEEDING || downloads[i].getStats().getCompleted() == 1000){
                                SeedContainer sc = new SeedContainer(downloads[i],seedsTable,SWT.NULL);
                                seedsMap.put(downloads[i].getHash(), sc);
                                resortSeeds = true;
                            }else{
                                DownloadContainer dc = new DownloadContainer(downloads[i],downloadsTable,SWT.BORDER);
                                downloadsMap.put(downloads[i].getHash(), dc);
                                resortDownloads = true;
                            }
                        }else{
                            //is present in one of the maps.. so find it and update it
                            if(downloadsMap.containsKey(downloads[i].getHash())){
                                if(downloads[i].getState() == Download.ST_SEEDING){
                                    downloadsMap.get(downloads[i].getHash()).removeFromTable();
                                    downloadsMap.remove(downloads[i].getHash());
                                    if(!seedsMap.containsKey(downloads[i].getHash())){
                                        SeedContainer sc = new SeedContainer(downloads[i],seedsTable,SWT.NULL);
                                        seedsMap.put(downloads[i].getHash(), sc);
                                    }

                                }
                                DownloadContainer dc = downloadsMap.get(downloads[i].getHash());
                                dc.update(false);
                            }else if(seedsMap.containsKey(downloads[i].getHash())){
                                SeedContainer sc = seedsMap.get(downloads[i].getHash());
                                sc.update(false);
                            }
                        }
                    }
                    if (resortSeeds) {
                        resortSeeds = false;
                        sortTable(seedsTable);
                    }
                    if (resortDownloads) {
                        resortDownloads = false;
                        sortTable(downloadsTable);
                    }
                }

                if ((updateSwitches & Constants.UPDATE_USERS) != 0){
                    setLogInOutButtons(true);
                }

            }
        };

        FireFrogMain.getFFM().getClient().addClientUpdateListener(cul);


        final ParameterListener pl = new ParameterListener(){

            public void azParameter(String key, String value, int type) {
                if(key.equalsIgnoreCase(RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC)){
                    azureusUpload = value;
                    refreshStatusBar();
                }

                if(key.equalsIgnoreCase(RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC)){
                    azureusDownload = value;
                    refreshStatusBar();
                }


            }

            public void pluginParameter(String key, String value, int type) {
                if(key.equalsIgnoreCase("singleUserMode")){
                    bSingleUserMode = Boolean.parseBoolean(value);
                    if(bSingleUserMode)
                        setStatusBarText("Connected in Single User Mode",SWT.COLOR_DARK_GREEN );

                }

            }
        };

        FireFrogMain.getFFM().getClient().addParameterListener(pl);

        //------------CtabFolder initialization here----------\\
        tabFolder = new CTabFolder(DOWNLOAD_MANAGER_SHELL,SWT.BORDER);
        tabFolder.setLayout(new GridLayout(1,false));
        gridData = new GridData(GridData.FILL_BOTH);
        //gridData.heightHint = 300;
        gridData.horizontalSpan = 2;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        tabFolder.setLayoutData(gridData);
        tabFolder.setSimple(false);

        //Set the tabs to the OS color scheme
        Display display = FireFrogMain.getFFM().getDisplay();
        tabFolder.setSelectionForeground(display.getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
        tabFolder.setSelectionBackground(
                new Color[]{display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND),
                        display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT)},
                        new int[] {100}, true);


        //add in the non closable My torrents ctabitem
        myTorrents = new CTabItem(tabFolder, SWT.NONE);




        //----------------------  Sash------------------------\\
        sash = new SashForm(tabFolder,SWT.VERTICAL);
        sash.setLayout(new GridLayout());

        myTorrents.setControl(sash);

        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 300;
        gridData.horizontalSpan = 2;
        sash.setLayoutData(gridData);

        //--------------------- Set Up Downloads Table ---------------------


        downloadsTable = new Table(sash, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        downloadsTable.setLayoutData(gridData);
        downloadsTable.setHeaderVisible(true);

        createTableColumns(downloadsTable, DownloadContainer.getColumns());

        //add menu to downloadsTable for downloads management
        addDownloadManagerMenu(downloadsTable);

        //Selection listener for the table
        downloadsTable.addListener(SWT.Selection, new Listener(){

            public void handleEvent(Event arg0) {
                TableItem[] items = downloadsTable.getSelection();
                if(items == null) return;
                if(items.length == 1){
                    //Single line selection
                    setTorrentMoveButtons(false,false,false,false);
                    int index = downloadsTable.indexOf(items[0]);
                    if(index==0 && downloadsTable.getItemCount() == 1)
                        setTorrentMoveButtons(false,true,true,false);
                    else if(index == 0)
                        setTorrentMoveButtons(false,true,true,true);
                    else if(index == downloadsTable.getItemCount())
                        setTorrentMoveButtons(true,true,true,false);
                    else
                        setTorrentMoveButtons(true,true,true,true);

                    //torrent control
                    Container container = (Container)items[0].getData();
                    Download download = container.getDownload();
                    if(download.getState() == Download.ST_QUEUED || download.getState() == Download.ST_DOWNLOADING){
                        setToolBarTorrentIcons(false,true,true);
                    }else if(download.getState() == Download.ST_STOPPED){
                        setToolBarTorrentIcons(true,false,true);
                    }


                }else if(items.length > 1){
                    //Multiple selection here
                    setTorrentMoveButtons(false,false,false,false);
                    setToolBarTorrentIcons(false,false,false);

                }

            }

        });

        downloadsTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if(e.button == 1) {
                    if(downloadsTable.getItem(new Point(e.x,e.y))==null){
                        downloadsTable.deselectAll();
                        setTorrentMoveButtons(false,false,false,false);
                        setToolBarTorrentIcons(false,false,false);
                    }

                }
            }
        });

        downloadsTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                if(e.button == 1) {
                    if(downloadsTable.getItem(new Point(e.x,e.y))==null){
                        downloadsTable.deselectAll();
                        setTorrentMoveButtons(false,false,false,false);
                    }else{
                        TableItem[] items = downloadsTable.getSelection();
                        if(items.length > 1) return;

                        Container dc = (Container)items[0].getData();
                        //Check for one already open
                        CTabItem[] ctabs = tabFolder.getItems();
                        for(CTabItem ctab:ctabs){
                            if(ctab.getText().equalsIgnoreCase(dc.getDownload().getName())){
                                tabFolder.setSelection(ctab);
                                return;
                            }

                        }
                        new TorrentDetailsTab(tabFolder,dc.getDownload());

                    }

                }
            }
        });

        createDragDrop(downloadsTable);


        //----------------------Seeds Table --------------------------\\
        seedsTable = new Table(sash, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        seedsTable.setLayoutData(gridData);
        seedsTable.setHeaderVisible(true);

        createTableColumns(seedsTable, SeedContainer.getColumns());

        //Add menu to seedsTable for download management
        addDownloadManagerMenu(seedsTable);

        //Listeners for seedsTable

        seedsTable.addListener(SWT.Selection, new Listener(){

            public void handleEvent(Event arg0) {
                TableItem[] items = seedsTable.getSelection();
                if(items == null) return;
                if(items.length == 1){
                    //Single line selection
                    setTorrentMoveButtons(false,false,false,false);
                    int index = seedsTable.indexOf(items[0]);
                    if(index==0 && seedsTable.getItemCount() == 1)
                        setTorrentMoveButtons(false,true,true,false);
                    else if(index == 0)
                        setTorrentMoveButtons(false,true,true,true);
                    else if(index == seedsTable.getItemCount())
                        setTorrentMoveButtons(true,true,true,false);
                    else
                        setTorrentMoveButtons(true,true,true,true);


                    //Torrent Control
                    //torrent control
                    Container container = (Container)items[0].getData();
                    Download download = container.getDownload();
                    if(download.getState() == Download.ST_QUEUED || download.getState() == Download.ST_SEEDING){
                        setToolBarTorrentIcons(false,true,true);
                    }else if(download.getState() == Download.ST_STOPPED){
                        setToolBarTorrentIcons(true,false,true);
                    }
                }else if(items.length > 1){
                    //Multiple selection here
                    setTorrentMoveButtons(false,false,false,false);
                    setToolBarTorrentIcons(false,false,false);

                }

            }

        });


        seedsTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if(e.button == 1) {
                    if(seedsTable.getItem(new Point(e.x,e.y))==null){
                        seedsTable.deselectAll();
                        setTorrentMoveButtons(false,false,false,false);
                        setToolBarTorrentIcons(false,false,false);
                    }

                }
            }
        });

        seedsTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                if(e.button == 1) {
                    if(seedsTable.getItem(new Point(e.x,e.y))==null){
                        seedsTable.deselectAll();
                        setTorrentMoveButtons(false,false,false,false);
                    }else{
                        TableItem[] items = seedsTable.getSelection();
                        if(items.length > 1) return;FireFrogMain.getFFM().getClient().addParameterListener(pl);
                        Container sc = (Container)items[0].getData();
                        //Check for one already open
                        CTabItem[] ctabs = tabFolder.getItems();
                        for(CTabItem ctab:ctabs){
                            if(ctab.getText().equalsIgnoreCase(sc.getDownload().getName())){
                                tabFolder.setSelection(ctab);
                                return;
                            }

                        }
                        new TorrentDetailsTab(tabFolder,sc.getDownload());
                    }

                }
            }
        });

        createDragDrop(seedsTable);

        Properties properties = FireFrogMain.getFFM().getProperties();
        if(properties.containsKey("sash0_weight")){
            sash.setWeights(new int[] {Integer.parseInt((String)properties.get("sash0_weight")),
                    Integer.parseInt((String)properties.get("sash1_weight"))});

        }
        String userLoggedIn = FireFrogMain.getFFM().getClient().getUsername();
        if(bSingleUserMode)
            myTorrents.setText("ALL Torrents");
        else if(userLoggedIn != null)
            myTorrents.setText(userLoggedIn + "'s Torrents");
        else
            myTorrents.setText("My Torrents");

        //------------bottom status bar---------------\\
        final int borderFlag = (Utilities.isOSX) ? SWT.NONE : SWT.SHADOW_IN;

        statusbarComp = new Composite(DOWNLOAD_MANAGER_SHELL,borderFlag);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 1;
        statusbarComp.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        statusbarComp.setLayoutData(gridData);


        statusBarText = new CLabel(statusbarComp,borderFlag);
        statusBarText.setText("");
        gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        statusBarText.setLayoutData(gridData);


        statusDown = new CLabelPadding(statusbarComp, borderFlag);
        statusDown.setText("0B/s");
        statusDown.setImage(ImageRepository.getImage("statusbar_down"));
        statusDown.setToolTipText("Right-click to set max download speed of core\n" +
                "Double-click to force a speed update\n" +
        "[N/S] = Data not yet received from server");


        statusUp = new CLabelPadding(statusbarComp, borderFlag);
        statusUp.setText("0B/s");
        statusUp.setImage(ImageRepository.getImage("statusbar_up"));
        statusUp.setToolTipText("Right-click to set max upload speed of core\n" +
                "Double-click to force a speed update\n" +
        "[N/S] = Data not yet received from server");

        connectionStatusIcon = new CLabelPadding(statusbarComp,borderFlag);
        setConnectionStatusBar(0);


        sslStatusIcon = new CLabelPadding(statusbarComp, borderFlag);
        sslStatusIcon.setImage(ImageRepository.getImage("ssl_disabled"));
        sslStatusIcon.setToolTipText("SSL Disabled");
        sslStatusIcon.setEnabled(false);

        setSSLStatusBar(false,false);


        statusbarComp.pack();

        //Double click listener for down and up
        Listener speedUpdate = new Listener(){
            public void handleEvent(Event arg0) {
                Client client = FireFrogMain.getFFM().getClient();
                client.transactionStart();
                client.sendGetAzParameter(
                        RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
                        RemoteConstants.PARAMETER_INT);
                client.sendGetAzParameter(
                        RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
                        RemoteConstants.PARAMETER_INT);
                client.transactionCommit();
            }
        };

        statusDown.addListener(SWT.MouseDoubleClick, speedUpdate);
        statusUp.addListener(SWT.MouseDoubleClick, speedUpdate);

        //menus
        final Menu menuUpSpeed = new Menu(DOWNLOAD_MANAGER_SHELL,SWT.POP_UP);
        menuUpSpeed.addListener(SWT.Show,new Listener() {
            public void handleEvent(Event e) {
                MenuItem[] items = menuUpSpeed.getItems();
                for(int i = 0 ; i < items.length ; i++) {
                    items[i].dispose();
                }

                //final String    config_param = TransferSpeedValidator.getActiveUploadParameter(globalManager);

                int upLimit;

                if(azureusUpload.equalsIgnoreCase("N/S") || azureusUpload.equalsIgnoreCase("0"))
                    upLimit = 0;
                else
                    upLimit = Integer.valueOf(azureusUpload);


                MenuItem item = new MenuItem(menuUpSpeed,SWT.RADIO);
                item.setText("Unlimited");
                item.addListener(SWT.Selection,new Listener() {
                    public void handleEvent(Event e) {
                        FireFrogMain.getFFM().getClient().sendSetAzParameter(
                                RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
                                "0",
                                RemoteConstants.PARAMETER_INT);
                        azureusUpload = "0";
                        refreshStatusBar();
                    }
                });
                if(upLimit == 0) item.setSelection(true);

                final SelectionListener speedChangeListener = new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        if(((MenuItem)e.widget).getSelection()){
                            String value = String.valueOf(((MenuItem)e.widget).getData("speed"));
                            FireFrogMain.getFFM().getClient().sendSetAzParameter(
                                    RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
                                    value,
                                    RemoteConstants.PARAMETER_INT);
                            azureusUpload = value;
                            refreshStatusBar();
                        }
                    }


                    public void widgetDefaultSelected(SelectionEvent arg0) {
                        // TODO Auto-generated method stub

                    }
                };

                int iRel = 0;
                for (int i = 0; i < 12; i++) {
                    int[] iAboveBelow;
                    if (iRel == 0) {
                        iAboveBelow = new int[] { upLimit };
                    } else {
                        iAboveBelow = new int[] { upLimit - iRel, upLimit + iRel };
                    }

                    for (int j = 0; j < iAboveBelow.length; j++) {
                        if (iAboveBelow[j] >= 5) {
                            item = new MenuItem(menuUpSpeed, SWT.RADIO,
                                    (j == 0) ? 1 : menuUpSpeed.getItemCount());
                            item.setText(DisplayFormatters.formatByteCountToKiBEtcPerSec(iAboveBelow[j] * 1024, true));
                            item.setData("speed", new Long(iAboveBelow[j]));
                            item.addSelectionListener(speedChangeListener);

                            if (upLimit == iAboveBelow[j]) item.setSelection(true);
                        }
                    }

                    iRel += (iRel >= 50) ? 50 : (iRel >= 10) ? 10 : (iRel >= 5) ? 5 : (iRel >= 2) ? 3 : 1;
                }

            }
        });
        statusUp.setMenu(menuUpSpeed);



        final Menu menuDownSpeed = new Menu(DOWNLOAD_MANAGER_SHELL,SWT.POP_UP);
        menuDownSpeed.addListener(SWT.Show,new Listener() {
            public void handleEvent(Event e) {
                MenuItem[] items = menuDownSpeed.getItems();
                for(int i = 0 ; i < items.length ; i++) {
                    items[i].dispose();
                }
                int downLimit;
                if(azureusDownload.equalsIgnoreCase("N/S") || azureusDownload.equalsIgnoreCase("0")){
                    downLimit = 0;
                }else{
                    downLimit = Integer.valueOf(azureusDownload);
                }
                final boolean unlim = (downLimit == 0);
                if(downLimit == 0)
                    downLimit = 275;

                MenuItem item = new MenuItem(menuDownSpeed,SWT.RADIO);
                item.setText("Unlimited");
                item.addListener(SWT.Selection,new Listener() {
                    public void handleEvent(Event e) {
                        FireFrogMain.getFFM().getClient().sendSetAzParameter(
                                RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
                                "0",
                                RemoteConstants.PARAMETER_INT);
                        azureusDownload = "0";
                        refreshStatusBar();
                    }
                });
                if(unlim) item.setSelection(true);
                final SelectionListener speedChangeListener = new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        if(((MenuItem)e.widget).getSelection()){
                            String value = String.valueOf(((MenuItem)e.widget).getData("speed"));
                            FireFrogMain.getFFM().getClient().sendSetAzParameter(
                                    RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
                                    value,
                                    RemoteConstants.PARAMETER_INT);
                            azureusDownload = value;
                            refreshStatusBar();
                        }

                    }

                    public void widgetDefaultSelected(SelectionEvent arg0) {
                        // TODO Auto-generated method stub

                    }
                };

                int iRel = 0;
                for (int i = 0; i < 12; i++) {
                    int[] iAboveBelow;
                    if (iRel == 0) {
                        iAboveBelow = new int[] { downLimit };
                    } else {
                        iAboveBelow = new int[] { downLimit - iRel, downLimit + iRel };
                    }
                    for (int j = 0; j < iAboveBelow.length; j++) {
                        if (iAboveBelow[j] >= 5) {
                            item = new MenuItem(menuDownSpeed, SWT.RADIO,
                                    (j == 0) ? 1 : menuDownSpeed.getItemCount());
                            item.setText(DisplayFormatters.formatByteCountToKiBEtcPerSec(iAboveBelow[j] * 1024, true));
                            item.setData("speed", new Long(iAboveBelow[j]));
                            item.addSelectionListener(speedChangeListener);
                            item.setSelection(!unlim && downLimit == iAboveBelow[j]);
                        }
                    }

                    iRel += (iRel >= 50) ? 50 : (iRel >= 10) ? 10 : (iRel >= 5) ? 5 : (iRel >= 2) ? 3 : 1;
                }

            }
        });
        statusDown.setMenu(menuDownSpeed);








        //--------------------Center/Open Shell-------------------\\


        DOWNLOAD_MANAGER_SHELL.addShellListener(new ShellListener(){

            public void shellActivated(ShellEvent arg0) {}

            public void shellClosed(ShellEvent arg0) {
                FireFrogMain.getFFM().updateTimer(false);
                FireFrogMain.getFFM().getClient().removeSpeedUpdateListener(sul);
                FireFrogMain.getFFM().getClient().removeConnectionListener(cl);
                FireFrogMain.getFFM().getClient().removeClientUpdateListener(cul);
                FireFrogMain.getFFM().getClient().removeParameterListener(pl);

                //Remove all DownloadListeners
                Set<String> keys = downloadsMap.keySet();
                for (String key:keys) {
                    DownloadContainer dc = downloadsMap.get(key);
                    dc.dispose();
                    downloadsMap.get(key).getDownload().removeDownloadListener(dlL);
                }
                downloadsMap.clear();
                keys = seedsMap.keySet();
                for (String key:keys) {
                    SeedContainer sc = seedsMap.get(key);
                    sc.dispose();
                    seedsMap.get(key).getDownload().removeDownloadListener(dlL);
                }
                seedsMap.clear();

                //Saving user preferences for the shell and sash
                int size_x = DOWNLOAD_MANAGER_SHELL.getSize().x;
                int size_y = DOWNLOAD_MANAGER_SHELL.getSize().y;

                int position_x = DOWNLOAD_MANAGER_SHELL.getLocation().x;
                int position_y = DOWNLOAD_MANAGER_SHELL.getLocation().y;
                Properties properties = FireFrogMain.getFFM().getProperties();

                if (!sash.isDisposed()) {
                    int[] weights = sash.getWeights();
                    properties.setProperty("sash0_weight", String.valueOf(weights[0]));
                    properties.setProperty("sash1_weight", String.valueOf(weights[1]));
                }
                properties.setProperty("DMS_SIZE_x", String.valueOf(size_x));
                properties.setProperty("DMS_SIZE_y", String.valueOf(size_y));
                properties.setProperty("DMS_POSITION_x", String.valueOf(position_x));
                properties.setProperty("DMS_POSITION_y", String.valueOf(position_y));

                //save the downloadsTable column widths
                if (!downloadsTable.isDisposed()) {
                    TableColumn[] columns = downloadsTable.getColumns();
                    List<Integer> dl_column_list = new ArrayList<Integer>();
                    for (TableColumn column:columns){
                        dl_column_list.add(column.getWidth());
                    }
                    properties.setProperty("downloadsTable.columns.widths", EncodingUtil.IntListToString(dl_column_list));
                }

                //save the seedsTable column widths
                if (!seedsTable.isDisposed()) {
                    TableColumn[] seed_columns = seedsTable.getColumns();
                    List<Integer> seed_column_list = new ArrayList<Integer>();
                    for (TableColumn column:seed_columns){
                        seed_column_list.add(column.getWidth());
                    }
                    properties.setProperty("seedsTable.columns.widths", EncodingUtil.IntListToString(seed_column_list));
                }

                //Save Everything!
                FireFrogMain.getFFM().saveConfig();
                DOWNLOAD_MANAGER_SHELL = null;
                if(!Boolean.parseBoolean(FireFrogMain.getFFM().getProperties().getProperty("tray.exit","true"))){
                    FireFrogMain.getFFM().close();
                }

            }

            public void shellDeactivated(ShellEvent arg0) {
                // TODO Auto-generated method stub

            }

            public void shellDeiconified(ShellEvent arg0) {
                // TODO Auto-generated method stub

            }

            public void shellIconified(ShellEvent arg0) {
                if(Boolean.parseBoolean(FireFrogMain.getFFM().getProperties().getProperty("tray.minimize","true"))){
                    DOWNLOAD_MANAGER_SHELL.setVisible(false);
                }

            }

        });

        //check to see if the console is auto open and open it up if it is
        if(Boolean.parseBoolean(properties.getProperty("auto_console", "false")))
            new ConsoleTab(tabFolder);


        tabFolder.setSelection(myTorrents);

        //check if connected, and if so, send the correct requests
        initializeConnection();

        //open shell
        if(!FireFrogMain.getFFM().getProperties().containsKey("DMS_SIZE_x"))
            GUI_Utilities.centerShellandOpen(DOWNLOAD_MANAGER_SHELL);
        else{
            properties = FireFrogMain.getFFM().getProperties();
            int size_x = Integer.parseInt((String)properties.get("DMS_SIZE_x"));
            int size_y = Integer.parseInt((String)properties.get("DMS_SIZE_y"));
            int position_x = Integer.parseInt((String)properties.get("DMS_POSITION_x"));
            int position_y = Integer.parseInt((String)properties.get("DMS_POSITION_y"));
            System.out.println("Open: " + size_x + " : "+size_y + " : " + position_x + " : " + position_y);
            DOWNLOAD_MANAGER_SHELL.setSize (size_x, size_y);
            DOWNLOAD_MANAGER_SHELL.setLocation (position_x, position_y);
            DOWNLOAD_MANAGER_SHELL.open();
        }

        try {
            //createDropTarget(DOWNLOAD_MANAGER_SHELL);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        FireFrogMain.getFFM().updateTimer(true);
        FireFrogMain.getFFM().getClient().transactionCommit();


    }

    public void close_shell () {
        if(DOWNLOAD_MANAGER_SHELL != null && !DOWNLOAD_MANAGER_SHELL.isDisposed()){
            DOWNLOAD_MANAGER_SHELL.close();
        }

    }

    public Shell getShell(){
        return DOWNLOAD_MANAGER_SHELL;
    }

    public boolean isCollapsed(){
        return COLLAPSED;
    }

    public void setCollapsed(boolean state){
        COLLAPSED=state;
    }




    /**
     *
     * @param connection -- int -- 0 for no connection, 1 for connecting, 2 for connected
     */
    public void setConnectionStatusBar(final int connection){
        try {
            if(connectionStatusIcon.getDisplay() == null) return;
        } catch (SWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        connectionStatusIcon.getDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    if(connection == 0){
                        connectionStatusIcon.setImage(ImageRepository.getImage("connect_no"));
                        connectionStatusIcon.setToolTipText("Not Connected to server");
                    }
                    else if(connection == 1){

                        connectionStatusIcon.setImage(ImageRepository.getImage("connect_creating"));
                        connectionStatusIcon.setToolTipText("Attempting to connect to server");
                    }
                    else if(connection == 2){
                        connectionStatusIcon.setImage(ImageRepository.getImage("connect_established"));
                        connectionStatusIcon.setToolTipText("Connected to server");
                    }
                } catch (SWTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }});
    }

    public void setSSLStatusBar(boolean benabled, boolean buse_ssl){
        Display display = FireFrogMain.getFFM().getDisplay();
        if(display == null || display.isDisposed()) return;
        if(benabled){
            if(buse_ssl){
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                            sslStatusIcon.setEnabled(true);
                            sslStatusIcon.setImage(ImageRepository.getImage("ssl_enabled"));
                            sslStatusIcon.setToolTipText("SSL Enabled");
                        } catch (SWTException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

            }else{
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                            sslStatusIcon.setEnabled(true);
                            sslStatusIcon.setImage(ImageRepository.getImage("ssl_disabled"));
                            sslStatusIcon.setToolTipText("SSL Disabled");
                        } catch (SWTException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

            }
        }else{
            display.asyncExec(new Runnable() {
                public void run() {
                    try {
                        sslStatusIcon.setImage(ImageRepository.getImage("ssl_disabled"));
                        sslStatusIcon.setEnabled(false);
                    } catch (SWTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    public void setTorrentMoveButtons(final boolean bTop, final boolean bUp, final boolean bDown, final boolean bBottom){
        Display display = FireFrogMain.getFFM().getDisplay();
        if(display == null || display.isDisposed()) return;
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    top.setEnabled(bTop);
                    up.setEnabled(bUp);
                    down.setEnabled(bDown);
                    bottom.setEnabled(bBottom);
                } catch (SWTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }


    public void setLogInOutButtons(final boolean bLoggedIn){
        Display display = FireFrogMain.getFFM().getDisplay();
        if(display == null || display.isDisposed()) return;
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    if(bLoggedIn){

                        login.setEnabled(false);
                        logout.setEnabled(true);
                        quickconnect.setEnabled(false);
                        refresh.setEnabled(true);
                        preferences.setEnabled(true);
                        addTorrent_by_file.setEnabled(true);
                        addTorrent_by_url.setEnabled(true);
                        pauseAll.setEnabled(true);
                        resumeAll.setEnabled(true);
                        User user = FireFrogMain.getFFM().getClient().getUserManager().getActiveUser();
                        if(user == null){
                            manage_users.setEnabled(false);
                        }else{
                            if(user.checkAccess(RemoteConstants.RIGHTS_ADMIN))
                                manage_users.setEnabled(true);
                            else
                                manage_users.setEnabled(false);
                        }


                    }else{
                        login.setEnabled(true);
                        logout.setEnabled(false);
                        quickconnect.setEnabled(true);
                        refresh.setEnabled(false);
                        preferences.setEnabled(true);
                        addTorrent_by_file.setEnabled(false);
                        addTorrent_by_url.setEnabled(false);
                        pauseAll.setEnabled(false);
                        resumeAll.setEnabled(false);
                        manage_users.setEnabled(false);
                        setTorrentMoveButtons(false,false,false,false);
                        setToolBarTorrentIcons(false,false,false);
                        setSSLStatusBar(false,false);
                        setConnectionStatusBar(0);
                        azureusDownload = "N/S";
                        downSpeed = 0;
                        azureusUpload = "N/S";
                        upSpeed = 0;
                        refreshStatusBar();
                    }
                } catch (SWTException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void redrawColumnsonTables(){
        setRequestedItems();
        TableColumn[] columns = downloadsTable.getColumns();
        for(TableColumn column:columns){
            column.dispose();
        }
        createTableColumns(downloadsTable, DownloadContainer.getColumns());
        downloadsMap.clear();


        TableColumn[] columns_seeds = seedsTable.getColumns();
        for(TableColumn column:columns_seeds){
            column.dispose();
        }
        createTableColumns(seedsTable, SeedContainer.getColumns());
        seedsMap.clear();


        //Be sure to save down the column widths
        FireFrogMain.getFFM().getMainWindow().saveColumnWidthsToPreferencesFile();
    }

    private void addDownloadManagerMenu(final Table table){
        final Menu menu = new Menu(table);

        final MenuItem queue = new MenuItem(menu,SWT.PUSH);
        queue.setText("Queue");
        queue.setImage(ImageRepository.getImage("menu_queue"));
        queue.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){
                        Container container = (Container)item.getData();
                        container.getDownload().restart();
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();
                }
            }
        });

        final MenuItem forceStart = new MenuItem(menu,SWT.CHECK);
        forceStart.setText("Force Start");
        forceStart.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){
                        Container container = (Container)item.getData();
                        container.getDownload().setForceStart(forceStart.getSelection());
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();
                }
            }
        });


        final MenuItem stop = new MenuItem(menu,SWT.PUSH);
        stop.setText("Stop");
        stop.setImage(ImageRepository.getImage("menu_stop"));
        stop.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){
                        Container container = (Container)item.getData();
                        container.getDownload().stop();
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();
                }
            }
        });



        final MenuItem remove = new MenuItem(menu,SWT.PUSH);
        remove.setText("Remove");
        remove.setImage(ImageRepository.getImage("menu_remove"));
        remove.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){
                        Container container = (Container)item.getData();

                        //Close any tabs that have this download name
                        CTabItem[] tabs = tabFolder.getItems();
                        for(CTabItem tab:tabs){
                            if(tab.getText().equalsIgnoreCase(container.getDownload().getName())){
                                tab.dispose();
                            }
                        }

                        container.getDownload().remove();
                        container.removeFromTable();
                        if(seedsMap.containsKey(container.getDownload().getHash())){
                            seedsMap.remove(container.getDownload().getHash());
                        }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                            downloadsMap.remove(container.getDownload().getHash());
                        }
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();

                }
            }
        });


        final MenuItem removeAnd = new MenuItem(menu, SWT.CASCADE);
        removeAnd.setText("Remove and");
        removeAnd.setImage(ImageRepository.getImage("menu_remove"));
        Menu removeAndMenu = new Menu(removeAnd);



        final MenuItem downloadDeleteTorrent = new MenuItem(removeAndMenu, SWT.PUSH);
        downloadDeleteTorrent.setText("Delete Torrent File");
        downloadDeleteTorrent.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){


                        Container container = (Container)item.getData();


                        //Close any tabs that have this download name
                        CTabItem[] tabs = tabFolder.getItems();
                        for(CTabItem tab:tabs){
                            if(tab.getText().equalsIgnoreCase(container.getDownload().getName())){
                                tab.dispose();
                            }
                        }


                        container.getDownload().remove(true, false);
                        container.removeFromTable();
                        if(seedsMap.containsKey(container.getDownload().getHash())){
                            seedsMap.remove(container.getDownload().getHash());
                        }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                            downloadsMap.remove(container.getDownload().getHash());
                        }
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();

                }
            }
        });

        final MenuItem downloadDeleteData = new MenuItem(removeAndMenu, SWT.PUSH);
        downloadDeleteData.setText("Delete Data");
        downloadDeleteData.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    MessageBox messageBox = new MessageBox(DOWNLOAD_MANAGER_SHELL, SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
                    messageBox.setText("Confirm Delete");
                    messageBox.setMessage("Are you sure you wish to remove the data associated with these torrents?");
                    int response = messageBox.open();
                    switch (response){
                    case SWT.OK:
                        FireFrogMain.getFFM().getClient().transactionStart();
                        for(TableItem item : items){
                            Container container = (Container)item.getData();
                            container.getDownload().remove(false, true);
                            container.removeFromTable();
                            if(seedsMap.containsKey(container.getDownload().getHash())){
                                seedsMap.remove(container.getDownload().getHash());
                            }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                                downloadsMap.remove(container.getDownload().getHash());
                            }
                        }
                        FireFrogMain.getFFM().getClient().transactionCommit();
                        break;
                    case SWT.CANCEL:
                        break;
                    }



                }
            }
        });

        final MenuItem downloadDeleteBoth = new MenuItem(removeAndMenu, SWT.PUSH);
        downloadDeleteBoth.setText("Delete Both");
        downloadDeleteBoth.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    MessageBox messageBox = new MessageBox(DOWNLOAD_MANAGER_SHELL, SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
                    messageBox.setText("Confirm Delete");
                    messageBox.setMessage("Are you sure you wish to remove both the data associated with these torrents and the torrents themselves?");
                    int response = messageBox.open();
                    switch (response){
                    case SWT.OK:
                        FireFrogMain.getFFM().getClient().transactionStart();
                        for(TableItem item : items){
                            Container container = (Container)item.getData();

                            //Close any tabs that have this download name
                            CTabItem[] tabs = tabFolder.getItems();
                            for(CTabItem tab:tabs){
                                if(tab.getText().equalsIgnoreCase(container.getDownload().getName())){
                                    tab.dispose();
                                }
                            }

                            container.getDownload().remove(true, true);
                            container.removeFromTable();
                            if(seedsMap.containsKey(container.getDownload().getHash())){
                                seedsMap.remove(container.getDownload().getHash());
                            }else if(downloadsMap.containsKey(container.getDownload().getHash())){
                                downloadsMap.remove(container.getDownload().getHash());
                            }
                        }
                        FireFrogMain.getFFM().getClient().transactionCommit();
                        break;
                    case SWT.CANCEL:
                        break;
                    }				}
            }
        });

        final MenuItem forceRecheck = new MenuItem(menu, SWT.PUSH);
        forceRecheck.setText("Force Re-check");
        forceRecheck.setImage(ImageRepository.getImage("menu_recheck"));
        forceRecheck.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                if(table != null || !table.isDisposed()){
                    TableItem[] items = table.getSelection();
                    if(items.length > 1) return;

                    FireFrogMain.getFFM().getClient().transactionStart();
                    for(TableItem item : items){
                        Container container = (Container)item.getData();
                        container.getDownload().recheckData();
                    }
                    FireFrogMain.getFFM().getClient().transactionCommit();

                }
            }
        });


        new MenuItem(menu,SWT.SEPARATOR);

        final MenuItem editColumns = new MenuItem(menu,SWT.PUSH);
        editColumns.setText("Edit Columns");
        editColumns.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event arg0) {
                new TableColumnEditorDialog(table.equals(downloadsTable)?true:false);
            }
        });


        removeAnd.setMenu(removeAndMenu);
        table.setMenu(menu);

        menu.addMenuListener(new MenuListener(){

            public void menuHidden(MenuEvent arg0) {
                // Nothing to do here

            }

            public void menuShown(MenuEvent arg0) {
                TableItem[] items = table.getSelection();
                if(items.length == 0){
                    stop.setEnabled(false);
                    queue.setEnabled(false);
                    remove.setEnabled(false);
                    forceStart.setEnabled(false);
                    forceStart.setSelection(false);
                    removeAnd.setEnabled(false);
                    forceRecheck.setEnabled(false);
                }else if(items.length == 1){
                    stop.setEnabled(true);
                    queue.setEnabled(true);
                    remove.setEnabled(true);
                    forceStart.setEnabled(true);
                    //Check to see if download is already in ForceStart
                    Container container = (Container)items[0].getData();
                    if(container.getDownload().isForceStart()){
                        forceStart.setSelection(true);
                    }

                    int state = container.getDownload().getState();
                    if(state == Download.ST_STOPPED ||
                            state == Download.ST_QUEUED ||
                            state == Download.ST_ERROR){
                        forceRecheck.setEnabled(true);
                    }else
                        forceRecheck.setEnabled(false);



                    removeAnd.setEnabled(true);

                }else{
                    stop.setEnabled(true);
                    queue.setEnabled(true);
                    remove.setEnabled(true);
                    forceStart.setEnabled(false);
                    forceStart.setSelection(false);
                    forceRecheck.setEnabled(false);
                    removeAnd.setEnabled(true);
                }

            }

        });
    }


    /**
     * sets the status bar text alert area given String text and SWT.COLOR_*
     * @param text
     * @param color
     */
    public void setStatusBarText(final String text, final int color){
        final Display display = FireFrogMain.getFFM().getDisplay();
        if(display == null || display.isDisposed()) return;
        display.asyncExec(new Runnable(){
            public void run() {
                try {
                    if(statusBarText == null || statusBarText.isDisposed())
                        return;
                    if(text == null){
                        statusBarText.setText("");
                        return;
                    }
                    statusBarText.setForeground(display.getSystemColor(color));
                    statusBarText.setText(text);
                    statusBarText.getParent().layout();
                } catch (SWTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });


    }





    /**
     * sets the status bar text alert area given String text and with the color black
     * @param text
     */
    public void setStatusBarText(String text){
        setStatusBarText(text,SWT.COLOR_BLACK);
    }




    public void setToolBarTorrentIcons(final boolean bQueue, final boolean bStop, final boolean bRemove){
        FireFrogMain.getFFM().getDisplay().syncExec(new Runnable(){
            public void run() {
                if(queueTorrent != null || !queueTorrent.isDisposed()){
                    queueTorrent.setEnabled(bQueue);
                }

                if(stopTorrent != null || !stopTorrent.isDisposed()){
                    stopTorrent.setEnabled(bStop);
                }

                if(removeTorrent != null || !removeTorrent.isDisposed()){
                    removeTorrent.setEnabled(bRemove);
                }


            }
        });
    }




    public void sortTable(final Table table){
        FireFrogMain.getFFM().getDisplay().syncExec(new Runnable(){
            public void run() {
                TableItem[] items = table.getItems();
                int minimum;
                for (int i = 0; i < items.length - 1; i++) {
                    minimum = i; /* current minimum */
                    /* find the global minimum */
                    Container cMinimum = (Container)items[minimum].getData();
                    for (int j = i + 1; j < items.length; j++) {
                        Container cJ = (Container)items[j].getData();
                        int result = cMinimum.compareTo(cJ);
                        if (result > 0) {
                            /* new minimum */
                            cMinimum = cJ;
                            minimum = j;
                        }
                    }
                    /* swap data[i] and data[minimum] */
                    if(i!=minimum){
                        cMinimum.changePosition(i, table);
                        TableItem temp = items[i];
                        items[i] = items[minimum];
                        items[minimum] = temp;
                    }
                }

            }

        });
    }

    public int findInsertionPosition(Container insertItem, Table table) {
        TableItem[] items = table.getItems();
        int pos = 0;
        boolean found = false;
        for (int j = 0; j < items.length; j++) {
            Container cJ = (Container)items[j].getData();
            int result = insertItem.compareTo(cJ);
            pos = j;
            if (result < 0)	{
                found = true;
                break;
            }
        }
        if (!found) pos = items.length;
        return pos;
    }

    public void createTableColumns(final Table table, final List<Integer> dlList) {
        ControlListener resizeListener = new ControlListener(){
            public void controlMoved(ControlEvent arg0) {}
            public void controlResized(ControlEvent arg0) {
                saveColumnWidthsToPreferencesFile();
            }
        };


        Listener sortListener = new Listener() {
            public void handleEvent(Event e) {
                sortTable(table);
            }
        };

        List<Integer> column_width_list = new ArrayList<Integer>();
        Properties properties = FireFrogMain.getFFM().getProperties();
        if(table.equals(downloadsTable)){
            if(properties.containsKey("downloadsTable.columns.widths"))
                column_width_list =  EncodingUtil.StringToIntegerList(properties.getProperty("downloadsTable.columns.widths"));
        }else{
            if(properties.containsKey("seedsTable.columns.widths"))
                column_width_list =  EncodingUtil.StringToIntegerList(properties.getProperty("seedsTable.columns.widths"));
        }


        table.removeAll(); // clear all

        for (int i = 0; i < dlList.size(); i++) {
            switch (dlList.get(i)) {
            case RemoteConstants.ST_POSITION:
                TableColumn position = new TableColumn(table, SWT.CENTER);
                position.setText("#");
                position.setMoveable(true);
                try{
                    position.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    position.setWidth(22);
                }
                position.addListener(SWT.Selection, sortListener);
                position.addControlListener(resizeListener);
                //table.setSortColumn(position);
                //table.setSortDirection(SWT.UP);
                break;

            case RemoteConstants.ST_HEALTH:
                TableColumn health = new TableColumn(table, SWT.CENTER);
                health.setResizable(false);
                health.setMoveable(true);
                health.setWidth(22);
                break;

            case RemoteConstants.ST_NAME:
                TableColumn name = new TableColumn(table, SWT.LEFT);
                name.setText("Name");
                name.addControlListener(resizeListener);
                name.setMoveable(true);
                try{
                    name.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    name.setWidth(300);
                }
                break;

            case RemoteConstants.ST_COMPLETITION:
                TableColumn progress = new TableColumn(table, SWT.CENTER);
                progress.setText("Percent Complete");
                progress.setWidth(120);
                progress.setMoveable(true);
                progress.setResizable(false);
                break;

            case RemoteConstants.ST_AVAILABILITY:
                TableColumn availability = new TableColumn(table, SWT.RIGHT);
                availability.setText("Availability");
                try{
                    availability.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    availability.setWidth(120);
                }
                availability.setMoveable(true);
                availability.addControlListener(resizeListener);
                break;

            case RemoteConstants.ST_DOWNLOAD_AVG:
                TableColumn dlSpeed = new TableColumn(table, SWT.RIGHT);
                dlSpeed.setText("Down Speed");
                dlSpeed.setMoveable(true);
                dlSpeed.addControlListener(resizeListener);
                try{
                    dlSpeed.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    dlSpeed.pack();
                }
                break;

            case RemoteConstants.ST_UPLOAD_AVG:
                TableColumn upSpeed = new TableColumn(table, SWT.RIGHT);
                upSpeed.setText("Up Speed");
                upSpeed.setMoveable(true);
                upSpeed.addControlListener(resizeListener);
                try{
                    upSpeed.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    upSpeed.pack();
                }

                break;

            case RemoteConstants.ST_ALL_SEEDS:
                TableColumn seeds = new TableColumn(table, SWT.RIGHT);
                seeds.setText("Seeds");
                seeds.setMoveable(true);
                seeds.addControlListener(resizeListener);
                try{
                    seeds.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    seeds.setWidth(120);
                }
                break;

            case RemoteConstants.ST_ALL_LEECHER:
                TableColumn leechers = new TableColumn(table, SWT.RIGHT);
                leechers.setText("Leechers");
                leechers.setMoveable(true);
                leechers.addControlListener(resizeListener);
                try{
                    leechers.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    leechers.setWidth(120);
                }

                break;

            case RemoteConstants.ST_UPLOADED:
                TableColumn uploaded = new TableColumn(table,SWT.RIGHT);
                uploaded.setText("Uploaded");
                uploaded.setMoveable(true);
                uploaded.addControlListener(resizeListener);
                try{
                    uploaded.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    uploaded.pack();
                }

                break;

            case RemoteConstants.ST_DOWNLOADED:
                TableColumn downloaded = new TableColumn(table,SWT.RIGHT);
                downloaded.setText("Downloaded");
                downloaded.setMoveable(true);
                downloaded.addControlListener(resizeListener);
                try{
                    downloaded.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    downloaded.pack();
                }

                break;


            case RemoteConstants.ST_ETA:
                TableColumn eta = new TableColumn(table, SWT.RIGHT);
                eta.setText("ETA");
                eta.setMoveable(true);
                eta.addControlListener(resizeListener);
                try{
                    eta.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    eta.setWidth(120);
                }

                break;

            case RemoteConstants.ST_STATUS:
                TableColumn status = new TableColumn(table, SWT.RIGHT);
                status.setText("Status");
                status.setMoveable(true);
                status.addControlListener(resizeListener);
                try{
                    status.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    status.setWidth(150);
                }

                break;
            case RemoteConstants.ST_SHARE:
                TableColumn shareRatio = new TableColumn(table, SWT.RIGHT);
                shareRatio.setText("Share Ratio");
                shareRatio.setMoveable(true);
                shareRatio.addControlListener(resizeListener);
                try{
                    shareRatio.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    shareRatio.pack();
                }

                break;


            case RemoteConstants.ST_TRACKER:
                TableColumn tracker = new TableColumn(table, SWT.RIGHT);
                tracker.setText("Tracker");
                tracker.setMoveable(true);
                tracker.addControlListener(resizeListener);
                try{
                    tracker.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    tracker.setWidth(150);
                }

                break;

            case RemoteConstants.ST_LIMIT_DOWN:
                TableColumn limitDown = new TableColumn(table, SWT.RIGHT);
                limitDown.setText("Download Limit");
                limitDown.setMoveable(true);
                limitDown.addControlListener(resizeListener);
                try{
                    limitDown.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    limitDown.pack();
                }

                break;

            case RemoteConstants.ST_LIMIT_UP:
                TableColumn upLimit = new TableColumn(table, SWT.RIGHT);
                upLimit.setText("Upload Limit");
                upLimit.setMoveable(true);
                upLimit.addControlListener(resizeListener);
                try{
                    upLimit.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    upLimit.pack();
                }

                break;

            case RemoteConstants.ST_DISCARDED:
                TableColumn discarded = new TableColumn(table, SWT.RIGHT);
                discarded.setText("Discarded");
                discarded.setMoveable(true);
                discarded.addControlListener(resizeListener);
                try{
                    discarded.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    discarded.pack();
                }

                break;

            case RemoteConstants.ST_SIZE:
                TableColumn size = new TableColumn(table, SWT.RIGHT);
                size.setText("Size");
                size.setMoveable(true);
                size.addControlListener(resizeListener);
                try{
                    size.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    size.setWidth(100);
                }

                break;

            case RemoteConstants.ST_ELAPSED_TIME:
                TableColumn elapsedTime = new TableColumn(table, SWT.RIGHT);
                elapsedTime.setText("Elapsed Time");
                elapsedTime.setMoveable(true);
                elapsedTime.addControlListener(resizeListener);
                try{
                    elapsedTime.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    elapsedTime.pack();
                }

                break;

            case RemoteConstants.ST_TOTAL_AVG:
                TableColumn swarm = new TableColumn(table, SWT.RIGHT);
                swarm.setText("Swarm Speed");
                swarm.setMoveable(true);
                swarm.addControlListener(resizeListener);
                try{
                    swarm.setWidth(column_width_list.get(i));
                }catch(Exception e){
                    swarm.pack();
                }

                break;


            }
        }
    }

    /**
     *  This function sets the Items that are requested.
     *  It uses the columns as datasource.
     */
    public void setRequestedItems() {
        int options = 0;
        List <Integer> list = DownloadContainer.getColumns();
        for (int i:list) {
            options |= i;
        }
        list = SeedContainer.getColumns();
        for (int i:list) {
            options |= i;
        }
        options |= RemoteConstants.ST_STATE; //set state since we need it
        FireFrogMain.getFFM().getProperties().setProperty("transfer_states", Integer.toString(options));
    }

    /*private void createDropTarget(final Control control) {
		DropTarget dropTarget = new DropTarget(control, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		dropTarget.setTransfer(new Transfer[] {URLTransfer.getInstance(), FileTransfer.getInstance()});
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				if(URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_COPY;
				}
			}
			public void drop(DropTargetEvent event) {
				openDroppedTorrents(event);
			}
		});
	}

	public static void
	openDroppedTorrents(
			DropTargetEvent event)
	{
		if(event.data == null)
			return;

		boolean bOverrideToStopped = event.detail == DND.DROP_COPY;

		if(event.data instanceof String[] || event.data instanceof String) {
			final String[] sourceNames = (event.data instanceof String[]) ?
					(String[]) event.data : new String[] { (String) event.data };
					if (sourceNames == null)
						event.detail = DND.DROP_NONE;
					if (event.detail == DND.DROP_NONE)
						return;

					for (int i = 0;(i < sourceNames.length); i++) {
						final File source = new File(sourceNames[i]);
						if (source.isFile()) {
							String filename = source.getAbsolutePath();
							try {
								if (filename.endsWith(".torrent")) {
									//FireFrogMain.getFFM().getClient().sendAddDownload(source);
									System.out.println("Valid FILE!!! -- " + filename);
								} else {
									System.out.println("This is not a valid torrent File: "+filename);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							System.out.println("This is not a valid File: "+source);
							// Probably a URL.. let torrentwindow handle it
							//TODO: openTorrentWindow(null, new String[] { sourceNames[i] },
							//		bOverrideToStopped);
						}
					}
		} else if (event.data instanceof URLTransfer.URLType) {
			System.out.println("This is a URL Transfer");

			System.out.println(((URLTransfer.URLType)event.data).toString());
			System.out.println(((URLTransfer.URLType) event.data).linkURL);
//          TODO:openTorrentWindow(null,
			//			new String[] { ((URLTransfer.URLType) event.data).linkURL },
			//			bOverrideToStopped);
		} else
			System.out.println("something else" + event.data);
	}*/

    private class CLabelPadding extends CLabel {
        private int lastWidth = 0;
        private long widthSetOn = 0;
        private final int KEEPWIDTHFOR_MS = 30 * 1000;

        public CLabelPadding(Composite parent, int style) {
            super(parent, style | SWT.CENTER);

            GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
                    | GridData.VERTICAL_ALIGN_FILL);
            setLayoutData(gridData);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.custom.CLabel#computeSize(int, int, boolean)
         */
        public Point computeSize(int wHint, int hHint, boolean changed) {
            if ( !isVisible()){
                return( new Point(0,0));
            }
            Point pt = super.computeSize(wHint, hHint, changed);
            pt.x += 4;

            long now = System.currentTimeMillis();
            if (lastWidth > pt.x && now - widthSetOn < KEEPWIDTHFOR_MS) {
                pt.x = lastWidth;
            } else {
                if (lastWidth != pt.x)
                    lastWidth = pt.x;
                widthSetOn = now;
            }

            return pt;
        }
    }

    public void clearMapsAndChildred(){
        downloadsMap.clear();
        seedsMap.clear();
        FireFrogMain.getFFM().getDisplay().syncExec(new Runnable(){
            public void run() {
                try {
                    downloadsTable.removeAll();
                    seedsTable.removeAll();
                    Control[] children_downloads = downloadsTable.getChildren();
                    for(Control child:children_downloads) child.dispose();
                    Control[] children_seeds = seedsTable.getChildren();
                    for(Control child:children_seeds) child.dispose();
                }catch(SWTException e){

                }
            }
        });

    }

    public void refreshStatusBar(){
        FireFrogMain.getFFM().getDisplay().syncExec(new Runnable(){
            public void run() {
                try {
                    if(statusDown != null || !statusDown.isDisposed()){
                        if(azureusDownload.equalsIgnoreCase("0"))
                            statusDown.setText(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downSpeed));
                        else if(azureusDownload.equalsIgnoreCase("N/S"))
                            statusDown.setText("[" + azureusDownload + "] " + DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downSpeed));
                        else
                            statusDown.setText("[" + azureusDownload + "K] " + DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downSpeed));

                        if(azureusUpload.equalsIgnoreCase("0"))
                            statusUp.setText(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(upSpeed));
                        else if(azureusUpload.equalsIgnoreCase("N/S"))
                            statusUp.setText("[" + azureusUpload + "] " + DisplayFormatters.formatByteCountToBase10KBEtcPerSec(upSpeed));
                        else
                            statusUp.setText("[" + azureusUpload + "K] " + DisplayFormatters.formatByteCountToBase10KBEtcPerSec(upSpeed));

                        statusbarComp.layout();
                    }
                } catch (SWTException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Call when a successfull connection occurs
     *
     */
    public void initializeConnection(){
        if (FireFrogMain.getFFM().connected()) {
            Client client = FireFrogMain.getFFM().getClient();
            client.transactionStart();
            client.sendListTransfers(RemoteConstants.ST_ALL);
            client.getUserManager().update();

            //pull the upload download settings
            client.sendGetAzParameter(
                    RemoteConstants.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC,
                    RemoteConstants.PARAMETER_INT);
            client.sendGetAzParameter(
                    RemoteConstants.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC,
                    RemoteConstants.PARAMETER_INT);
            client.sendGetPluginParameter("singleUserMode", RemoteConstants.PARAMETER_BOOLEAN);
            client.transactionCommit();
        }
    }

    private void createDragDrop(final Table parent) {
        try{


            Transfer[] types = new Transfer[] { TextTransfer.getInstance()};

            DragSource dragSource = new DragSource(parent, DND.DROP_MOVE);
            dragSource.setTransfer(types);
            dragSource.addDragListener(new DragSourceAdapter() {
                public void dragStart(DragSourceEvent event) {
                    Table table = parent;
                    if (table.getSelectionCount() != 0 &&
                            table.getSelectionCount() != table.getItemCount())
                    {
                        event.doit = true;
                        //System.out.println("DragStart");
                        drag_drop_line_start = table.getSelectionIndex();
                    } else {
                        event.doit = false;
                        drag_drop_line_start = -1;
                    }
                }

                public void dragSetData(DragSourceEvent event) {
                    event.data = "moveRow";
                }
            });

            DropTarget dropTarget = new DropTarget(parent,
                    DND.DROP_DEFAULT | DND.DROP_MOVE |
                    DND.DROP_COPY | DND.DROP_LINK |
                    DND.DROP_TARGET_MOVE);

            if (SWT.getVersion() >= 3107) {
                dropTarget.setTransfer(new Transfer[] { HTMLTransfer.getInstance(),
                        URLTransfer.getInstance(), FileTransfer.getInstance(),
                        TextTransfer.getInstance() });
            } else {
                dropTarget.setTransfer(new Transfer[] { URLTransfer.getInstance(),
                        FileTransfer.getInstance(), TextTransfer.getInstance() });
            }

            dropTarget.addDropListener(new DropTargetAdapter() {
                public void dropAccept(DropTargetEvent event) {
                    event.currentDataType = URLTransfer.pickBestType(event.dataTypes,
                            event.currentDataType);
                }

                public void dragEnter(DropTargetEvent event) {
                    // no event.data on dragOver, use drag_drop_line_start to determine if
                    // ours
                    if(drag_drop_line_start < 0) {
                        if(event.detail != DND.DROP_COPY) {
                            if ((event.operations & DND.DROP_LINK) > 0)
                                event.detail = DND.DROP_LINK;
                            else if ((event.operations & DND.DROP_COPY) > 0)
                                event.detail = DND.DROP_COPY;
                        }
                    } else if(TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT | DND.FEEDBACK_INSERT_BEFORE | DND.FEEDBACK_INSERT_AFTER;
                        event.detail = event.item == null ? DND.DROP_NONE : DND.DROP_MOVE;
                    }
                }

                public void drop(DropTargetEvent event) {
                    if (!(event.data instanceof String) || !((String)event.data).equals("moveRow")) {
                        openDroppedTorrents(event);
                        return;
                    }

                    // Torrent file from shell dropped
                    if(drag_drop_line_start >= 0) { // event.data == null
                        event.detail = DND.DROP_NONE;
                        if(event.item == null)
                            return;
                        int drag_drop_line_end = parent.indexOf((TableItem)event.item);

                        //moveSelectedTorrents(drag_drop_line_start, drag_drop_line_end);
                        if(parent.equals(downloadsTable)){
                            Iterator iter = downloadsMap.keySet().iterator();
                            while (iter.hasNext()){
                                Container container = downloadsMap.get(iter.next());
                                if(container.getDownload().getPosition() == drag_drop_line_start+1){
                                    System.out.println("Moving " + container.getDownload().getName()+ " From position " + (drag_drop_line_start +1) + " to " + (drag_drop_line_end+1));
                                    container.getDownload().setPosition(drag_drop_line_end+1);
                                    if(FireFrogMain.getFFM().connected())
                                        FireFrogMain.getFFM().getClient().sendListTransfers(RemoteConstants.ST_ALL);
                                }
                            }
                        }else{
                            Iterator iter = seedsMap.keySet().iterator();
                            while (iter.hasNext()){
                                Container container = seedsMap.get(iter.next());
                                if(container.getDownload().getPosition() == drag_drop_line_start){
                                    System.out.println("Moving " + container.getDownload().getName()+ " From position " + (drag_drop_line_start +1) + " to " + (drag_drop_line_end+1));
                                    container.getDownload().setPosition(drag_drop_line_end+1);
                                    if(FireFrogMain.getFFM().connected())
                                        FireFrogMain.getFFM().getClient().sendListTransfers(RemoteConstants.ST_ALL);
                                }
                            }
                        }
                        drag_drop_line_start = -1;
                    }
                }
            });

        }
        catch( Throwable t ) {
            FireFrogMain.getFFM().getDebugLogger().severe("failed to init drag-n-drop + \n" + t);
        }
    }

    public static String parseTextForURL(String text) {
        // examples:
        // <A HREF=http://abc.om/moo>test</a>
        // <A style=cow HREF="http://abc.om/moo">test</a>
        // <a href="http://www.gnu.org/licenses/fdl.html" target="_top">moo</a>

        Pattern pat = Pattern.compile("<.*a\\s++.*href=\"?([^\\'\"\\s>]++).*", Pattern.CASE_INSENSITIVE);
        Matcher m = pat.matcher(text);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    public static void openDroppedTorrents(DropTargetEvent event) {
        if (event.data == null)
            return;

        //boolean bOverrideToStopped = event.detail == DND.DROP_COPY;

        if (event.data instanceof String[] || event.data instanceof String) {
            final String[] sourceNames = (event.data instanceof String[])
            ? (String[]) event.data : new String[] { (String) event.data };
            if (sourceNames == null)
                event.detail = DND.DROP_NONE;
            if (event.detail == DND.DROP_NONE)
                return;

            for (int i = 0; (i < sourceNames.length); i++) {
                final File source = new File(sourceNames[i]);
                String sURL = parseTextForURL(sourceNames[i]);

                if (sURL != null || !source.exists()) {
                    //openTorrentWindow(null, new String[] { sURL }, bOverrideToStopped);
                    //System.out.println("Dropped is a URL: " + sURL);
                    new OpenByURLDialog(FireFrogMain.getFFM().getDisplay(),sURL);
                } else if (source.isFile()) {
                    String filename = source.getAbsolutePath();
                    try {
                        if (!isTorrentFile(filename)) {
                            FireFrogMain.getFFM().getDebugLogger().info("openDroppedTorrents: file not a torrent file");


                            //Torrent creation if we ever support that in FF
                            //ShareUtils.shareFile(azureus_core, filename);
                        } else {
                            System.out.println("Dropped file IS torrent -- to open: " + filename);
                            new OpenByFileDialog(FireFrogMain.getFFM().getDisplay(),new String[] {filename});
                            /* openTorrentWindow(null, new String[] { filename },
										bOverrideToStopped);*/
                        }
                    } catch (Exception e) {
                        FireFrogMain.getFFM().getDebugLogger().info("Torrent open fails for '" + filename + "'\n"  + e.toString());
                    }
                } else if (source.isDirectory()) {

                    //If we ever want to support torrent creation, this is a directory drop
                    //torrent create

                    /*                        String dir_name = source.getAbsolutePath();

						if (!bAllowShareAdd) {
							openTorrentWindow(dir_name, null, bOverrideToStopped);
						} else {
							String drop_action = COConfigurationManager.getStringParameter(
									"config.style.dropdiraction" );

							if (drop_action.equals("1")) {
								ShareUtils.shareDir(azureus_core, dir_name);
							} else if (drop_action.equals("2")) {
								ShareUtils.shareDirContents(azureus_core, dir_name, false);
							} else if (drop_action.equals("3")) {
								ShareUtils.shareDirContents(azureus_core, dir_name, true);
							} else {
								openTorrentWindow(dir_name, null, bOverrideToStopped);
							}
						}*/
                }
            }
        } else if (event.data instanceof URLTransfer.URLType) {
            System.out.println("Dropped is a URLTransfer.UrlType: " + ((URLTransfer.URLType) event.data).linkURL);
            /* openTorrentWindow(null,
						new String[] { ((URLTransfer.URLType) event.data).linkURL },
						bOverrideToStopped);*/
        }
    }

    public static boolean isTorrentFile(String filename) throws FileNotFoundException, IOException {
        File check = new File(filename);
        if (!check.exists())
            throw new FileNotFoundException("File "+filename+" not found.");
        if (!check.canRead())
            throw new IOException("File "+filename+" cannot be read.");
        if (check.isDirectory()){
            FireFrogMain.getFFM().getDebugLogger().info("File "+filename+" is a directory.");
            return false;
        }

        try {
            if(!check.isFile()) {
                FireFrogMain.getFFM().getDebugLogger().info("Torrent must be a file ('" + check.getName() + "')");
                return false;
            }

            if ( check.length() == 0 ){
                FireFrogMain.getFFM().getDebugLogger().info("Torrent is zero length('" + check.getName() + "')");
            }


            FileInputStream fis = null;

            try{
                fis = new FileInputStream(check);

                //               construct( fis );

            }catch( IOException e ){
                FireFrogMain.getFFM().getDebugLogger().info("IO Exception reading torrent ('" + check.getName() + "')");

            }finally{

                if ( fis != null ){

                    try{

                        fis.close();

                    }catch( IOException e ){

                        FireFrogMain.getFFM().getDebugLogger().severe( e.toString() );
                        return false;
                    }
                }
                return true;
            }
        } catch (Throwable e) {
            return false;
        }
    }


    /*    protected void
	construct(
		InputStream     is )

		throws TOTorrentException
	{
		ByteArrayOutputStream metaInfo = new ByteArrayOutputStream();

		try{
			byte[] buf = new byte[32*1024]; // raised this limit as 2k was rather too small

			// do a check to see if it's a BEncode file.
			int iFirstByte = is.read();

			if (    iFirstByte != 'd' &&
					iFirstByte != 'e' &&
					iFirstByte != 'i' &&
					!(iFirstByte >= '0' && iFirstByte <= '9')){

					// often people download an HTML file by accident - if it looks like HTML
					// then produce a more informative error

				try{
					metaInfo.write(iFirstByte);

					int nbRead;

					while ((nbRead = is.read(buf)) > 0 && metaInfo.size() < 32000 ){

						metaInfo.write(buf, 0, nbRead);
					}

					String  char_data = new String( metaInfo.toByteArray());

					if ( char_data.toLowerCase().indexOf( "html") != -1 ){

						char_data = HTMLUtils.convertHTMLToText2( char_data );

						char_data = HTMLUtils.splitWithLineLength( char_data, 80 );

						if ( char_data.length() > 400 ){

							char_data = char_data.substring(0,400) + "...";
						}

						throw(  new TOTorrentException(
									"Contents maybe HTML:\n" + char_data,
									TOTorrentException.RT_DECODE_FAILS ));
					}
				}catch( Throwable e ){

					if ( e instanceof TOTorrentException ){

						throw((TOTorrentException)e);
					}

						// ignore this
				}

				throw( new TOTorrentException( "Contents invalid - bad header",
						TOTorrentException.RT_DECODE_FAILS ));

			}



			metaInfo.write(iFirstByte);

			int nbRead;

			while ((nbRead = is.read(buf)) > 0){

				metaInfo.write(buf, 0, nbRead);
			}
		}catch( IOException e ){

			throw( new TOTorrentException( "TOTorrentDeserialise: IO exception reading torrent '" + e.toString()+ "'",
											TOTorrentException.RT_READ_FAILS ));
		}

		construct( metaInfo.toByteArray());
	}*/



    public void saveColumnWidthsToPreferencesFile(){
        Display display = FireFrogMain.getFFM().getDisplay();
        if(display == null || display.isDisposed()) return;
        display.asyncExec(new Runnable(){

            public void run() {
                Properties properties = FireFrogMain.getFFM().getProperties();
                //save the downloadsTable column widths
                if (!downloadsTable.isDisposed()) {
                    TableColumn[] columns = downloadsTable.getColumns();
                    List<Integer> dl_column_list = new ArrayList<Integer>();
                    for (TableColumn column:columns){
                        dl_column_list.add(column.getWidth());
                    }
                    properties.setProperty("downloadsTable.columns.widths", EncodingUtil.IntListToString(dl_column_list));
                }

                //save the seedsTable column widths
                if (!seedsTable.isDisposed()) {
                    TableColumn[] seed_columns = seedsTable.getColumns();
                    List<Integer> seed_column_list = new ArrayList<Integer>();
                    for (TableColumn column:seed_columns){
                        seed_column_list.add(column.getWidth());
                    }
                    properties.setProperty("seedsTable.columns.widths", EncodingUtil.IntListToString(seed_column_list));
                }

                //Save Everything!
                FireFrogMain.getFFM().saveConfig();


            }

        });

    }
}//EOF