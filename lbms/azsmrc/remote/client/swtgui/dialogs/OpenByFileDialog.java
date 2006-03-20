/*
 * Created on Jan 27, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import lbms.azsmrc.remote.client.swtgui.DownloadManagerShell;
import lbms.azsmrc.remote.client.swtgui.FireFrogMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.URLTransfer;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class OpenByFileDialog {

    private Table filesTable;
    private String lastDir;
    ArrayList<String> files = new ArrayList<String>(5);
    private int drag_drop_line_start = -1;

    public OpenByFileDialog(Display display, final String[] filenames) {
        //Shell
        final Shell shell = new Shell(display);
        shell.setLayout(new GridLayout(1,false));
        shell.setText("Send Torrent File to Server");

        //Comp on shell
        Group comp = new Group(shell,SWT.NULL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = 2;
        comp.setLayout(gridLayout);




        //first line
        Button open_file_button = new Button(comp, SWT.PUSH);
        open_file_button.setToolTipText("Choose a file to upload");
        open_file_button.setText("Choose a file to upload");
        open_file_button.setImage(ImageRepository.getImage("openfile"));
        open_file_button.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                FileDialog dialog = new FileDialog (shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] {"*.torrent" , "*.*"});
                dialog.setText("Choose Torrent File to Send to Server");
                if(lastDir != null){
                    dialog.setFilterPath(lastDir);
                }
                String choosen_file = dialog.open();

                Iterator iter = files.iterator();
                while(iter.hasNext()){
                    if(iter.next().toString().equalsIgnoreCase(choosen_file))
                        return;
                }

                if(choosen_file != null)
                    try{
                        File test = new File(choosen_file);
                        if(test.isFile() && test.canRead()){
                            TableItem item = new TableItem(filesTable,SWT.NULL);
                            item.setText(0,test.getName());
                            String path = test.getAbsolutePath().substring(0, test.getAbsolutePath().length() - test.getName().length());
                            item.setText(1,path);
                            files.add(choosen_file);
                            lastDir = path;
                        }
                    }catch(Exception e1){
                        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText("Error");
                        messageBox.setMessage("File not valid, please check permissions and try again.");
                        messageBox.open();
                    }
            }
        });

        Button remove = new Button(comp, SWT.PUSH);
        remove.setText("Remove selected from list");
        remove.addSelectionListener(new SelectionListener(){

            public void widgetSelected(SelectionEvent arg0) {
                int[] items = filesTable.getSelectionIndices();
                if(items.length == 0) return;
                for(int index:items){
                    files.remove(index);

                }
                filesTable.remove(items);
            }

            public void widgetDefaultSelected(SelectionEvent arg0) { }


        });


        filesTable = new Table(comp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 3;
        gridData.verticalSpan = 20;
        gridData.grabExcessVerticalSpace = true;
        filesTable.setLayoutData(gridData);
        filesTable.setHeaderVisible(true);

        TableColumn name = new TableColumn(filesTable, SWT.NULL);
        name.setText("Name");
        name.setWidth(130);

        TableColumn path = new TableColumn(filesTable, SWT.NULL);
        path.setText("Local Path");
        path.setWidth(350);

        createDragDrop(filesTable);

        //Buttons
        Composite button_comp = new Composite(shell, SWT.NULL);
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        button_comp.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        button_comp.setLayout(gridLayout);

        Button connect = new Button(button_comp,SWT.PUSH);
        connect.setText("Send File(s) to Server");
        connect.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                if(files.size() == 0){
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("Please provide a file to upload.");
                    messageBox.open();
                    return;
                }else{
                    for(String file:(String[])files.toArray(new String[] {})){
                        File test = new File(file);
                        if(test.exists() && test.canRead()){
                            FireFrogMain.getFFM().getClient().sendAddDownload(test);
                        }else{
                            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                            messageBox.setText("Error");
                            messageBox.setMessage("File Error:  File does not exist or cannot be read.");
                            messageBox.open();
                            return;
                        }
                    }


                }
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

        //See if we come in with a file already
        if(filenames != null){
            try{
                for(String filename:filenames){
                    File test = new File(filename);
                    if(test.isFile() && test.canRead()){
                        TableItem item = new TableItem(filesTable,SWT.NULL);
                        item.setText(0,test.getName());
                        String filepath = test.getAbsolutePath().substring(0, test.getAbsolutePath().length() - test.getName().length());
                        item.setText(1,filepath);
                        files.add(filename);
                        lastDir = filepath;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        //Center Shell and open
        GUI_Utilities.centerShellandOpen(shell);

    }


    public OpenByFileDialog(Display display){
        new OpenByFileDialog(display,null);
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

          }
        });

        }
        catch( Throwable t ) {
            FireFrogMain.getFFM().getDebugLogger().severe("failed to init drag-n-drop + \n" + t);
        }
      }


    public void openDroppedTorrents(DropTargetEvent event) {
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
                String sURL = DownloadManagerShell.parseTextForURL(sourceNames[i]);

                if (sURL != null || !source.exists()) {
                    //openTorrentWindow(null, new String[] { sURL }, bOverrideToStopped);
                    //System.out.println("Dropped is a URL: " + sURL);
                    MessageBox messageBox = new MessageBox(FireFrogMain.getFFM().getMainWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("You have dropped a URL:\n" + sURL + "\nPlease drop only torrent files.");
                    messageBox.open();
                    return;
                } else if (source.isFile()) {
                    String filename = source.getAbsolutePath();
                    try {
                        if (!DownloadManagerShell.isTorrentFile(filename)) {
                            FireFrogMain.getFFM().getDebugLogger().info("openDroppedTorrents: file not a torrent file");

                        } else {
                           //System.out.println("Dropped file IS torrent -- to open: " + filename);
                           try{

                                   File test = new File(filename);
                                   if(test.isFile() && test.canRead()){
                                       TableItem item = new TableItem(filesTable,SWT.NULL);
                                       item.setText(0,test.getName());
                                       String filepath = test.getAbsolutePath().substring(0, test.getAbsolutePath().length() - test.getName().length());
                                       item.setText(1,filepath);
                                       files.add(filename);
                                       lastDir = filepath;
                                   }

                           }catch(Exception e){
                               e.printStackTrace();
                           }
                        }
                    } catch (Exception e) {
                        FireFrogMain.getFFM().getDebugLogger().info("Torrent open fails for '" + filename + "'\n"  + e.toString());
                    }
                } else if (source.isDirectory()) {
                    MessageBox messageBox = new MessageBox(FireFrogMain.getFFM().getMainWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error");
                    messageBox.setMessage("You have dropped a directory, please drop one file at a time.");
                    messageBox.open();
                    return;
                }
            }
        } else if (event.data instanceof URLTransfer.URLType) {
            System.out.println("Dropped is a URLTransfer.UrlType: " + ((URLTransfer.URLType) event.data).linkURL);

        }
    }


}//EOF
