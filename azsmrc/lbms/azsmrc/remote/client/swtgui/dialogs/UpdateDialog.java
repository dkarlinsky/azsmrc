package lbms.azsmrc.remote.client.swtgui.dialogs;


import java.util.List;

import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.tools.updater.Changelog;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateFile;
import lbms.tools.updater.Updater;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;


public class UpdateDialog{

    private Shell dialogShell;
    private Tree ChangelogTree;
    private Button cancel;
    private Button accept;
    private CLabel updateLabel1;



    public UpdateDialog(final Display parent, final Update update, final Updater updater) {

        parent.asyncExec(new Runnable(){

            public void run() {
                try {
                    List<UpdateFile> files = update.getFileList();

                    dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

                    GridLayout dialogShellLayout = new GridLayout();
                    dialogShell.setLayout(dialogShellLayout);
                    dialogShellLayout.numColumns = 2;
                    dialogShell.layout();
                    dialogShell.pack();
                    dialogShell.setSize(488, 315);

                    long totalSize = 0;
                    //pull total size
                    for(int i = 0; i < files.size(); i++){
                        totalSize += files.get(i).getSize();
                    }
                    //START >>  updateLabel1
                    updateLabel1 = new CLabel(dialogShell, SWT.BORDER);
                    GridData updateLabel1LData = new GridData();
                    updateLabel1LData.horizontalSpan = 2;
                    updateLabel1LData.horizontalAlignment = GridData.FILL;
                    updateLabel1.setLayoutData(updateLabel1LData);
                    updateLabel1.setText("Update To Version " + update.getVersion().toString() +
                            " Available\nImportance Level: " + getImportanceLevelString(update.getImportance_level())+
                            "\nUpdate Type: " + getTypeString(update.getType()) +
                            "\nTotal Update Size: " + DisplayFormatters.formatByteCountToBase10KBEtc(totalSize));
                    updateLabel1.setAlignment(SWT.CENTER);
                    updateLabel1.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
                    //END <<  updateLabel1

                    Group gfiles = new Group(dialogShell, SWT.NULL);
                    gfiles.setText("Files to be updated:");
                    GridData gd = new GridData(GridData.FILL_BOTH);
                    gd.horizontalSpan = 2;
                    gfiles.setLayoutData(gd);
                    gfiles.setLayout(new GridLayout(1,false));

                    Table fileTable = new Table(gfiles,SWT.V_SCROLL | SWT.BORDER);
                    gd = new GridData(GridData.FILL_BOTH);
                    fileTable.setLayoutData(gd);

                    for(int i = 0; i < files.size(); i++){
                        TableItem item = new TableItem(fileTable,SWT.NULL);
                        item.setText(files.get(i).getName() + "  (" + DisplayFormatters.formatByteCountToBase10KBEtc(files.get(i).getSize()) + ")");
                    }


                    Group gChangelog = new Group(dialogShell, SWT.NULL);
                    gChangelog.setText("Changelog:");
                    gd = new GridData(GridData.FILL_BOTH);
                    gd.horizontalSpan = 2;
                    gChangelog.setLayoutData(gd);
                    gChangelog.setLayout(new GridLayout(1,false));

                    //START >>  ChangelogTree
                    ChangelogTree = new Tree(gChangelog, SWT.BORDER);
                    GridData ChangelogTreeLData = new GridData();
                    ChangelogTreeLData.horizontalAlignment = GridData.CENTER;
                    ChangelogTreeLData.grabExcessHorizontalSpace = true;
                    ChangelogTreeLData.grabExcessVerticalSpace = true;
                    ChangelogTreeLData.verticalSpan = 5;
                    ChangelogTreeLData.widthHint = 460;
                    ChangelogTreeLData.heightHint = 208;
                    ChangelogTree.setLayoutData(ChangelogTreeLData);



                    //pull the changelog
                    Changelog log = update.getChangeLog();
                    //pull all of the lists
                    List<String> bugs = log.getBugFixes();
                    List<String> changes = log.getChanges();
                    List<String> features = log.getFeatures();
                    int totalItems = bugs.size() + changes.size() + features.size();
                    gChangelog.setText("Changelog (" + totalItems +" items):");


                    if(bugs.size() > 0){
                        TreeItem mainBugItem = new TreeItem(ChangelogTree,SWT.NULL);
                        mainBugItem.setText("BugFixes (" + bugs.size() + " Items)");

                        for(int j = 0; j < bugs.size(); j++){
                            TreeItem bugItem = new TreeItem(mainBugItem,SWT.NULL);
                            bugItem.setText(bugs.get(j).toString());
                        }

                    }


                    if(changes.size() > 0){
                        TreeItem mainChangeItem = new TreeItem(ChangelogTree,SWT.NULL);
                        mainChangeItem.setText("Changes (" + changes.size() + " Items)");


                        for(int j = 0; j < changes.size(); j++){
                            TreeItem changeItem = new TreeItem(mainChangeItem,SWT.NULL);
                            changeItem.setText(changes.get(j).toString());
                        }

                    }



                    if(features.size() > 0){
                        TreeItem mainFeatureItem = new TreeItem(ChangelogTree,SWT.NULL);
                        mainFeatureItem.setText("Feature Additions (" + features.size() + " Items)");


                        for(int j = 0; j < features.size(); j++){
                            TreeItem featureItem = new TreeItem(mainFeatureItem,SWT.NULL);
                            featureItem.setText(features.get(j).toString());
                        }

                    }



                    //END <<  ChangelogTree

                    //START >>  accept
                    accept = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
                    GridData acceptLData = new GridData();
                    acceptLData.horizontalAlignment = GridData.END;
                    acceptLData.grabExcessHorizontalSpace = true;
                    accept.setLayoutData(acceptLData);
                    accept.setText("Update Now");
                    accept.addListener(SWT.Selection, new Listener() {
                        public void handleEvent(Event arg0) {
                            updater.doUpdate();
                            dialogShell.close();
                        }
                    });
                    //END <<  accept

                    //START >>  cancel
                    cancel = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
                    GridData cancelLData = new GridData();
                    cancelLData.horizontalAlignment = GridData.END;
                    cancel.setLayoutData(cancelLData);
                    cancel.setText("Cancel");
                    cancel.addListener(SWT.Selection, new Listener() {
                        public void handleEvent(Event arg0) {
                            dialogShell.close();
                        }
                    });
                    //END <<  cancel
                    GUI_Utilities.centerShellandOpen(dialogShell);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private String getImportanceLevelString(int level){
        if(level == Update.LV_BUGFIX) return "BugFix";
        else if(level == Update.LV_CHANGE) return "Change";
        else if(level == Update.LV_FEATURE) return "Feature Enhancement";
        else if(level == Update.LV_LOW) return "Low";
        else if(level == Update.LV_SEC_RISK) return "Security Risk";
        else return "NULL";
    }

    private String getTypeString(int type){
        if(type == Update.TYPE_BETA) return "Beta Release";
        else if(type == Update.TYPE_MAINTENANCE) return "Maintenance Release";
        else if(type == Update.TYPE_STABLE) return "Stable Release";
        else return "NULL";
    }

}
