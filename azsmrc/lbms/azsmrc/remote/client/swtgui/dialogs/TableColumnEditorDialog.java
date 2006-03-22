package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.List;

import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.container.Container;
import lbms.azsmrc.remote.client.swtgui.container.DownloadContainer;
import lbms.azsmrc.remote.client.swtgui.container.SeedContainer;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * Choose columns to display, and in what order
 */
public class TableColumnEditorDialog {

    private Display display;
    private Shell shell;
    private Color blue;
    private Table table;


    private List<Integer> tableColumns;
    private List<Integer> totalTableColumns;

    private boolean bIsDownloadContainer;
    private boolean mousePressed;
    private TableItem selectedItem;
    private Point oldPoint;
    private Image oldImage;


    /**
     * Default Constructor
     * @param bIsDownloadConatiner
     */
    public TableColumnEditorDialog(final boolean _bIsDownloadConatiner) {
        bIsDownloadContainer = _bIsDownloadConatiner;
        if(bIsDownloadContainer)
            tableColumns = DownloadContainer.getColumns();
        else
            tableColumns = SeedContainer.getColumns();

        totalTableColumns = Container.getTotalColumns();


        RowData rd;
        display = RCMain.getRCMain().getDisplay();

        blue = new Color(display,0,0,128);

        shell = new Shell(SWT.DIALOG_TRIM | SWT.RESIZE);

        shell.setText("Choose the columns to display");

        GridLayout layout = new GridLayout();
        shell.setLayout (layout);

        GridData gridData;

        Label label = new Label(shell,SWT.NULL);
        label.setText("Drag rows to re-order them");
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gridData);

        table = new Table (shell, SWT.VIRTUAL | SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        gridData = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);
        table.setHeaderVisible(true);

        Composite cButtonArea = new Composite(shell, SWT.NULL);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        cButtonArea.setLayoutData(gridData);
        RowLayout rLayout = new RowLayout(SWT.HORIZONTAL);
        rLayout.marginLeft = 0;
        rLayout.marginTop = 0;
        rLayout.marginRight = 0;
        rLayout.marginBottom = 0;
        rLayout.spacing = 5;
        cButtonArea.setLayout (rLayout);

        Button bOk = new Button(cButtonArea,SWT.PUSH);
        bOk.setText("OK");
        rd = new RowData();
        rd.width = 70;
        bOk.setLayoutData(rd);
        bOk.addListener(SWT.Selection,new Listener() {
            public void handleEvent(Event e) {
                saveAndApply();
                close();
            }
        });

        Button bCancel = new Button(cButtonArea,SWT.PUSH);
        bCancel.setText("Cancel");
        rd = new RowData();
        rd.width = 70;
        bCancel.setLayoutData(rd);
        bCancel.addListener(SWT.Selection,new Listener() {
            public void handleEvent(Event e) {
                close();
            }
        });

        Button bApply = new Button(cButtonArea,SWT.PUSH);
        bApply.setText("Apply");
        rd = new RowData();
        rd.width = 70;
        bApply.setLayoutData(rd);
        bApply.addListener(SWT.Selection,new Listener() {
            public void handleEvent(Event e) {
                saveAndApply();
            }
        });




        TableColumn column_name = new TableColumn(table, SWT.NONE);
        column_name.setText("Column Name");

        TableColumn column_description = new TableColumn(table, SWT.NONE);
        column_description.setText("Column Description");


        table.getColumn(0).setWidth(160);
        table.getColumn(1).setWidth(500);

        table.addListener(SWT.Selection,new Listener() {
            public void handleEvent(Event e) {
                if (e.detail != SWT.CHECK)
                    return;
                //TableItem item = (TableItem) e.item;
                //int index = item.getParent().indexOf(item);
                //in case we need to do anything on a selection event
            }
        });

        table.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                final TableItem item = (TableItem) event.item;
                if (item == null)
                    return;
                Table table = item.getParent();
                int index = table.indexOf(item);
                if (index < 0) {
                    // Trigger a Table._getItem, which assigns the item to the array
                    // in Table, so indexOf(..) can find it.  This is a workaround for
                    // a WinXP bug.
                    Rectangle r = item.getBounds(0);
                    table.getItem(new Point(r.x, r.y));
                    index = table.indexOf(item);
                    if (index < 0)
                        return;
                }

                String[] column_text = lookUpTableItem(totalTableColumns.get(index));
                item.setText(0,column_text[0]);
                item.setText(1,column_text[1]);
                if(tableColumns.contains(totalTableColumns.get(index))){
                    item.setChecked(true);
                }
            }
        });
        table.setItemCount(totalTableColumns.size());

        table.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent arg0) {
                mousePressed = true;
                selectedItem = table.getItem(new Point(arg0.x,arg0.y));
            }

            public void mouseUp(MouseEvent e) {
                mousePressed = false;
                //1. Restore old image
                if(oldPoint != null && oldImage != null) {
                    GC gc = new GC(table);
                    gc.drawImage(oldImage,oldPoint.x,oldPoint.y);
                    oldImage.dispose();
                    oldImage = null;
                    oldPoint = null;
                }
                Point p = new Point(e.x,e.y);
                TableItem item = table.getItem(p);
                if(item != null && selectedItem != null) {
                    int index = table.indexOf(item);
                    int oldIndex = table.indexOf(selectedItem);
                    if(index == oldIndex)
                        return;
                    Integer value = totalTableColumns.get(oldIndex);
                    totalTableColumns.remove(oldIndex);
                    totalTableColumns.add(index, value);
                    table.clearAll();
                }
            }
        });

        table.addMouseMoveListener(new MouseMoveListener(){
            public void mouseMove(MouseEvent e) {
                if (!mousePressed || selectedItem == null)
                    return;

                Point p = new Point(e.x,e.y);
                TableItem item = table.getItem(p);
                if (item == null)
                    return;

                GC gc = new GC(table);
                Rectangle bounds = item.getBounds(0);
                int selectedPosition = table.indexOf(selectedItem);
                int newPosition = table.indexOf(item);

                //1. Restore old image
                if(oldPoint != null && oldImage != null) {
                    gc.drawImage(oldImage,oldPoint.x,oldPoint.y);
                    oldImage.dispose();
                    oldImage = null;
                    oldPoint = null;
                }
                bounds.y += (-table.getHeaderHeight());
                if(newPosition <= selectedPosition)
                    oldPoint = new Point(bounds.x,bounds.y);
                else
                    oldPoint = new Point(bounds.x,bounds.y+bounds.height);
                //2. Store the image
                oldImage = new Image(display,bounds.width,2);
                gc.copyArea(oldImage,oldPoint.x,oldPoint.y);
                //3. Draw a thick line
                gc.setBackground(blue);
                gc.fillRectangle(oldPoint.x,oldPoint.y,bounds.width,2);
            }
        });

        shell.pack();
        Point p = shell.getSize();
        p.x = 550;
        // For Windows, to get rid of the scrollbar
        p.y += 2;

        if (p.y + 64 > display.getClientArea().height)
            p.y = display.getBounds().height - 64;

        shell.setSize(p);

        GUI_Utilities.centerShellandOpen(shell);
    }

    private void close() {
        if(blue != null && ! blue.isDisposed())
            blue.dispose();
        if (!shell.isDisposed())
            shell.dispose();
    }

    private void saveAndApply() {
        tableColumns.clear();
        TableItem[] items = table.getItems();
        int position = 0;
        for(int i = 0 ; i < items.length ; i++) {
            if(items[i].getChecked()){
                tableColumns.add(position, totalTableColumns.get(i));
                position++;
            }
        }
        //save the tableColumns back to the original set
        if(bIsDownloadContainer){
            DownloadContainer.saveColumns();
        }else
            SeedContainer.saveColumns();

        //redraw the tables
        RCMain.getRCMain().getMainWindow().redrawColumnsonTables();
    }



    private String[] lookUpTableItem(int remoteConstantInteger){
        switch (remoteConstantInteger) {

        case RemoteConstants.ST_NAME:
            return new String[] {"Name", "Name of the Download"};

        case RemoteConstants.ST_POSITION:
            return new String[] {"Position", "Position of the Download"};

        case RemoteConstants.ST_DOWNLOAD_AVG:
            return new String[] {"Download Average", "Average Download Speed"};

        case RemoteConstants.ST_UPLOAD_AVG:
            return new String[] {"Upload Average", "Average Upload Speed"};

        case RemoteConstants.ST_DOWNLOADED:
            return new String[] {"Downloaded", "Total Amount Downloaded"};

        case RemoteConstants.ST_UPLOADED:
            return new String[] {"Uploaded", "Total Amount Uploaded"};

        case RemoteConstants.ST_HEALTH:
            return new String[] {"Health", "Icon representing the health of the download conneciton"};

        case RemoteConstants.ST_COMPLETITION:
            return new String[] {"Percent Complete", "Progess bar showing percent completion"};

        case RemoteConstants.ST_AVAILABILITY:
            return new String[] {"Availability", "Number of full copies being seen"};

        case RemoteConstants.ST_ETA:
            return new String[] {"ETA", "Estimated time for the download to finish"};

        case RemoteConstants.ST_STATE:
            return new String[] {"State", ""};

        case RemoteConstants.ST_STATUS:
            return new String[] {"Status", "Overal Status of the tracker"};

        case RemoteConstants.ST_SHARE:
            return new String[] {"Share Ratio", "Ratio of amount uploaded to amount downloaded"};

        case RemoteConstants.ST_ALL_SEEDS:
            return new String[] {"Seeds", "Number of seeds you are connected to as well as the total amount of seeds"};

        case RemoteConstants.ST_ALL_LEECHER:
            return new String[] {"Leechers", "Number of leechers you are connected to as well as the total amount of leechers"};

        case RemoteConstants.ST_SIZE:
            return new String[] {"Size", "Total size on disk of the download"};

        case RemoteConstants.ST_TRACKER:
            return new String[] {"Tracker Status", "Result of the last scrape of the tracker"};

        case RemoteConstants.ST_DISCARDED:
            return new String[] {"Discarded", "Amount of bad data discarded"};

        case RemoteConstants.ST_ELAPSED_TIME:
            return new String[] {"Elapsed Time", "Total elpased time since adding the torrent"};

        case RemoteConstants.ST_TOTAL_AVG:
            return new String[] {"Swarm Speed", "Total overall speed of the swarm"};

        case RemoteConstants.ST_LIMIT_UP:
            return new String[] {"Upload Limit", "Set upload limit for each torrent"};

        case RemoteConstants.ST_LIMIT_DOWN:
            return new String[] {"Download Limit", "Set download limit for each torrent"};

        default:
            return new String[] {"null","null"};
        }
    }


}//EOF

